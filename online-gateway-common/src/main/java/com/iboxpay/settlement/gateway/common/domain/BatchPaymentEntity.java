package com.iboxpay.settlement.gateway.common.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.iboxpay.settlement.gateway.common.IBankProfile;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;

//批量支付表
@Entity
@Table(name = "T_EB_BatchPayment")
public class BatchPaymentEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "BATCH_SEQ_ID")
    private String batchSeqId;//VARCHAR2(20) 批次流水号			

    @Column(name = "ACC_NO")
    private String accNo;//	VARCHAR2(30)	交易主账号

    @Column(name = "BANK_NAME")
    private String bankName;//	VARCHAR2(30)	交易主账号

    @Column(name = "type")
    private String type;//业务类别

    @Column(name = "BATCH_COUNT")
    private int batchCount;

    @Column(name = "BATCH_AMOUNT")
    private BigDecimal batchAmount;

    @Column(name = "REQUEST_SYSTEM")
    private String requestSystem;//调用者系统

    @Column(name = "TRANS_DATE")
    private Date transDate;//	TIMESTAMP(6)	N
    
    /**优先级（默认为0(最低)， 优先级分为32个(0 - 32)，最高优先级应设置为32(T+0交易)）*/
    @Column(name = "PRIORITY")
    private int priority;

    @Column(name = "CREATE_TIME")
    private Date createTime;//	TIMESTAMP(6)	N

    @Column(name = "UPDATE_TIME")
    private Date updateTime;//	TIMESTAMP(6)	N

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "BATCH_SEQ_ID")
    private List<PaymentEntity> paymentEntitys;

    public String getBatchSeqId() {
        return batchSeqId;
    }

    public void setBatchSeqId(String batchSeqId) {
        this.batchSeqId = batchSeqId;
    }

    public String getAccNo() {
        return accNo;
    }

    public void setAccNo(String accNo) {
        this.accNo = accNo;
    }

    public int getBatchCount() {
        return batchCount;
    }

    public void setBatchCount(int batchCount) {
        this.batchCount = batchCount;
    }

    public BigDecimal getBatchAmount() {
        return batchAmount;
    }

    public void setBatchAmount(BigDecimal batchAmount) {
        this.batchAmount = batchAmount;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public List<PaymentEntity> getPaymentEntitys() {
        return paymentEntitys;
    }

    public void setPaymentEntitys(List<PaymentEntity> paymentEntitys) {
        this.paymentEntitys = paymentEntitys;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRequestSystem() {
        return requestSystem;
    }

    public void setRequestSystem(String requestSystem) {
        this.requestSystem = requestSystem;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankFullName() {
        IBankProfile bankProfile = BankTransComponentManager.getBankProfile(this.bankName);
        if (bankProfile != null)
            return bankProfile.getBankFullName();
        else return this.bankName;
    }

    public Date getTransDate() {
        return transDate;
    }

    public void setTransDate(Date transDate) {
        this.transDate = transDate;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority == null ? 0 : priority.intValue();
    }
    
}
