package com.iboxpay.settlement.gateway.alipay.servie.model;

import java.io.Serializable;

public class MerchantDto implements Serializable {

    /**
     * Comment for <code>serialVersionUID</code>
     */

    private static final long serialVersionUID = -555677948539075981L;
    /**
     * 支行名称
     */
    private String oaaBankName;
    /**
     * 二维码地址
     */
    private String qrcodeurl;

    private String userName;// 主用户名
    private String cardNo;// 证件号
    private String account;// 账户开户名
    private String expandName;// 拓展员姓名
    private String expand; //拓展员编号

    private String iboxSn; // 盒子号

    private String brhNo;
    private String mccGroup; // mcc组
    private String mchtSource; // 商户来源
    private String mchtKind; // 商户种类
    private String mchtRank; // 商户等级
    private String auditStatus; // 商户审核状态

    private String partner;// 商户号
    private String mchtName;
    private String bankAccout;
    private String bankAcName; //银行卡账户名称
    private String settleDate;
    private int flag; //费率模式
    private Double rate; //基准配置费率
    private String level;
    private String merchantContact;
    private String merchantMobile;
    private String isMainMerchant;
    private String recommendCode; //推荐码

    // T+0开通状态（0:已经开通；1：失败）
    private String t0OpenStatus;

    // 是否允许开通T+0业务状态(1:允许，其它:不允许)
    private String t0ApplyStatus;

    // 商户T+0等级
    private String t0Rank;

    // T+0费率
    private String rateT0;

    // 是否是联合营销商户 1-是 2-否
    private String isComarketing;

    /**
     * 商户费率模型
     */
    private String mchtDiscId;

    // 商户注册来源 1-开放注册 2-开通宝 3-平台录入 4-钱盒注册
    // 如果source=4且mcht_kind=A3则改字段有值=4
    private String merchantType;

    // 商户S+0等级
    private String s0Rank;

    // S+0开通状态0 未开通 1 开通  2 关闭
    private String s0OpenStatus;

    //S+0费率
    private String s0Rate;

    //    // S0开通权限状态（1:符合条件，其它不符合）
    //    private String s0ApplyStatus;

    public String getOaaBankName() {
        return oaaBankName;
    }

    public void setOaaBankName(String oaaBankName) {
        this.oaaBankName = oaaBankName;
    }

    public String getQrcodeurl() {
        return qrcodeurl;
    }

