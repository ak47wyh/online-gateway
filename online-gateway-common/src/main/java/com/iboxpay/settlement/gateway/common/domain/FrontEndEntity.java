package com.iboxpay.settlement.gateway.common.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "T_EB_FrontEnd")
public class FrontEndEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "FrontEnd_generator")
    @SequenceGenerator(name = "FrontEnd_generator", sequenceName = "FRONTEND_ID_SEQ", allocationSize = 20, initialValue = 1)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "NAME")
    private String name;//	NAME VARCHAR2(30) not null,--前置机配置名称

    @Column(name = "BANK_NAME")
    private String bankName;//	BANK_NAME	VARCHAR2(10) not null,--银行简码

    @Column(name = "CREATE_TIME")
    private Date createTime;//	CREATE_TIME TIMESTAMP(6) not null,

    @Column(name = "UPDATE_TIME")
    private Date updateTime;//	TIMESTAMP(6)	N	

    @Column(name = "STATUS")
    private String status;

    public final static String STATUS_ENABLE = "enable";
    public final static String STATUS_DISABLE = "disable";

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "PARENT_ID")
    private List<FrontEndPropertyEntity> propertys;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
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

    public List<FrontEndPropertyEntity> getPropertys() {
        return propertys;
    }

    public void setPropertys(List<FrontEndPropertyEntity> propertys) {
        this.propertys = propertys;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
