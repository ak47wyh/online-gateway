package com.iboxpay.settlement.gateway.common.trans.detail;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.config.Property;
import com.iboxpay.settlement.gateway.common.dao.DetailDao;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.DetailEntity;
import com.iboxpay.settlement.gateway.common.domain.DetailQueryRecordEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.exception.FrontEndException;
import com.iboxpay.settlement.gateway.common.inout.CommonRequestModel;
import com.iboxpay.settlement.gateway.common.inout.CommonResultModel;
import com.iboxpay.settlement.gateway.common.inout.detail.DetailModelInfo;
import com.iboxpay.settlement.gateway.common.inout.detail.DetailRequestModel;
import com.iboxpay.settlement.gateway.common.inout.detail.DetailResultModel;
import com.iboxpay.settlement.gateway.common.page.PageBean;
import com.iboxpay.settlement.gateway.common.task.RunnableTask;
import com.iboxpay.settlement.gateway.common.task.TaskParam;
import com.iboxpay.settlement.gateway.common.task.TaskScheduler;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;
import com.iboxpay.settlement.gateway.common.trans.ErrorCode;
import com.iboxpay.settlement.gateway.common.trans.IBankTrans;
import com.iboxpay.settlement.gateway.common.trans.IBankTransInterceptor;
import com.iboxpay.settlement.gateway.common.trans.ITransDelegate;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

/**
 * 交易明细查询业务流程处理
 * @author jianbo_chen
 */
@Service
public class DetailDelegateService implements ITransDelegate, RunnableTask {

    private static Logger logger = LoggerFactory.getLogger(DetailDelegateService.class);
    private final static Property todayDetCacheTime = new Property("todayDetCacheTime", "15", "当日交易明细多长时间(分钟)不需要到银行取最新数据.").asConfig();
    private final static int MAX_PAGESIZE = 3000;
    private final static int DEFAULT_PAGESIZE = 500;
    private final static String DATE_PATTERN = "yyyy-MM-dd";
    private final static long TIMEMILLIS_OF_DAY = 24 * 60 * 60 * 1000;
    private final static int MAX_DAY_SPAN = 30;//最多只能时间跨度为一个月
    private final static long MAX_TIMEMILLIS_SPAN = TIMEMILLIS_OF_DAY * MAX_DAY_SPAN;//最多只能时间跨度为一个月

    private final static String PARAM_TODAY = "today";
    private final static String PARAM_BEGINDATE = "beginDate";
    private final static String PARAM_ENGDATE = "engDate";
    

    @Resource
    DetailDao detailDao;

    @Override
    public TransCode getTransCode() {
        return TransCode.DETAIL;
    }

    @Override
    public CommonRequestModel parseInput(String input) throws Exception {
        DetailRequestModel requestModel = (DetailRequestModel) JsonUtil.jsonToObject(input, "UTF-8", DetailRequestModel.class);
        return requestModel;
    }

