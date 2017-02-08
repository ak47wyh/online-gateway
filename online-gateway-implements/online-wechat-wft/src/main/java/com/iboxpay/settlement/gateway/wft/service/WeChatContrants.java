package com.iboxpay.settlement.gateway.wft.service;


public class WeChatContrants {
	/************************************************************************************
	 * 交易状态字典： 
	 * SUCCESS—支付成功 
	 * REFUND—转入退款 
	 * NOTPAY—未支付 
	 * CLOSED—已关闭 
	 * REVERSE—已冲正
	 * REVOK—已撤销
	 *************************************************************************************/
	
	/**
	 * 支付成功
	 */
	public static String TRADE_STATE_SUCCESS="SUCCESS";
	/**
	 * 转入退款
	 */
	public static String TRADE_STATE_REFUND="REFUND";
	/**
	 * 用户支付中
	 */
	public static String TRADE_STATE_USERPAYING="USERPAYING";
	/**
	 * 未支付
	 */
	public static String TRADE_STATE_NOTPAY="NOTPAY";
	/**
	 * 支付失败
	 */
	public static String TRADE_STATE_PAYERROR="PAYERROR";
	/**
	 * 已关闭
	 */
	public static String TRADE_STATE_CLOSED="CLOSED";
	/**
	 * 已冲正
	 */
	public static String TRADE_STATE_REVERSE="REVERSE";
	/**
	 * 已撤销
	 */
	public static String TRADE_STATE_REVOK="REVOK";
	
	/************************************************************************************
	 * 退款状态字典：
	 * 
	 * SUCCES—退款成功
	 * FAIL—退款失败
	 * PROCESSING—退款处理中
	 * NOTSURE—未确定， 需要商户
	 * CHANGE—转入代发 
	 ***********************************************************************************/
	public static String REFUND_STATE_SUCCESS="SUCCESS";
	public static String REFUND_STATE_FAIL="FAIL";
	public static String REFUND_STATE_PROCESSING="PROCESSING";
	public static String REFUND_STATE_NOTSURE="NOTSURE";
	public static String REFUND_STATE_CHANGE="CHANGE";
	
	
	/************************************************************************************
	 * 微信支付类型字典：
	 * 
	 * 0-扫码支付Native
	 * 1-刷卡支付Micropay
	 ***********************************************************************************/
	public static String PAY_TYPE_NATIVE="0";
	// 业务结果成功标志
    public static String PAY_TYPE_MICROPAY="1";
	

	// 返回码成功标志
	public static String STATUS_SUCCESS="0";
	// 业务结果成功标志
    public static String RESULT_CODE_SUCCESS="0";
    // 支付成功标准
    public static String PAY_RESULT_SUCCESS="0";
    

}

