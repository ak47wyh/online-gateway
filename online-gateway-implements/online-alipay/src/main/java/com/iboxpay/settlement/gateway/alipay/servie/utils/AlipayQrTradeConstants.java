package com.iboxpay.settlement.gateway.alipay.servie.utils;

/**
 * 
 * @author Jim Yang (oraclebone@gmail.com)
 *
 */
public class AlipayQrTradeConstants {

    //============================================================
    //
    //		支付宝状态码
    //
    //============================================================

    /**
     * HD交易类型
     */
    public static final String ALIPAY_HD_CLIENT_TYPE = "ios-hd";
    /**
     * 交易创建，等待买家付款。
     */
    public static final String ALIPAY_STATUS_WAIT_BUYER_PAY = "WAIT_BUYER_PAY";
    /**
     * 在指定时间段内未支付时关闭的交易；在交易完成全额退款成功时关闭的交易。
     */
    public static final String ALIPAY_STATUS_TRADE_CLOSED = "TRADE_CLOSED";
    /**
     * 交易成功，且可对该交易做操作，如：多级分润、退款等。
     */
    public static final String ALIPAY_STATUS_TRADE_SUCCESS = "TRADE_SUCCESS";
    /**
     * 等待卖家收款（买家付款后，如果卖家账号被冻结）。
     */
    public static final String ALIPAY_STATUS_TRADE_PENDING = "TRADE_PENDING";
    /**
     * 交易成功且结束，即不可再做任何操作。
     */
    public static final String ALIPAY_STATUS_TRADE_FINISHED = "TRADE_FINISHED";

    /**
     * 分账信息校验失败
     */
    public static final String ALIPAY_TRADE_SETTLE_ERROR = "TRADE_SETTLE_ERROR";
    /**
     * 交易买家不匹配
     */
    public static final String ALIPAY_TRADE_BUYER_NOT_MATCH = "TRADE_BUYER_NOT_MATCH";
    /**
     * 交易信息被篡改
     */
    public static final String ALIPAY_CONTEXT_INCONSISTENT = "CONTEXT_INCONSISTENT";
    /**
     * 交易已经支付
     */
    public static final String ALIPAY_TRADE_HAS_SUCCESS = "TRADE_HAS_SUCCESS";
    /**
     * 交易已经关闭
     */
    public static final String ALIPAY_TRADE_HAS_CLOSE = "TRADE_HAS_CLOSE";
    /**
     * 交易的状态不合法
     */
    public static final String ALIPAY_REASON_ILLEGAL_STATUS = "REASON_ILLEGAL_STATUS";
    /**
     * 订单信息中包含违禁词
     */
    public static final String ALIPAY_EXIST_FORBIDDEN_WORD = "EXIST_FORBIDDEN_WORD";
    /**
     * 没有权限使用该产品
     */
    public static final String ALIPAY_ACCESS_FORBIDDEN = "ACCESS_FORBIDDEN";
    /**
     * 卖家不存在
     */
    public static final String ALIPAY_SELLER_NOT_EXIST = "SELLER_NOT_EXIST";
    /**
     * 买家不存在
     */
    public static final String ALIPAY_BUYER_NOT_EXIST = "BUYER_NOT_EXIST";
    /**
     * 买家状态非法，无法继续交易
     */
    public static final String ALIPAY_BUYER_ENABLE_STATUS_FORBID = "BUYER_ENABLE_STATUS_FORBID";
    /**
     * 卖家买家账号相同，不能进行交易
     */
    public static final String ALIPAY_BUYER_SELLER_EQUAL = "BUYER_SELLER_EQUAL";
    /**
     * 参数无效
     */
    public static final String ALIPAY_INVALID_PARAMETER = "INVALID_PARAMETER";
    /**
     * 卖家不在设置的收款账户列表之中
     */
    public static final String ALIPAY_INVALID_RECEIVE_ACCOUNT = "INVALID_RECEIVE_ACCOUNT";
    /**
     * 交易不存在
     */
    public static final String ALIPAY_TRADE_NOT_EXIST = "TRADE_NOT_EXIST";
    /**
     * 同一笔退款单号退款金额不一致
     */
    public static final String ALIPAY_DISCORDANT_REPEAT_REQUEST = "DISCORDANT_REPEAT_REQUEST";
    /**
     * 交易已经被冻结
     */
    public static final String ALIPAY_REASON_TRADE_BEEN_FREEZEN = "REASON_TRADE_BEEN_FREEZEN";
    /**
     * 买家不存在
     */
    public static final String ALIPAY_BUYER_ERROR = "BUYER_ERROR";
    /**
     * 卖家不存在
     */
    public static final String ALIPAY_SELLER_ERROR = "SELLER_ERROR";
    /**
     * 交易状态不合法
     */
    public static final String ALIPAY_TRADE_STATUS_ERROR = "TRADE_STATUS_ERROR";
    /**
     * 交易已结束
     */
    public static final String ALIPAY_TRADE_HAS_FINISHED = "TRADE_HAS_FINISHED";
    /**
     * 撤销或退款金额与订单金额不一致
     */
    public static final String ALIPAY_REFUND_AMT_NOT_EQUAL_TOTAL = "REFUND_AMT_NOT_EQUAL_TOTAL";
    /**
     * 没有该笔交易的退款权限
     */
    public static final String ALIPAY_TRADE_ROLE_ERROR = "TRADE_ROLE_ERROR";

