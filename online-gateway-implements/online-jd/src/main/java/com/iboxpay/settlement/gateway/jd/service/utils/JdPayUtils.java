package com.iboxpay.settlement.gateway.jd.service.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

public class JdPayUtils {
	 /**
     * 初始http post请求参数（http 4.2）
     * 
     * @param param
     * @throws UnsupportedEncodingException
     */
    public static HttpEntity buildMapParam(Map<String, String> param) throws UnsupportedEncodingException {
    	HttpEntity reqEntity =null;
    	// 设置参数
        List<NameValuePair> list = new ArrayList<NameValuePair>();
        Set<String> sets = param.keySet();
    	Iterator<String> it = sets.iterator();
        
        while (it.hasNext()) {
			String key = it.next();
			String value = param.get(key);
			if (key != null) {
				setParam(list, key, value);
			}
			
		}
        if (!list.isEmpty()) {
            reqEntity = new UrlEncodedFormEntity(list, "utf-8");
        }
        return reqEntity;
    }

    private static void setParam(List<NameValuePair> list, String key, Object value) {
        if (value == null) {
            list.add(new BasicNameValuePair(key, null));
        } else {
            if (value instanceof String) {
                list.add(new BasicNameValuePair(key, (String) value));
            } else {
                list.add(new BasicNameValuePair(key, String.valueOf(value)));
            }
        }
    }
}
