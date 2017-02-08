package com.iboxpay.settlement.gateway.xmcmbc;

import java.math.BigDecimal;

/**
 * 常量类
 * @author weiyuanhua
 * @date 2016年2月2日 上午9:58:38
 * @Version 1.0
 */
public class Constants {
	
	/**
	 * 配置文件路径及名称
	 */
	public static final String PROPERTIES_PATH_CONFIG = "/xmcmbc.properties";

	//币种
	public static final String CURRENCY = "RMB";
	//交易时间格式
	public static final String TRANS_TIME_FORMAT = "yyyyMMddHHmmss";
	//交易日期格式
	public static final String TRANS_DATE_FORMAT = "yyyyMMdd";
	//小数点转化基数
	public static final BigDecimal MULTI_100 = new BigDecimal("100");
	/**实时代扣报文应答码类型,E-错误,S-成功,R-不确定*/
	public static final String RESPONSE_TYPE_SUCCESS = "S";
	public static final String RESPONSE_TYPE_ERROR = "E";
	public static final String RESPONSE_TYPE_UNKNOW = "R";
	
	/**实时代扣报文应答码:000000-成功 */
	public static final String RESPONSE_CODE_SUCCESS = "000000";
//	public static final String RESPONSE_TYPE_ERROR = "E";
//	public static final String RESPONSE_TYPE_UNKNOW = "R";
	
	/**实时代扣认证应答码：00-认证成功 99-认证失败 */
	public static final String RESPONSE_VALIDATE_SUCCESS = "00";
	public static final String RESPONSE_VALIDATE_ERROR = "99";

	/**实时代扣白名单采集应答码：000000-采集成功 000022-账号信息重复 */
	public static final String RESPONSE_EXECCODE_SUCCESS = "000000";
	public static final String RESPONSE_EXECCODE_REPEAT = "000022";
	
	/**
	 * 证件类型字典
	 * ZR01	身份证
	 * ZR02	临时身份证
	 * ZR03	户口簿
	 * ZR04	军官证
	 * ZR05	警官证
	 * ZR06	士兵证
	 * ZR07	文职干部证
	 * ZR08	外国护照
	 * ZR09	香港通行证
	 * ZR10	澳门通行证
	 * ZR11	台湾通行证或有效旅行证件
	 * ZR12	军官退休证
	 * ZR13	中国护照
	 * ZR14	外国人永久居留证
	 * ZR15	军事学员证
	 * ZR16	离休干部荣誉证
	 * ZR17	边民出入境通行证
	 * ZR18	村民委员会证明
	 * ZR19	学生证
	 * ZR20	其它
	 * ZR21	护照
	 * ZR22	香港居民来往内地通行证
	 * ZR23	澳门居民来往内地通行证
	 * ZR24	台湾同胞来往内地通行证
	 */
	public static String CERT_TYPE_ZR01 = "ZR01";//身份证
	public static String CERT_TYPE_ZR02 = "ZR02";//临时身份证
	public static String CERT_TYPE_ZR03 = "ZR03";//户口簿
	public static String CERT_TYPE_ZR04 = "ZR04";//军官证
	public static String CERT_TYPE_ZR05 = "ZR05";//警官证
	public static String CERT_TYPE_ZR06 = "ZR06";//士兵证
	public static String CERT_TYPE_ZR07 = "ZR07";//文职干部证
	public static String CERT_TYPE_ZR08 = "ZR08";//外国护照
	public static String CERT_TYPE_ZR09 = "ZR09";//香港通行证
	public static String CERT_TYPE_ZR10 = "ZR10";//澳门通行证
	public static String CERT_TYPE_ZR11 = "ZR11";//台湾通行证或有效旅行证件
	public static String CERT_TYPE_ZR12 = "ZR12";//军官退休证
	public static String CERT_TYPE_ZR13 = "ZR13";//中国护照
	public static String CERT_TYPE_ZR14 = "ZR14";//外国人永久居留证
	public static String CERT_TYPE_ZR15 = "ZR15";//军事学员证
	public static String CERT_TYPE_ZR16 = "ZR16";//离休干部荣誉证
	public static String CERT_TYPE_ZR17 = "ZR17";//边民出入境通行证
	public static String CERT_TYPE_ZR18 = "ZR18";//村民委员会证明
	public static String CERT_TYPE_ZR19 = "ZR19";//学生证
	public static String CERT_TYPE_ZR20 = "ZR20";//其它
	public static String CERT_TYPE_ZR21 = "ZR21";//护照
	public static String CERT_TYPE_ZR22 = "ZR22";//香港居民来往内地通行证
	public static String CERT_TYPE_ZR23 = "ZR23";//澳门居民来往内地通行证
	public static String CERT_TYPE_ZR24 = "ZR24";//台湾同胞来往内地通行证
}
