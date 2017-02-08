package com.iboxpay.settlement.gateway.common.trans;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.inout.CommonRequestModel;
import com.iboxpay.settlement.gateway.common.inout.CommonResultModel;

/**
 * 业务入口(与具体银行无关的)
 * @author jianbo_chen
 */
public interface ITransDelegate {

    /**
     * 交易码.对外接口使用.如 /online/{transCode}
     * @return
     */
    public TransCode getTransCode();

    /**
     * 解析请求输入串
     * @param input
     * @return
     */
    public CommonRequestModel parseInput(String input) throws Exception;

    /**
     * 交易
     * @param str
     * @return
     */
    public CommonResultModel trans(TransContext context, CommonRequestModel requestModel);

}
