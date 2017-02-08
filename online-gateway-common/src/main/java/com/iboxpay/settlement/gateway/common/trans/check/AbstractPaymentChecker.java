package com.iboxpay.settlement.gateway.common.trans.check;

import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import com.iboxpay.settlement.gateway.common.dao.PaymentCheckRecordDao;
import com.iboxpay.settlement.gateway.common.dao.PaymentCheckResultDao;
import com.iboxpay.settlement.gateway.common.domain.PaymentCheckResultEntity;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;

/**
 * 对账实现基类
 * @author jianbo_chen
 */
public abstract class AbstractPaymentChecker implements IPaymentChecker {

    public final static long MINUS = 60 * 1000;

    @Override
    public List<PaymentCheckResultEntity> check(String accNo, Date transDate, Session session, PaymentCheckResultDao paymentCheckResultDao, PaymentCheckRecordDao paymentCheckRecordDao)
            throws Exception {
        int timeDiffMinus = PaymentCheckDelegateService.TIME_DIFFERENCE.getIntVal();
        long timeDiffMillis = timeDiffMinus * MINUS;
        Date beginTransDate = DateTimeUtil.truncateTime(transDate);
        Date endTransDate = DateTimeUtil.addDay(beginTransDate, 1);
        List<CheckerData> paymentList = readPaymentEntitys(accNo, session, beginTransDate, endTransDate);;
        List<CheckerData> detailList = readDetailEntitys(accNo, session, endTransDate, beginTransDate);
        checkBaseData(paymentList, detailList);
        List<PaymentCheckResultEntity> checkResultList = loopCheck(timeDiffMinus, timeDiffMillis, paymentList, detailList);
        paymentList = null;
        detailList = null;
        return checkResultList;
    }

    /**
     * 循环对账，交给子类实现
     * @param timeDiffMinus
     * @param timeDiffMillis
     * @param paymentList
     * @param detailList
     * @return
     */
    protected List<PaymentCheckResultEntity> loopCheck(int timeDiffMinus, long timeDiffMillis, List<CheckerData> paymentList, List<CheckerData> detailList) {
        throw new UnsupportedOperationException("对账未实现");
    }

    protected void checkBaseData(List<CheckerData> paymentList, List<CheckerData> detailList) throws Exception {
        if (paymentList != null && paymentList.size() > 0 && (detailList == null || detailList.size() == 0)) throw new Exception("银行交易明细为空 或 未下载");
    }

    protected List<CheckerData> readDetailEntitys(String accNo, Session session, Date endTransDate, Date beginTransDate) {
        Query q =
                session.createQuery("select new com.iboxpay.settlement.gateway.common.trans.check.CheckerData" + "(" + "customerAccNo, customerAccName, debitAmount, transDate, id" + ")"
                        + "from DetailEntity " + "where accNo=:accNo " + "and transDate>=:beginTransDate " + "and transDate<:endTransDate " + "order by customerAccNo asc, debitAmount asc");
        q.setString("accNo", accNo);
        q.setDate("beginTransDate", beginTransDate);
        q.setDate("endTransDate", endTransDate);
        List<CheckerData> detailList = q.list();
        return detailList;
    }

    protected List<CheckerData> readPaymentEntitys(String accNo, Session session, Date beginTransDate, Date endTransDate) {
        Query q =
                session.createQuery("select new com.iboxpay.settlement.gateway.common.trans.check.CheckerData" + "(" + "p.customerAccNo,p.customerAccName,p.amount,p.submitPayTime,p.id,p.status"
                        + ") " + "from PaymentEntity p " + "where p.accNo=:accNo " + "and p.transDate>=:beginTransDate " + "and p.transDate<:endTransDate " + "and p.status >= :status "
                        + "order by p.customerAccNo asc, p.amount asc");//对可能存在一天内一个收款账号出现多次情况
        q.setString("accNo", accNo);
        q.setDate("beginTransDate", beginTransDate);
        q.setDate("endTransDate", endTransDate);
        q.setInteger("status", PaymentStatus.STATUS_SUBMITTED);
        List<CheckerData> paymentList = q.list();
        return paymentList;
    }

    @Override
    public String getBankTransCode() {
        return null;
    }

    @Override
    public String getBankTransDesc() {
        return "对账";
    }

    @Override
    public TransCode getTransCode() {
        return TransCode.CHECK;
    }

}
