package com.iboxpay.settlement.gateway.common.trans.payment;

import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.IBankTrans;
import com.iboxpay.settlement.gateway.common.trans.callback.ICallBackPayment;
import com.iboxpay.settlement.gateway.common.trans.close.IClosePayment;
import com.iboxpay.settlement.gateway.common.trans.query.IQueryPayment;
import com.iboxpay.settlement.gateway.common.trans.refund.IRefundPayment;
import com.iboxpay.settlement.gateway.common.trans.refund.query.IRefundQueryPayment;
import com.iboxpay.settlement.gateway.common.trans.reverse.IReversePayment;

/**
 * 支付接口
 * @author jianbo_chen
 */
public interface IPayment extends IBankTrans<PaymentEntity[]> {

    /**
     * 支付业务导航信息
     * @return
     */
    public PaymentNavigation navigate();

    /**
     * 手动判断是否符合此支付组件。<b>注意： 此方法执行前会先匹配{@link IPayment#navigate()}中的业务信息；必须两个方法都匹配通过时才算符合该业务组件。</b>组件未实现时，默认实现会返回true。
     * @param payment : 单个支付对象，用于自动分批
     * @return
     */
    public boolean navigateMatch(PaymentEntity payment);

    /**
     * 接口实现必要的输入检查.主要的检测会在框架进行，但特殊的请自行实现.<br/>
     * <b>注意：检查通过，直接返回null；某一笔校验不通过，直接设置该笔状态为失败，返回null；检查不通过，返回非null说明信息，整批请求都会被拒绝；</b>
     * @param payments
     * @return 
     */
    public String check(PaymentEntity payments[]);

    /**
     * 获取银行批次流水号(如果支持批量)，生成的流水号默认是8位，需要的可以重写.<br/>
     * 注：由框架调用，生成后写入数据库后，才向银行发起交易请求.
     * @param payments
     * @return
     */
    public void genBankBatchSeqId(PaymentEntity payments[]);

    /**
     * 获取银行明细流水号(每一笔的)，生成的流水号默认是8位，需要的可以重写.<br/>
     * 注：由框架调用，生成后写入数据库后，才向银行发起交易请求.
     * @param payments
     * @return
     */
    public void genBankSeqId(PaymentEntity payments[]);

    /**
     * 执行支付
     * @param payments
     * @return
     */
    public void pay(PaymentEntity[] payments) throws BaseTransException;

    /**
     * 查询状态接口是哪个类
     * @return
     */
    public Class<? extends IQueryPayment> getQueryClass();
    
    
    /**
     * 退款接口实现类
     * @return
     */
    public abstract Class<? extends IRefundPayment> getRefundClass();
    
    
    /**
     * 查询退款接口实现类
     * @return
     */
    public abstract Class<? extends IRefundQueryPayment> getRefundQueryClass();
    
    /**
     * 冲正接口实现类
     * @return
     */
    public abstract Class<? extends IReversePayment> getReverseClass();
    
    
    /**
     * 关闭订单接口实现类
     * @return
     */
    public abstract Class<? extends IClosePayment> getCloseClass();
    
    /**
     * 异步回调接口实现类
     * @return
     */
    public abstract Class<? extends ICallBackPayment> getCallBackClass();
    
    
    
}
