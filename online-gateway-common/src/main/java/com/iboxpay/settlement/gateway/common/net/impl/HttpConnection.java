package com.iboxpay.settlement.gateway.common.net.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.settlement.gateway.common.exception.FrontEndException;
import com.iboxpay.settlement.gateway.common.net.IConnection;

/**
 * http连接实现
 * @author jianbo_chen
 */
public class HttpConnection implements IConnection {

    private static Logger logger = LoggerFactory.getLogger(HttpConnection.class);

    protected HttpURLConnection con;
    private String ip;
    private int port;
    private int timeout;
    private String uri;
    private String method;
    private Map<String, String> headerMap;

    public HttpConnection(String ip, int port, int timeout) {
        this.ip = ip;
        this.port = port;
        this.timeout = timeout;
    }

    private String getServerInfo() {
        return "(ip:" + ip + ", port:" + port + ")";
    }

    @Override
    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void openConnection() throws FrontEndException {
        try {
            uri = uri == null ? "" : uri;
            URL url = new URL("http", ip, port, uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(method == null ? "POST" : method);
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);
            con.setConnectTimeout(timeout);
            con.setReadTimeout(this.timeout);
            //设置http头部值
            Map<String, String> headerMap = this.headerMap;
            if (headerMap != null) {
                Map.Entry<String, String> entry;
                for (Iterator<Map.Entry<String, String>> itr = headerMap.entrySet().iterator(); itr.hasNext();) {
                    entry = itr.next();
                    if (entry.getValue() == null) continue;

                    con.addRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            this.con = con;
            con.connect();
        } catch (IOException e) {
            throw new FrontEndException("打开网络连接失败" + getServerInfo(), e);
        }
    }

    @Override
    public void closeConnection() {
        try {
            this.con.disconnect();
        } catch (Exception e) {
            logger.info(e.getMessage() + getServerInfo(), e);
        }
    }

    private void verifyConnection() throws IOException {
        if (null == this.con) {
            throw new IOException("程序错误:请先调用openConnection()方法！");
        }
    }

    @Override
    public InputStream getInputStream() throws FrontEndException {
        try {
            verifyConnection();
            return this.con.getInputStream();
        } catch (IOException e) {
            throw new FrontEndException(e.getMessage() + getServerInfo(), e);
        }
    }

    @Override
    public OutputStream getOutputStream() throws FrontEndException {
        try {
            verifyConnection();
            return this.con.getOutputStream();
        } catch (IOException e) {
            throw new FrontEndException(e.getMessage() + getServerInfo(), e);
        }
    }

    @Override
    public int getResponseCode() throws FrontEndException {
        try {
            verifyConnection();
            return this.con.getResponseCode();
        } catch (IOException e) {
            throw new FrontEndException(e.getMessage() + getServerInfo(), e);
        }
    }

    @Override
    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public void setHeaderMap(Map<String, String> headerMap) {
        this.headerMap = headerMap;
    }

    public static void main(String[] args) throws Exception {
        HttpConnection con = new HttpConnection("www.baidu.com", 80, 3 * 60 * 1000);
        con.openConnection();
        con.getOutputStream().write("ok".getBytes());
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "GBK"));
        String line = null;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
    }

}