    @Override
    public CommonResultModel trans(TransContext context, CommonRequestModel requestModel) {
        String accNo = requestModel.getAppCode();
        DetailRequestModel detailModel = (DetailRequestModel) requestModel;
        IBankTrans[] detailInstances = BankTransComponentManager.getBankComponent(context.getMainAccount(), context.getBankProfile().getBankName(), IDetail.class);
        if (detailInstances.length != 1) {
            String message = detailInstances.length > 1 ? "【代码错误】有多于1个交易明细查询实现类." : "未实现交易明细查询";
            logger.warn(message);
            return new DetailResultModel().setAppCode(accNo).fail(ErrorCode.sys_not_support, message);
        }
        if (detailModel.getPageSize() > MAX_PAGESIZE) {
            return new DetailResultModel().setAppCode(accNo).fail(ErrorCode.input_error, "一页记录数不能大于" + MAX_PAGESIZE);
        }
        if (detailModel.getPageSize() == 0) detailModel.setPageSize(DEFAULT_PAGESIZE);

        if (StringUtils.isBlank(detailModel.getBeginDate()) || StringUtils.isBlank(detailModel.getEndDate())) {
            return new DetailResultModel().setAppCode(accNo).fail(ErrorCode.input_error, "缺少查询参数“起始日期(beginDate)”或“结束日期(endDate)”");
        }
        Date beginDate, endDate, reqBeginDate, reqEndDate;
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        try {
            reqBeginDate = beginDate = sdf.parse(detailModel.getBeginDate());
        } catch (ParseException e) {
            return new DetailResultModel().setAppCode(accNo).fail(ErrorCode.input_error, "查询的“起始日期(beginDate)”格式错误，格式要求：" + DATE_PATTERN);
        }
        try {
            reqEndDate = endDate = sdf.parse(detailModel.getEndDate());
        } catch (ParseException e) {
            return new DetailResultModel().setAppCode(accNo).fail(ErrorCode.input_error, "查询的“结束日期(endDate)”格式错误，格式要求：" + DATE_PATTERN);
        }
    	
        long queryTimeSpan = endDate.getTime() - beginDate.getTime();
        if (queryTimeSpan > MAX_TIMEMILLIS_SPAN) {
            return new DetailResultModel().setAppCode(accNo).fail(ErrorCode.input_error, "查询的时间跨度不允许大于" + MAX_DAY_SPAN + "天");
        }
        Date now = new Date();
        try {
            now = sdf.parse(sdf.format(now));
        } catch (ParseException e) {}
        if (endDate.getTime() > now.getTime()) {
            return new DetailResultModel().setAppCode(accNo).fail(ErrorCode.input_error, "查询的“结束日期(endDate)”不能大于今天.");
        }
        IDetail detailImpl = (IDetail) detailInstances[0];
        int hisDaysSpan = detailImpl.supportQueryHisDaysSpan();
        if (hisDaysSpan <= 0) {
            return new DetailResultModel().setAppCode(accNo).fail(ErrorCode.sys_internal_err, "开发实现错误： 天数跨度设置有误（小于等于0）.");
        }
        //当日明细请求
        if (endDate.getTime() == now.getTime()) {
            int cacheTime;
            try {
                cacheTime = Integer.parseInt(todayDetCacheTime.getVal());
            } catch (Exception e) {
                cacheTime = 15;
            }
            DetailQueryRecordEntity todayQueryRecord = detailDao.getTodayQueryRecord(accNo);
            //当日明细不要查询太频繁，通过时间间隔控制
            if (todayQueryRecord == null || (todayQueryRecord != null && new Date().getTime() - todayQueryRecord.getUpdateTime().getTime() > cacheTime * 60 * 1000)) {
                if (detailImpl.isTodayDetailIndependent()) {//
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put(PARAM_TODAY, Boolean.TRUE);
                    TaskScheduler.scheduleTask(TransCode.DETAIL, context.getMainAccount(), context.getBankProfile().getBankName(), data, "d:" + accNo + ":" + sdf.format(now),//互斥体
                            true);
                } else {
                    Map<String, Object> data = new HashMap<String, Object>();
                    data.put(PARAM_BEGINDATE, endDate);
                    data.put(PARAM_ENGDATE, endDate);
                    TaskScheduler.scheduleTask(TransCode.DETAIL, context.getMainAccount(), context.getBankProfile().getBankName(), data,
                            "d:" + accNo + ":" + sdf.format(endDate) + "," + sdf.format(endDate),//互斥体
                            true);
                }
            }
            endDate = DateTimeUtil.addDay(endDate, -1);
        }
        //需要发起请求的日期碎片
        List<Date[]> needReqQueryDates = null;
        if (detailModel.isForceUpdate()) {
            needReqQueryDates = new LinkedList<Date[]>();
            needReqQueryDates.add(new Date[] { beginDate, endDate });
        } else if (beginDate.getTime() <= endDate.getTime()) {
            needReqQueryDates = detailDao.determineActualQueryDate(accNo, beginDate, endDate);
        }

        if (needReqQueryDates != null) for (Date[] needReqQueryDate : needReqQueryDates) {
            //历史明细请求
            Date bankEndDate = needReqQueryDate[1], needReqBeginDate = needReqQueryDate[0], //这个时间区间的结束
            bankBeginDate;
            while (bankEndDate.getTime() >= needReqBeginDate.getTime()) {
                Map<String, Object> data = new HashMap<String, Object>();
                //				data.put(REQ_BEGINDATE, reqBeginDate);
                //				data.put(REQ_ENGDATE, reqEndDate);
                bankBeginDate = DateTimeUtil.addDay(bankEndDate, -(hisDaysSpan - 1));
                Date realBeginDate;
                if (bankBeginDate.getTime() < needReqBeginDate.getTime()) {//时间不够一个区间（可能开始与结束是同一天）
                    realBeginDate = needReqBeginDate;
                } else {
                    realBeginDate = bankBeginDate;
                }
                data.put(PARAM_BEGINDATE, realBeginDate);
                data.put(PARAM_ENGDATE, bankEndDate);
                TaskScheduler.scheduleTask(TransCode.DETAIL, context.getMainAccount(), context.getBankProfile().getBankName(), data,
                        "d:" + accNo + ":" + sdf.format(realBeginDate) + "," + sdf.format(bankEndDate),//互斥体
                        true);
                bankEndDate = DateTimeUtil.addDay(bankBeginDate, -1);//下一次请求不能再包括当前的开始日期
            }
        }
        PageBean pageBean =
                detailDao.queryDetails(accNo, StringUtils.trim(detailModel.getCustomerAccNo()), StringUtils.trim(detailModel.getCustomerAccName()), detailModel.getBeginAmount(),
                        detailModel.getEndAmount(), detailModel.isQueryCredit(), reqBeginDate, reqEndDate, detailModel.getPageSize(), detailModel.getPageNo());

        DetailResultModel detailResultModel = wrapOutboundDetail(detailModel, pageBean);
        if (reqBeginDate.getTime() == reqEndDate.getTime()) {//只查一天的明细才返回明细更新时间
            DetailQueryRecordEntity[] queryRecords = detailDao.getAccountQueryRecords(accNo, reqBeginDate, reqEndDate);
            if (queryRecords != null && queryRecords.length > 0) {
                detailResultModel.setDetailUpdateTime(DateTimeUtil.format(queryRecords[0].getUpdateTime(), "yyyy-MM-dd HH:mm:ss"));
            }
        }
        return detailResultModel;
    }

