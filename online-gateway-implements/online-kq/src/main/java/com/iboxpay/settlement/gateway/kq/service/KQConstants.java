package com.iboxpay.settlement.gateway.kq.service;

/**
 * 快钱代扣常量定义
 * @author liaoxiongjian
 * @date 2015-12-22 14:45
 */
public class KQConstants {
	
	/**
	 * 银行处理结果集合 00000 批次已接受 00001 批次校验失败 00002 批次完成 00003 批次处理中
	 */
	public static String BATCH_DEAL_RESULT_00000="00000";//批次已接收
	public static String BATCH_DEAL_RESULT_00001="00001";//批次校验失败
	public static String BATCH_DEAL_RESULT_00002="00002";//批次完成
	public static String BATCH_DEAL_RESULT_00003="00003";//批次处理中
	
	
	/**
	 * 明细结果状态字典
	 */
	public static String PAYMENT_SATUS_01001="01001";//扣款成功
	public static String PAYMENT_SATUS_01002="01002";//扣款失败
	public static String PAYMENT_SATUS_01004="01004";//交易失败
	
	/**
	 * 帐号类型
	 */
	public static String ACCOUNT_TYPE_0100="0100";//对公账户
	public static String ACCOUNT_TYPE_0101="0101";//对公存款账户
	public static String ACCOUNT_TYPE_0200="0200";//个人帐号
	public static String ACCOUNT_TYPE_0201="0201";//个人借记卡帐户
	public static String ACCOUNT_TYPE_0204="0101";//个人存折账户
	
	/**
	 * 字符编码
	 */
	public static String CHARSET_UTF8="UTF-8";
	public static String CHARSET_GBK="GBK";
	public static String CHARSET_GB2312="GB2312";
	/**
	 * 字符编码（快钱）
	 */
	public static String KQ_CHARSET_UTF8="1";
	public static String KQ_CHARSET_GBK="2";
	public static String KQ_CHARSET_GB2312="3";
	
	/**
	 * 证件类型字典
	 */
	public static String ID_TYPE_101="101";//居民身份证
	public static String ID_TYPE_102="102";//临时身份证
	public static String ID_TYPE_103="103";//居民户口簿
	public static String ID_TYPE_104="104";//军官证
	public static String ID_TYPE_105="105";//警官证
	public static String ID_TYPE_106="106";//士兵证
	public static String ID_TYPE_107="107";//港澳通行证
	public static String ID_TYPE_108="108";//台湾通行证
	public static String ID_TYPE_201="201";//护照
	public static String ID_TYPE_301="301";//机动车驾驶证
	public static String ID_TYPE_900="900";//其他
	
}


