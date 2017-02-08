package com.iboxpay.settlement.gateway.common.inout;

public class AccntsettleModel {

    // Fields
    private String id;
    private String clearMerchid;// 清算商户号
    private String clearMerchant;// 商户名称
    private String accntNo;// 收款账号（银行卡号）
    private String accntName;// 账号户名
    private String unionNo;// 联行行号
    private String unionName;// 联行行名
    private String clrflag;
    private String trnamount;// 交易金额累计 单位为分
    private String trncnt;// 交易笔数
    private String supcharge;// 收单行手续费 单位为分，即渠道端手续费
    private String crncharge;// 代理商结算手续费 单位为分
    private String charge;// 手续费 单位为分，即商户签约手续费
    private String accntamount;// 实付金额 单位为分
    private String settleDate;// 结算日期(清算日期) 格式：YYYYMMDD
    private String stlexday;// 结算T+X
    private String settleDateFn;
    private String settleType;
    private String xferFlag;// 支付标识 0表示 未支付; 1表示 支付成功; 2表示 支付失败; 3表示 支付未确定
    private String asstFlag;
    private String xferDate;// 转账日期(即操作日期)
    private String userid;// 操作员ID
    private String instid;// 转账指令ID(民生)
    private String accntType;// 账户类型 1-表示对公 2-表示对私 3-表示对私存折
    private String reserved;// 备注
    private String chckFlag;// 复核标识 '0'表示 未经办; '1'表示 经办通过; '2'表示 复核不通过;
    private String netpayNo;// 网银支付号
    private String netpayName;// 网银支付行名
    private String code;// 支付状态码
    private String localflag;// 汇路 '0'表示 同行本地; '1',表示表示 同行异地; '2','表示 小额; '3'表示
                             // 大额; '4',表示 上海同城; '5'表示 网银互联;
    private String xferType;// 转账类型 1-表示跨行转账 2-表示同行转账
    private String mchType;// 商户类型 '0'表示 直联商户; '1',表示表示 二清商户; '2','表示 盒子支付
    private String cpatchid;// 转账指令ID(光大，报包流水号)
    private String batchid;// 转账批次号（光大）
    private String packetid;// 报告流水号 新增为新版
    private String elecchequeno;// 电子凭证号 新增为新版
    private String acceptno;// 受理编号
    private String bankname;// 出账银行
    private String settlebatch;
    private String start;
    private String limit;
    private String startTime;
    private String endTime;
    private String serviceCode;
    private String token;
    private String log;

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getSettleDate() {
        return settleDate;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClearMerchid() {
        return this.clearMerchid;
    }

    public void setClearMerchid(String clearMerchid) {
        this.clearMerchid = clearMerchid;
    }

    public String getClearMerchant() {
        return this.clearMerchant;
    }

    public void setClearMerchant(String clearMerchant) {
        this.clearMerchant = clearMerchant;
    }

    public String getAccntNo() {
        return this.accntNo;
    }

    public void setAccntNo(String accntNo) {
        this.accntNo = accntNo;
    }

    public String getAccntName() {
        return this.accntName;
    }

    public void setAccntName(String accntName) {
        this.accntName = accntName;
    }

    public String getUnionNo() {
        return this.unionNo;
    }

    public void setUnionNo(String unionNo) {
        this.unionNo = unionNo;
    }

    public String getUnionName() {
        return this.unionName;
    }

    public void setUnionName(String unionName) {
        this.unionName = unionName;
    }

    public String getClrflag() {
        return this.clrflag;
    }

    public void setClrflag(String clrflag) {
        this.clrflag = clrflag;
    }

    public String getTrnamount() {
        return this.trnamount;
    }

    public void setTrnamount(String trnamount) {
        this.trnamount = trnamount;
    }

    public String getTrncnt() {
        return this.trncnt;
    }

    public void setTrncnt(String trncnt) {
        this.trncnt = trncnt;
    }

    public String getSupcharge() {
        return this.supcharge;
    }

    public void setSupcharge(String supcharge) {
        this.supcharge = supcharge;
    }

    public String getCrncharge() {
        return this.crncharge;
    }

    public void setCrncharge(String crncharge) {
        this.crncharge = crncharge;
    }

    public String getCharge() {
        return this.charge;
    }

    public void setCharge(String charge) {
        this.charge = charge;
    }

    public String getAccntamount() {
        return this.accntamount;
    }

    public void setAccntamount(String accntamount) {
        this.accntamount = accntamount;
    }

    public String getSettleString() {
        return this.settleDate;
    }

    public void setSettleDate(String settleDate) {
        this.settleDate = settleDate;
    }

    public String getStlexday() {
        return this.stlexday;
    }

    public void setStlexday(String stlexday) {
        this.stlexday = stlexday;
    }

    public String getSettleDateFn() {
        return this.settleDateFn;
    }

    public void setSettleDateFn(String settleDateFn) {
        this.settleDateFn = settleDateFn;
    }

    public String getSettleType() {
        return this.settleType;
    }

    public void setSettleType(String settleType) {
        this.settleType = settleType;
    }

    public String getXferFlag() {
        return this.xferFlag;
    }

    public void setXferFlag(String xferFlag) {
        this.xferFlag = xferFlag;
    }

    public String getAsstFlag() {
        return this.asstFlag;
    }

    public void setAsstFlag(String asstFlag) {
        this.asstFlag = asstFlag;
    }

    public String getXferDate() {
        return this.xferDate;
    }

    public void setXferDate(String xferDate) {
        this.xferDate = xferDate;
    }

    public String getUserid() {
        return this.userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getInstid() {
        return this.instid;
    }

    public void setInstid(String instid) {
        this.instid = instid;
    }

    public String getAccntType() {
        return this.accntType;
    }

    public void setAccntType(String accntType) {
        this.accntType = accntType;
    }

    public String getReserved() {
        return this.reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

    public String getChckFlag() {
        return this.chckFlag;
    }

    public void setChckFlag(String chckFlag) {
        this.chckFlag = chckFlag;
    }

    public String getNetpayNo() {
        return this.netpayNo;
    }

    public void setNetpayNo(String netpayNo) {
        this.netpayNo = netpayNo;
    }

    public String getNetpayName() {
        return this.netpayName;
    }

    public void setNetpayName(String netpayName) {
        this.netpayName = netpayName;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLocalflag() {
        return this.localflag;
    }

    public void setLocalflag(String localflag) {
        this.localflag = localflag;
    }

    public String getXferType() {
        return this.xferType;
    }

    public void setXferType(String xferType) {
        this.xferType = xferType;
    }

    public String getMchType() {
        return this.mchType;
    }

    public void setMchType(String mchType) {
        this.mchType = mchType;
    }

    public String getCpatchid() {
        return this.cpatchid;
    }

    public void setCpatchid(String cpatchid) {
        this.cpatchid = cpatchid;
    }

    public String getBatchid() {
        return this.batchid;
    }

    public void setBatchid(String batchid) {
        this.batchid = batchid;
    }

    public String getPacketid() {
        return this.packetid;
    }

    public void setPacketid(String packetid) {
        this.packetid = packetid;
    }

    public String getElecchequeno() {
        return this.elecchequeno;
    }

    public void setElecchequeno(String elecchequeno) {
        this.elecchequeno = elecchequeno;
    }

    public String getAcceptno() {
        return this.acceptno;
    }

    public void setAcceptno(String acceptno) {
        this.acceptno = acceptno;
    }

    public String getBankname() {
        return this.bankname;
    }

    public void setBankname(String bankname) {
        this.bankname = bankname;
    }

    public String getSettlebatch() {
        return this.settlebatch;
    }

    public void setSettlebatch(String settlebatch) {
        this.settlebatch = settlebatch;
    }

}