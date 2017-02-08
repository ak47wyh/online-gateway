package com.iboxpay.settlement.gateway.common.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.exception.FrontEndException;
import com.iboxpay.settlement.gateway.common.net.impl.HttpConnection;
import com.iboxpay.settlement.gateway.common.net.impl.HttpsConnection;
import com.iboxpay.settlement.gateway.common.net.impl.TcpConnection;
import com.iboxpay.settlement.gateway.common.trace.Trace;
import com.iboxpay.settlement.gateway.common.trace.TraceType;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

/**
 * 连接代理
 * @author jianbo_chen
 */
public abstract class ConnectionAdapter implements IConnection {

    private final static CompositeConnectionListener compositeConnectionListener = new CompositeConnectionListener();
    //bug fix: 业务类是单例
    private static ThreadLocal<IConnection> con = new ThreadLocal<IConnection>();

    public static CompositeConnectionListener getCompositeconnectionlistener() {
        return compositeConnectionListener;
    }

    @Override
    public void closeConnection() {
        try {
            con.get().closeConnection();
        } catch (Exception e) {}
        con.set(null);
        Trace.endTrace(TraceType.NET);
    }

    @Override
    public InputStream getInputStream() throws FrontEndException {
        return con.get().getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws FrontEndException {
        return con.get().getOutputStream();
    }

    @Override
    public int getResponseCode() throws FrontEndException {
        return con.get().getResponseCode();
    }

    /**
     * http or https 头信息
     * @return
     */
    protected Map<String, String> getHeaderMap() {
        return null;
    }

    /**
     * http or https 的请求方法
     * @return
     */
    protected String getMethod() {
        return null;
    }

    /**
     * http or https 的uri
     * @return
     */
    protected String getUri() {
        return null;
    }

    private void prepareBase(IConnection con) {
        String ip = getIp();
        if (!StringUtils.isBlank(ip)) con.setIp(ip);

        int port = getPort();
        if (port > 0) con.setPort(port);

        con.setHeaderMap(getHeaderMap());
        con.setMethod(getMethod());
        con.setUri(getUri());
    }

    public String getIp() {
        return null;
    }

    public int getPort() {
        return 0;
    }

    @Override
    public void openConnection() throws FrontEndException {
        Trace.beginTrace(TraceType.NET);
        FrontEndConfig feConfig = TransContext.getContext().getFrontEndConfig();
        if (feConfig == null) {
            throw new NullPointerException("no available frontEnd.");
        }
        String protocol = feConfig.getProtocal().getVal();
        String ip = feConfig.getIp().getVal();
        int port = feConfig.getPort().getIntVal();
        int timeout = feConfig.getTimeout().getIntVal() * 1000;
        IConnection _con;
        if ("http".equalsIgnoreCase(protocol)) {
            _con = new HttpConnection(ip, port, timeout);
        } else if ("tcp".equalsIgnoreCase(protocol)) {
            _con = new TcpConnection(ip, port, timeout);
        } else if ("https".equalsIgnoreCase(protocol)) {
            _con = new HttpsConnection(ip, port, timeout);
        } else {
            throw new UnsupportedOperationException("暂不支持通信协议 : " + protocol);
        }
        prepareBase(_con);
        try {
            _con.openConnection();
        } catch (FrontEndException e) {
            compositeConnectionListener.onConnectionFail(ip, port, feConfig, e);
            throw e;
        }
        compositeConnectionListener.onConnectionSuccess(ip, port, feConfig);
        con.set(_con);
    }
    
    public void openConnection(String ip, int port) throws FrontEndException {
        Trace.beginTrace(TraceType.NET);
        FrontEndConfig feConfig = TransContext.getContext().getFrontEndConfig();
        if (feConfig == null) {
            throw new NullPointerException("no available frontEnd.");
        }
        String protocol = feConfig.getProtocal().getVal();
        int timeout = feConfig.getTimeout().getIntVal() * 1000;
        IConnection _con;
        if ("http".equalsIgnoreCase(protocol)) {
            _con = new HttpConnection(ip, port, timeout);
        } else if ("tcp".equalsIgnoreCase(protocol)) {
            _con = new TcpConnection(ip, port, timeout);
        } else if ("https".equalsIgnoreCase(protocol)) {
            _con = new HttpsConnection(ip, port, timeout);
        } else {
            throw new UnsupportedOperationException("暂不支持通信协议 : " + protocol);
        }
        prepareBase(_con);
        try {
            _con.openConnection();
        } catch (FrontEndException e) {
            compositeConnectionListener.onConnectionFail(ip, port, feConfig, e);
            throw e;
        }
        compositeConnectionListener.onConnectionSuccess(ip, port, feConfig);
        con.set(_con);
    }

    public void send(OutputStream out, byte[] b) throws FrontEndException {
        try {
            out.write(b);
            out.flush();
        } catch (IOException e) {
            throw new FrontEndException(e);
        }
    }

    public void send(OutputStream out, String s) throws FrontEndException {
        FrontEndConfig feConfig = TransContext.getContext().getFrontEndConfig();
        try {
            send(out, s.getBytes(feConfig.getCharset().getVal()));
        } catch (UnsupportedEncodingException e) {
            throw new FrontEndException("不支持字符编码【" + feConfig.getCharset().getVal() + "】", e);
        }
    }

    public String read(InputStream is) throws FrontEndException {
        FrontEndConfig feConfig = TransContext.getContext().getFrontEndConfig();
        InputStreamReader inputStreamReader;
        try {
            inputStreamReader = new InputStreamReader(is, feConfig.getCharset().getVal());
        } catch (UnsupportedEncodingException e) {
            throw new FrontEndException("不支持字符编码【" + feConfig.getCharset().getVal() + "】", e);
        }
        StringBuilder buf = new StringBuilder();
        int ch;
        try {
            while ((ch = inputStreamReader.read()) != -1) {
                buf.append((char) ch);
            }
        } catch (IOException e) {
            throw new FrontEndException(e);
        }
        return buf.toString();
    }
    
    /**
     * 发送流后处理，一般不需要关闭，因为接收端会根据报文内容判断报文已经发送完毕，比如建行的结束标志为</TX>
     * @param os
     */
    protected void handleAfterWrite(OutputStream os) {

    }

    /**
     * 读取完成后处理
     * @param is
     */
    protected void handleAfterRead(InputStream is) {
    }

    protected void closeNoException(OutputStream os) {
        try {
            os.close();
        } catch (IOException e) {}
    }

    protected void closeNoException(InputStream is) {
        try {
            is.close();
        } catch (IOException e) {}
    }

    @Override
    public final void setHeaderMap(Map<String, String> headerMap) {

    }

    @Override
    public final void setMethod(String method) {

    }

    @Override
    public final void setUri(String uri) {

    }

    @Override
    public final void setIp(String ip) {

    }

    @Override
    public final void setPort(int port) {

    }

}
