package com.iboxpay.settlement.gateway.common.domain;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

//账号前置绑定
@Entity
@Table(name = "T_EB_AccountFrontEndBinding")
public class AccountFrontEndBindingEntity {

    @Id
    private Pk pk;

    @Column(name = "CREATE_TIME")
    private Date createTime;//	TIMESTAMP(6)	N

    @Column(name = "UPDATE_TIME")
    private Date updateTime;//	TIMESTAMP(6)	N	

    public Pk getPk() {
        return pk;
    }

    public void setPk(Pk pk) {
        this.pk = pk;
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

    @Embeddable
    public static class Pk implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        @OneToOne
        @JoinColumn(name = "ACC_NO")
        private AccountEntity account;//	VARCHAR2(30) 账号

        @OneToOne
        @JoinColumn(name = "FRONT_END_ID")
        private FrontEndEntity frontEnd;

        public Pk() {

        }

        public Pk(AccountEntity account, FrontEndEntity frontEnd) {
            this.account = account;
            this.frontEnd = frontEnd;
        }

        public AccountEntity getAccount() {
            return account;
        }

        public void setAccount(AccountEntity account) {
            this.account = account;
        }

        public FrontEndEntity getFrontEnd() {
            return frontEnd;
        }

        public void setFrontEnd(FrontEndEntity frontEnd) {
            this.frontEnd = frontEnd;
        }

        @Override
        public int hashCode() {
            return new StringBuilder().append(account.getAccNo()).append("_").append(frontEnd.getId()).toString().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            Pk pk = (Pk) obj;
            return this.account.getAccNo().equals(pk.account.getAccNo()) && this.frontEnd.equals(pk.frontEnd.getId());
        }
    }
}
