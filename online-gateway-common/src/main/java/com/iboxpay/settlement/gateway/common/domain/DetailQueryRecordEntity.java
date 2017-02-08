package com.iboxpay.settlement.gateway.common.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.iboxpay.settlement.gateway.common.util.ClassUtil;

@Entity
@Table(name = "T_EB_DetailQueryRecord")
public class DetailQueryRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "detail_qr_generator")
    @SequenceGenerator(name = "detail_qr_generator", sequenceName = "DETAIL_QR_ID_SEQ", allocationSize = 50, initialValue = 1)
    @Column(name = "ID")
    private long id;//long

    //	ACC_NO  VARCHAR2(30) not null,  --交易主账号
    @Column(name = "ACC_NO")
    private String accNo;

    //	DETAIL_DAY TIMESTAMP(0),--明细日期
    @Column(name = "DETAIL_DAY")
    private Date detailDay;//	TIMESTAMP(6)	N	

    @Column(name = "TYPE")
    private int type;

    public final static int TYPE_TODAY = 0;
    public final static int TYPE_HISTORY = 1;

    //	CREATE_TIME  TIMESTAMP(6),  --创建时间
    @Column(name = "CREATE_TIME")
    private Date createTime;//	TIMESTAMP(6)	N

    //		  	UPDATE_TIME  TIMESTAMP(6)  --更新时间 
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

    public Date getDetailDay() {
        return detailDay;
    }

    public void setDetailDay(Date detailDay) {
        this.detailDay = detailDay;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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

    @Override
    public String toString() {
        return ClassUtil.toString(this);
    }
}
