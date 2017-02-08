package com.iboxpay.settlement.gateway.common;

public interface Constant {

    public String CURRENCY_CNY = "CNY"; //人民币
    //转账
    String TRADE_TYPE_TRAN = "transfer";
    //转账查询
    String TRADE_TYPE_QUERY = "query";
    //查询账户余额
    String TRADE_TYPE_BALANCE = "balance";
    String PAY_FAIL = "交易失败";
    String PARA_NULL = "交易类型为空";

    // 解析无卡路由json失败，ibox
    String BOX_RESOLVE_JSON_FAIL = "box-29201";
    // 必传参数为空 ibox
    String BOX_PARAMS_NULL = "box-29202";
    // 发起交易异常
    String BOX_DOPAY_ERROR = "box-29203";
    // 订单已存在
    String BOX_ORDER_EXISTS = "box-29204";
    // 初始订单保存错误
    String BOX_ORDER_SAVE_ERROR = "box-29205";
    // 查询订单号出错
    String BOX_ORDER_QUERY_ERROR = "box-29206";
    // 返回内容为空
    String BOX_RESULT_NULL = "box-29207";
    // 返回后查询订单失败
    String BOX_FIND_ORDER_BY_PAY = "box-29208";
    // 返回后保存更新订单失败
    String BOX_SAVE_ORDER_BY_PAY_ERROR = "box-29209";
    // 未登录
    String BOX_UN_SIGN = "box-29210";
    // 返回数据解包错误
    String BOX_UN_XML = "box-29211";
    // 交易失败
    String BOX_TRADE_FAIL = "box-29212";
    //路由与网关连接通讯异常异常
    String BOX_CONNECTION_EXCEPTION = "box-31013";//"box-30012";
    //报文解析失败
    String BOX_MESSAGE_PARSE_EXCEPTION = "box-40036";

    String GATEWAY_PAYMENT_NOT_FOUND = "payment_not_Found";
    String GATEWAY_TRANS_COMPONENT_NOT_FOUND = "trans_component_not_found";
    //TransContext中是的Parameter里的参数名
    String PARAMETER_DEFAULT = "default";

    String REQ_SYSTEM_bankagent = "bankagent";
}
