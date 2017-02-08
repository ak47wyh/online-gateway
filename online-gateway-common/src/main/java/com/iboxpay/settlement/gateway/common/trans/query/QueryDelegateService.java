package com.iboxpay.settlement.gateway.common.trans.query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.dao.PaymentDao;
import com.iboxpay.settlement.gateway.common.domain.BatchPaymentEntity;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.inout.CommonRequestModel;
import com.iboxpay.settlement.gateway.common.inout.CommonResultModel;
import com.iboxpay.settlement.gateway.common.inout.payment.PaymentOuterStatus;
import com.iboxpay.settlement.gateway.common.inout.payment.PaymentResultModel;
import com.iboxpay.settlement.gateway.common.inout.query.QueryPaymentRequestModel;
import com.iboxpay.settlement.gateway.common.service.PaymentService;
import com.iboxpay.settlement.gateway.common.task.RunnableTask;
import com.iboxpay.settlement.gateway.common.task.TaskParam;
import com.iboxpay.settlement.gateway.common.task.TaskScheduler;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;
import com.iboxpay.settlement.gateway.common.trans.CompositeTransListener;
import com.iboxpay.settlement.gateway.common.trans.ErrorCode;
import com.iboxpay.settlement.gateway.common.trans.IBankTransInterceptor;
import com.iboxpay.settlement.gateway.common.trans.ITransDelegate;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.payment.IPayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;

@Service
public class QueryDelegateService implements ITransDelegate, RunnableTask {

    private final Logger logger = LoggerFactory.getLogger(QueryDelegateService.class);
    private final static CompositeTransListener compositeListener = new CompositeTransListener();
    final static String PARAM_QPAYMENT = "qpayment";
    final static String PARAM_QPAYMENT_IDS = "qpids";
    public final static String BATCH_NUM = "batchNum";

    @Resource
    private PaymentDao paymentDao;

    @Resource
    private PaymentService paymentService;

    @Override
    public TransCode getTransCode() {
        return TransCode.QUERY;
    }

    public static CompositeTransListener getCompositelistener() {
        return compositeListener;
    }

    @Override
    public CommonRequestModel parseInput(String input) throws Exception {
        QueryPaymentRequestModel requestModel = (QueryPaymentRequestModel) JsonUtil.jsonToObject(input, "UTF-8", QueryPaymentRequestModel.class);
        return requestModel;
    }

