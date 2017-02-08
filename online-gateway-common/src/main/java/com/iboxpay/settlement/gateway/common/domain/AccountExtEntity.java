package com.iboxpay.settlement.gateway.common.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 账户扩展属性表
 * @author jianbo_chen
 */
@Entity
@Table(name = "T_EB_AccountExt")
public class AccountExtEntity {

    @Id
    private Pk pk;

    @Column(name = "VALUE")
    private String value;//nvarchar2(100)

    public Pk getPk() {
        return pk;
    }

    public void setPk(Pk pk) {
        this.pk = pk;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Embeddable
    public static class Pk implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        @Column(name = "ACC_NO")
        private String accNo;//	VARCHAR2(30) 账号

        @Column(name = "NAME")
        private String name;//varchar2(20) 

        public Pk() {

        }

        public Pk(String accNo, String name) {
            this.accNo = accNo;
            this.name = name;
        }

        public String getAccNo() {
            return accNo;
        }

        public void setAccNo(String accNo) {
            this.accNo = accNo;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int hashCode() {
            return new StringBuilder().append(accNo).append("_").append(name).toString().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            Pk pk = (Pk) obj;
            return this.accNo.equals(pk.accNo) && this.name.equals(pk.name);
        }
    }
}
