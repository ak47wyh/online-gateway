package com.iboxpay.settlement.gateway.xmcmbc;

import java.util.HashMap;
import java.util.Map;

import com.iboxpay.settlement.gateway.common.util.PropertyReader;

/**
 * The class Configuration.
 *
 * Description: 
 *
 * @author weiyuanhua
 * @date 2016年2月2日 上午9:58:38	
 * @version: $Revision$ $Date$ $LastChangedBy$
 *
 */
public class Configuration {
	
	public static Map<String, Object> configParams = new HashMap<String, Object>() {
		{
			int interval = Integer.parseInt(getMessage("HEART_INTERVAL"));
			put("HEART_INTERVAL", interval * 1000);//心跳间隔,默认30s
			put("HEART_BEAT_MESSAGE", "00000000");//心跳包内容
			int timeOut = Integer.parseInt(getMessage("READ_TIME_OUT"));
			put("READ_TIME_OUT", timeOut * 1000);//60s
			put("CAN_RUN", getMessage("CAN_RUN"));// 是否启动
			put("DIFF_CAN_RUN", getMessage("DIFF_CAN_RUN"));// 是否启动
			//ip和端口
			put("CONNECT_HOST_IP", getMessage("CONNECT_HOST_IP"));//ip172.30.61.3 172.30.4.92
			put("CONNECT_HOST_DIFF_IP", getMessage("CONNECT_HOST_DIFF_IP"));//ip172.30.61.3 172.30.4.92
			put("CONNECT_HOST_PORT", getMessage("CONNECT_HOST_PORT"));//port6604 9010
			put("CONNECT_HOST_DIFF_PORT", getMessage("CONNECT_HOST_DIFF_PORT"));//port 9006
			
			put("CHARSET", "UTF-8");//字符集
		}
	};
	
	public static String getMessage(String key) {
		PropertyReader reader = PropertyReader.getInstance();
		String value = null;
		try {
			value = reader.getPropertyValue(Constants.PROPERTIES_PATH_CONFIG,
					key);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}
	//从业务上判断，当读取返回报文超时的时候，设置该笔交易为未确定，不再重连原来的Socket
	public static final String READ_TIME_OUT = "Read timed out";
}
