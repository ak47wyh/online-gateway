package com.iboxpay.settlement.gateway.jd.service.model;

public class JdCancleReqParam {

    private String orderNo;
    private String amount;
    private String note;
    private String cancleNo;

    public String getCancleNo() {
        return cancleNo;
    }

    public void setCancleNo(String cancleNo) {
        this.cancleNo = cancleNo;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

}
