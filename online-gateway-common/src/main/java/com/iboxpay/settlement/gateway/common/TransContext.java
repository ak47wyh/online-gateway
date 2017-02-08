package com.iboxpay.settlement.gateway.common;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.trans.TransCode;

/**
 * 当前业务上下文
 * @author jianbo_chen
 */
public class TransContext {

    public enum ResultCode {
        connectionFail, connectionSuccess
    }

    private static Logger logger = LoggerFactory.getLogger(TransContext.class);

    private static ThreadLocal<TransContext> contextThreadLocal = new ThreadLocal<TransContext>();

    private IBankProfile bankProfile;
    private AccountEntity accountEntity;
    private String isoCurrency;
    private TransCode transCode;
    private FrontEndConfig frontEndConfig;//当前分配到的前置机.
    private Map<String, Object> extractParam;
    private ResultCode resultCode;//结果码
    private String requestSystem;

    public TransContext(IBankProfile bankProfile, AccountEntity accountEntity, TransCode transCode) {
        this.bankProfile = bankProfile;
        this.accountEntity = accountEntity;
        this.transCode = transCode;
    }

    public TransContext(IBankProfile bankProfile, AccountEntity accountEntity) {
        this.bankProfile = bankProfile;
        this.accountEntity = accountEntity;
    }

    public AccountEntity getMainAccount() {
        return accountEntity;
    }

    public void setMainAccount(AccountEntity account) {
        this.accountEntity = account;
    }

    public static TransContext getContext() {
        return contextThreadLocal.get();
    }

    public static void setContext(TransContext context) {
        contextThreadLocal.set(context);
        if (context != null)
            MDC.put("bankName", context.getBankProfile().getBankName());
        else MDC.put("bankName", "common");
    }

    public void setIsoCurrency(String isoCurrency) {
        this.isoCurrency = isoCurrency;
    }

    public String getIsoCurrency() {
        return isoCurrency;
    }

    public String getBankCurrency() {
        return bankProfile.convertToBankCurrency(isoCurrency);
    }

    public IBankProfile getBankProfile() {
        return bankProfile;
    }

    public String convertCurrency(String isoCurrency) {
        return this.bankProfile.convertToBankCurrency(isoCurrency);
    }

    public void setFrontEndConfig(FrontEndConfig frontEndConfig) {
        this.frontEndConfig = frontEndConfig;
    }

    public FrontEndConfig getFrontEndConfig() {
        return frontEndConfig;
    }

    public String getCharset() {
        return frontEndConfig.getCharset().getVal();
    }

    public void setTransCode(TransCode transCode) {
        this.transCode = transCode;
    }

    public TransCode getTransCode() {
        return transCode;
    }

    /**
     * 设置线程上下文参数，方便在线程范围内传递参数
     * @param key
     * @param param
     */
    public void setParameter(String key, Object param) {
        if (extractParam == null) extractParam = new HashMap<String, Object>();

        this.extractParam.put(key, param);
    }

    /**
     * 获取线程上下文参数
     * @param key
     * @return
     */
    public Object getParameter(String key) {
        if (extractParam != null)
            return this.extractParam.get(key);
        else return null;
    }

    public ResultCode getResultCode() {
        return resultCode;
    }

    public void setResultCode(ResultCode resultCode) {
        this.resultCode = resultCode;
    }

    public void setRequestSystem(String requestSystem) {
        this.requestSystem = requestSystem;
    }

    public String getRequestSystem() {
        return requestSystem;
    }
}
