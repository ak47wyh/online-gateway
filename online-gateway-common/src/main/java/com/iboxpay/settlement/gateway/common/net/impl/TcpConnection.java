package com.iboxpay.settlement.gateway.common.net.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.settlement.gateway.common.exception.FrontEndException;
import com.iboxpay.settlement.gateway.common.net.IConnection;

public class TcpConnection implements IConnection {

    private static Logger logger = LoggerFactory.getLogger(TcpConnection.class);

    private Socket socket;
    private String ip;
    private int port;
    private int timeout;

    private boolean ssl = false;
    private File keyStore = null;
    private File trustKeyStore = null;

    private String uri;
    private String method = "POST";
    private Map<String, String> headerMap;

    /**
     * @param ip
     *            目标地址
     * @param port
     *            目标端口
     * @param timeout
     *            读取时的响应超时，单位:毫秒
     */
    public TcpConnection(String ip, int port, int timeout) {
        super();
        this.ip = ip;
        this.port = port;
        this.timeout = timeout;
    }

    TcpConnection(String host, int port, int timeout, File keyStore, File trustKeyStore) {
        this(host, port, timeout);
        this.keyStore = keyStore;
        this.trustKeyStore = trustKeyStore;
        this.ssl = true;
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

    public void openConnection() throws FrontEndException {
        try {
            if (!ssl) {
                socket = new Socket(ip, port);
            } else {
                socket = createSSLSocket();
            }
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(timeout);
        } catch (IOException e) {
            throw new FrontEndException("打开网络连接失败" + getServerInfo(), e);
        }
    }

    public OutputStream getOutputStream() throws FrontEndException {
        try {
            verifyConnection();
            return socket.getOutputStream();
        } catch (IOException e) {
            throw new FrontEndException(e.getMessage() + getServerInfo(), e);
        }
    }

    public int getResponseCode() throws FrontEndException {
        verifyConnection();
        return 200;
    }

    public InputStream getInputStream() throws FrontEndException {
        try {
            verifyConnection();
            return socket.getInputStream();
        } catch (IOException e) {
            throw new FrontEndException(e.getMessage() + getServerInfo(), e);
        }
    }

    public void closeConnection() {
        if (null != socket) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.warn("关闭连接时IO异常" + getServerInfo(), e);
            }
        }
    }

    private void verifyConnection() throws FrontEndException {
        if (null == socket) {
            throw new FrontEndException("【程序错误】请先调用openConnection方法.");
        } else if (socket.isClosed()) {
            throw new FrontEndException("socket已经关闭.");
        } else if (!socket.isConnected()) {
            throw new FrontEndException("socket连接已经断开.");
        }
    }

    private Socket createSSLSocket() throws FrontEndException {
        FileInputStream keyStoreInStream = null;
        FileInputStream trustStoreInStream = null;
        if (this.keyStore == null) {
            throw new FrontEndException("不能装入私钥KeyStore.");
        }
        if (this.trustKeyStore == null) {
            throw new FrontEndException("不能装入信任证书KeyStore.");
        }
        try {
            SSLContext ctx = SSLContext.getInstance("SSL");

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

            KeyStore ks = KeyStore.getInstance("JKS");
            KeyStore tks = KeyStore.getInstance("JKS");

            keyStoreInStream = new FileInputStream("serverStore.jks");// new FileInputStream(this.keyStore); 
            trustStoreInStream = new FileInputStream("serverStore.jks"); //new FileInputStream(this.trustKeyStore);
            ks.load(keyStoreInStream, "storepass".toCharArray());
            tks.load(trustStoreInStream, "storepass".toCharArray());

            kmf.init(ks, "serverpass".toCharArray());
            tmf.init(tks);
            ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            return ctx.getSocketFactory().createSocket(this.ip, this.port);
        } catch (NoSuchAlgorithmException e) {
            throw new FrontEndException("找不到密钥算法", e);
        } catch (KeyStoreException e) {
            throw new FrontEndException("密钥读取错误", e);
        } catch (UnrecoverableKeyException e) {
            throw new FrontEndException("密钥读取错误", e);
        } catch (KeyManagementException e) {
            throw new FrontEndException("密钥读取错误", e);
        } catch (FileNotFoundException e) {
            throw new FrontEndException("密钥库文件serverStore.jks找不到", e);
        } catch (UnknownHostException e) {
            throw new FrontEndException("目标地址(前置机)无法识别", e);
        } catch (Exception e) {
            throw new FrontEndException("初始化SSL失败", e);
        } finally {
            try {
                if (keyStoreInStream != null) {
                    keyStoreInStream.close();
                }
                if (trustStoreInStream != null) {
                    trustStoreInStream.close();
                }
            } catch (IOException e) {}
        }
    }

    @Override
    public void setHeaderMap(Map<String, String> headerMap) {
        this.headerMap = headerMap;
    }

    @Override
    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public void setMethod(String method) {
        this.method = method;
    }

}
