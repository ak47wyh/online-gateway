package com.iboxpay.settlement.gateway.common.trans.reverse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.dao.PaymentDao;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.BatchPaymentEntity;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.inout.CommonRequestModel;
import com.iboxpay.settlement.gateway.common.inout.CommonResultModel;
import com.iboxpay.settlement.gateway.common.inout.payment.PaymentOuterStatus;
import com.iboxpay.settlement.gateway.common.inout.payment.PaymentResultModel;
import com.iboxpay.settlement.gateway.common.inout.refund.RefundPaymentCustomerInfo;
import com.iboxpay.settlement.gateway.common.inout.refund.RefundPaymentRequestModel;
import com.iboxpay.settlement.gateway.common.service.PaymentService;
import com.iboxpay.settlement.gateway.common.task.RunnableTask;
import com.iboxpay.settlement.gateway.common.task.TaskParam;
import com.iboxpay.settlement.gateway.common.task.TaskScheduler;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;
import com.iboxpay.settlement.gateway.common.trans.ErrorCode;
import com.iboxpay.settlement.gateway.common.trans.ITransDelegate;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.payment.IPayment;
import com.iboxpay.settlement.gateway.common.trans.query.QueryDelegateService;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;

@Service
public class ReversePaymentDelegateServer implements ITransDelegate, RunnableTask {

    private final Logger logger = LoggerFactory.getLogger(QueryDelegateService.class);
    public final static String BATCH_NUM = "batchNum";
    public final static String PARAM_RPAYMENT = "rpayment";
    final static String PARAM_QPAYMENT_IDS = "qpids";
    @Resource
    private PaymentDao paymentDao;

    @Resource
    private PaymentService paymentService;

    @Override
    public TransCode getTransCode() {
        return TransCode.REVERSE;
    }

    @Override
    public CommonRequestModel parseInput(String input) throws Exception {
        RefundPaymentRequestModel requestModel = (RefundPaymentRequestModel) JsonUtil.jsonToObject(input, "UTF-8", RefundPaymentRequestModel.class);
        return requestModel;
    }

