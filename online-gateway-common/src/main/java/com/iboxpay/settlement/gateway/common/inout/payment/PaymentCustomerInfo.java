package com.iboxpay.settlement.gateway.common.inout.payment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

import com.iboxpay.settlement.gateway.common.util.StringUtils;

/**
 * 详细支付信息
 * @author jianbo_chen
 */
public class PaymentCustomerInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    //中间件使用的单笔流水号，同一批次内不能重复.【必须填写】
    private String seqId;
    //交易金额.【必须填写】
    private BigDecimal amount;
    //客户账号.【必须填写】
    private String accNo;
    //客户账户名.【必须填写】
    private String accName;
    //账户类型 1-表示对公 2-表示对私 3-表示对私存折.【必须填写】
    private int accType = 2;//默认为对私
    //卡类型 0-借记卡（默认） 1-存折    2-贷记卡（信用卡）3-公司账号.【必须填写】
    private int cardType = 0;//默认为对私
    //客户开户银行,如：ccb.(与bankFullName必须有一个不为空)
    private String bankName;
    //银行全名,如：中国建设银行.
    private String bankFullName;
    //支行名称.可空
    private String bankBranchName;
    //开户地区(对应联行号中的四位地区码)
    private String areaCode;
    //联行号.跨行必须
    private String cnaps;
    //网银支付行号(走第二代支付系统 网银互联 时需要)
    private String cnapsBankNo;
    // 汇路 '0'表示 同行本地; '1',表示表示 同行异地; '2','表示 小额; '3'表示
    // 大额; '4',表示 上海同城; '5'表示 网银互联;
    @Deprecated
    private int localFlag;//兼容旧的，这个应该不用的
    //用途	比如工行的是枚举值
    private String useCode;
    //用途说明
    private String useDesc;
    //附加信息	备忘
    private String remark;
    //其他扩展属性，如身份证等
    private Map<String,Object> extProperties;

    public String getSeqId() {
        return seqId;
    }

    public void setSeqId(String seqId) {
        this.seqId = seqId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public int getAccType() {
        return accType;
    }

    public void setAccType(int accType) {
        this.accType = accType;
    }
    
    public int getCardType() {
        return cardType;
    }

    public void setCardType(int cardType) {
        this.cardType = cardType;
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

    public String getAreaCode() {
        if (StringUtils.isBlank(areaCode) && !StringUtils.isBlank(this.cnaps))
            return StringUtils.getAreaCodeFromCnaps(this.cnaps);
        else return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getCnaps() {
        return cnaps;
    }

    public void setCnaps(String cnaps) {
        this.cnaps = cnaps;
    }

    public String getCnapsBankNo() {
        return cnapsBankNo;
    }

    public void setCnapsBankNo(String cnapsBankNo) {
        this.cnapsBankNo = cnapsBankNo;
    }

    public String getUseCode() {
        return useCode;
    }

    public void setUseCode(String useCode) {
        this.useCode = useCode;
    }

    public String getUseDesc() {
        return useDesc;
    }

    public void setUseDesc(String useDesc) {
        this.useDesc = useDesc;
    }

    public void setLocalFlag(int localFlag) {
        this.localFlag = localFlag;
    }

    public int getLocalFlag() {
        return localFlag;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Map<String,Object> getExtProperties() {
        return extProperties;
    }

    public void setExtProperties(Map<String,Object> extProperties) {
        this.extProperties = extProperties;
    }
}
