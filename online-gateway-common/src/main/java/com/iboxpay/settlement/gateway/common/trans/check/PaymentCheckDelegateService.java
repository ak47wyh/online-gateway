package com.iboxpay.settlement.gateway.common.trans.check;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.config.Property;
import com.iboxpay.settlement.gateway.common.dao.CommonSessionFactory;
import com.iboxpay.settlement.gateway.common.dao.DetailDao;
import com.iboxpay.settlement.gateway.common.dao.PaymentCheckRecordDao;
import com.iboxpay.settlement.gateway.common.dao.PaymentCheckResultDao;
import com.iboxpay.settlement.gateway.common.domain.DetailQueryRecordEntity;
import com.iboxpay.settlement.gateway.common.domain.PaymentCheckRecordEntity;
import com.iboxpay.settlement.gateway.common.domain.PaymentCheckResultEntity;
import com.iboxpay.settlement.gateway.common.inout.CommonRequestModel;
import com.iboxpay.settlement.gateway.common.inout.CommonResultModel;
import com.iboxpay.settlement.gateway.common.inout.check.CheckModelInfo;
import com.iboxpay.settlement.gateway.common.inout.check.CheckRequestModel;
import com.iboxpay.settlement.gateway.common.inout.check.CheckResultModel;
import com.iboxpay.settlement.gateway.common.inout.payment.PaymentOuterStatus;
import com.iboxpay.settlement.gateway.common.page.PageBean;
import com.iboxpay.settlement.gateway.common.task.RunnableTask;
import com.iboxpay.settlement.gateway.common.task.TaskParam;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;
import com.iboxpay.settlement.gateway.common.trans.ErrorCode;
import com.iboxpay.settlement.gateway.common.trans.IBankTrans;
import com.iboxpay.settlement.gateway.common.trans.ITransDelegate;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

/**
 * 对账入口
 * @author jianbo_chen
 */
@Service
public class PaymentCheckDelegateService implements ITransDelegate, RunnableTask {

    private static Logger logger = LoggerFactory.getLogger(PaymentCheckDelegateService.class);
    private final static int MAX_PAGESIZE = 3000;
    private final static int DEFAULT_PAGESIZE = 500;
    private final static String DATE_PATTERN = "yyyy-MM-dd";
    private static IPaymentChecker DEFAULT_PAYMENT_CHECKER;
    public final static Property TIME_DIFFERENCE = new Property("timeDifference", "15", "对账时允许银行返回交易时间与本地交易时间差异（分钟）");

    @Resource
    private DetailDao detailDao;
    @Resource
    private PaymentCheckResultDao paymentCheckResultDao;
    @Resource
    private PaymentCheckRecordDao paymentCheckRecordDao;

    @PostConstruct
    void postInit() {
        DEFAULT_PAYMENT_CHECKER = new DefaultPaymentChecker();
    }

    @Override
    public TransCode getTransCode() {
        return TransCode.CHECK;
    }

    @Override
    public CommonRequestModel parseInput(String input) throws Exception {
        CheckRequestModel requestModel = (CheckRequestModel) JsonUtil.jsonToObject(input, "UTF-8", CheckRequestModel.class);
        return requestModel;
    }

