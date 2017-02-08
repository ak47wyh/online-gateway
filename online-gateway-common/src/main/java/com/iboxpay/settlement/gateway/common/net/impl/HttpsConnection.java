package com.iboxpay.settlement.gateway.common.net.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.exception.FrontEndException;
import com.iboxpay.settlement.gateway.common.net.HttpsContext;
import com.iboxpay.settlement.gateway.common.net.IConnection;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

/**
 * https连接实现
 * @author jianbo_chen
 */
public class HttpsConnection implements IConnection {

    private static Logger logger = LoggerFactory.getLogger(HttpsConnection.class);

    protected HttpURLConnection con;
    private String ip;
    private int port;
    private int timeout;
    private String uri;
    private String method;
    private Map<String, String> headerMap;

    public HttpsConnection(String ip, int port, int timeout) {
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

    private static class TrustAnyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }
    }

    private static class TrustAnyHostnameVerifier implements HostnameVerifier {

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    @Override
    public void openConnection() throws FrontEndException {
        boolean trustCertification = false;
        SSLContext sslContext = null;
        KeyManager[] kms = null;
        FrontEndConfig feConfig = TransContext.getContext().getFrontEndConfig();
        try {
            if (feConfig instanceof HttpsContext) {
                HttpsContext feSslContext = (HttpsContext) feConfig;
                if (!StringUtils.isBlank(feSslContext.keyStoreType()) && feSslContext.keyStoreFile() != null) {//双向认证私匙
                    KeyStore clientStore = KeyStore.getInstance(feSslContext.keyStoreType());//如"PKCS12"
                    clientStore.load(new FileInputStream(feSslContext.keyStoreFile()), feSslContext.keyStorePassword().toCharArray());

                    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                    kmf.init(clientStore, StringUtils.isBlank(feSslContext.keyPassword()) ? "".toCharArray() : feSslContext.keyPassword().toCharArray());
                    kms = kmf.getKeyManagers();
                }
                trustCertification = feSslContext.trustCertification();
            }
            TrustManager[] trustManagers = trustCertification ? new TrustManager[] { new TrustAnyTrustManager() } : null;
            if (kms != null || trustManagers != null) {
                sslContext = SSLContext.getInstance("SSL");
                sslContext.init(kms, trustManagers, new SecureRandom());
            }
        } catch (Exception e) {
            throw new FrontEndException("初始化https密钥信息异常" + getServerInfo(), e);
        }

        try {
            uri = uri == null ? "" : uri;
            URL url = new URL("https", ip, port, uri);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            if (sslContext != null) con.setSSLSocketFactory(sslContext.getSocketFactory());
            if (trustCertification) con.setHostnameVerifier(new TrustAnyHostnameVerifier());
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
        //        HttpsConnection con = new HttpsConnection("git.iboxpay.com", 443, 3 * 60 * 1000);
        //        con.setMethod("GET");
        //        con.openConnection();
        //        con.getOutputStream().write("ok".getBytes());
        //        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "GBK"));
        //        String line = null;
        //        while ((line = br.readLine()) != null) {
        //            System.out.println(line);
        //        }
        KeyStore.getInstance("X509");
    }

}
