package com.iboxpay.settlement.gateway.wft.service.payment;

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
import com.iboxpay.settlement.gateway.common.trans.close.IClosePayment;
import com.iboxpay.settlement.gateway.common.trans.payment.AbstractPayment;
import com.iboxpay.settlement.gateway.common.trans.payment.PaymentNavigation;
import com.iboxpay.settlement.gateway.common.trans.query.IQueryPayment;
import com.iboxpay.settlement.gateway.common.trans.refund.IRefundPayment;
import com.iboxpay.settlement.gateway.common.trans.refund.query.IRefundQueryPayment;
import com.iboxpay.settlement.gateway.common.trans.reverse.IReversePayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.wft.WftFrontEndConfig;
import com.iboxpay.settlement.gateway.wft.service.PaymentWeChatService;
import com.iboxpay.settlement.gateway.wft.service.WeChatContrants;
import com.iboxpay.settlement.gateway.wft.service.close.ClosePayment_Native;
import com.iboxpay.settlement.gateway.wft.service.query.QueryPayment_Native;
import com.iboxpay.settlement.gateway.wft.service.refund.RefundPayment_Native;
import com.iboxpay.settlement.gateway.wft.service.refund.query.QueryRefundPayment_Native;
import com.iboxpay.settlement.gateway.wft.service.reverse.ReversePayment_Micropay;
import com.iboxpay.settlement.gateway.wft.service.utils.SignUtils;
import com.iboxpay.settlement.gateway.wft.service.utils.XmlUtils;


@Service
public class Payment_Native extends AbstractPayment{
	private static Logger logger = LoggerFactory.getLogger(Payment_Native.class);
	public static final String BANK_TRANS_CODE = "wftNative";
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
		WftFrontEndConfig config=(WftFrontEndConfig) TransContext.getContext().getFrontEndConfig();

		// 数据组装转换
		PaymentEntity paymentEntity=payments[0];
		SortedMap<String, String> map =PaymentWeChatService.initNativePayData(config, paymentEntity);
		
		// 提交报文信息
		String reqContent = XmlUtils.parseXML(map);
		return reqContent;
	}



	@Override
	public void parse(String response, PaymentEntity[] payments) throws ParseMessageException {
		// 获取前置机信息
		WftFrontEndConfig config=(WftFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		String charset=config.getCharset().getVal();
		try {
			PaymentEntity paymentEntity = payments[0];
			Map<String, Object> merchantMap=paymentEntity.getMerchantMap();
			// 交易秘钥
			String merchantKey=String.valueOf(merchantMap.get("payMerchantKey"));
			
			byte[] xmlBytes = response.getBytes(charset);
			Map<String,String> resultMap = XmlUtils.toMap(xmlBytes, charset);

			if (resultMap.containsKey("sign")) {
				if (!SignUtils.checkParam(resultMap, merchantKey)) {
					logger.error("验证签名不通过!");
				} else {
					if (WeChatContrants.STATUS_SUCCESS.equals(resultMap.get("status")) && WeChatContrants.RESULT_CODE_SUCCESS.equals(resultMap.get("result_code"))) {
						String codeImgUrl = resultMap.get("code_img_url");//二维码图片
						String codeUrl = resultMap.get("code_url");//二维码地址
						// 扩展属性存放json格式数据
		            	Map<String,Object> extParam=new HashMap<String,Object>();
		            	extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeUrl, codeUrl);
		            	extParam.put(PaymentEntity.CALLBACK_EXT_PROPERTY_CodeImgUrl, codeImgUrl);
						String callbackExtProperties=JsonUtil.toJson(extParam);
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUBMITTED, "", WeChatContrants.STATUS_SUCCESS, "提交成功",callbackExtProperties);
					} else {
						String errCode = (String) resultMap.get("err_code");
						String errMsg = (String) resultMap.get("err_msg");
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", errCode, errMsg);
					}
				}
			} else {
				String status =resultMap.get("status");
				String message =resultMap.get("message");
				PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", status, message);
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
		return ((WftFrontEndConfig) TransContext.getContext().getFrontEndConfig()).getUri().getVal();
	}

	
	
	@Override
	public Class<? extends IQueryPayment> getQueryClass() {
		return QueryPayment_Native.class;
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
     * 冲正接口实现类
     * @return
     */
    public Class<? extends IReversePayment> getReverseClass(){
    	return ReversePayment_Micropay.class;
    }
    
    /**
     * 关闭订单接口实现类
     * @return
     */
    public Class<? extends IClosePayment> getCloseClass(){
    	return ClosePayment_Native.class;
    }
}

