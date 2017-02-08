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
@Table(name = "T_EB_PaymentCheckRecord")
public class PaymentCheckRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "paycheck_id_generator")
    @SequenceGenerator(name = "paycheck_id_generator", sequenceName = "PAYCHECK_ID_SEQ", allocationSize = 50, initialValue = 1)
    @Column(name = "ID")
    private long id;//long

    //	ACC_NO  VARCHAR2(30) not null,  --交易主账号
    @Column(name = "ACC_NO")
    private String accNo;

    @Column(name = "CHECK_DAY")
    private Date checkDay;

    //	CREATE_TIME  TIMESTAMP(6),  --创建时间
    @Column(name = "CREATE_TIME")
    private Date createTime;//	TIMESTAMP(6)	N

    //  UPDATE_TIME  TIMESTAMP(6)  --更新时间 
    @Column(name = "UPDATE_TIME")
    private Date updateTime;//	TIMESTAMP(6)	N	

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAccNo() {
        return accNo;
    }

    public void setAccNo(String accNo) {
        this.accNo = accNo;
    }

    public Date getCheckDay() {
        return checkDay;
    }

    public void setCheckDay(Date checkDay) {
        this.checkDay = checkDay;
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
}
