package com.iboxpay.settlement.gateway.common.trans.payment;

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
import com.iboxpay.settlement.gateway.common.trans.callback.ICallBackPayment;
import com.iboxpay.settlement.gateway.common.trans.close.IClosePayment;
import com.iboxpay.settlement.gateway.common.trans.refund.IRefundPayment;
import com.iboxpay.settlement.gateway.common.trans.refund.query.IRefundQueryPayment;
import com.iboxpay.settlement.gateway.common.trans.reverse.IReversePayment;
import com.iboxpay.settlement.gateway.common.util.Sequence;

public abstract class AbstractPayment extends ConnectionAdapter implements IPayment {

    private static Logger logger = LoggerFactory.getLogger(AbstractPayment.class);

    @Override
    public TransCode getTransCode() {
        return TransCode.PAY;
    }

    @Override
    public void genBankBatchSeqId(PaymentEntity payments[]) {
        String bankBatchSeqId = Sequence.genSequence();
        for (PaymentEntity payment : payments)
            payment.setBankBatchSeqId(bankBatchSeqId);
    }

    //一般不需要明细号,有的话各自实现放开吧.
    @Override
    public void genBankSeqId(PaymentEntity payments[]) {
        //		for(PaymentEntity payment : payments)
        //			payment.setBankSeqId(Sequence.genSequence());
    }

    @Override
    public boolean navigateMatch(PaymentEntity payment) {
        return true;//默认必须返回匹配
    }

    @Override
    public void pay(PaymentEntity[] payments) throws BaseTransException {
        StringBuilder ids = new StringBuilder();
        for (int i = 0; i < payments.length; i++) {
            if (i > 0) ids.append(",");
            ids.append(payments[i].getId());
        }
        try {
            String rsqt = pack(payments);
            logger.info("准备【支付】报文(paymentId:[" + ids + "], batchSeqId:" + payments[0].getBatchSeqId() + "): \n" + rsqt);
            openConnection();
            OutputStream os = getOutputStream();
            try {
                //发送请求
                send(os, rsqt);

                handleAfterWrite(os);
                // 获取输入流
                InputStream is = getInputStream();
                // 接收报文
                String rsp = read(is);
                logger.info("接收【支付】返回报文(paymentId:[" + ids + "], batchSeqId:" + payments[0].getBatchSeqId() + "): \n" + rsp);
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
        return "支付交易码: " + getBankTransCode() + "， 业务描述： " + getBankTransDesc() + "。支持业务： " + navigate();
    }
    
    
    /**
     * 退款接口实现类
     * @return
     */
	@Override
    public Class<? extends IRefundPayment> getRefundClass(){
    	return null;
    }
    
    
    /**
     * 查询退款接口实现类
     * @return
     */
    public Class<? extends IRefundQueryPayment> getRefundQueryClass(){
    	return null;
    }
    
    /**
     * 冲正接口实现类
     * @return
     */
    public Class<? extends IReversePayment> getReverseClass(){
    	return null;
    }
    
    
    /**
     * 关闭订单接口实现类
     * @return
     */
    public Class<? extends IClosePayment> getCloseClass(){
    	return null;
    }
    
    /**
     * 异步回调接口实现类
     * @return
     */
    public  Class<? extends ICallBackPayment> getCallBackClass(){
    	return null;
    }
}
