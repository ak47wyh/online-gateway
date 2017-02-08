package com.iboxpay.settlement.gateway.alipay.servie.utils;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * 增强的XML解析器，通过传入符合约定规则的路径字符串，获取需要的值。目前支持如下的路径规则:<br>
 * <br>
 * 举例，给定XML文档内容如下：<br>
 * <pre>
 *     &lt;alipay>
 *         &lt;is_success>T&lt;/is_success>
 *         &lt;request>
 *             &lt;param name="body">珠宝饰品&lt;/param>
 *             &lt;param name="operator_id">8888&lt;/param>
 *             &lt;param name="subject">测试&lt;/param>
 *             &lt;param name="sign_type">MD5&lt;/param>
 *         &lt;/request>
 *     &lt;/alipay>
 * </pre>
 * 则如下路径获取的值分别为
 * <br>
 * <li>alipay.is_success  T
 * <li>alipay.request.param@name  body
 * <li>alipay.request.param@name#body  珠宝饰品
 * 
 * @author Jim Yang (oraclebone@gmail.com)
 *
 */
public class XmlDomParser {

	private final Logger logger = LoggerFactory.getLogger(XmlDomParser.class);
	private Document doc = null;
	
	public  XmlDomParser(String xmlStr) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		 try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
		    is.setCharacterStream(new StringReader(xmlStr));
		    this.doc = db.parse(is);
			doc.normalize();
		} catch (Exception e) {
			logger.error("parse xmlStr error!", e);
		}
	}
	
	/**
	 * 根据Path表达式获取值
	 * @param pathStr 如alipay.request.param@name#body
	 * @return 如果取不到值，则返回null
	 */
	public String getValue(String pathStr){
		String value = null;
		String[] path1 = pathStr.split("@");
		String[] path = path1[0].split("\\.");
		String attr = null;
		String attrValue = null;
		if(path1.length > 1 && StringUtils.isNotEmpty(path1[1])){
			String[] path2 = path1[1].split("#");
			attr = path2[0];
			if(path2.length > 1){
				attrValue = path2[1];
			}
		}
		boolean needCheckAttrValue = StringUtils.isNotEmpty(attrValue);
		Element e = (Element)doc.getElementsByTagName(path[0]).item(0);
		for(int i = 1; i < path.length - 1; i++){
			e = (Element)e.getElementsByTagName(path[i]).item(0);
		}
		if(e != null){
			NodeList nl = e.getElementsByTagName(path[path.length - 1]);
			if(StringUtils.isNotEmpty(attr)){
				
				for(int j = 0; j < nl.getLength(); j++){
					e = (Element)nl.item(j);
					if(e.hasAttribute(attr)){
						value = e.getAttribute(attr);
						if(needCheckAttrValue){
							if(attrValue.equals(value)){
								return getCharacterDataFromElement(e);
							}else{
								continue;
							}
						}{
							return value;
						}
					}
				}
			}else{
				e = (Element)nl.item(0);
				return getCharacterDataFromElement(e);
			}
		}
		return null;
	}
	

	
	private static String getCharacterDataFromElement(Element e) {
		if(e != null){
			Node child = e.getFirstChild();
			if (child instanceof CharacterData) {
				CharacterData cd = (CharacterData) child;
				return cd.getData();
			}
		}
	    return null;
	}
}
