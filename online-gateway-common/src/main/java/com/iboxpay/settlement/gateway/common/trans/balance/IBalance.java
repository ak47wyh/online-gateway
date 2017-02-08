package com.iboxpay.settlement.gateway.common.trans.balance;

import java.io.IOException;

import com.iboxpay.settlement.gateway.common.domain.BalanceEnity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.IBankTrans;

/**
 * 余额查询接口
 * @author jianbo_chen
 */
public interface IBalance extends IBankTrans<BalanceEnity[]> {

    /**
     * 执行查询
     * @param balanceEntity : 账户信息已经包括在里面，也可以通过业务上下文TransContext获取账户必要信息。
     */
    public void queryBalance(BalanceEnity[] balanceEntities) throws BaseTransException, IOException;

}
