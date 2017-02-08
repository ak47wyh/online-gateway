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

import com.iboxpay.settlement.gateway.common.trans.IBankTrans;

@Entity
@Table(name = "T_EB_AccountTransConfig")
public class AccountTransConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private Pk pk;

    @Column(name = "TRANS_COMPONENT_TYPE")
    private String transComponentType;
    
    @Column(name = "TRANS_ORDER")
    private int transOrder;//接口优先级(数值越大优先级越高)

    @Column(name = "COMPONENT_ENABLE")
    private Integer componentEnable;// --是否启用接口(是否支持接口). 新开发添加的接口都会是禁用的, 如果旧接口过时不存在则会自动 
    
    private final static int COMPONENT_DISABLE = 0;//禁用
    private final static int COMPONENT_ENABLE = 1;//启用
    
    private transient boolean componentExist;//接口是否存在了(接口升级后可能下线了)
    private transient boolean componentNew;//接口是否新增
    private transient IBankTrans bankTrans;//接口是否新增
    
    @Column(name = "CREATE_TIME")
    private Date createTime;//  TIMESTAMP(6)    N

    @Column(name = "UPDATE_TIME")
    private Date updateTime;//  TIMESTAMP(6)    N   

    public Pk getPk() {
        return pk;
    }

    public void setPk(Pk pk) {
        this.pk = pk;
    }
    
    public String getTransComponentType() {
        return transComponentType;
    }
    
    public void setTransComponentType(String transComponentType) {
        this.transComponentType = transComponentType;
    }
    
    public IBankTrans getBankTrans() {
        return bankTrans;
    }
    
    public void setBankTrans(IBankTrans bankTrans) {
        this.bankTrans = bankTrans;
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

    public int getTransOrder() {
        return transOrder;
    }

    public void setTransOrder(int transOrder) {
        this.transOrder = transOrder;
    }

    public int getComponentEnable() {
        return componentEnable;
    }

    public void setComponentEnable(Integer componentEnable) {
        this.componentEnable = componentEnable;
    }
    
    public boolean isComponentEnabled(){
        return this.componentEnable == COMPONENT_ENABLE;
    }
    
    public void setComponentEnabled(boolean componentEnabled){
        if(componentEnabled)
            this.componentEnable = COMPONENT_ENABLE;
        else
            this.componentEnable = COMPONENT_DISABLE;
            
    }
    
    public boolean isComponentExist() {
        return componentExist;
    }
    
    public void setComponentExist(boolean componentExist) {
        this.componentExist = componentExist;
    }
    
    public boolean isComponentNew() {
        return componentNew;
    }

    public void setComponentNew(boolean componentNew) {
        this.componentNew = componentNew;
    }

    @Embeddable
    public static class Pk implements Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        @OneToOne
        @JoinColumn(name = "ACC_NO")
        private AccountEntity account;//   账号

        @Column(name = "TRANS_COMPONENT")
        private String transComponent;//组件名(类全名)

        public Pk() {

        }

        public Pk(AccountEntity account, String transComponent) {
            this.account = account;
            this.transComponent = transComponent;
        }

        public AccountEntity getAccount() {
            return account;
        }

        public void setAccount(AccountEntity account) {
            this.account = account;
        }

        public String getTransComponent() {
            return transComponent;
        }

        public void setTransComponent(String transComponent) {
            this.transComponent = transComponent;
        }

        @Override
        public int hashCode() {
            return new StringBuilder().append(account.getAccNo()).append("_").append(transComponent).toString().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            Pk pk = (Pk) obj;
            return this.account.getAccNo().equals(pk.account.getAccNo()) && this.transComponent.equals(pk.transComponent);
        }
    }
}
