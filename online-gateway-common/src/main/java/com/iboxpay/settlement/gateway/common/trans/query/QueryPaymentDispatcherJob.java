package com.iboxpay.settlement.gateway.common.trans.query;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.iboxpay.settlement.gateway.common.SystemManager;
import com.iboxpay.settlement.gateway.common.config.Property;
import com.iboxpay.settlement.gateway.common.dao.PaymentDao;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.schedule.AbstractSchedulerJob;
import com.iboxpay.settlement.gateway.common.trans.ITransListener;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.payment.PaymentDelegateService;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;
import com.iboxpay.settlement.gateway.common.web.BankTransController;

//查询交易状态
@Service
public class QueryPaymentDispatcherJob extends AbstractSchedulerJob {

    private static Logger logger = LoggerFactory.getLogger(QueryPaymentDispatcherJob.class);
    private final static Property isAutoSyncPay = new Property("isAutoSyncPay", "true", "【查询交易状态】是否开启自动查询交易状态（true:开启，false：关闭）").asConfig();
    private final static Property autoSyncPayTime = new Property("autoSyncPayTime", "180", "【查询交易状态】系统自动查询多长时间内（分钟）的交易状态").asConfig(); //默认三小时
    private final static Property syncPayTimeUnit = new Property("syncPayTimeUnit", "5", "【查询交易状态】默认时间频率查询一次交易结果（分钟）").asConfig();
    private final static Property syncPayBaseTimes = new Property("syncPayBaseTimes", "3", "【查询交易状态】多少次查询交易结果后仍未确定时，会倍数延长查询时间").asConfig();
    private final static Property syncAfterLastPay = new Property("syncAfterLastPay", "3", "【查询交易状态】多长时间（分钟）内都没有支付请求才可以发起查询").asConfig();
    private final static long minusMillisMultiple = 60 * 1000;
    //key:batchSeqId
    private final static ConcurrentHashMap<String, SubmittedBatchPayment> submittedBatchPaymentMap = new ConcurrentHashMap<String, SubmittedBatchPayment>();
    //key:accNo 用于记录账号最后一次的提交支付时间,只有一定时间内没有支付任务时才会发起查询状态
    private final static ConcurrentHashMap<String, Date> accSubmmitPayTimeMap = new ConcurrentHashMap<String, Date>();

    private static PaymentDao paymentDao;
    private static BankTransController bankTransController;

    final static ITransListener transListener = new ITransListener() {//交易监听器

                public void onBatchPaymentSubmitComplete(AccountEntity accountEntity, String batchSeqId, PaymentEntity[] paymentEntitys) {
                    logger.info("批次[" + batchSeqId + "]提交完毕");
                    if (submittedBatchPaymentMap.size() >= 50000) //避免任务禁用后内存溢出
                        submittedBatchPaymentMap.clear();
                    Date now = new Date();
                    SubmittedBatchPayment submittedBatchPayment = new SubmittedBatchPayment(accountEntity.getBankName(), accountEntity.getAccNo(), batchSeqId, now);
                    submittedBatchPayment.canQuery = true;
                    submittedBatchPayment.lastQueryTime = now;//第一次查询以这个时间以基准
                    submittedBatchPaymentMap.put(batchSeqId, submittedBatchPayment);
                    accSubmmitPayTimeMap.put(accountEntity.getAccNo(), now);
                }

                public void onPaymentQueryComplete(AccountEntity accountEntity, String batchSeqId, PaymentEntity[] paymentEntitys) {
                    logger.info("批次[" + batchSeqId + "]查询完毕");
                    SubmittedBatchPayment bankBatchPayment = submittedBatchPaymentMap.get(batchSeqId);
                    if (bankBatchPayment != null) {
                        bankBatchPayment.lastQueryTime = new Date();
                        bankBatchPayment.canQuery = true;
                    }
                }
            };

    static {
        PaymentDelegateService.getCompositelistener().addListener(transListener);
        QueryDelegateService.getCompositelistener().addListener(transListener);
    }

    private static void ensureInit() {
        if (paymentDao == null) {
            paymentDao = (PaymentDao) SystemManager.getSpringContext().getBean("paymentDao");
            bankTransController = (BankTransController) SystemManager.getSpringContext().getBean("bankTransController");
        }
    }

    @Override
    public String getJobGroup() {
        return "trans";
    }

    @Override
    public String getJobName() {
        return "query";
    }

    @Override
    public String getJobType() {
        return "query";
    }

    @Override
    public String getTitle() {
        return "查询交易状态定时任务";
    }

    //该批次是否需要查询：1.超过指定时间的不需要再查询，2.已经是最终状态的，不需要再查询
    private static boolean needQuery(String batchSeqId) {
        ensureInit();
        List result = paymentDao.findByHQL("select status,count(status) from PaymentEntity where batchSeqId=? group by status", batchSeqId);
        int finalStatusCount = 0, notFinalStatusCount = 0;
        if (result != null) {
            for (int i = 0; i < result.size(); i++) {
                Object[] statusStats = (Object[]) result.get(i);
                int status = (Integer) statusStats[0];
                long statusCount = (Long) statusStats[1];
                if (isFinalStatus(status))
                    finalStatusCount += statusCount;
                else notFinalStatusCount += statusCount;
            }
        }
        return notFinalStatusCount > 0;//还有未确定的
    }

