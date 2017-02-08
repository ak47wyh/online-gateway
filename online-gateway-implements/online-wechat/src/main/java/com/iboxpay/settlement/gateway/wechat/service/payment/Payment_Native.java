package com.iboxpay.settlement.gateway.wechat.service.payment;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.callback.ICallBackPayment;
import com.iboxpay.settlement.gateway.common.trans.close.IClosePayment;
import com.iboxpay.settlement.gateway.common.trans.payment.AbstractPayment;
import com.iboxpay.settlement.gateway.common.trans.payment.PaymentNavigation;
import com.iboxpay.settlement.gateway.common.trans.query.IQueryPayment;
import com.iboxpay.settlement.gateway.common.trans.refund.IRefundPayment;
import com.iboxpay.settlement.gateway.common.trans.refund.query.IRefundQueryPayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.zxing.QRCodeUtil;
import com.iboxpay.settlement.gateway.wechat.WechatFrontEndConfig;
import com.iboxpay.settlement.gateway.wechat.service.PaymentWechatService;
import com.iboxpay.settlement.gateway.wechat.service.WeChatContrants;
import com.iboxpay.settlement.gateway.wechat.service.callback.CallbackPayment_Native;
import com.iboxpay.settlement.gateway.wechat.service.closed.ClosePayment_Native;
import com.iboxpay.settlement.gateway.wechat.service.query.QueryPayment_Native;
import com.iboxpay.settlement.gateway.wechat.service.refund.RefundPayment_Native;
import com.iboxpay.settlement.gateway.wechat.service.refund.query.QueryRefundPayment_Native;
import com.iboxpay.settlement.gateway.wechat.service.utils.SignUtils;
import com.iboxpay.settlement.gateway.wechat.service.utils.XmlUtils;


@Service
public class Payment_Native extends AbstractPayment{
	private static Logger logger = LoggerFactory.getLogger(Payment_Native.class);
	public static final String BANK_TRANS_CODE = "wechatNative";
	public static final String TRANS_DESC = "微信扫码支付";
	@Override
	public PaymentNavigation navigate() {
		return PaymentNavigation.create()
				.setBatchSize(1)//单笔
				.setDiffBank(true)//跨行
				.setSameBank(true)
				.setToPrivate(true)
				.setToCompany(true)//对公,对私都支持
				.setType(PaymentNavigation.Type.online);
	}

	@Override
	public String check(PaymentEntity[] payments) {
		return null;
	}

	@Override
	public Class<? extends IQueryPayment> getQueryClass() {
		return QueryPayment_Native.class;
	}

	@Override
	public String getBankTransCode() {
		return BANK_TRANS_CODE;
	}

	@Override
	public String getBankTransDesc() {
		return "微信扫码支付(Native)";
	}

	@Override
	public void genBankSeqId(PaymentEntity[] payments) {
		for (int i = 0; i <payments.length; i++) {
			String bankSeqId =payments[i].getSeqId();
			payments[i].setBankSeqId(String.valueOf(bankSeqId));
		}
	}
	
	
	@Override
	public String pack(PaymentEntity[] payments) throws PackMessageException {
	    // 获取前置机
		WechatFrontEndConfig wechatConfig=(WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		        
		// 数据组装转换
		PaymentEntity paymentEntity=payments[0];
		SortedMap<String,String> map = PaymentWechatService.initNativePayData(wechatConfig, paymentEntity);
        
		// 提交报文信息
		String reqContent = XmlUtils.parseXML(map);
		return reqContent;
	}

	@Override
	public void parse(String response, PaymentEntity[] payments) throws ParseMessageException {
		// 获取前置机
		WechatFrontEndConfig config = (WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		String charset = config.getCharset().getVal();
		PaymentEntity paymentEntity=payments[0];
		try {
			Map<String, Object> merchantMap=paymentEntity.getMerchantMap();
			// 交易秘钥
			String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
			
			byte[] xmlBytes = response.getBytes(charset);
			Map<String,String> resultMap = XmlUtils.toMap(xmlBytes, charset);
			
			String returnCode=resultMap.get("return_code");
			String returnMsg= resultMap.get("return_msg");
			if (returnCode.equals(WeChatContrants.RETURN_CODE_SUCCESS)) {
				String resultCode=resultMap.get("result_code");
				String errCode=resultMap.get("err_code");
				String errCodeDes=resultMap.get("err_code_des");
				if (resultMap.containsKey("sign") && !SignUtils.checkParam(resultMap, merchantKey)) {
					logger.error("验证签名不通过!");
				}else if(resultCode.equals(WeChatContrants.RESULT_CODE_SUCCESS)){
					String outTradeNo=resultMap.get("out_trade_no");
					String totalFee=resultMap.get("total_fee");
					String codeUrl = resultMap.get("code_url");//二维码地址

					// 根据二维码地址生成二维码图片
					String destPath = "F://";
					String fileName = QRCodeUtil.encode(codeUrl, destPath);

					// 二维码图片
					String codeImgUrl = destPath + fileName;

					// 扩展属性存放json格式数据
					Map<String, Object> extParam = new HashMap<String, Object>();
					extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeUrl, codeUrl);
					extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeImgUrl, codeImgUrl);
					String callbackExtProperties = JsonUtil.toJson(extParam);
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUBMITTED, "",WeChatContrants.RESULT_CODE_SUCCESS, "提交成功", callbackExtProperties);
				}else{
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", errCode, errCodeDes);
				}
			}else{
				PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", returnCode, returnMsg);
			}
			
			
		} catch (UnsupportedEncodingException e) { 
			logger.error("解析反馈报文异常："+e.getStackTrace());
		} catch (Exception e) {
			logger.error("解析反馈报文异常："+e.getStackTrace());
		}
	}
	
	
	@Override
	public Map<String, String> getHeaderMap() {
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("Content-Type", "text/xml; charset=utf-8");
		return headerMap;
	}

	@Override
	protected String getUri() {
		return ((WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig()).getNativePayUrl().getVal();
	}
	
	
	
    /**
     * 退款接口实现类
     * @return
     */
	@Override
    public Class<? extends IRefundPayment> getRefundClass(){
    	return RefundPayment_Native.class;
    }
    
    
    /**
     * 查询退款接口实现类
     * @return
     */
    public Class<? extends IRefundQueryPayment> getRefundQueryClass(){
    	return QueryRefundPayment_Native.class;
    }
    
    /**
     * 关闭订单接口实现类
     * @return
     */
    public Class<? extends IClosePayment> getCloseClass(){
    	return ClosePayment_Native.class;
    }
    
    /**
     * 异步回调接口实现类
     * @return
     */
    public Class<? extends ICallBackPayment> getCallBackClass(){
    	return CallbackPayment_Native.class;
    }

	
}

