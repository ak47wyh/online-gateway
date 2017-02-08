package com.iboxpay.settlement.gateway.common.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.iboxpay.settlement.gateway.common.exception.FrontEndException;

public interface IConnection {

    /**
     * 设置IP(必须在OpenConnection前调用)
     * @param ip
     */
    void setIp(String ip);

    /**
     * 设置端口(必须在OpenConnection前调用)
     * @param port
     */
    void setPort(int port);

    /**建立连接
     * @throws IOException
     */
    void openConnection() throws FrontEndException;

    /**
     * @return 获取输入流
     * @throws IOException
     */
    InputStream getInputStream() throws FrontEndException;

    /**
     * @return 获取输出流
     * @throws IOException
     */
    OutputStream getOutputStream() throws FrontEndException;

    /**
     * @return 获取响应码
     * @throws IOException
     */
    int getResponseCode() throws FrontEndException;

    /**	关闭连接，释放资源
     * @throws IOException
     */
    void closeConnection();

    /**
     * 操作方法，GET or POST
     * @return
     */
    void setMethod(String method);

    /**
     * 访问uri(http or https 用到)
     * @return
     */
    void setUri(String uri);

    /**
     * 头部值(http or https用到)
     * @return
     */
    void setHeaderMap(Map<String, String> headerMap);
}
