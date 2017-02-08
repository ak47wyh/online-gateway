package com.iboxpay.settlement.gateway.kq.service.api;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import org.apache.commons.lang.Validate;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bill99.asap.exception.CryptoException;
import com.bill99.asap.service.ICryptoService;
import com.bill99.asap.service.impl.CryptoServiceFactory;
import com.bill99.schema.asap.commons.Mpf;
import com.bill99.schema.asap.data.SealedData;
import com.bill99.schema.asap.data.UnsealedData;
import com.bill99.schema.asap.message.SealRequestBody;
import com.bill99.schema.commons.Version;
import com.bill99.schema.ddp.product.MerchantDebitPkiRequest;
import com.bill99.schema.ddp.product.MerchantDebitPkiRequestBody;
import com.bill99.schema.ddp.product.MerchantDebitPkiResponse;
import com.bill99.schema.ddp.product.MerchantDebitPkiResponseBody;
import com.bill99.schema.ddp.product.MerchantDebitQueryResponse;
import com.bill99.schema.ddp.product.MerchantDebitResponse;
import com.bill99.schema.ddp.product.MerchantDebitSingleQueryResponse;
import com.bill99.schema.ddp.product.MerchantSingleDebitResponse;
import com.bill99.schema.ddp.product.head.MerchantDebitHead;
import com.bill99.schema.ddp.product.pki.SealDataType;
import com.iboxpay.settlement.gateway.kq.entity.DealInfoEntity;
import com.iboxpay.settlement.gateway.kq.service.util.Base64Util;
import com.iboxpay.settlement.gateway.kq.service.util.CustomerUtil;
import com.iboxpay.settlement.gateway.kq.service.util.GzipUtil;

public class CustomerTool {
	private static Logger logger = LoggerFactory.getLogger(CustomerTool.class);
	public static String ENCODING = "utf-8";
	
	private final String ACTION_DEBIT_SINGLE= "ddp.product.debit.single";
	private final String ACTION_DEBIT_SINGLE_QUERY="ddp.product.debitsinglequery";
	private final String ACTION_DEBIT_BATCH="ddp.product.debit";
	private final String ACTION_DEBIT_QUERY = "ddp.product.debitquery";
	
	
	/**
	 * 将提交数据进行加密处理，并且返回一个加密后的数据类sealedData
	 * @param dealInfo 请求的数据内容
	 * @return SealedData 加密后的数据
	 */
	private SealedData seal(DealInfoEntity dealInfo) {
		String originalData = "";		
		if (ACTION_DEBIT_SINGLE.equalsIgnoreCase(dealInfo.getServiceType())) {//单笔免签约代扣
			originalData = CustomerUtil.merchantSingleDebitRequestToXml(CustomerUtil.getMerchantSingleDebitRequest(dealInfo));
		} else if(ACTION_DEBIT_SINGLE_QUERY.equalsIgnoreCase(dealInfo.getServiceType())){//单笔免签约查询
			originalData = CustomerUtil.merchantDebitQueryRequestToXml(CustomerUtil.getMerchantDebitSingleQueryRequest(dealInfo));
		} else if(ACTION_DEBIT_BATCH.equalsIgnoreCase(dealInfo.getServiceType())){//批量免签约代扣
			originalData = CustomerUtil.merchantDebitRequestToXml(CustomerUtil.getMerchantDebitRequest(dealInfo));
		} else if (ACTION_DEBIT_QUERY.equalsIgnoreCase(dealInfo.getServiceType())) {//批量免签约查询明细
			originalData = CustomerUtil.merchantDebitQueryRequestToXml(CustomerUtil.getMerchantDebitQueryRequest(dealInfo));
		} 
		Validate.notNull(originalData);
		logger.info("提交的原始报文为:\n"+originalData);
		
		SealedData sealedData = null;
		try {
			SealRequestBody srb = new SealRequestBody();
			srb.setOriginalData(originalData.getBytes(ENCODING));
			Mpf mpf = new Mpf();
			mpf.setMemberCode(dealInfo.getMemberCode());
			mpf.setFeatureCode(dealInfo.getFeatureCode());
			ICryptoService service = CryptoServiceFactory.createCryptoService();
			logger.info("跟踪0");
			sealedData = service.seal(mpf, originalData.getBytes(ENCODING));
			logger.info("跟踪1");
			byte[] nullbyte = {};
//			byte[] byteOri = new byte[0];
			byte[] byteEnc = new byte[0];
			byte[] byteEnv = new byte[0];
			byte[] byteSig = new byte[0];
			byte[] byteOri = sealedData.getOriginalData() == null ? nullbyte
					: sealedData.getOriginalData();
			if (null != sealedData.getEncryptedData())
				byteEnc = sealedData.getEncryptedData();
			if (null != sealedData.getDigitalEnvelope())
				byteEnv = sealedData.getDigitalEnvelope();
			if (null != sealedData.getSignedData())
				byteSig = sealedData.getSignedData();
			logger.info("跟踪2");
			byte[] byteOri2 = base64Gzip(byteOri);
			byte[] byteEnc2 = base64Gzip(byteEnc);
			byte[] byteEnv2 = base64Gzip(byteEnv);
			byte[] byteSig2 = base64Gzip(byteSig);

			sealedData.setOriginalData(byteOri2);
			sealedData.setSignedData(byteSig2);
			sealedData.setEncryptedData(byteEnc2);
			sealedData.setDigitalEnvelope(byteEnv2);
		

			
		} catch (CryptoException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e){
			e.printStackTrace();
		}
		return sealedData;
	}
	
