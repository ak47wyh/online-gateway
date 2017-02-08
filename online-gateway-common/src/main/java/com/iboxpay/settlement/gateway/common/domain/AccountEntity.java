package com.iboxpay.settlement.gateway.common.domain;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.settlement.gateway.common.cache.local.AreaCodeCache;
import com.iboxpay.settlement.gateway.common.config.Property;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

@Entity
@Table(name = "T_EB_Account")
public class AccountEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private static Logger logger = LoggerFactory.getLogger(AccountEntity.class);

    @Id
    @Column(name = "ACC_NO")
    private String accNo;//	VARCHAR2(30)	交易主账号

    @Column(name = "ACC_NAME")
    private String accName;//	NVARCHAR2(30)	交易主账户名

    @Column(name = "BANK_NAME")
    private String bankName;//	VARCHAR2(10)	银行简码

    @Column(name = "BANK_FULL_NAME")
    private String bankFullName;// NVARCHAR2(30) 客户银行全称

    @Column(name = "BANK_BRANCH_NAME")
    private String bankBranchName;//NVARCHAR2(50) 客户银行开户行全称. 如招商银行深圳高新园支行

    @Column(name = "AREACODE")
    private String areaCode; //	CHAR(4)	开户地区号

    @Column(name = "CNAPS")
    private String cnaps; //	CHAR(15)	客户账号CNAP号

    @Column(name = "CURRENCY")
    private String currency;//	char(5) 账户币别

    @Column(name = "BANK_DEFAULT")
    private int bankDefault;//是否该银行默认账号
    
    @Column(name = "TRANS_CONFIG_ENABLE")
    private Integer transConfigEnable;//是否启用自定义接口配置
    
    private final static Integer TRANS_CONFIG_DISABLE = 0;//禁用
    private final static Integer TRANS_CONFIG_ENABLE = 1;//启用


    

    @Column(name = "CREATE_TIME")
    private Date createTime;//	TIMESTAMP(6)	N

    @Column(name = "UPDATE_TIME")
    private Date updateTime;//	TIMESTAMP(6)	N	

    private transient List<Property> extProperties;

    public AccountEntity() {
    }

    public String getAccNo() {
        return accNo;
    }

    public void setAccNo(String accNo) {
        this.accNo = accNo;
    }

    public String getAccName() {
        return accName;
    }

    public void setAccName(String accName) {
        this.accName = accName;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankFullName() {
        return bankFullName;
    }

    public void setBankFullName(String bankFullName) {
        this.bankFullName = bankFullName;
    }

    public String getBankBranchName() {
        return bankBranchName;
    }

    public void setBankBranchName(String bankBranchName) {
        this.bankBranchName = bankBranchName;
    }

    public String getCnaps() {
        return cnaps;
    }

    public void setCnaps(String cnaps) {
        this.cnaps = cnaps;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public int getTransConfigEnable() {
        return transConfigEnable;
    }
    
    public void setTransConfigEnable(Integer transConfigEnable) {
        this.transConfigEnable = transConfigEnable;
    }

    public boolean isTransConfigEnabled(){
        return TRANS_CONFIG_ENABLE.equals(this.transConfigEnable);
    }
    
    public void setTransConfigEnabled(boolean transConfigEnabled){
        if(transConfigEnabled)
            this.transConfigEnable = TRANS_CONFIG_ENABLE;
        else
            this.transConfigEnable = TRANS_CONFIG_DISABLE;
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

    public String getRealAreaCode() {
        return areaCode;
    }

    public String getAreaCode() {
        if (StringUtils.isBlank(areaCode) && !StringUtils.isBlank(this.cnaps)) return StringUtils.getAreaCodeFromCnaps(cnaps);

        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public CityEntity getCityInfo() {
        AreaCodeEntity areaCodeEntity = getAreaCodeInfo();
        if (areaCodeEntity != null) {
            return areaCodeEntity.getCity();
        }
        return null;
    }

    public AreaCodeEntity getAreaCodeInfo() {
        return AreaCodeCache.getAreaCode(getAreaCode());
    }

    public int getBankDefault() {
        return bankDefault;
    }

    /**是否银行默认账号*/
    public boolean isBankDefault() {
        return this.bankDefault == 1;
    }

    public void setBankDefault(int bankDefault) {
        this.bankDefault = bankDefault;
    }

    public void setIsBankDefault(boolean bankDefault) {
        this.bankDefault = bankDefault ? 1 : 0;
    }

    /**
     * 获取扩展账户对象的扩展属性.
     * @return
     */
    public List<Property> getExtPropertys() {
        if (extProperties == null && this.getClass() != AccountEntity.class) {
            try {
                extProperties = Property.findExtPropertys(this);
            } catch (Exception e) {
                logger.error("find ext-propertys error.", e);
                return null;
            }
        }
        return extProperties;
    }

    @Override
    public String toString() {
        StringBuilder sb =
                new StringBuilder().append("{accNo=").append(this.accNo).append(",").append("accName=").append(this.accName).append(",").append("bankName=").append(this.bankName).append(",")
                        .append("bankFullName=").append(this.bankFullName).append(",").append("bankBranchName=").append(this.bankBranchName).append(",").append("areaCode=").append(this.areaCode)
                        .append(",").append("cnaps=").append(this.cnaps).append(",").append("bankDefault=").append(isBankDefault()).append(",").append("currency=").append(this.currency);
        List<Property> extPropertys = getExtPropertys();
        if (extPropertys != null) {
            sb.append("; ext-propertys: {");
            for (Property extProperty : extPropertys) {
                sb.append(extProperty.getName()).append("=").append(Arrays.toString(extProperty.getExactVals()));
            }
            sb.append("}");
        }
        sb.append("}");
        return sb.toString();
    }
}
