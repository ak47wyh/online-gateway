package com.iboxpay.settlement.gateway.common.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Properties;

/**
 * The class PropertyReader.
 *
 * Description: 读取配置文件 .properties文件工具类
 *
 * @author: weiyuanhua
 * @since: 2016年3月12日 上午11:06:20 	
 * @version: $Revision$ $Date$ $LastChangedBy$
 *
 */
public class PropertyReader {
	// 创建对象ec
	static PropertyReader ec;
	// 静态对象初始化在其它对象之前
	private static Hashtable<String, Properties> register = new Hashtable<String, Properties>();

	private PropertyReader() {
		super();
	}

	/*
	 * 单例
	 * 
	 * @return
	 */
	public static PropertyReader getInstance() {
		if (ec == null)
			// 创建EnvironmentConfig对象
			ec = new PropertyReader();
		// 返回EnvironmentConfig对象
		return ec;
	}

	/**
	 * 根据传入的配置文件路径，获取Properties
	 * 
	 * @param fileName
	 * @return
	 */
	public Properties getProperties(String fileName) {
		InputStream is = null;// 定义输入流is
		Properties p = null;
		try {
			p = (Properties) register.get(fileName);// 将fileName存于一个HashTable

			if (p == null) {
				try {
					is = new FileInputStream(fileName);// 创建输入流
				} catch (Exception e) {
					if (fileName.startsWith("/"))
						// 用getResourceAsStream()方法用于定位并打开外部文件。
						is = PropertyReader.class.getResourceAsStream(fileName);
					else
						is = PropertyReader.class.getResourceAsStream("/"
								+ fileName);
				}
				p = new Properties();
				p.load(is);// 加载输入流
				register.put(fileName, p);// 将其存放于HashTable缓存
				is.close();// 关闭输入流
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		return p;// 返回Properties对象
	}

	/**
	 * 根据传入的配置文件路径 及 key，获取响应的Value
	 * 
	 * @param fileName
	 * @param strKey
	 * @return
	 */
	public String getPropertyValue(String fileName, String strKey) {
		Properties p = getProperties(fileName);
		try {
			return (String) p.getProperty(strKey);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
		return null;
	}
}
