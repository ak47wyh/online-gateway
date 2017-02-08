package com.iboxpay.settlement.gateway.common.trans;

import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.net.ConnectionAdapter;

public abstract class CommonRequestAdapter<T> extends ConnectionAdapter {

    private static Logger logger = LoggerFactory.getLogger(CommonRequestAdapter.class);

    public abstract String getImplInfo();

    public void request(T[] objs) throws BaseTransException {
        try {
            String rsqt = pack(objs);
            logger.info("准备【" + getImplInfo() + "】报文: \n" + rsqt);
            openConnection();
            OutputStream os = getOutputStream();
            //发送请求
            send(os, rsqt);

            handleAfterWrite(os);
            // 获取输入流
            InputStream is = getInputStream();
            // 接收报文
            String rsp = read(is);
            logger.info("接收【" + getImplInfo() + "】返回报文: \n" + rsp);
            handleAfterRead(is);
            // 解析报文
            parse(rsp, objs);
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
    public abstract String pack(T[] objs) throws PackMessageException;

    /**
     * 解析报文
     * @param resultModel
     * @param respStr
     * @param payments
     * @return
     * @throws Exception
     */
    public abstract void parse(String respStr, T[] objs) throws ParseMessageException;

}
