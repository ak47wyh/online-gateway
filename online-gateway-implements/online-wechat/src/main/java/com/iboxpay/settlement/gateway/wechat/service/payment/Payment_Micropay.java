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
import com.iboxpay.settlement.gateway.common.trans.close.IClosePayment;
import com.iboxpay.settlement.gateway.common.trans.payment.AbstractPayment;
import com.iboxpay.settlement.gateway.common.trans.payment.PaymentNavigation;
import com.iboxpay.settlement.gateway.common.trans.query.IQueryPayment;
import com.iboxpay.settlement.gateway.common.trans.refund.query.IRefundQueryPayment;
import com.iboxpay.settlement.gateway.common.trans.reverse.IReversePayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;
import com.iboxpay.settlement.gateway.wechat.WechatFrontEndConfig;
import com.iboxpay.settlement.gateway.wechat.service.PaymentWechatService;
import com.iboxpay.settlement.gateway.wechat.service.closed.ClosePayment_Native;
import com.iboxpay.settlement.gateway.wechat.service.query.QueryPayment_Micropay;
import com.iboxpay.settlement.gateway.wechat.service.refund.query.QueryRefundPayment_Native;
import com.iboxpay.settlement.gateway.wechat.service.reverse.ReversePayment_Micropay;
import com.iboxpay.settlement.gateway.wechat.service.utils.XmlUtils;


@Service
public class Payment_Micropay extends AbstractPayment{
	private static Logger logger = LoggerFactory.getLogger(Payment_Micropay.class);
	public static final String BANK_TRANS_CODE = "wechatMicropay";
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
		return "微信刷卡支付(Micropay)";
	}

	@Override
	public String pack(PaymentEntity[] payments) throws PackMessageException {
	    // 获取前置机
		WechatFrontEndConfig wechatConfig=(WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		
		// 数据组装转换
		PaymentEntity paymentEntity=payments[0];
		SortedMap<String,String> map =PaymentWechatService.initMicropayPayData(wechatConfig, paymentEntity);
		
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
			byte[] xmlBytes = response.getBytes(charset);
			Map<String, String> resultMap = XmlUtils.toMap(xmlBytes, charset);
			String returnCode=resultMap.get("return_code");
			String returnMsg= resultMap.get("return_msg");
			if (returnCode.equals("SUCCESS")) {
				String resultCode=resultMap.get("result_code");
				String errCode=resultMap.get("err_code");
				String errCodeDes=resultMap.get("err_code_des");
				if(resultCode.equals("SUCCESS")){
					String outTradeNo=resultMap.get("out_trade_no");
					String callbackExtProperties = PaymentWechatService.initCallbackExtProperties(resultMap,paymentEntity);
					if(outTradeNo.equals(paymentEntity.getBankSeqId())){
						PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUCCESS, "", "SUCCESS", "支付成功",callbackExtProperties);
					}
				} else if(errCode.equals("USERPAYING")){
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_WAITTING_PAY, "", errCode, errCodeDes);
				} else {
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", errCode, errCodeDes);
				}
			}else{
				PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", returnCode, returnMsg);
			}

		} catch (UnsupportedEncodingException e) {
			logger.error("解析反馈报文异常：" + e.getStackTrace());
			PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "", "0", "支付异常:"+e.getMessage());
		} catch (Exception e) {
			logger.error("解析反馈报文异常：" + e.getStackTrace());
			PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "", "0", "支付异常:"+e.getMessage());
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
		return ((WechatFrontEndConfig) TransContext.getContext().getFrontEndConfig()).getMicropayPayUrl().getVal();
	}
	
	//查询接口实现类
	@Override
	public Class<? extends IQueryPayment> getQueryClass() {
		return QueryPayment_Micropay.class;
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
