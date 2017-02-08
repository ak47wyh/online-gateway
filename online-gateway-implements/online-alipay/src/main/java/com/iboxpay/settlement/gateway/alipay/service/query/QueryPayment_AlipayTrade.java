package com.iboxpay.settlement.gateway.alipay.service.query;

import java.io.IOException;
import java.util.Date;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.alipay.AlipayFrontEndConfig;
import com.iboxpay.settlement.gateway.alipay.service.AlipayContranst;
import com.iboxpay.settlement.gateway.alipay.service.reverse.ReversePayment_QrTrade;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.query.IQueryPayment;

@Service
public class QueryPayment_AlipayTrade implements IQueryPayment{

	private static Logger logger = LoggerFactory.getLogger(QueryPayment_AlipayQrTrade.class);
	private final static String TRANS_CODE_QUERY = "queryAlipayTrade";
	private final static String TRANS_CODE_QUERY_DESC = "支付宝交易查询";

	@Resource
	private QueryPayment_AlipayQrTrade alipayQrTrade;
	
	@Resource
	private ReversePayment_QrTrade reversePaymentQrTrade;
	
	
	@Override
	public TransCode getTransCode() {
		return TransCode.QUERY;
	}

	@Override
	public String getBankTransCode() {
		return TRANS_CODE_QUERY;
	}

	@Override
	public String getBankTransDesc() {
		return TRANS_CODE_QUERY_DESC;
	}
	
	
	@Override
	public void query(PaymentEntity[] payments) throws BaseTransException, IOException {
		// 获取前置机
		AlipayFrontEndConfig config = (AlipayFrontEndConfig) TransContext.getContext().getFrontEndConfig();
    	
        PaymentEntity payment = payments[0];
        int queryOverTime = Integer.parseInt(config.getQueryOverTime().getVal());
        long queryOverTimeMs = queryOverTime*60*1000;
        
        long queryTimeMs = payment.getSubmitPayTime().getTime();
        long queryInterval = new Date().getTime() - queryTimeMs;
        // 设置扫码订单大于10分钟未支付就调用关闭订单服务处理
        if (queryInterval<=queryOverTimeMs) {
    		queryAlipay(payments);
        } else {
        	try {
        		reversePaymentQrTrade.reverse(payments);
			} catch (IOException e) {
				logger.error("关闭订单报文异常："+e);
			}
        }
	}

	/**
	 * 支付宝查询分发入口程序
	 * @param payments
	 * @throws BaseTransException
	 * @throws IOException
	 */
	private void queryAlipay(PaymentEntity[] payments) throws BaseTransException, IOException {
		PaymentEntity paymentEntity=payments[0];
		String appType= paymentEntity.getAppType();
		if(appType.equals(AlipayContranst.PAY_TYPE_NATIVE)){//扫码支付
			alipayQrTrade.query(payments);
		} else if (appType.equals(AlipayContranst.PAY_TYPE_MICROPAY)){//刷卡支付
			
		} else if (appType.equals(AlipayContranst.PAY_TYPE_WAP)){//手机网站支付
			
		}
	}

}