	/**
	 * 对返回的应答信息进行解密，得到请求响应结果：成功or失败
	 * @param dealInfo 请求的数据内容
	 * @return String 响应结果
	 * */
	public Object unseal(MerchantDebitPkiResponse response,DealInfoEntity dealInfo) {

		if (response == null) {
			logger.info("====应答信息为空=====");
			return null;
		} else {
			try {
				byte[] unsealedResultbyte = this.unsealData(response, dealInfo);
				if(unsealedResultbyte!=null){
					if (ACTION_DEBIT_BATCH.equalsIgnoreCase(dealInfo.getServiceType())){//批量免签约支付
						MerchantDebitResponse responseObject = CustomerUtil.xmlToMerchantDebitResponse(new String(unsealedResultbyte, ENCODING));
						return responseObject;
					} else if (ACTION_DEBIT_QUERY.equalsIgnoreCase(dealInfo.getServiceType())) {//批量免签约查询明细
						MerchantDebitQueryResponse responseObject = CustomerUtil.xmlToMerchantDebitQueryResponse(new String(unsealedResultbyte, ENCODING));
						return responseObject;
					} else if (ACTION_DEBIT_SINGLE.equalsIgnoreCase(dealInfo.getServiceType())) {//单笔免签约支付
						MerchantSingleDebitResponse responseObject = CustomerUtil.xmlToMerchantSingleDebitResponse(new String(unsealedResultbyte, ENCODING));
						return responseObject;
					} else if (ACTION_DEBIT_SINGLE_QUERY.equalsIgnoreCase(dealInfo.getServiceType())){//单笔免签约查询
						MerchantDebitSingleQueryResponse responseObject = CustomerUtil.xmlToMerchantDebitSingleQueryResponse(new String(unsealedResultbyte, ENCODING));
						return responseObject;
					} 
				}

			} catch (UnsupportedEncodingException e) {
				logger.error("解密异常"+e.getMessage());
				return null;
			}
		}
		return null;
	}
	

	
	
	
	/**
	 * 对应返回的应答信息做解密处理
	 * @param response 返回的应答信息类
	 * @param dealInfo 交易数据
	 * @return byte[] 解密数据
	 * */
	public byte[] unsealData(MerchantDebitPkiResponse response,DealInfoEntity dealInfo){
		if(response==null){
			logger.info("得到的应答报文为空");
			return null;
		}else{
			try {
				String errorCode="";
				SealDataType responseSealedData=null;
				MerchantDebitPkiResponseBody responsebody=response.getBody();
				errorCode=responsebody.getErrorCode();
				responseSealedData = responsebody.getData();
				logger.info("付款商户号"+responsebody.getMemberCode());
				logger.info("应答状态"+responsebody.getErrorCode());

				SealDataType sdt = response.getBody().getData();
				SealedData sd = new SealedData();
				
				byte[] byteOri = new byte[0];
				byte[] byteEnc = new byte[0];
				byte[] byteEnv = new byte[0];
				byte[] byteSig = new byte[0];
				if (null != sdt.getOriginalData())
					byteOri = sdt.getOriginalData().getBytes(ENCODING);
				if (null != sdt.getEncryptedData())
					byteEnc = sdt.getEncryptedData().getBytes(ENCODING);
				if (null != sdt.getDigitalEnvelope())
					byteEnv = sdt.getDigitalEnvelope().getBytes(ENCODING);
				if (null != sdt.getSignedData())
					byteSig = sdt.getSignedData().getBytes(ENCODING);

				

				byte[] byteOri2 = base64Ungzip(byteOri);
				byte[] byteEnc2 = base64Ungzip(byteEnc);
				byte[] byteEnv2 = base64Ungzip(byteEnv);
				byte[] byteSig2 = base64Ungzip(byteSig);

				sd.setOriginalData(byteOri2);
				sd.setEncryptedData(byteEnc2);
				sd.setDigitalEnvelope(byteEnv2);
				sd.setSignedData(byteSig2);
				
				Mpf mpf = new Mpf();
				mpf.setMemberCode(dealInfo.getMemberCode());
				mpf.setFeatureCode(dealInfo.getFeatureCode());
				
				ICryptoService service = CryptoServiceFactory.createCryptoService();
				
				UnsealedData unsealedData = service.unseal(mpf, sd);
				if (unsealedData.getVerifySignResult()) {
					byte[] unsealedResultbyte=unsealedData.getDecryptedData();
					return unsealedResultbyte;
				}else {
					logger.info("验签失败");
					return null;
				}
			} catch (CryptoException e) {
				logger.error("解密处理异常"+e.getMessage());
				return null;
			} catch (UnsupportedEncodingException e) {
				logger.error("解密处理异常"+e.getMessage());
				return null;
			} catch (IOException e){
				logger.error("解密处理异常"+e.getMessage());
				return null;
			}
		}
	}
	