    @Override
    public CommonResultModel trans(TransContext context, CommonRequestModel requestModel) {
        String accNo = requestModel.getAppCode();
        CheckRequestModel checkRqModel = (CheckRequestModel) requestModel;
        IBankTrans[] checkInstances = BankTransComponentManager.getBankComponent(context.getMainAccount(), context.getBankProfile().getBankName(), IPaymentChecker.class);
        IPaymentChecker paymentChecker;
        if (checkInstances == null || checkInstances.length == 0)
            paymentChecker = DEFAULT_PAYMENT_CHECKER;
        else if (checkInstances.length == 1)
            paymentChecker = (IPaymentChecker) checkInstances[0];
        else {
            String message = "【代码错误】有多于1个对账实现类.";
            logger.warn(message);
            return new CheckResultModel().setAppCode(accNo).fail(ErrorCode.sys_not_support, message);
        }

        if (checkRqModel.getPageSize() > MAX_PAGESIZE) {
            return new CheckResultModel().setAppCode(accNo).fail(ErrorCode.input_error, "一页记录数不能大于" + MAX_PAGESIZE);
        }
        if (checkRqModel.getPageSize() == 0) checkRqModel.setPageSize(DEFAULT_PAGESIZE);
        if (StringUtils.isBlank(checkRqModel.getTransDate())) {
            return new CheckResultModel().setAppCode(accNo).fail(ErrorCode.input_error, "请填写要对账的“交易日期(transDate)”");
        }
        Date transDate;
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        try {
            transDate = sdf.parse(checkRqModel.getTransDate());
        } catch (ParseException e) {
            return new CheckResultModel().setAppCode(accNo).fail(ErrorCode.input_error, "“交易日期(transDate)”格式错误，格式要求：" + DATE_PATTERN);
        }
        boolean needCheck = false;
        PaymentCheckRecordEntity checkRecord = paymentCheckRecordDao.getCheckRecord(accNo, transDate);
        if (checkRecord != null) {//已经对账过
            DetailQueryRecordEntity[] queryRecords = detailDao.getAccountQueryRecords(accNo, transDate, transDate);
            if (queryRecords != null && queryRecords.length != 0) {
                if (queryRecords[0].getUpdateTime().getTime() > checkRecord.getUpdateTime().getTime()) {
                    needCheck = true;
                }
            }
        } else {
            needCheck = true;
        }
        String checkStatusMsg = null;
        if (needCheck) {
            Session session = CommonSessionFactory.getHibernateSession();
            try {
                List<PaymentCheckResultEntity> checkResults = null;
                try {
                    checkResults = paymentChecker.check(accNo, transDate, session, paymentCheckResultDao, paymentCheckRecordDao);
                } catch (Exception e) {
                    logger.error("执行对账异常:" + e.getMessage(), e);
                    checkStatusMsg = e.getMessage();
                }
                if (checkResults != null && checkResults.size() > 0) {
                    paymentCheckResultDao.save(accNo, transDate, checkResults);
                    paymentCheckRecordDao.setCheck(accNo, transDate);// 两个是否同事务还没那么重要
                }
            } finally {
                if (session.isOpen()) session.close();
            }
        }

        PageBean pageBean =
                paymentCheckResultDao.queryCheckResults(accNo, StringUtils.trim(checkRqModel.getCustomerAccNo()), StringUtils.trim(checkRqModel.getCustomerAccName()), checkRqModel.getBeginAmount(),
                        checkRqModel.getEndAmount(), transDate, checkRqModel.getHasCheck(), checkRqModel.getStatus() <= 0 ? -1 : checkRqModel.getStatus(), checkRqModel.getPageSize(),
                        checkRqModel.getPageNo());

        CheckResultModel checkResultModel = wrapOutboundCheckResult(accNo, transDate, sdf, pageBean);
        DetailQueryRecordEntity[] queryRecords = detailDao.getAccountQueryRecords(accNo, transDate, transDate);
        if (queryRecords != null && queryRecords.length > 0) {
            checkResultModel.setDetailUpdateTime(DateTimeUtil.format(queryRecords[0].getUpdateTime(), "yyyy-MM-dd HH:mm:ss"));
        }
        checkResultModel.setErrorMsg(checkStatusMsg);
        return checkResultModel;
    }

