package com.iboxpay.settlement.gateway.common.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "T_EB_BankCnaps")
public class BankCnapsEntity {

    @Id
    @Column(name = "CNAPS")
    private String cnaps;
    //  CNAPS CHAR(12) PRIMARY KEY,

    @Column(name = "BANK_CODE")
    private String bankCode;
    //	BANK_CODE CHAR(3),--三位银行编码

    @Column(name = "AREA_CODE")
    private String areaCode;
    //  AREA_CODE CHAR(4),--地区编码(对应联行号中的4位地区编码)

    @Column(name = "BRANCH_NAME")
    private String branchName;
    //  BRANCH_NAME NVARCHAR2(50),--支行名称

    @Column(name = "UPDATE_TIME")
    private Date updateTime;

    //	UPDATE_TIME TIMESTAMP(0)--更新时间

    public String getCnaps() {
        return cnaps;
    }

    public void setCnaps(String cnaps) {
        this.cnaps = cnaps;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}
