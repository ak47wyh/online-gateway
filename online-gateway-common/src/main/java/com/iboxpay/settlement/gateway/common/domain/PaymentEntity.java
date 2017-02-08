package com.iboxpay.settlement.gateway.common.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.cache.local.AreaCodeCache;
import com.iboxpay.settlement.gateway.common.util.ClassUtil;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

@Entity
@Table(name = "T_EB_Payment")
public class PaymentEntity implements Serializable, Cloneable, Comparable<PaymentEntity> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "paymententity_generator")
    @SequenceGenerator(name = "paymententity_generator", sequenceName = "PAYMENT_ID_SEQ", allocationSize = 50, initialValue = 1)
    @Column(name = "ID")
    private Long id;//long

    @Column(name = "BATCH_SEQ_ID")
    private String batchSeqId;//VARCHAR2(20) 批次流水号			

    @Column(name = "SEQ_ID")
    private String seqId;//	VARCHAR2(20)	流水号

    @Column(name = "ACC_NO")
    private String accNo;//	VARCHAR2(30)	交易主账号

    @Column(name = "ACC_NAME")
    private String accName;//	NVARCHAR2(30)	交易主账户名

    @Column(name = "BANK_NAME")
    private String bankName;//	VARCHAR2(10)	银行简码

    @Column(name = "PAY_TYPE")
    private String payType;//	VARCHAR2(10)	业务类别

    @Column(name = "TRANS_DATE")
    private Date transDate;//	TIMESTAMP(6)	预约日期

    @Column(name = "AMOUNT")
    private BigDecimal amount;//	NUMBER(19,2)	付款金额

    @Column(name = "CUSTOMER_ACC_NO")
    private String customerAccNo;//	VARCHAR2(30)	客户账号

    @Column(name = "CUSTOMER_ACC_NAME")
    private String customerAccName;//	NVARCHAR2(30)	客户账户名

    @Column(name = "CUSTOMER_ACC_TYPE")
    private int customerAccType;//	int	账户类型 1-表示对公 2-表示对私 3-表示对私存折
    public final static int CUSTOMERACCTYPE_COMPANY = 1;
    public final static int CUSTOMERACCTYPE_PRIVATE = 2;
    public final static int CUSTOMERACCTYPE_PRIVATE_BOOK = 3;
    
    @Column(name = "CUSTOMER_CARD_TYPE")
    private int customerCardType;//	int	卡类型 0-借记卡（默认） 1-存折    2-贷记卡（信用卡）3-公司账号
    public final static int CUSTOMERCARDTYPE_DEBIT = 0;
    public final static int CUSTOMERCARDTYPE_PASSBOOK = 1;
    public final static int CUSTOMERCARDTYPE_CREDIT = 2;
    public final static int CUSTOMERCARDTYPE_COMPANY = 3;

    @Column(name = "CUSTOMER_BANK_NAME")
    private String customerBankName;//	VARCHAR2(10)	客户银行简称

    @Column(name = "CUSTOMER_BANK_FULL_NAME")
    private String customerBankFullName;// NVARCHAR2(30) 客户银行全称

    @Column(name = "CUSTOMER_BANK_BRANCH_NAME")
    private String customerBankBranchName;//NVARCHAR2(50) 客户银行开户行全称. 如招商银行深圳高新园支行

    @Column(name = "CUSTOMER_AREACODE")
    private String customerAreaCode;//	CHAR(4)	 --客户账号开户地区

    @Column(name = "CUSTOMER_CNAPS")
    private String customerCnaps;//	CHAR(15)	客户账号CNAP号

    @Column(name = "CUSTOMER_CNAPS_BANKNO")
    private String customerCnapsBankno;//--网银支付行号(网银互联使用)

    // 汇路 '0'表示 同行本地; '1',表示表示 同行异地; '2','表示 小额; '3'表示
    // 大额; '4',表示 上海同城; '5'表示 网银互联;
    @Column(name = "LOCAL_FLAG")
    private int localFlag;

    // 转账类型 1-表示跨行转账 2-表示同行转账
    @Column(name = "SAME_BANK")
    private int sameBank;

    public final static int SAME_BANK_NO = 1;
    public final static int SAME_BANK_YES = 2;

    @Column(name = "PAY_TRANS_CODE")
    private String payTransCode;//	VARCHAR2(20)	银行交易指令

    @Column(name = "BANK_BATCH_SEQ_ID")
    private String bankBatchSeqId;//	VARCHAR2(20)	银行批次号

    @Column(name = "BANK_SEQ_ID")
    private String bankSeqId;//	VARCHAR2(20)	银行明细号

    @Column(name = "CURRENCY")
    private String currency;//	char(5)	交易币别

    @Column(name = "USE_CODE")
    private String useCode;//	NVARCHAR2(15)	用途代码

    @Column(name = "USE_DESC")
    private String useDesc;//	NVARCHAR2(50)	用途描述

    @Column(name = "REMARK")
    private String remark;//	NVARCHAR2(100)	备注

    @Column(name = "QUERY_TRANS_COUNT")
    private int queryTransCount;//同步次数

    /**
     * 设值见{@link com.iboxpay.settlement.gateway.common.trans.PaymentStatus}
     */
    @Column(name = "STATUS")
    private int status;//	int	交易状态

    @Column(name = "STATUS_MSG")
    private String statusMsg;//状态信息

    @Column(name = "PAY_BANK_STATUS")
    private String payBankStatus;//	NVARCHAR2(30) 支付时银行返回状态

    @Column(name = "PAY_BANK_STATUS_MSG")
    private String payBankStatusMsg;//	NVARCHAR2(100) 支付时银行返回状态信息

    @Column(name = "PAY_ERROR_CODE")
    private int payErrorCode;//支付时返回的错误码(一般是银行校验不通过时返回)

    @Column(name = "BANK_STATUS")
    private String bankStatus;//	NVARCHAR2(30) 当前银行返回状态

    @Column(name = "BANK_STATUS_MSG")
    private String bankStatusMsg;//	NVARCHAR2(100)

    @Column(name = "ERROR_CODE")
    private int errorCode;//错误码(如果查询过,则返回查询错误码)

    @Column(name = "SUBMIT_PAY_TIME")
    private Date submitPayTime;//	TIMESTAMP(6)	提交支付时间

    @Column(name = "CREATE_TIME")
    private Date createTime;//	TIMESTAMP(6)	N

    @Column(name = "UPDATE_TIME")
    private Date updateTime;//	TIMESTAMP(6)	N	

    //	EXT_PROPERTIES	NVARCHAR2(1000)			
    @Column(name = "EXT_PROPERTIES")
    private String extProperties;//
    
    @Column(name = "CAllBACK_EXT_PROPERTIES")
    private String callbackExtProperties;//回调扩展属性
    
    @Column(name = "APP_CODE")
    private String appCode;//应用编号
    
    @Column(name = "APP_TYPE")
    private String appType;//应用类型
    
    @Column(name = "PAY_MERCHANT_NO")
    private String payMerchantNo;//交易主账号
    
    @Column(name = "MERCHANT_EXT_PROPERTIES")
    private String merchantExtProperties;//交易商户号信息
    
    /**
     * 证件类型字典：
     * 0:公民身份证;
     * 1:中国护照;
     * 2:军人身份证;
     * 3:警官证;
     * 4:户口簿;
     * 5:临时身份证
     * 6:外国护照;
     * 7:港澳通行证;
     * 8:台胞通行证;
     * 9:离休干部荣誉证;
     * A:军官退休证;
     * B:文职干部退休证;
     * C:军事院校学员证;
     * D:武装警察身份证;
     * E:军官证;
     * F:文职干部证;
     * G:军人士兵证;
     * H:武警士兵证;
     * Z:其他证件
     * */
    public final static String EXT_PROPERTY_CertType = "certType";//证件类型
    /**证件号码*/
    public final static String EXT_PROPERTY_CertNo = "certNo";//证件号码
    /**手机号码*/
    public final static String EXT_PROPERTY_MOBILENO = "mobileNo";//手机号码
    /**虚拟账户(盒子内部商户号)*/
    public final static String EXT_PROPERTY_CLEAR_MERCHANT_NO = "clearMerchNo";

    /**二维码地址*/
    public final static String CALLBACK_EXT_PROPERTY_CodeUrl = "codeUrl";//二维码地址
    /**二维码图片*/
    public final static String CALLBACK_EXT_PROPERTY_CodeImgUrl = "codeImgUrl";//二维码图片
    public final static String CALLBACK_EXT_PROPERTY_BigImgUrl = "bigImgUrl";//二维码图片[大图]
    public final static String CALLBACK_EXT_PROPERTY_BUYERID ="buyerId";//买家支付宝用户号
    public final static String CALLBACK_EXT_PROPERTY_BUYERLOGINID ="buyerLoginId";//买家登陆账号
  

    //扩展属性生成的map
    private transient Map<String, Object> extPropertiesMap;
    private transient Map<String, Object> callbackExtPropertiesMap;
    private transient Map<String,Object> merchantMap;

    // 扩展回调html请求内容
    private transient String htmlContext;
    
    
	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBatchSeqId() {
        return batchSeqId;
    }

    public void setBatchSeqId(String batchSeqId) {
        this.batchSeqId = batchSeqId;
    }

    public String getSeqId() {
        return seqId;
    }

    public void setSeqId(String seqId) {
        this.seqId = seqId;
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

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public Date getTransDate() {
        return transDate;
    }

    public void setTransDate(Date transDate) {
        this.transDate = transDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public void setCustomerBankName(String customerBankName) {
        this.customerBankName = customerBankName;
    }

    public String getCustomerBankName() {
        return customerBankName;
    }

    public String getCustomerAreaCode() {
        if (StringUtils.isBlank(customerAreaCode) && !StringUtils.isBlank(this.customerCnaps)) return StringUtils.getAreaCodeFromCnaps(customerCnaps);

        return customerAreaCode;
    }

    public void setCustomerAreaCode(String customerAreaCode) {
        this.customerAreaCode = customerAreaCode;
    }

    public String getCustomerCnaps() {
        return customerCnaps;
    }

    public void setCustomerCnaps(String customerCnaps) {
        this.customerCnaps = customerCnaps;
    }

    public String getPayTransCode() {
        return payTransCode;
    }

    public void setPayTransCode(String payTransCode) {
        this.payTransCode = payTransCode;
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public int getQueryTransCount() {
        return queryTransCount;
    }

    public void setQueryTransCount(int queryTransCount) {
        this.queryTransCount = queryTransCount;
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

    public Date getSubmitPayTime() {
        return submitPayTime;
    }

    public void setSubmitPayTime(Date submitPayTime) {
        this.submitPayTime = submitPayTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
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

    public String getCustomerBankBranchName() {
        return customerBankBranchName;
    }

    public void setCustomerBankBrachName(String customerBankBranchName) {
        this.customerBankBranchName = customerBankBranchName;
    }

    public String getCustomerBankFullName() {
        if (!StringUtils.isBlank(customerBankFullName)) {
            return customerBankFullName;
        } else if (!StringUtils.isBlank(this.customerCnaps)) {
            String bankCode = StringUtils.getBankCodeFromCnaps(this.customerCnaps);
            return AreaCodeCache.getBankNameByCode(bankCode);
        }
        return customerBankFullName;
    }

    public void setCustomerBankFullName(String customerBankFullName) {
        this.customerBankFullName = customerBankFullName;
    }

    /**
     * 获取实际填写的银行全名（未通过联行号转换的）
     * @return
     */
    public String getRealCustomerBankFullName() {
        return customerBankFullName;
    }

    public void setLocalFlag(int localFlag) {
        this.localFlag = localFlag;
    }

    public int getLocalFlag() {
        return localFlag;
    }

    public void setSameBank(int sameBank) {
        this.sameBank = sameBank;
    }

    public int getSameBank() {
        return sameBank;
    }

    public boolean isToSameBack() {
        if (sameBank != 0) {
            return SAME_BANK_YES == sameBank;
        } else if (this.bankName != null && this.customerBankName != null) {
            return this.bankName.equals(this.customerBankName);//银行不同
        } else {
            AccountEntity accountEntity = TransContext.getContext().getMainAccount();
            String mainAccBankFullName = accountEntity.getBankFullName();
            String mainAccCnaps = accountEntity.getCnaps();
            if (!StringUtils.isBlank(mainAccCnaps) && !StringUtils.isBlank(this.customerCnaps)) {
                return StringUtils.getBankCodeFromCnaps(mainAccCnaps).equals(StringUtils.getBankCodeFromCnaps(this.customerCnaps));
            } else if (!StringUtils.isBlank(mainAccBankFullName) && !StringUtils.isBlank(this.customerBankFullName)) {
                return mainAccBankFullName.equals(this.customerBankFullName);
            } else {
                throw new IllegalArgumentException("没有足够信息判断同行或者跨行,请检查输入支付信息.");
            }
        }
    }

    public boolean isToCompay() {
        return CUSTOMERACCTYPE_COMPANY == this.customerAccType;
    }

    public boolean isToPrivate() {
        return CUSTOMERACCTYPE_PRIVATE == this.customerAccType || CUSTOMERACCTYPE_PRIVATE_BOOK == this.customerAccType;
    }

    public String getPayBankStatus() {
        return payBankStatus;
    }

    public void setPayBankStatus(String payBankStatus) {
        this.payBankStatus = autoTruncateStringN(payBankStatus, 30);
    }

    public String getPayBankStatusMsg() {
        return payBankStatusMsg;
    }

    public void setPayBankStatusMsg(String payBankStatusMsg) {
        this.payBankStatusMsg = autoTruncateStringN(payBankStatusMsg, 100);
    }

    public int getPayErrorCode() {
        return payErrorCode;
    }

    public void setPayErrorCode(int payErrorCode) {
        this.payErrorCode = payErrorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setCustomerBankBranchName(String customerBankBranchName) {
        this.customerBankBranchName = customerBankBranchName;
    }
    
    public String getAppCode() {
		return appCode;
	}

	public void setAppCode(String appCode) {
		this.appCode = appCode;
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

	public String getMerchantExtProperties() {
		return merchantExtProperties;
	}

	public void setMerchantExtProperties(String merchantExtProperties) {
		this.merchantExtProperties = merchantExtProperties;
	}

	public PaymentEntity clone() throws CloneNotSupportedException {
        return (PaymentEntity) super.clone();
    }

    public CityEntity getCustomerCityInfo() {
        AreaCodeEntity areaCodeEntity = getCustomerAreaCodeInfo();
        if (areaCodeEntity != null) {
            return areaCodeEntity.getCity();
        }
        return null;
    }

    public AreaCodeEntity getCustomerAreaCodeInfo() {
        String areaCode = getCustomerAreaCode();
        if (areaCode == null)
            return null;
        else return AreaCodeCache.getAreaCode(areaCode);
    }

    public void setCustomerCnapsBankno(String customerCnapsBankno) {
        this.customerCnapsBankno = customerCnapsBankno;
    }

    public String getCustomerCnapsBankno() {
        return customerCnapsBankno;
    }

    public void setExtProperties(String extProperties) {
        this.extProperties = extProperties;
    }

    public String getExtProperties() {
        return extProperties;
    }

    
    public String getHtmlContext() {
		return htmlContext;
	}

	public void setHtmlContext(String htmlContext) {
		this.htmlContext = htmlContext;
	}
    
	public Map<String, Object> getMerchantMap() {
		return merchantMap;
	}

	public void setMerchantMap(Map<String, Object> merchantMap) {
		this.merchantMap = merchantMap;
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

    
   
    /****************************************************************************
     * add callbackExtProperties
     *
     ****************************************************************************/
	public void setCallbackExtProperties(String callbackExtProperties) {
		this.callbackExtProperties = callbackExtProperties;
	}

	public String getCallbackExtProperties() {
		return callbackExtProperties;
	}
	
    public Object getCallbackExtProperties(String name) {
        if (!StringUtils.isBlank(callbackExtProperties)) {
            try {
            	callbackExtPropertiesMap = (Map) JsonUtil.jsonToObject(callbackExtProperties, "UTF-8", Map.class);
            } catch (Exception e) {
                System.err.print(e);
            }
        }
        if (callbackExtPropertiesMap != null) return callbackExtPropertiesMap.get(name);
        return "";
    }



	@Override
    public String toString() {
        return ClassUtil.toString(this);
    }

    @Override
    public int compareTo(PaymentEntity o) {
        long r = this.getId() - o.getId();
        if (r > 0)
            return 1;
        else if (r < 0)
            return -1;
        else return 0;
    }
}