    private CheckResultModel wrapOutboundCheckResult(String accNo, Date transDate, SimpleDateFormat sdf, PageBean pageBean) {
        SimpleDateFormat timeSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Map<String, Object>> resultList = pageBean.getResult();
        CheckResultModel checkResultModel = new CheckResultModel();
        if (resultList != null) {
            CheckModelInfo checkModelInfos[] = new CheckModelInfo[resultList.size()];
            for (int i = 0; i < resultList.size(); i++) {
                Map<String, Object> checkResult = resultList.get(i);
                checkModelInfos[i] = new CheckModelInfo();
                checkModelInfos[i].setBatchSeqId((String) checkResult.get("batchSeqId"));
                checkModelInfos[i].setSeqId((String) checkResult.get("seqId"));
                checkModelInfos[i].setPaymentId((Long) checkResult.get("paymentId"));
                checkModelInfos[i].setAccNo((String) checkResult.get("accNo"));
                checkModelInfos[i].setAmount((BigDecimal) checkResult.get("amount"));
                Date transTime = (Date) checkResult.get("transTime");
                if (transTime != null) checkModelInfos[i].setTransTime(timeSdf.format(transTime));
                checkModelInfos[i].setCustomerAccNo((String) checkResult.get("customerAccNo"));
                checkModelInfos[i].setCustomerAccName((String) checkResult.get("customerAccName"));
                checkModelInfos[i].setCustomerBankFullName((String) checkResult.get("customerBankFullName"));
                Long detailId = (Long) checkResult.get("detailId");
                checkModelInfos[i].setDetailId(detailId == null ? 0 : detailId.longValue());
                checkModelInfos[i].setStatus((Integer) checkResult.get("status"));
                checkModelInfos[i].setStatusMsg(getStatusMsg(checkResult));
                Integer checkStatus = (Integer) checkResult.get("checkStatus");
                checkModelInfos[i].setCheckStatus(checkStatus == null ? 0 : checkStatus.intValue());
                checkModelInfos[i].setCheckStatusMsg((String) checkResult.get("checkStatusMsg"));
                Date checkCreateTime = (Date) checkResult.get("createTime");
                if (checkCreateTime != null) checkModelInfos[i].setCreateTime(timeSdf.format(checkCreateTime));
                Date checkupdateTime = (Date) checkResult.get("updateTime");
                if (checkupdateTime != null) checkModelInfos[i].setUpdateTime(timeSdf.format(checkupdateTime));
            }
            checkResultModel.setCheckModelInfos(checkModelInfos);
        }
        checkResultModel.setAppCode(accNo);
        checkResultModel.setStatus(CommonResultModel.STATUS_SUCCESS);
        checkResultModel.setPageNo(pageBean.getPageNo());
        checkResultModel.setPageSize(pageBean.getPageSize());
        checkResultModel.setTotalCount((int) pageBean.getTotalCount());
        checkResultModel.setTotalPages(pageBean.getTotalPages());
        checkResultModel.setTransDate(sdf.format(transDate));
        return checkResultModel;
    }

    private String getStatusMsg(Map<String, Object> checkResult) {
        String payStatusMsg = PaymentOuterStatus.getStatusInfo((Integer) checkResult.get("status"), (String) checkResult.get("statusMsg"), null)[1];

        String payBankStatus = (String) checkResult.get("payBankStatus");
        String payBankStatusMsg = (String) checkResult.get("payBankStatusMsg");
        String bankStatus = (String) checkResult.get("bankStatus");
        String bankStatusMsg = (String) checkResult.get("bankStatusMsg");
        StringBuilder message = new StringBuilder();
        message.append(payStatusMsg);
        StringBuilder bankMsg = new StringBuilder();
        if (!StringUtils.isBlank(payBankStatus) || !StringUtils.isBlank(payBankStatusMsg)) {
            bankMsg.append("[付]");
            bankMsg.append(payBankStatus);
            if (!StringUtils.isBlank(payBankStatusMsg)) bankMsg.append("(").append(payBankStatusMsg).append(")");
        }

        if ((bankStatus != null && !bankStatus.equals(payBankStatus)) || (bankStatusMsg != null && !bankStatusMsg.equals(payBankStatusMsg))) {//重复的为未查询过.
            bankMsg.append(bankMsg.length() > 0 ? "; " : "").append("[查]");
            bankMsg.append(bankStatus);
            if (!StringUtils.isBlank(bankStatusMsg)) bankMsg.append("(").append(bankStatusMsg).append(")");
        }
        if (bankMsg.length() > 0) message.append(".银行返回:").append(bankMsg);
        String reservedMsg = message.toString();
        if (reservedMsg.length() > 80) {//varchar2(255) 当作UTF-8
            reservedMsg = reservedMsg.substring(0, 79) + "…";
        }
        return reservedMsg;
    }

    @Override
    public CommonResultModel run(TaskParam taskParam) {
        return null;
    }

}