    //封装返回结果
    private DetailResultModel wrapOutboundDetail(DetailRequestModel detailModel, PageBean pageBean) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<DetailEntity> resultList = pageBean.getResult();
        DetailModelInfo detailInfos[] = new DetailModelInfo[resultList.size()];
        for (int i = 0; i < resultList.size(); i++) {
            DetailEntity detailEntity = resultList.get(i);
            DetailModelInfo detailModelInfo = new DetailModelInfo();
            detailModelInfo.setId(detailEntity.getId());// long
            detailModelInfo.setAccNo(detailEntity.getAccNo());
            detailModelInfo.setDebitAmount(detailEntity.getDebitAmount());
            detailModelInfo.setCreditAmount(detailEntity.getCreditAmount());
            detailModelInfo.setCurrency(detailEntity.getCurrency());
            detailModelInfo.setTransDate(sdf.format(detailEntity.getTransDate()));
            detailModelInfo.setRemark(detailEntity.getRemark());
            detailModelInfo.setUseCode(detailEntity.getUseCode());
            detailModelInfo.setUseDesc(detailEntity.getUseDesc());
            detailModelInfo.setBalance(detailEntity.getBalance());
            detailModelInfo.setCustomerAccNo(detailEntity.getCustomerAccNo());
            detailModelInfo.setCustomerAccName(detailEntity.getCustomerAccName());
            detailModelInfo.setCustomerBankFullName(detailEntity.getCustomerBankFullName());
            detailModelInfo.setReserved(detailEntity.getReserved());
            detailModelInfo.setBankBatchSeqId(detailEntity.getBankBatchSeqId());
            detailModelInfo.setCreateTime(sdf.format(detailEntity.getCreateTime()));
            detailModelInfo.setUpdateTime(sdf.format(detailEntity.getUpdateTime()));
            detailInfos[i] = detailModelInfo;
        }
        DetailResultModel detailResultModel = new DetailResultModel();
        detailResultModel.setStatus(CommonResultModel.STATUS_SUCCESS);
        detailResultModel.setBeginDate(detailModel.getBeginDate());
        detailResultModel.setEndDate(detailModel.getEndDate());
        detailResultModel.setAppCode(detailModel.getAppCode());
        detailResultModel.setPageNo(detailModel.getPageNo());
        detailResultModel.setPageSize(detailModel.getPageSize());
        detailResultModel.setTotalCount((int) pageBean.getTotalCount());
        detailResultModel.setTotalPages(pageBean.getTotalPages());
        detailResultModel.setDetailModelInfos(detailInfos);
        return detailResultModel;
    }

    @Override
    public CommonResultModel run(TaskParam taskParam) {
        TransContext context = TransContext.getContext();
        AccountEntity accountEntity = context.getMainAccount();
        IBankTrans[] detailInstances = BankTransComponentManager.getBankComponent(accountEntity, context.getBankProfile().getBankName(), IDetail.class);
        IDetail detailImpl = (IDetail) detailInstances[0];

        if (detailImpl instanceof IBankTransInterceptor) {
            try {
                ((IBankTransInterceptor) detailImpl).beforeTrans(null);
            } catch (Exception e) {
                logger.error("查询交易明细前处理异常", e);
                return new DetailResultModel().fail(ErrorCode.getErrorCodeByException(e), e.getMessage());
            }
        }
        DetailResult detailResult;
        Map<String, Object> pageInfoMap = new HashMap<String, Object>();
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        Boolean isToday = (Boolean) taskParam.getParams().get(PARAM_TODAY);
        Date beginDate = null, endDate = null;
        Date today = null;
        Exception exception;
        int globalIndex = 0;//某个时间范围内的索引
        int pageIndex = 1;
        int retryCount;
        
        try {
            today = sdf.parse(sdf.format(now));
        } catch (ParseException e) {}
        do {
            exception = null;
            detailResult = null;
            retryCount = 2;
            while (true) {
                try {
                    if (isToday != null && isToday.booleanValue()) {
                        //当日明细的起始和结束时间都是今天
                        beginDate = endDate = today;
                        detailResult = detailImpl.queryTodayDetail(accountEntity, pageIndex, pageInfoMap);
                    } else {
                        beginDate = (Date) taskParam.getParams().get(PARAM_BEGINDATE);
                        endDate = (Date) taskParam.getParams().get(PARAM_ENGDATE);
                        detailResult = detailImpl.queryHisDetail(accountEntity, beginDate, endDate, pageIndex, pageInfoMap);
                    }
                    break;
                } catch (BaseTransException e) {
                    exception = e;
                    logger.warn("查询" + (beginDate.getTime() == endDate.getTime() ? "当日" : "历史") + "明细异常：(accNo=" + accountEntity.getAccNo() + ", beginDate=" + sdf.format(beginDate) + ", endDate="
                            + sdf.format(endDate) + ", pageIndex=" + pageIndex + ")", e);
                    if (e instanceof FrontEndException) {
                        if (--retryCount <= 0)
                            break;
                        else logger.info("查询明细出现网络异常，将重试查询...");
                    } else {
                        break;
                    }
                }
            }
            if (exception != null) break;
            //移除该条件判断:detailResult.getDetailEntitys().length == 0
            if (detailResult == null || detailResult.getDetailEntitys() == null) break;

            globalIndex = saveDetails(detailResult.getDetailEntitys(), accountEntity, beginDate, endDate, globalIndex);
            pageInfoMap = detailResult.getParams();//参数传递
            logger.info("是否有下一页(beginDate=" + DateTimeUtil.format(beginDate, "yyyy-MM-dd") + ", endDate=" + DateTimeUtil.format(endDate, "yyyy-MM-dd") + ")："
                    + (detailResult.isHasNextPage() ? "是" : "否") + "(当前第" + pageIndex + "页)");
            pageIndex++;
        } while (detailResult.isHasNextPage());

        if (exception == null) {
            if (today.getTime() == beginDate.getTime() && today.getTime() == endDate.getTime()) {//只查当日明细
                detailDao.updateQueryRecords(accountEntity.getAccNo(), beginDate, endDate, DetailQueryRecordEntity.TYPE_TODAY);
            } else if (today.getTime() == endDate.getTime()) {//查询包括当日明细
                Date recordEndDate = DateTimeUtil.addDay(endDate, -1);
                detailDao.updateQueryRecords(accountEntity.getAccNo(), endDate, endDate, DetailQueryRecordEntity.TYPE_TODAY);
                detailDao.updateQueryRecords(accountEntity.getAccNo(), beginDate, recordEndDate, DetailQueryRecordEntity.TYPE_HISTORY);
            } else {//只查历史明细
                detailDao.updateQueryRecords(accountEntity.getAccNo(), beginDate, endDate, DetailQueryRecordEntity.TYPE_HISTORY);
            }
        }

        if (detailImpl instanceof IBankTransInterceptor) {
            try {
                ((IBankTransInterceptor) detailImpl).afterTrans(null);
            } catch (Exception e) {
                logger.warn("查询交易明细后处理异常", e);
            }
        }
        return null;//一般异步的，无需返回结果
    }

    private int saveDetails(DetailEntity[] detailEntitys, AccountEntity accountEntity, Date beginDate, Date endDate, int globalIndex) {
        if (detailEntitys == null || detailEntitys.length == 0) return globalIndex;

        BigDecimal defZero = new BigDecimal("0");
        int filterLeftLen = detailEntitys.length;
        for (int i = 0; i < detailEntitys.length; i++) {
            if (detailEntitys[i].getTransDate() == null) throw new RuntimeException("交易明细的交易日期字段为空:" + detailEntitys[i]);

            if (detailEntitys[i].getDebitAmount() == null && detailEntitys[i].getCreditAmount() == null) throw new RuntimeException("交易明细的借贷金额均为空：" + detailEntitys[i]);

            Date transDay = DateTimeUtil.truncateTime(detailEntitys[i].getTransDate());
            if (transDay.getTime() > endDate.getTime() || transDay.getTime() < beginDate.getTime()) {
                logger.info((beginDate.getTime() == endDate.getTime() ? "[当日明细]" : "[历史明细]")//过滤因银行数据错误，或者日切问题导致的查询不正常
                        + "银行返回交易明细日期有误：" + detailEntitys[i] + "(beginDate=" + DateTimeUtil.format(beginDate, DATE_PATTERN) + ", endDate=" + DateTimeUtil.format(endDate, DATE_PATTERN) + ")");
                detailEntitys[i] = null;
                filterLeftLen--;
                continue;
            }
            if (detailEntitys[i].getDebitAmount() == null) detailEntitys[i].setDebitAmount(defZero);

            if (detailEntitys[i].getCreditAmount() == null) detailEntitys[i].setCreditAmount(defZero);

            if (StringUtils.isBlank(detailEntitys[i].getCustomerAccNo())) detailEntitys[i].setCustomerAccNo("*");//数据库的空值要用is null查询，不好弄.

            detailEntitys[i].setAccNo(accountEntity.getAccNo());
            detailEntitys[i].setAccName(accountEntity.getAccName());
            detailEntitys[i].setBankName(accountEntity.getBankName());
            detailEntitys[i].setOrderIndex(globalIndex++);//设置顺序号
        }
        if (filterLeftLen != detailEntitys.length) {
            if (filterLeftLen > 0) {
                DetailEntity[] _detailEntitys = new DetailEntity[filterLeftLen];
                int index = 0;
                for (int i = 0; i < detailEntitys.length; i++) {
                    if (detailEntitys[i] == null) continue;
                    _detailEntitys[index] = detailEntitys[i];
                }
                detailEntitys = _detailEntitys;
            } else {
                detailEntitys = new DetailEntity[0];
            }
        }
        detailDao.saveDetails(detailEntitys, accountEntity.getAccNo(), beginDate, endDate);
        return globalIndex;
    }
}
