package com.iboxpay.settlement.gateway.common.trans.balance;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.settlement.gateway.common.domain.BalanceEnity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.net.ConnectionAdapter;
import com.iboxpay.settlement.gateway.common.trans.TransCode;

/**
 * 查询余额抽象实现
 * @author jianbo_chen
 */
public abstract class AbstractBalance extends ConnectionAdapter implements IBalance {

    private static Logger logger = LoggerFactory.getLogger(AbstractBalance.class);

    @Override
    public void queryBalance(BalanceEnity[] balanceEntities) throws BaseTransException, IOException {
        try {
            String rsqt = pack(balanceEntities);
            logger.info("发送【查询余额】报文：" + rsqt);
            openConnection();
            OutputStream os = getOutputStream();
            // 发送请求
            send(os, rsqt);

            handleAfterWrite(os);
            // 获取输入流
            InputStream is = getInputStream();
            // 接收报文
            String rsp = read(is);
            logger.info("接收【查询余额】返回报文：" + rsp);

            handleAfterRead(is);
            // 解析报文
            parse(rsp, balanceEntities);

        } finally {
            closeConnection();
        }
    }

    /**
     * 封闭报文.某些银行可能支持批量账户查询余额.
     * @param balanceEntity
     * @return
     */
    public abstract String pack(BalanceEnity[] balanceEntities) throws PackMessageException;

    /**
     * 解析余额返回报文
     * @param respStr
     * @param balanceEntities
     * @return
     */
    public abstract void parse(String respStr, BalanceEnity[] balanceEntities) throws ParseMessageException;

    @Override
    public TransCode getTransCode() {
        return TransCode.BALANCE;
    }

}
