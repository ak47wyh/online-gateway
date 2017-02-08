package com.iboxpay.settlement.gateway.common.inout;

import java.io.Serializable;
/**
 * 公共请求头
 * @author jianbo_chen
 */
public class CommonRequestModel implements Serializable {

    private static final long serialVersionUID = 1L;
    //银行与主账号必须有一个不为空
    private String bankName;// 银行
    private String appCode;//  应用编号 
    private String appType;//  应用类型：0-扫码支付,1-刷卡支付
    private String type;//	业务类别, 如支付的: pay（普通转账支付，默认）；salary（工资）；transdown（下拔）；transup（上划）等等。默认为pay。
    private String requestSystem;//请求系统
    private String payMerchantNo;//交易账户
    @Deprecated
    //压力测试时使用
    private boolean forTest;

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRequestSystem(String requestSystem) {
        this.requestSystem = requestSystem;
    }

    public String getRequestSystem() {
        return requestSystem;
    }

    public boolean isForTest() {
        return forTest;
    }

    public void setForTest(boolean forTest) {
        this.forTest = forTest;
    }

	public String getAppType() {
		return appType;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}

	public String getPayMerchantNo() {
		return payMerchantNo;
	}

	public void setPayMerchantNo(String payMerchantNo) {
		this.payMerchantNo = payMerchantNo;
	}
    
}
