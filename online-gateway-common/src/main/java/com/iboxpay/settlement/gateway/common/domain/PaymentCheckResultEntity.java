package com.iboxpay.settlement.gateway.common.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "T_EB_PaymentCheckResult")
public class PaymentCheckResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "paycheck_rs_id_generator")
    @SequenceGenerator(name = "paycheck_rs_id_generator", sequenceName = "PAYCHECK_RS_ID_SEQ", allocationSize = 50, initialValue = 1)
    @Column(name = "ID")
    private Long id;
    // 	PAYMENT_ID int UNIQUE,--引用Payment
    @Column(name = "PAYMENT_ID")
    private Long paymentId;
    //	CHECK_STATUS int,--对账状态 . 对应于PaymentEntity中的status,即PaymentStatus中的常量
    @Column(name = "CHECK_STATUS")
    private int checkStatus;
    //	CHECK_STATUS_MSG NVARCHAR2(255),--对账附加说明
    @Column(name = "CHECK_STATUS_MSG")
    private String checkStatusMsg;

    @Column(name = "DETAIL_ID")
    private Long detailId;
    //	CREATE_TIME TIMESTAMP(6), 	
    @Column(name = "CREATE_TIME")
    private Date createTime;
    //	UPDATE_TIME TIMESTAMP(6)
    @Column(name = "UPDATE_TIME")
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public int getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(int checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getCheckStatusMsg() {
        return checkStatusMsg;
    }

    public void setCheckStatusMsg(String checkStatusMsg) {
        this.checkStatusMsg = checkStatusMsg;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Long getDetailId() {
        return detailId;
    }

    public void setDetailId(Long detailId) {
        this.detailId = detailId;
    }
}