    private static boolean isExpired(String batchSeqId) {
        SubmittedBatchPayment submittedBatchPayment = submittedBatchPaymentMap.get(batchSeqId);
        if (submittedBatchPayment == null) return true;
        int syncWithin = autoSyncPayTime.getIntVal();
        long deadline = submittedBatchPayment.submittedTime.getTime() + syncWithin * minusMillisMultiple;
        boolean expired = new Date().getTime() > deadline;//超过指定时间，不再同步了
        return expired;
    }

    private final static boolean isFinalStatus(int status) {
        return status == PaymentStatus.STATUS_SUCCESS || status == PaymentStatus.STATUS_FAIL || status == PaymentStatus.STATUS_CANCEL;
    }

    @Override
    public void execute(String params) throws JobExecutionException {
        dispatchQuery(params);
    }

    private synchronized static void dispatchQuery(String params) {
        ensureInit();
        Date now = new Date();
        int baseTimes = syncPayBaseTimes.getIntVal();
        baseTimes = baseTimes <= 0 ? 1 : baseTimes;
        Map map = null;
        if (!StringUtils.isBlank(params)) {
            try {
                map = (Map) JsonUtil.jsonToObject(params, "UTF-8", Map.class);
            } catch (Exception e) {
                logger.warn("读取定时参数异常", e);
            }
        }
        int syncAfterLastPayTime = syncAfterLastPay.getIntVal();
        Iterator<SubmittedBatchPayment> itr = submittedBatchPaymentMap.values().iterator();
        while (itr.hasNext()) {
            SubmittedBatchPayment submittedBatchPayment = itr.next();
            try {
                if (isExpired(submittedBatchPayment.batchSeqId)) {
                    logger.info("批次[" + submittedBatchPayment.batchSeqId + "]超过指定同步时间未全部返回最终结果，不再同步状态");
                    submittedBatchPaymentMap.remove(submittedBatchPayment.batchSeqId);//不需要再对了
                    continue;
                }
                if (!submittedBatchPayment.canQuery) //还在排队或者未查询完毕
                    continue;
//                Date accSubmitTime = accSubmmitPayTimeMap.get(submittedBatchPayment.appCode);
//                if (now.getTime() - accSubmitTime.getTime() < syncAfterLastPayTime * minusMillisMultiple) {//x分钟内没有支付请求才可以发起查询
//                    continue;
//                }
                Integer time = null;
                if (map != null) {
                    time = (Integer) map.get(submittedBatchPayment.bankName);
                    if (time != null && time <= 0) {//配置了 负数或者0 ，表示该银行的交易状态不再自动同步
                        continue;
                    }
                }
                if (time == null) time = syncPayTimeUnit.getIntVal();
                if (time <= 0) time = 5;

                int multiple = (submittedBatchPayment.times < baseTimes ? 1 : submittedBatchPayment.times - baseTimes);
                multiple = multiple <= 0 ? 1 : multiple;
                long interval = multiple * time * minusMillisMultiple;
                if (interval > 30 * minusMillisMultiple) //最长30分钟同步一次
                    interval = 30 * minusMillisMultiple;

                if (submittedBatchPayment.times != 0 && //第一次为提交后3分钟
                        now.getTime() - submittedBatchPayment.lastQueryTime.getTime() < interval) //距离上次同步时间判断
                    continue;
                if (!needQuery(submittedBatchPayment.batchSeqId)) {
                    logger.info("批次[" + submittedBatchPayment.batchSeqId + "]已经全部确认状态，不再同步状态");
                    submittedBatchPaymentMap.remove(submittedBatchPayment.batchSeqId);//不需要再对了
                    continue;
                }
                if (!Boolean.valueOf(isAutoSyncPay.getVal())) {
                    continue;
                }
                submittedBatchPayment.times++;
                submittedBatchPayment.canQuery = false;

                bankTransController.trans(TransCode.QUERY.getCode(), "{\"appCode\":\"" + submittedBatchPayment.appCode + "\", \"batchSeqId\":\"" + submittedBatchPayment.batchSeqId + "\"}", null);
            } catch (Exception e) {
                logger.warn("", e);
            }
        }
    }

    static class SubmittedBatchPayment {

        public final String appCode;
        public final String bankName;
        public final String batchSeqId;
        public final Date submittedTime;
        public int times;//同步次数
        public volatile Date lastQueryTime;
        public volatile boolean canQuery;

        public SubmittedBatchPayment(String bankName, String appCode, String batchSeqId, Date submittedTime) {
            this.bankName = bankName;
            this.appCode = appCode;
            this.batchSeqId = batchSeqId;
            this.submittedTime = submittedTime;
        }
    }

    public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
        Map map = (Map) JsonUtil.jsonToObject("{\"ccb\":5, \"icbc\": 0}", "UTF-8", Map.class);
        System.out.println((Integer) map.get("dd"));
        System.out.println((Integer) map.get("ccb"));
        System.out.println(map.get("icbc"));
    }

    @Override
    public boolean isUniqueConfig() {
        return true;
    }

    @Override
    public String getConfigDesc() {
        return "本定时任务（根据“定时表达式”）会扫描有哪些批次需要查询状态的（提交后未确定的），扫描时再根据具体某个银行配置的时间间隔（分钟）进行同步，" + "配置格式为JSON，如：{\"ccb\":5, \"icbc\":10, \"abc\":0}。其中，值小于等于0的表示不会执行自动同步。（注意：具体执行时有一定的时间偏差）";
    }
}