	/**
	 * MerchantSingleDebitRequest转换为xml格式 
	 * @param request 付款请求（密文）
	 * @return String xml 字符串
	 */
	public static String settlementPkiApiRequestToXml(MerchantDebitPkiRequest request) {
		try {
			IBindingFactory bfact = BindingDirectory.getFactory(MerchantDebitPkiRequest.class);
			IMarshallingContext mctx = bfact.createMarshallingContext();
			mctx.setIndent(2);
			StringWriter sw = new StringWriter();
			mctx.setOutput(sw);
			mctx.marshalDocument(request);	
			return sw.toString();
		} catch (JiBXException e) {
			logger.error("settlementPkiApiRequestToXml处理异常"+e.getMessage());
			return null;
		}
	}
	

	
	/**
	 * 将提交信息设置到SettlementPkiApiRequest类
	 * @param dealInfo 请求的数据内容
	 * @return SettlementPkiApiRequest 请求数据类
	 */
	public MerchantDebitPkiRequest getMerchantDebitPkiRequest(DealInfoEntity dealInfo){
		
		MerchantDebitPkiRequest request=new MerchantDebitPkiRequest();
		String memberCode=dealInfo.getMemberCode();
		// 设置头部信息
		MerchantDebitHead head = new MerchantDebitHead();
		Version version = new Version();
		version.setService(dealInfo.getServiceType());
		version.setVersion(dealInfo.getVersion());
		head.setVersion(version);
		request.setHead(head);
		
		MerchantDebitPkiRequestBody body = new MerchantDebitPkiRequestBody();
		SealedData sealedData = null;
		try {
			//
			sealedData=this.seal(dealInfo);
			body.setMemberCode(dealInfo.getMemberCode());
			SealDataType sealdata = new SealDataType();
			byte[] byteOri = sealedData.getOriginalData();
			byte[] byteEnc = sealedData.getEncryptedData();
			byte[] byteEnv = sealedData.getDigitalEnvelope();
			byte[] byteSig = sealedData.getSignedData();
			sealdata.setOriginalData(new String(byteOri, ENCODING));
			sealdata.setEncryptedData(new String(byteEnc, ENCODING));
			sealdata.setDigitalEnvelope(new String(byteEnv, ENCODING));
			sealdata.setSignedData(new String(byteSig, ENCODING));
			body.setData(sealdata);
			body.setMemberCode(memberCode);
			request.setBody(body);
			return request;
			
		} catch (UnsupportedEncodingException e) {
			logger.error("getMerchantDebitPkiRequest处理异常"+e.getMessage());
		}
		return request;
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////
	private  byte[] base64Gzip(byte[] b) throws IOException {
		if (null == b)
			return new byte[0];
        //先压缩
		byte[] bytes01 = GzipUtil.gzip(b);
		//再加密
		byte[] bytes02 = Base64Util.encode(bytes01);
		return bytes02;
	}

	
	private static byte[] base64Ungzip(byte[] b) throws IOException {
		if (null == b)
			return new byte[0];
		//先解密
		byte[] b2 = Base64Util.decode(b);
		//再解压缩
		byte[] b3 = GzipUtil.unBGzip(b2);
		return b3;
	}

		
}
