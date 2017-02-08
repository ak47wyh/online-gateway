/*
 * Copyright (C) 2011-2015 ShenZhen iBOXPAY Information Technology Co.,Ltd.
 * 
 * All right reserved.
 * 
 * This software is the confidential and proprietary
 * information of iBoxPay Company of China. 
 * ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only
 * in accordance with the terms of the contract agreement 
 * you entered into with iBoxpay inc.
 *
 */
package com.iboxpay.settlement.gateway.common.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.iboxpay.settlement.gateway.common.util.ClassUtil;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

/**
 * The class AccountVerifyEntity.
 *
 * Description: 卡验证实体类
 *
 * @author: weiyuanhua
 * @since: 2015年10月14日 上午11:03:35 	
 * @version: $Revision$ $Date$ $LastChangedBy$
 *
 */
@Entity
@Table(name = "T_EB_AccountVerify")
public class AccountVerifyEntity implements Serializable, Cloneable, Comparable<AccountVerifyEntity> {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accountVerify_generator")
	@SequenceGenerator(name = "accountVerify_generator", sequenceName = "ACCOUNTVERIFY_ID_SEQ", allocationSize = 50, initialValue = 1)
	@Column(name = "ID")
	private Long id;//long
	
	@Column(name = "PAY_TRANS_CODE")
    private String payTransCode;//	VARCHAR2(20)	银行交易指令

    @Column(name = "BATCH_SEQ_ID")
    private String batchSeqId;//	VARCHAR2(20)	批次号
    
    @Column(name = "SEQ_ID")
	private String seqId;//序列号
	
	@Column(name = "SYS_NAME")
	private String sysName;//系统名称
	
	@Column(name = "CUSTOMER_ACC_NO")
	private String customerAccNo;//	VARCHAR2(30)	客户账号

	@Column(name = "CUSTOMER_ACC_NAME")
	private String customerAccName;//	NVARCHAR2(30)	客户账户名

	@Column(name = "CUSTOMER_ACC_TYPE")
	private int customerAccType = 2;//	int	账户类型 1-表示对公 2-表示对私 3-表示对私存折
	public final static int CUSTOMERACCTYPE_COMPANY = 1;
	public final static int CUSTOMERACCTYPE_PRIVATE = 2;
	public final static int CUSTOMERACCTYPE_PRIVATE_BOOK = 3;

	@Column(name = "CUSTOMER_CARD_TYPE")
	private int customerCardType = 1;//	int	卡类型 0-存折 1-借记卡 2-贷记卡
	public final static int CUSTOMECARDTYPE_BOOK = 0;
	public final static int CUSTOMERCARDTYPE_DEBIT = 1;
	public final static int CUSTOMERCARDTYPE_CREDIT = 2;
	
	@Column(name = "CERT_TYPE")
	private String certType;//证件类型
	
	@Column(name = "CERT_NO")
	private String certNo;//证件号码
	
	@Column(name = "MOBILE_NO")
	private String mobileNo;
	
    @Column(name = "BANK_BATCH_SEQ_ID")
    private String bankBatchSeqId;//	VARCHAR2(20)	银行批次号

    @Column(name = "BANK_SEQ_ID")
    private String bankSeqId;//	VARCHAR2(20)	银行明细号

	/**
	 * 设值见{@link com.iboxpay.settlement.gateway.common.trans.PaymentStatus}
	 */
	@Column(name = "STATUS")
	private int status;//	int	交易状态
	
	@Column(name = "STATUS_MSG")
    private String statusMsg;//状态信息
	
    @Column(name = "BANK_STATUS")
    private String bankStatus;//	NVARCHAR2(30) 当前银行返回状态

    @Column(name = "BANK_STATUS_MSG")
    private String bankStatusMsg;//	NVARCHAR2(100)

	@Column(name = "ERROR_CODE")
	private String errorCode;//返回的错误码

	@Column(name = "ERROR_MSG")
	private String errorMsg;//	NVARCHAR2(100)
	
//	@Column(name = "VALIDATE_STATUS")
//	private String validateStatus;//认证状态00-认证成功 99-认证失败
	
	@Column(name = "VERIFY_ERROR_CODE")
    private String verifyErrorCode;//验证时返回的错误码(一般是银行校验不通过时返回)

	@Column(name = "CREATE_TIME")
	private Date createTime;//	TIMESTAMP(6)	N

	@Column(name = "UPDATE_TIME")
	private Date updateTime;//	TIMESTAMP(6)	N	
	
    @Column(name = "EXT_PROPERTIES")
    private String extProperties;// NVARCHAR2(1000)		
	
