package com.iboxpay.settlement.gateway.jd.service.model;

public class JdRefundReqParam extends JdGatewayParam {

    /**
     * 退款订单号
     */
    private String orderNo;
    //退款金额
    private String refundAmount;
    //退款编号
    private String refundNo;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(String refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getRefundNo() {
        return refundNo;
    }

    public void setRefundNo(String refundNo) {
        this.refundNo = refundNo;
    }

}