    @Override
    public CommonResultModel trans(TransContext context, CommonRequestModel requestModel) {
        context.setTransCode(TransCode.QUERY);
        QueryPaymentRequestModel model = (QueryPaymentRequestModel) requestModel;
        PaymentResultModel resultModel = new PaymentResultModel();
        resultModel.setBatchSeqId(model.getBatchSeqId());
        resultModel.setAppCode(context.getMainAccount().getAccNo());

        BatchPaymentEntity batchPaymentEntity = paymentDao.getBatchPaymentEntity(model.getBatchSeqId());
        if (batchPaymentEntity == null) {
            return resultModel.fail(ErrorCode.payment_not_exist, "找不到批次流水号");
        }
        //		List<PaymentEntity> paymentEntitys = batchPaymentEntity.getPaymentEntitys();//不采用延迟加载了，延迟加载没法设置排序
        List<PaymentEntity> paymentEntitys = paymentDao.findByHQL("from PaymentEntity where batchSeqId=? order by id asc", batchPaymentEntity.getBatchSeqId());
        Set<String> needReqBankBatchSeqs = new HashSet<String>();//需要向银行发起请求的 ,即未确定状态的单笔
        //过滤支付状态
        filterPayments(paymentEntitys, needReqBankBatchSeqs, model.isForceRefresh());
        List<PaymentEntity> needReqBankPayments = new LinkedList<PaymentEntity>();
        //只要某一批次里有一笔未确定的，都会整批传送到查询接口中，具体是否需要整批发送请求，由银行接口而定.
        for (String needReqBankBatchSeq : needReqBankBatchSeqs) {
            for (PaymentEntity checkedNeedReqBankEntity : paymentEntitys) {
                if (needReqBankBatchSeq.equals(checkedNeedReqBankEntity.getBankBatchSeqId())) needReqBankPayments.add(checkedNeedReqBankEntity);
            }
        }
        AtomicInteger batchNum = new AtomicInteger(0);//需要同步的批次总数
        if (needReqBankPayments.size() > 0) {
            Map<String, Object> params;
            if (paymentEntitys.size() == 1) {//原来也只有一笔
                params = new HashMap<String, Object>();
                batchNum.set(1);
                params.put(PARAM_QPAYMENT, needReqBankPayments.toArray(new PaymentEntity[0]));
                params.put(BATCH_NUM, batchNum);
                return TaskScheduler.scheduleTask(TransCode.QUERY, context.getMainAccount(), context.getBankProfile().getBankName(), params, false);//同步返回
            } else {//异步处理
                    //key:银行批次，value:数据库ID. 按银行批次划分后查询
                Map<String, List<Long>> bankBatchIdMap = new HashMap<String, List<Long>>();
                for (PaymentEntity needReqBankPayment : needReqBankPayments) {
                    if (needReqBankPayment.getBankBatchSeqId() == null) {
                        logger.warn("批次号为空: payment-id=" + needReqBankPayment.getId());
                        continue;
                    }
                    List<Long> ids = bankBatchIdMap.get(needReqBankPayment.getBankBatchSeqId());
                    if (ids == null) {
                        ids = new LinkedList<Long>();
                        bankBatchIdMap.put(needReqBankPayment.getBankBatchSeqId(), ids);
                    }
                    ids.add(needReqBankPayment.getId());
                }
                batchNum.set(bankBatchIdMap.size());
                Iterator<Entry<String, List<Long>>> itr = bankBatchIdMap.entrySet().iterator();
                paymentService.cache(needReqBankPayments);//放到cache中，在后台调度时不用再到数据库取
                while (itr.hasNext()) {
                    Entry<String, List<Long>> entry = itr.next();
                    Long[] ids = entry.getValue().toArray(new Long[0]);
                    params = new HashMap<String, Object>();
                    params.put(PARAM_QPAYMENT_IDS, ids);
                    params.put(BATCH_NUM, batchNum);
                    TaskScheduler.scheduleTask(TransCode.QUERY, context.getMainAccount(), context.getBankProfile().getBankName(), params, "q:" + model.getBatchSeqId() + "," + entry.getKey(), true);
                }
            }
        }
        //返回现有的结果，新的在后台查询
        PaymentOuterStatus.transmitStatusToResultModel(paymentEntitys.toArray(new PaymentEntity[0]), resultModel);
        return resultModel;
    }

    @Override
    public CommonResultModel run(TaskParam taskParam) {
        PaymentResultModel resultModel = new PaymentResultModel();
        //		AccountEntity accountEntity = taskParam.getAccountEntity();
        TransContext context = TransContext.getContext();
        PaymentEntity[] paymentEntitys = (PaymentEntity[]) taskParam.getParams().get(PARAM_QPAYMENT);
        if (paymentEntitys == null) {
            Long[] ids = (Long[]) taskParam.getParams().get(PARAM_QPAYMENT_IDS);
            paymentEntitys = paymentService.getPaymentEntitys(ids);//再读一次对象，队列中只保存ID
        }
        AtomicInteger batchNum = (AtomicInteger) taskParam.getParams().get(BATCH_NUM);
        Arrays.sort(paymentEntitys);//查询的顺序要与发送顺序相同，否则同一批次里 账号、金额都相同 的支付就没法区分(在没有明细号的情况下，如招行).
        doQuery(context, paymentEntitys);
        if (batchNum.decrementAndGet() <= 0) compositeListener.onPaymentQueryComplete(context.getMainAccount(), paymentEntitys[0].getBatchSeqId(), paymentEntitys);
        //		paymentDao.updateQueryTransCount(paymentEntity.getId());//TODO 
        resultModel.setAppCode(paymentEntitys[0].getAccNo());
        PaymentOuterStatus.transmitStatusToResultModel(paymentEntitys, resultModel);
        return resultModel;
    }