	@Column(name = "REMARK")
	private String remark;//	NVARCHAR2(100)	备注
	
	//扩展属性生成的map
    private transient Map<String, Object> extPropertiesMap;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPayTransCode() {
		return payTransCode;
	}

	public void setPayTransCode(String payTransCode) {
		this.payTransCode = payTransCode;
	}

 
	public String getBatchSeqId() {
		return batchSeqId;
	}

	public void setBatchSeqId(String batchSeqId) {
		this.batchSeqId = batchSeqId;
	}

	public String getSysName() {
		return sysName;
	}

	public void setSysName(String sysName) {
		this.sysName = sysName;
	}

	public String getSeqId() {
		return seqId;
	}

	public void setSeqId(String seqId) {
		this.seqId = seqId;
	}

	public String getCustomerAccNo() {
		return customerAccNo;
	}

	public void setCustomerAccNo(String customerAccNo) {
		this.customerAccNo = customerAccNo;
	}

	public String getCustomerAccName() {
		return customerAccName;
	}

	public void setCustomerAccName(String customerAccName) {
		this.customerAccName = customerAccName;
	}

	public int getCustomerAccType() {
		return customerAccType;
	}

	public void setCustomerAccType(int customerAccType) {
		this.customerAccType = customerAccType;
	}

	public int getCustomerCardType() {
		return customerCardType;
	}

	public void setCustomerCardType(int customerCardType) {
		this.customerCardType = customerCardType;
	}
	
	public String getCertType() {
		return certType;
	}
	
	public void setCertType(String certType) {
		this.certType = certType;
	}

	public String getCertNo() {
		return certNo;
	}
	
	public void setCertNo(String certNo) {
		this.certNo = certNo;
	}

	public String getMobileNo() {
		return mobileNo;
	}
	
	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}
	
    public String getBankBatchSeqId() {
        return bankBatchSeqId;
    }

    public void setBankBatchSeqId(String bankBatchSeqId) {
        this.bankBatchSeqId = bankBatchSeqId;
    }

    public String getBankSeqId() {
        return bankSeqId;
    }

    public void setBankSeqId(String bankSeqId) {
        this.bankSeqId = bankSeqId;
    }

	public int getStatus() {
		return status;
	}

	/**
	 * 设值见{@link com.iboxpay.settlement.gateway.common.trans.PaymentStatus}
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	
    public String getStatusMsg() {
        return statusMsg;
    }

    private static String autoTruncateStringN(String str, int length) {//截断unicode的字符数
        if (str != null) {//超长自动截取
            str = str.length() > length ? str.substring(0, length - 1) + "…" : str;
        }
        return str;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = autoTruncateStringN(statusMsg, 100);
    }
    
    public String getBankStatus() {
        return bankStatus;
    }

    public void setBankStatus(String bankStatus) {
        this.bankStatus = autoTruncateStringN(bankStatus, 30);
    }

    public String getBankStatusMsg() {
        return bankStatusMsg;
    }

    public void setBankStatusMsg(String bankStatusMsg) {
        this.bankStatusMsg = autoTruncateStringN(bankStatusMsg, 100);
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

	public boolean isToCompay() {
		return CUSTOMERACCTYPE_COMPANY == this.customerAccType;
	}

	public boolean isToPrivate() {
		return CUSTOMERACCTYPE_PRIVATE == this.customerAccType || CUSTOMERACCTYPE_PRIVATE_BOOK == this.customerAccType;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public AccountVerifyEntity clone() throws CloneNotSupportedException {
		return (AccountVerifyEntity) super.clone();
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	
	public String getVerifyErrorCode() {
        return verifyErrorCode;
    }

    public void setVerifyErrorCode(String verifyErrorCode) {
        this.verifyErrorCode = verifyErrorCode;
    }
    
    public void setExtProperties(String extProperties) {
        this.extProperties = extProperties;
    }

    public String getExtProperties() {
        return extProperties;
    }
    
	public Object getExtProperty(String name) {
        if (extPropertiesMap == null && !StringUtils.isBlank(extProperties)) {
            try {
                extPropertiesMap = (Map) JsonUtil.jsonToObject(extProperties, "UTF-8", Map.class);
            } catch (Exception e) {
                System.err.print(e);
            }
        }
        if (extPropertiesMap != null) return extPropertiesMap.get(name);
        return "";
    }
	
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Override
	public String toString() {
		return ClassUtil.toString(this);
	}

	@Override
	public int compareTo(AccountVerifyEntity o) {
		long r = this.getId() - o.getId();
		if (r > 0)
			return 1;
		else if (r < 0)
			return -1;
		else return 0;
	}
}