    /**
     * 签名不正确
     */
    public static final String ALIPAY_ILLEGAL_SIGN = "ILLEGAL_SIGN";
    /**
     * 动态密钥信息错误
     */
    public static final String ALIPAY_ILLEGAL_DYN_MD5_KEY = "ILLEGAL_DYN_MD5_KEY";
    /**
     * 加密不正确
     */
    public static final String ALIPAY_ILLEGAL_ENCRYPT = "ILLEGAL_ENCRYPT";
    /**
     * 参数不正确
     */
    public static final String ALIPAY_ILLEGAL_ARGUMENT = "ILLEGAL_ARGUMENT";
    /**
     * Service参数不正确
     */
    public static final String ALIPAY_ILLEGAL_SERVICE = "ILLEGAL_SERVICE";
    /**
     * 用户ID不正确
     */
    public static final String ALIPAY_ILLEGAL_USER = "ILLEGAL_USER";
    /**
     * 合作伙伴ID不正确
     */
    public static final String ALIPAY_ILLEGAL_PARTNER = "ILLEGAL_PARTNER";
    /**
     * 接口配置不正确
     */
    public static final String ALIPAY_ILLEGAL_EXTERFACE = "ILLEGAL_EXTERFACE";
    /**
     * 合作伙伴接口信息不正确
     */
    public static final String ALIPAY_ILLEGAL_PARTNER_EXTERFACE = "ILLEGAL_PARTNER_EXTERFACE";
    /**
     * 未找到匹配的密钥配置
     */
    public static final String ALIPAY_ILLEGAL_SECURITY_PROFILE = "ILLEGAL_SECURITY_PROFILE";
    /**
     * 代理ID不正确
     */
    public static final String ALIPAY_ILLEGAL_AGENT = "ILLEGAL_AGENT";
    /**
     * 签名类型不正确
     */
    public static final String ALIPAY_ILLEGAL_SIGN_TYPE = "ILLEGAL_SIGN_TYPE";
    /**
     * 字符集不合法
     */
    public static final String ALIPAY_ILLEGAL_CHARSET = "ILLEGAL_CHARSET";
    /**
     * 无权访问
     */
    public static final String ALIPAY_HAS_NO_PRIVILEGE = "HAS_NO_PRIVILEGE";
    /**
     * 字符集无效
     */
    public static final String ALIPAY_INVALID_CHARACTER_SET = "INVALID_CHARACTER_SET";

    /**
     * 支付宝系统错误
     */
    public static final String ALIPAY_SYSTEM_ERROR = "SYSTEM_ERROR";
    /**
     * session超时
     */
    public static final String ALIPAY_SESSION_TIMEOUT = "SESSION_TIMEOUT";
    /**
     * 错误的target_service
     */
    public static final String ALIPAY_ILLEGAL_TARGET_SERVICE = "ILLEGAL_TARGET_SERVICE";
    /**
     * partner不允许访问该类型的系统
     */
    public static final String ALIPAY_ILLEGAL_ACCESS_SWITCH_SYSTEM = "ILLEGAL_ACCESS_SWITCH_SYSTEM";
    /**
     * 接口已关闭
     */
    public static final String ALIPAY_EXTERFACE_IS_CLOSED = "EXTERFACE_IS_CLOSED";

    /**
     * 成功（业务）
     */
    public static final String ALIPAY_RESULT_CODE_SUCCESS = "SUCCESS";

    /**
     * 失败（业务）
     */
    public static final String ALIPAY_RESULT_CODE_FAIL = "FAIL";

    /**
     * 未知（业务）
     */
    public static final String ALIPAY_RESULT_CODE_UNKNOWN = "UNKNOWN";

    /**
     * 处理异常（业务）
     */
    public static final String ALIPAY_RESULT_CODE_PROCESS_EXCEPTION = "PROCESS_EXCEPTION";

    //============================================================
    //
    //		支付宝交易相关常量
    //
    //============================================================

    /**
     * 支付宝网关地址
     */
    public static final String ALIPAY_GATEWAY_URL = "alipay_gateway_url";

    /**
     * 支付宝预下单接口地址
     */
    public static final String ALIPAY_SERVICE_ACQUIRE_PRECREATE = "alipay_service_acquire_precreate";

    /**
     * 支付宝收单查询接口地址
     */
    public static final String ALIPAY_SERVICE_ACQUIRE_QUERY = "alipay_service_acquire_query";

