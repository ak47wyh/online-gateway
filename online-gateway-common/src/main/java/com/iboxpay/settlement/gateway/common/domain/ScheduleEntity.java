package com.iboxpay.settlement.gateway.common.domain;

import java.io.Serializable;
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
@Table(name = "T_EB_Schedule")
public class ScheduleEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "schedule_generator")
    @SequenceGenerator(name = "schedule_generator", sequenceName = "SCHEDUAL_ID_SEQ", allocationSize = 20, initialValue = 1)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "JOB_TYPE")
    private String jobType;

    @Column(name = "CRON")
    private String cron;

    @Column(name = "PARAMS")
    private String params;

    @Column(name = "STATUS")
    private String status;

    public final static String STATUS_ENABLE = "enable";
    public final static String STATUS_DISABLE = "disable";

    @Column(name = "CREATE_TIME")
    private Date createTime;

    @Column(name = "UPDATE_TIME")
    private Date updateTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
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

    public boolean isEnable() {
        return STATUS_ENABLE.equals(status);
    }

    @Override
    public String toString() {
        return ClassUtil.toString(this);
    }
}
