package com.iboxpay.settlement.gateway.common.trans.close;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.net.ConnectionAdapter;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.TransCode;

public abstract class AbstractClosePayment extends ConnectionAdapter implements IClosePayment{
	private static Logger logger = LoggerFactory.getLogger(AbstractClosePayment.class);
	@Override
	public TransCode getTransCode() {
		 return TransCode.CLOSE;
	}

	@Override
	public String getBankTransCode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getBankTransDesc() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void closeOrder(PaymentEntity[] payments) throws BaseTransException, IOException {
		StringBuilder ids = new StringBuilder();
		for (int i = 0; i < payments.length; i++) {
			if (i > 0)ids.append(",");
			ids.append(payments[i].getId());
		}
		try {
			String rsqt = pack(payments);
			logger.info("准备【关闭订单】报文(paymentId:[" + ids + "], batchSeqId:" + payments[0].getBatchSeqId() + "): \n" + rsqt);
			openConnection();
			OutputStream os = getOutputStream();
			try {
				// 发送请求
				send(os, rsqt);

				handleAfterWrite(os);
				// 获取输入流
				InputStream is = getInputStream();
				// 接收报文
				String rsp = read(is);
				logger.info("接收【关闭订单】返回报文(paymentId:[" + ids + "], batchSeqId:" + payments[0].getBatchSeqId() + "): \n"+ rsp);
				
				handleAfterRead(is);
				// 解析报文
				parse(rsp, payments);
			} catch (Throwable e) {
				logger.error("", e);
				PaymentStatus.processExceptionWhenPay(e, payments);
			}
		} catch (Throwable e) {
			logger.error("", e);
			PaymentStatus.processExceptionBeforePay(e, payments);
		} finally {
			closeConnection();
		}

	}
	
	
    /**
     * 封装报文
     * @param payments
     * @return
     * @throws Exception
     */
    public abstract String pack(PaymentEntity[] payments) throws PackMessageException;

    /**
     * 解析报文
     * @param resultModel
     * @param respStr
     * @param payments
     * @return
     * @throws Exception
     */
    public abstract void parse(String respStr, PaymentEntity[] payments) throws ParseMessageException;
    
    public String toString() {
        return "支付交易码: " + getBankTransCode() + "， 业务描述： " + getBankTransDesc();
    }

}
