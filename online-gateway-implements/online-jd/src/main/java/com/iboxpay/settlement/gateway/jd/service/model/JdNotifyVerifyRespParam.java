package com.iboxpay.settlement.gateway.jd.service.model;

public class JdNotifyVerifyRespParam extends CommonRespParam {

    /**
     * 订单号
     */
    private String orderNo;
    /**
     * 交易流水号
     */
    private String tradeNo;
    /**
     * 支付用户的名称
     */
    private String user;
    /**
     * 支付结果状态: 0代表支付成功 1代表支付失败 
     */
    private int status;
    /**
     * 结果描述
     */
    private String desc;
    /**
     * 营销金额
     */
    private Double promotionAmount;
    /**
     * 订单金额
     */
    private Double amount;
    /**
     * 订单支付时间
     */
    private String payTime;
    /**
     * 退款单号
     */
    private String refundNo;
    /**
     * 退款时间
     */
    private String refundTime;
    /**
     * 商户号
     */
    private String merchantNo;
    /**
     * 门店号
     */
    private String subMer;
    /**
     * 机具号
     */
    private String termNo;
    /**
     * 额外信息
     */
    private String extraInfo;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Double getPromotionAmount() {
        return promotionAmount;
    }

    public void setPromotionAmount(Double promotionAmount) {
        this.promotionAmount = promotionAmount;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public String getMerchantNo() {
        return merchantNo;
    }

    public void setMerchantNo(String merchantNo) {
        this.merchantNo = merchantNo;
    }

    public String getSubMer() {
        return subMer;
    }

    public void setSubMer(String subMer) {
        this.subMer = subMer;
    }

    public String getTermNo() {
        return termNo;
    }

    public void setTermNo(String termNo) {
        this.termNo = termNo;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public String getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(String refundTime) {
        this.refundTime = refundTime;
    }

    public String getRefundNo() {
        return refundNo;
    }

    public void setRefundNo(String refundNo) {
        this.refundNo = refundNo;
    }

}