    /**
     * 支付宝收单撤销接口地址
     */
    public static final String ALIPAY_SERVICE_ACQUIRE_CANCEL = "alipay_service_acquire_cancel";

    /**
     * 支付宝异步通知验证地址
     */
    public static final String ALIPAY_SERVICE_NOTIFY_VERIFY = "alipay_service_notify_verify";

    /**
     * 支付宝请求参数签名方式  MD5, RSA, DSA
     */
    public static final String ALIPAY_SERVICE_SIGN_TYPE = "alipay_service_sign_type";

    /**
     * 支付宝请求参数MD5签名私钥
     */
    public static final String ALIPAY_SERVICE_SIGN_MD5_KEY = "alipay_service_sign_md5_key";

    /**
     * 支付宝合作方ID
     */
    public static final String ALIPAY_PARTNER_ID = "alipay_partner_id";

    /**
     * 支付宝请求参数字符编码
     */
    public static final String ALIPAY_SERVICE_INPUT_CHARSET = "alipay_service_input_charset";

    /**
     * 支付宝异步通知回调地址
     */
    public static final String ALIPAY_SERVICE_NOTIFY_URL = "alipay_service_notify_url";

    /**
     * 支付宝请求签名类型 1 证书签名； 2 其他密钥签名，默认为2
     */
    public static final String ALIPAY_SERVICE_CA_REQUEST = "alipay_service_ca_request";

    /**
     * 支付宝卖家账号
     */
    public static final String ALIPAY_SELLER_EMAIL = "alipay_seller_email";

    /**
     * 支付宝钱盒代理商ID
     */
    public static final String ALIPAY_AGENT_ID = "alipay_agent_id";

    /**
     * 是否启用支付宝统一扣率（1为启用，其它为不起用）
     */
    public static final String ALIPAY_TRADE_UNIQUE_RATE_OPEN = "alipay_trade_unique_rate_open";

    /**
     * 支付宝统一扣率
     */
    public static final String ALIPAY_TRADE_UNIQUE_RATE = "alipay_trade_unique_rate";

    /**
     * 支付宝交易同一个买家每日交易次数限制
     */
    public static final String ALIPAY_SAME_BUYER_TRADE_COUNT_LIMIT = "alipay_same_buyer_trade_count_limit";

    //============================================================
    //
    //		其他常量
    //
    //============================================================
    /**
     * 待确认
     */
    public static final String ALIPAY_QR_WATER_STATUS_PROCESS = "0";

    /**
     * 已完成
     */
    public static final String ALIPAY_QR_WATER_STATUS_SUCCESS = "1";

    /**
     * 已冲正
     */
    public static final String ALIPAY_QR_WATER_STATUS_REVERSED = "2";

    /**
     * 已撤销
     */
    public static final String ALIPAY_QR_WATER_STATUS_CANCELED = "3";

    /**
     * 消费
     */
    public static final String ALIPAY_QR_WATER_TRADE_TYPE_CONSUME = "9";

    /**
     * 消费撤销
     */
    public static final String ALIPAY_QR_WATER_TRADE_TYPE_CONSUME_CANCEL = "A";

    /**
     * 支付宝交易同一个买家每日交易次数限制错误码
     */
    public static final String ALIPAY_SAME_BUYER_TRADE_COUNT_LIMIT_ERROR_CODE = "box-5AQ44";

    /**
     * 支付宝订单从生成二维码到最终支付成功的最大超时时间：2小时+10分钟
     */
    public static final long ALIPAY_ORDER_EXPIRE_INTERVAL = 7800000;

    /**
     * 查询支付宝收单状态的发起源：客户端
     */
    public static final int ALIPAY_QUERY_SOURCE_FROM_CLIENT = 1;

    /**
     * 查询支付宝收单状态的发起源：非客户端(撤销前自动发起查询，后台定时任务自动查询）
     */
    public static final int ALIPAY_QUERY_SOURCE_NOT_FROM_CLIENT = 2;

    /**
     * 支付宝交易开关
     */
    public static final String ALIPAY_TRADE_SWITCH = "alipay_trade_switch";

    /**
     * 支付宝消费类型
     */
    public static final String ALIPAY_WATER_TRAN_TYPE_CONSUME = "9";

    /**
     * 是否允许开放注册、推荐注册商户使用微信扫码支付，0：不允许进行交易，1：允许交易
     */
    public static final String ALIPAY_MCHT_TRADE_SWITCH = "alipay_mcht_trade_switch";

    public static final String TOTAL_FEE = "totalFee";

    public static final String OUT_TRADE_NO = "outTradeNo";

    public static final String APP_CODE = "appCode";

    public static final String MCHT_COUPON_ID = "mchtCouponId";

    public static final String IBOX_NO = "iboxNo";

    public static final String TRADE_NO = "tradeNo";

    public static final String TRANS_TYPE = "transType";

    public static final String MERCHANT_NO = "merchantNo";
}