    @Override
    public CommonResultModel trans(TransContext context, CommonRequestModel requestModel) {
        context.setTransCode(TransCode.REVERSE);
        RefundPaymentRequestModel model = (RefundPaymentRequestModel) requestModel;
        PaymentResultModel resultModel = new PaymentResultModel();
        resultModel.setBatchSeqId(model.getBatchSeqId());
        resultModel.setAppCode(context.getMainAccount().getAccNo());

        BatchPaymentEntity batchPaymentEntity = paymentDao.getBatchPaymentEntity(model.getBatchSeqId());
        if (batchPaymentEntity == null) {
            return resultModel.fail(ErrorCode.payment_not_exist, "找不到批次流水号");
        }
        List<PaymentEntity> paymentQueryList = paymentDao.findByHQL("from PaymentEntity where batchSeqId=? order by id asc", batchPaymentEntity.getBatchSeqId());

        List<PaymentEntity> paymentEntitys = new ArrayList<PaymentEntity>();
        // 过滤订单
        if (paymentQueryList != null && paymentQueryList.size() > 0) {
            for (PaymentEntity paymentEntity : paymentQueryList) {
                if (paymentEntity.getStatus() == PaymentStatus.STATUS_REFUND_SUCCESS) {
                    return resultModel.fail(ErrorCode.payment_exist_refund, "批次交易记录存在已退款交易");
                } else if (paymentEntity.getStatus() == PaymentStatus.STATUS_REVERSE_FAIL || paymentEntity.getStatus() == PaymentStatus.STATUS_SUBMITTED
                        || paymentEntity.getStatus() == PaymentStatus.STATUS_UNKNOWN || paymentEntity.getStatus() == PaymentStatus.STATUS_SUCCESS
                        || paymentEntity.getStatus() == PaymentStatus.STATUS_FAIL || paymentEntity.getStatus() == PaymentStatus.STATUS_WAITTING_PAY) {
                    paymentEntitys.add(paymentEntity);
                }
            }
        } else {
            return resultModel.fail(ErrorCode.payment_not_exist, "交易订单不可撤销");
        }
        if (paymentEntitys == null || paymentEntitys.size() <= 0) {
            return resultModel.fail(ErrorCode.payment_not_exist, "交易订单不可撤销");
        }

        //将传递的退款信息封装到实体中
        for (PaymentEntity paymentEntity : paymentEntitys) {
            RefundPaymentCustomerInfo[] data = model.getData();
            for (int i = 0; i < data.length; i++) {
                RefundPaymentCustomerInfo rs = data[i];
                if (rs.getSeqId().equals(paymentEntity.getSeqId().trim())) {
                    Map<String, Object> extPropertiesMap = rs.getExtProperties();
                    if (extPropertiesMap == null) {
                        extPropertiesMap = new HashMap<String, Object>();
                    }
                    if (!extPropertiesMap.containsKey("cancleNo")) {
                        extPropertiesMap.put("cancleNo", DateTimeUtil.getTimeMillisY(new Date(), "yyyyMMddHHmmss"));
                    }
                    String extProperties = JsonUtil.toJson(extPropertiesMap);
                    paymentEntity.setExtProperties(extProperties);
                }
            }
        }
        //
        AtomicInteger batchNum = new AtomicInteger(0);//需要同步的批次总数
        Map<String, Object> params;
        if (paymentEntitys.size() == 1) {//原来也只有一笔
            params = new HashMap<String, Object>();
            batchNum.set(1);
            params.put(PARAM_RPAYMENT, paymentEntitys.toArray(new PaymentEntity[0]));
            params.put(BATCH_NUM, batchNum);
            return TaskScheduler.scheduleTask(TransCode.REVERSE, context.getMainAccount(), context.getBankProfile().getBankName(), params, false);//同步返回
        } else {//异步处理
                //key:银行批次，value:数据库ID. 按银行批次划分后查询
            Map<String, List<Long>> bankBatchIdMap = new HashMap<String, List<Long>>();
            for (PaymentEntity needReqBankPayment : paymentEntitys) {
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
            paymentService.cache(paymentEntitys);//放到cache中，在后台调度时不用再到数据库取
            while (itr.hasNext()) {
                Entry<String, List<Long>> entry = itr.next();
                Long[] ids = entry.getValue().toArray(new Long[0]);
                params = new HashMap<String, Object>();
                params.put(PARAM_QPAYMENT_IDS, ids);
                params.put(BATCH_NUM, batchNum);
                TaskScheduler.scheduleTask(TransCode.REVERSE, context.getMainAccount(), context.getBankProfile().getBankName(), params, "q:" + model.getBatchSeqId() + "," + entry.getKey(), true);
            }
        }
        return resultModel;
    }

    @Override
    public CommonResultModel run(TaskParam taskParam) {
        PaymentResultModel resultModel = new PaymentResultModel();
        TransContext context = TransContext.getContext();
        PaymentEntity[] paymentEntitys = (PaymentEntity[]) taskParam.getParams().get(PARAM_RPAYMENT);
        AccountEntity accountEntity = taskParam.getAccountEntity();
        if (paymentEntitys == null) {
            Long[] ids = (Long[]) taskParam.getParams().get(PARAM_QPAYMENT_IDS);
            paymentEntitys = paymentService.getPaymentEntitys(ids);//再读一次对象，队列中只保存ID
        }
        Arrays.sort(paymentEntitys);//查询的顺序要与发送顺序相同，否则同一批次里 账号、金额都相同 的支付就没法区分(在没有明细号的情况下，如招行).
        doRefund(context, paymentEntitys, accountEntity);

        // 组装返回参数
        resultModel.setAppCode(accountEntity.getAccNo());
        resultModel.setBatchSeqId(paymentEntitys[0].getBatchSeqId());
        // 返回现有的结果，新的在后台查询
        PaymentOuterStatus.transmitStatusToResultModel(paymentEntitys, resultModel);
        return resultModel;
    }

    private void doRefund(TransContext context, PaymentEntity[] payments, AccountEntity accountEntity) {
        String payTransCode = payments[0].getPayTransCode();
        IPayment paymentImpl = BankTransComponentManager.getPaymentByTransCode(context.getBankProfile().getBankName(), payTransCode);
        Class<? extends IReversePayment> queryClass = paymentImpl.getReverseClass();
        IReversePayment reversePaymentImpl = (IReversePayment) BankTransComponentManager.getBankComponent(queryClass);

        try {
            reversePaymentImpl.reverse(payments);
        } catch (Throwable e) {
            String message = "冲正结果后更新数据库失败";
            logger.error(message, e);
        }

        //更新数据库状态
        try {
            paymentService.updateStatus(payments, true);
        } catch (Throwable e) {
            String message = "冲正结果后更新数据库失败";
            logger.error(message, e);
        }
    }
}