    private void doQuery(TransContext context, PaymentEntity[] payments) {
        //查找支付使用的实现类，再从实现类里找对象的查询实现
        String payTransCode = payments[0].getPayTransCode();
        IPayment paymentImpl = BankTransComponentManager.getPaymentByTransCode(context.getBankProfile().getBankName(), payTransCode);
        boolean hasException = false;//错了也要更新数据库
        //一般是系统代码调整后才有可能出现.
        if (paymentImpl == null) {
            hasException = true;
            String message = "【系统错误】 找不到[银行=" + context.getBankProfile().getBankName() + "]的支付实现组件[payTransCode=" + payTransCode + "]";
            updateOnlyUnknowStatus(payments, ErrorCode.sys_internal_err, PaymentStatus.STATUS_UNKNOWN, message);
            logger.error("", new Exception(message));
        }
        if (!hasException) {
            PaymentStatus.resetErrorCode(payments);//重设错误码(上次查询出错，这次先把上次的清空)
            //发起查询
            Class<? extends IQueryPayment> queryClass = paymentImpl.getQueryClass();
            if (queryClass == null) {
                updateOnlyUnknowStatus(payments, ErrorCode.sys_not_support, PaymentStatus.STATUS_UNKNOWN, "不支持或未实现查询交易状态");
                logger.warn("业务实现类'" + paymentImpl.getClass().getName() + "'不支持或未实现查询交易状态");
                return;
            }
            IQueryPayment queryPaymentImpl = (IQueryPayment) BankTransComponentManager.getBankComponent(queryClass);
            //1. 查询前处理
            if (queryPaymentImpl instanceof IBankTransInterceptor) {
                try {
                    ((IBankTransInterceptor) queryPaymentImpl).beforeTrans(payments);
                } catch (Exception e) {
                    hasException = true;
                    String message = "查询前处理异常";
                    logger.error(message, e);
                    updateOnlyUnknowStatus(payments, ErrorCode.getErrorCodeByException(e), PaymentStatus.STATUS_UNKNOWN, message + ":" + e.getMessage());
                }
            }
            //2.发起查询
            if (!hasException) {
                try {
                    queryPaymentImpl.query(payments);
                } catch (Throwable e) {//囧
                    hasException = true;
                    String message = "查询交易异常";
                    logger.error(message, e);
                    updateOnlyUnknowStatus(payments, ErrorCode.getErrorCodeByException(e), PaymentStatus.STATUS_UNKNOWN, message + ":" + e.getMessage());
                }
            }
            //3.交易后处理
            if (!hasException) {
                if (queryPaymentImpl instanceof IBankTransInterceptor) {
                    try {
                        ((IBankTransInterceptor) queryPaymentImpl).afterTrans(payments);
                    } catch (Exception e) {//忽略 
                        logger.error("查询后处理异常", e);
                    }
                }
            }
        }
        try {
            paymentService.updateStatus(payments, true);
        } catch (Throwable e) {
            String message = "查询支付结果后更新数据库失败";
            updateOnlyUnknowStatus(payments, ErrorCode.sys_internal_err, PaymentStatus.STATUS_UNKNOWN, message);
            logger.error(message, e);
        }

    }

    //异常时，只更新未确认的状态
    private void updateOnlyUnknowStatus(PaymentEntity[] payments, ErrorCode unexpectedErrorCode, int status, String statusMsg) {
        for (PaymentEntity paymentEntity : payments) {
            if (!PaymentStatus.isFinalStatus(paymentEntity)) PaymentStatus.setStatus(paymentEntity, unexpectedErrorCode, status, statusMsg);
        }
    }

    private void filterPayments(List<PaymentEntity> paymentEntitys, Set<String> needReqBankBatchSeqs, boolean forceRefresh) {
        //需要向银行发起请求的
        for (PaymentEntity paymentEntity : paymentEntitys) {
            int status = paymentEntity.getStatus();
            if (forceRefresh) {
                needReqBankBatchSeqs.add(paymentEntity.getBankBatchSeqId());
            } else if (status == PaymentStatus.STATUS_SUBMITTED// 已提交 
                    || status == PaymentStatus.STATUS_WAITTING_PAY  // 等待支付
                    || status == PaymentStatus.STATUS_UNKNOWN) {//状态未知
                needReqBankBatchSeqs.add(paymentEntity.getBankBatchSeqId());
            }
        }
    }

}
