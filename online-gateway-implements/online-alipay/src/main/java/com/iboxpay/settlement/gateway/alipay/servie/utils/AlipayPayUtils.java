package com.iboxpay.settlement.gateway.alipay.servie.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.iboxpay.common.cst.CommonCst;
import com.iboxpay.common.exception.IboxpayException;
import com.squareup.okhttp.MediaType;

public class AlipayPayUtils {

    private final static Logger LOG = LoggerFactory.getLogger(AlipayPayUtils.class);

    public static final MediaType JSON = MediaType.parse("text/xml; charset=utf-8");

    public static String newHttpClientResponseCharset(String url, Map<String, Object> paramMap, int timeout) throws IboxpayException {
        HttpPost post = new HttpPost(url);
        try {
            if (paramMap != null && paramMap.size() != 0) {
                buildMapParam(paramMap, post);
            }
            HttpClient httpClient = new DefaultHttpClient();
            httpClient = wrapClient(httpClient);

            BasicHttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, timeout * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, timeout * 1000);
            post.setParams(httpParams);
            

            HttpResponse response = httpClient.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                // 根据响应的字符集返回报文
                ContentType contentType = ContentType.getOrDefault(response.getEntity());
                Charset charset = contentType.getCharset();
                // 默认UTF-8
                String charsetName = CommonCst.CHARSET_UTF_8_NAME;
                if (charset != null) {
                    charsetName = charset.name();
                }
                String res = getResponseBodyString(response.getEntity().getContent(), charsetName);
                if (res.length() != 0) {
                    return res;
                }
            } else {
                LOG.error("newHttpClient has response error!reposne code is " + statusCode);
                throw new IboxpayException(statusCode, "newHttpClient has response error!reposne code is " + statusCode);
            }
        } catch (ConnectionPoolTimeoutException e) {
            LOG.error("newHttpClient connection pool time out, " + e.getMessage(), e);
            throw new IboxpayException(100, "newHttpClient connection time out");
        } catch (ConnectTimeoutException e) {
            LOG.error("newHttpClient connection time out, " + e.getMessage(), e);
            throw new IboxpayException(100, "newHttpClient connection time out");
        } catch (SocketTimeoutException e) {
            LOG.error("newHttpClient socket time out, " + e.getMessage(), e);
            throw new IboxpayException(100, "SocketTimeoutException");
        } catch (ClientProtocolException e) {
            LOG.error("newHttpClient has client protocol exception. " + e.getMessage(), e);
            throw new IboxpayException(100, "newHttpClient has client protocol exception");
        } catch (IllegalStateException e) {
            LOG.error("newHttpClient has illegal state exception. " + e.getMessage(), e);
            throw new IboxpayException(100, "newHttpClient IllegalStateException");
        } catch (IOException e) {
            LOG.error("newHttpClient has io exception. " + e.getMessage(), e);
            throw new IboxpayException(100, "newHttpClient IOException");
        } catch (Exception e) {
            LOG.error("newHttpClient has exception. " + e.getMessage(), e);
            throw new IboxpayException(100, "exception");
        } finally {
            // 关闭请求
            post.releaseConnection();
        }

        return null;
    }

    /**
     * 根据指定的字符集获取响应数据流
     * 
     * @param postMethod
     * @param charset
     *            编码字符
     * @return
     * @throws Exception
     */
    public static String getResponseBodyString(InputStream inputStream, String charset) {
        StringBuffer sb = new StringBuffer();
        BufferedReader reader = null;
        try {
            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream, charset));
            String str = "";
            while ((str = reader.readLine()) != null) {
                sb.append(str);
            }
            return sb.toString();
        } catch (IOException e) {
            LOG.error("read http response body stream has error. " + e.getMessage(), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.error("close http response body stream has error. " + e.getMessage(), e);
                }
            }
        }

        return null;
    }

    /**
     * 避免HttpClient的”SSLPeerUnverifiedException: peer not authenticated”异常
     */
    @SuppressWarnings("deprecation")
    public static HttpClient wrapClient(HttpClient base) {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }

                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                }
            };
            ctx.init(null, new TrustManager[] { tm }, null);
            SSLSocketFactory ssf = new SSLSocketFactory(ctx);
            ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager ccm = base.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", ssf, 443));

            return new DefaultHttpClient(ccm, base.getParams());
        } catch (Exception ex) {
            LOG.error("wrapClient has error. " + ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * 初始http post请求参数（http 4.2）
     * 
     * @param paramMap
     * @param httpEntityEnclosingRequestBase
     * @throws UnsupportedEncodingException
     */
    private static void buildMapParam(Map<String, Object> paramMap, HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase) throws UnsupportedEncodingException {
        // 设置参数
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        Iterator<Entry<String, Object>> iterator = paramMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, Object> elem = iterator.next();
            if (elem.getKey() != null) {
                Object value = elem.getValue();
                setParam(list, elem, value);
            }
        }
        if (!list.isEmpty()) {
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, CommonCst.CHARSET_UTF_8_NAME);
            httpEntityEnclosingRequestBase.setEntity(entity);
        }
        httpEntityEnclosingRequestBase.addHeader(CommonCst.TRANSACTION_ID, MDC.get(CommonCst.TRANSACTION_ID));
    }

    private static void setParam(List<NameValuePair> list, Entry<String, Object> elem, Object value) {
        if (value == null) {
            list.add(new BasicNameValuePair(elem.getKey(), null));
        } else {
            if (value instanceof String) {
                list.add(new BasicNameValuePair(elem.getKey(), (String) value));
            } else {
                list.add(new BasicNameValuePair(elem.getKey(), String.valueOf(value)));
            }
        }
    }

}