    public void setQrcodeurl(String qrcodeurl) {
        this.qrcodeurl = qrcodeurl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getExpandName() {
        return expandName;
    }

    public void setExpandName(String expandName) {
        this.expandName = expandName;
    }

    public String getIboxSn() {
        return iboxSn;
    }

    public void setIboxSn(String iboxSn) {
        this.iboxSn = iboxSn;
    }

    public String getBrhNo() {
        return brhNo;
    }

    public void setBrhNo(String brhNo) {
        this.brhNo = brhNo;
    }

    public String getMccGroup() {
        return mccGroup;
    }

    public void setMccGroup(String mccGroup) {
        this.mccGroup = mccGroup;
    }

    public String getMchtSource() {
        return mchtSource;
    }

    public void setMchtSource(String mchtSource) {
        this.mchtSource = mchtSource;
    }

    public String getMchtKind() {
        return mchtKind;
    }

    public void setMchtKind(String mchtKind) {
        this.mchtKind = mchtKind;
    }

    public String getMchtRank() {
        return mchtRank;
    }

    public void setMchtRank(String mchtRank) {
        this.mchtRank = mchtRank;
    }

    public String getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(String auditStatus) {
        this.auditStatus = auditStatus;
    }

    public String getPartner() {
        return partner;
    }

    public void setPartner(String partner) {
        this.partner = partner;
    }

    public String getMchtName() {
        return mchtName;
    }

    public void setMchtName(String mchtName) {
        this.mchtName = mchtName;
    }

    public String getBankAccout() {
        return bankAccout;
    }

    public void setBankAccout(String bankAccout) {
        this.bankAccout = bankAccout;
    }

    public String getBankAcName() {
        return bankAcName;
    }

    public void setBankAcName(String bankAcName) {
        this.bankAcName = bankAcName;
    }

    public String getSettleDate() {
        return settleDate;
    }

    public void setSettleDate(String settleDate) {
        this.settleDate = settleDate;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMerchantContact() {
        return merchantContact;
    }

    public void setMerchantContact(String merchantContact) {
        this.merchantContact = merchantContact;
    }

    public String getMerchantMobile() {
        return merchantMobile;
    }

    public void setMerchantMobile(String merchantMobile) {
        this.merchantMobile = merchantMobile;
    }

    public String getIsMainMerchant() {
        return isMainMerchant;
    }

    public void setIsMainMerchant(String isMainMerchant) {
        this.isMainMerchant = isMainMerchant;
    }
    
    public String getRecommendCode() {
        return recommendCode;
    }
    
    public void setRecommendCode(String recommendCode) {
        this.recommendCode = recommendCode;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getExpand() {
        return expand;
    }

    public void setExpand(String expand) {
        this.expand = expand;
    }

    public String getT0OpenStatus() {
        return t0OpenStatus;
    }

    public void setT0OpenStatus(String t0OpenStatus) {
        this.t0OpenStatus = t0OpenStatus;
    }

    public String getT0ApplyStatus() {
        return t0ApplyStatus;
    }

    public void setT0ApplyStatus(String t0ApplyStatus) {
        this.t0ApplyStatus = t0ApplyStatus;
    }

    public String getT0Rank() {
        return t0Rank;
    }

    public void setT0Rank(String t0Rank) {
        this.t0Rank = t0Rank;
    }

    public String getRateT0() {
        return rateT0;
    }

    public void setRateT0(String rateT0) {
        this.rateT0 = rateT0;
    }

    public String getIsComarketing() {
        return isComarketing;
    }

    public void setIsComarketing(String isComarketing) {
        this.isComarketing = isComarketing;
    }

    public String getMchtDiscId() {
        return mchtDiscId;
    }

    public void setMchtDiscId(String mchtDiscId) {
        this.mchtDiscId = mchtDiscId;
    }

    public String getMerchantType() {
        return merchantType;
    }

    public void setMerchantType(String merchantType) {
        this.merchantType = merchantType;
    }

    public String getS0Rank() {
        return s0Rank;
    }

    public void setS0Rank(String s0Rank) {
        this.s0Rank = s0Rank;
    }

    public String getS0OpenStatus() {
        return s0OpenStatus;
    }

    public void setS0OpenStatus(String s0OpenStatus) {
        this.s0OpenStatus = s0OpenStatus;
    }

    public String getS0Rate() {
        return s0Rate;
    }

    public void setS0Rate(String s0Rate) {
        this.s0Rate = s0Rate;
    }

    @Override
    public String toString() {
        return "MerchantDTO [oaaBankName=" + oaaBankName + ", qrcodeurl=" + qrcodeurl + ", userName=" + userName + ", cardNo=" + cardNo + ", account=" + account + ", expandName=" + expandName
                + ", expand=" + expand + ", iboxSn=" + iboxSn + ", brhNo=" + brhNo + ", mccGroup=" + mccGroup + ", mchtSource=" + mchtSource + ", mchtKind=" + mchtKind + ", mchtRank=" + mchtRank
                + ", auditStatus=" + auditStatus + ", partner=" + partner + ", mchtName=" + mchtName + ", bankAccout=" + bankAccout + ", settleDate=" + settleDate + ", rate=" + rate + ", level="
                + level + ", merchantContact=" + merchantContact + ", merchantMobile=" + merchantMobile + ", isMainMerchant=" + isMainMerchant + ", t0OpenStatus=" + t0OpenStatus + ", t0ApplyStatus="
                + t0ApplyStatus + ", t0Rank=" + t0Rank + ", rateT0=" + rateT0 + ", isComarketing=" + isComarketing + ", mchtDiscId=" + mchtDiscId + ", merchantType=" + merchantType + ", s0Rank="
                + s0Rank + ", s0OpenStatus=" + s0OpenStatus + ", s0Rate=" + s0Rate + "]";
    }

}
