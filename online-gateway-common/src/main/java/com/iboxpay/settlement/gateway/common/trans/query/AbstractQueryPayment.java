package com.iboxpay.settlement.gateway.common.trans.query;

import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.net.ConnectionAdapter;
import com.iboxpay.settlement.gateway.common.trans.TransCode;

public abstract class AbstractQueryPayment extends ConnectionAdapter implements IQueryPayment {

    private static Logger logger = LoggerFactory.getLogger(AbstractQueryPayment.class);

    @Override
    public TransCode getTransCode() {
        return TransCode.QUERY;
    }

    public void query(PaymentEntity[] payments) throws BaseTransException {
        StringBuilder ids = new StringBuilder();
        for (int i = 0; i < payments.length; i++) {
            if (i > 0) ids.append(",");
            ids.append(payments[i].getId());
        }
        try {
            String rsqt = pack(payments);
            logger.info("准备【查询交易状态】请求报文(paymentId:[{}], batchSeqId:{}): \n{}", ids.toString(), payments[0].getBatchSeqId(), rsqt);
            openConnection();
            OutputStream os = getOutputStream();
            // 发送请求
            send(os, rsqt);

            handleAfterWrite(os);
            // 获取输入流
            InputStream is = getInputStream();
            // 接收报文
            String rsp = read(is);
            logger.info("接收【查询交易状态】返回报文(paymentId:[{}], batchSeqId:{}): \n{}", ids.toString(), payments[0].getBatchSeqId(), rsp);

            handleAfterRead(is);
            //接收完报文可以关连接了
            closeConnection();
            // 解析报文
            parse(rsp, payments);

        } finally {
            closeConnection();
        }
    }

    /**
     * 封装报文
     * 
     * @param payments
     * @return
     * @throws Exception
     */
    public abstract String pack(PaymentEntity[] payments) throws PackMessageException;

    /**
     * 解析报文
     * 
     * @param resultModel
     * @param respStr
     * @param payments
     * @return
     * @throws Exception
     */
    public abstract void parse(String respStr, PaymentEntity[] payments) throws ParseMessageException;

}
