package com.iboxpay.settlement.gateway.common.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "T_EB_Bank")
public class BankEntity implements Serializable, Comparable<BankEntity> {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CODE")
    private String code;//char(3)

    @Column(name = "NAME")
    private String name;//nvarchar2(50)

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(BankEntity o) {
        return this.code.compareTo(o.code);
    }

}
