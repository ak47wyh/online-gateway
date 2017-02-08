package com.iboxpay.settlement.gateway.common.trans.balance;

import java.math.BigDecimal;
import java.util.Date;

import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.Constant;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.cache.remote.MemcachedService;
import com.iboxpay.settlement.gateway.common.dao.CommonDao;
import com.iboxpay.settlement.gateway.common.dao.impl.CommonDaoImpl;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.BalanceEnity;
import com.iboxpay.settlement.gateway.common.inout.AccntsettleModel;
import com.iboxpay.settlement.gateway.common.inout.CommonRequestModel;
import com.iboxpay.settlement.gateway.common.inout.CommonResultModel;
import com.iboxpay.settlement.gateway.common.inout.ResultModel;
import com.iboxpay.settlement.gateway.common.inout.balance.BalanceRequestModel;
import com.iboxpay.settlement.gateway.common.inout.balance.BalanceResultModel;
import com.iboxpay.settlement.gateway.common.task.RunnableTask;
import com.iboxpay.settlement.gateway.common.task.TaskParam;
import com.iboxpay.settlement.gateway.common.task.TaskScheduler;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;
import com.iboxpay.settlement.gateway.common.trans.ErrorCode;
import com.iboxpay.settlement.gateway.common.trans.IBankTrans;
import com.iboxpay.settlement.gateway.common.trans.IBankTransInterceptor;
import com.iboxpay.settlement.gateway.common.trans.ITransDelegate;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;

//余额查询
@Service
public class BalanceDelegateService implements ITransDelegate, RunnableTask {

    private final Logger logger = LoggerFactory.getLogger(BalanceDelegateService.class);

    private CommonDao commonDao = CommonDaoImpl.getDao(BalanceEnity.class);

    @Resource
    private MemcachedService memcachedService;

    @Resource
    public void setSessionFactory(SessionFactory sessionFactory) {
        //		this.commonDao = new CommonDaoImpl<BalanceEnity>(sessionFactory);
    }

    @Override
    public TransCode getTransCode() {
        return TransCode.BALANCE;
    }

    @Override
    public CommonRequestModel parseInput(String input) throws Exception {
        BalanceRequestModel requestModel = (BalanceRequestModel) JsonUtil.jsonToObject(input, "UTF-8", BalanceRequestModel.class);
        return requestModel;
    }

    public CommonResultModel trans(TransContext context, CommonRequestModel requestModel) {
        String accNo = requestModel.getAppCode();
        IBankTrans[] balanceInstances = BankTransComponentManager.getBankComponent(context.getMainAccount(), context.getBankProfile().getBankName(), IBalance.class);
        if (balanceInstances.length != 1) {
            String message = balanceInstances.length > 1 ? "【代码错误】有多于1个余额查询实现类." : "未实现查询余额";
            logger.warn(message);
            return new BalanceResultModel().setAppCode(accNo).fail(ErrorCode.sys_not_support, message);
        }
        return TaskScheduler.scheduleTask(TransCode.BALANCE, context.getMainAccount(), context.getBankProfile().getBankName(), null, false);
    }

    @Override
    public CommonResultModel run(TaskParam taskParam) {
        AccountEntity accountEntity = taskParam.getAccountEntity();
        BalanceResultModel resultModel = new BalanceResultModel();
        TransContext context = TransContext.getContext();
        resultModel.setAppCode(accountEntity.getAccNo());
        //它的作用只是判断是否需要保存，貌似还要从数据库读，但不用要求太高了.
        BalanceEnity balanceEnity = (BalanceEnity) memcachedService.getWithType(accountEntity.getAccNo(), BalanceEnity.class);

        boolean needSave = false;//是否保存：新的要保存，和上次不同的余额值也保存
        BigDecimal oldBalance = null, oldAvailableBalance = null;
        if (balanceEnity != null) {//已存在就直接使用旧的对象重置下
            oldBalance = balanceEnity.getBalance();
            oldAvailableBalance = balanceEnity.getAvailableBalance();
            resetBalanceEntity(balanceEnity);
        } else {
            balanceEnity = new BalanceEnity();
            balanceEnity.setAccNo(accountEntity.getAccNo());
            balanceEnity.setBankName(context.getBankProfile().getBankName());
            needSave = true;
        }
        BalanceEnity balanceEnities[] = new BalanceEnity[] { balanceEnity };
        IBankTrans[] balanceInstances = BankTransComponentManager.getBankComponent(accountEntity, context.getBankProfile().getBankName(), IBalance.class);
        IBalance balaceImpl = (IBalance) balanceInstances[0];

        if (balaceImpl instanceof IBankTransInterceptor) {
            try {
                ((IBankTransInterceptor) balaceImpl).beforeTrans(balanceEnities);
            } catch (Exception e) {
                logger.error("查询余额前处理异常", e);
                return resultModel.fail(ErrorCode.getErrorCodeByException(e), e.getMessage());
            }
        }
        try {
            balaceImpl.queryBalance(balanceEnities);
        } catch (Throwable e) {
            logger.info("查询余额时异常", e);
            return resultModel.fail(ErrorCode.getErrorCodeByException(e), e.getMessage());
        }

        if ((oldBalance == null && balanceEnity.getBalance() == null) || (oldBalance != null && balanceEnity.getBalance() != null && oldBalance.compareTo(balanceEnity.getBalance()) == 0)) {
            //值相同，忽略
        } else {//余额有变动
            needSave = true;
        }

        if ((oldAvailableBalance == null && balanceEnity.getAvailableBalance() == null)
                || (oldAvailableBalance != null && balanceEnity.getAvailableBalance() != null && oldAvailableBalance.compareTo(balanceEnity.getAvailableBalance()) == 0)) {
            //值相同，忽略
        } else {//余额有变动
            needSave = true;
        }
        balanceEnity.setCreateTime(new Date());
        if (needSave) {
            try {
                commonDao.save(balanceEnity);
            } catch (Exception e) {
                logger.error("保存查询余额结果失败", e);
            }
        }
        if (balaceImpl instanceof IBankTransInterceptor) {
            try {
                ((IBankTransInterceptor) balaceImpl).afterTrans(balanceEnities);
            } catch (Exception e) {
                logger.warn("查询后处理异常", e);
            }
        }
        memcachedService.setWithType(accountEntity.getAccNo(), balanceEnity);
        resultModel.setStatus(CommonResultModel.STATUS_SUCCESS);
        resultModel.setCurrency(context.getIsoCurrency());
        resultModel.setBalance(balanceEnity.getBalance());
        resultModel.setAvailableBalance(balanceEnity.getAvailableBalance());
        resultModel.setBankName(balanceEnity.getBankName());
        return resultModel;
    }

    private void resetBalanceEntity(BalanceEnity balanceEnity) {
        balanceEnity.setAvailableBalance(null);
        balanceEnity.setBalance(null);
        balanceEnity.setCreateTime(null);
        balanceEnity.setId(null);
    }

}
