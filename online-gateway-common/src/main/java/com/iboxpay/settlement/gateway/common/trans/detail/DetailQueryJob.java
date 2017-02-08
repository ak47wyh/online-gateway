package com.iboxpay.settlement.gateway.common.trans.detail;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.SystemManager;
import com.iboxpay.settlement.gateway.common.dao.AccountDao;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.schedule.AbstractSchedulerJob;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;
import com.iboxpay.settlement.gateway.common.util.Weekday;
import com.iboxpay.settlement.gateway.common.web.BankTransController;

@Service
public class DetailQueryJob extends AbstractSchedulerJob {

    private static Logger logger = LoggerFactory.getLogger(DetailQueryJob.class);

    @Override
    public void execute(String params) throws JobExecutionException {
        Map map;
        if (StringUtils.isBlank(params)) return;
        try {
            map = (Map) JsonUtil.jsonToObject(params, "UTF-8", Map.class);
        } catch (Exception e) {
            logger.warn("读取定时参数异常", e);
            return;
        }
        List<String> accNoList = new LinkedList<String>();
        String accNo = (String) map.get("accNo");
        String bankName = (String) map.get("bankName");
        if (!StringUtils.isBlank(accNo))
            accNoList.add(accNo);
        else if (!StringUtils.isBlank(bankName)) {
            AccountDao accountDao = (AccountDao) SystemManager.getSpringContext().getBean("accountDao");
            List<AccountEntity> accList = accountDao.find(" where bankName = ? ", bankName);
            for (AccountEntity accountEntity : accList) {
                accNoList.add(accountEntity.getAccNo());
            }
        }
        Date today = DateTimeUtil.truncateTime(new Date());
        Weekday weekday = new Weekday(today);
        Date previousWeekday = weekday.getPreviousWeekday();//前一工作日
        Date yestoday = DateTimeUtil.addDay(today, -1);
        if (accNoList != null && accNoList.size() > 0) {
            BankTransController bankTransController = (BankTransController) SystemManager.getSpringContext().getBean("bankTransController");
            for (String queryAccNo : accNoList) {
                bankTransController.trans(TransCode.DETAIL.getCode(), "{" + "\"accNo\": \"" + queryAccNo + "\"," + "\"forceUpdate\": true,"
                        + //强制更新，否则周五到周日的交易到周一时会下载不了
                        "\"beginDate\": \"" + DateTimeUtil.format(previousWeekday, "yyyy-MM-dd") + "\"," + "\"endDate\": \"" + DateTimeUtil.format(yestoday, "yyyy-MM-dd") + "\","
                        + "\"pageSize\": \"1\"" + "}", null);
            }
        }
    }

    @Override
    public String getJobGroup() {
        return "trans";
    }

    @Override
    public String getJobName() {
        return "detail";
    }

    @Override
    public String getJobType() {
        return "detail";
    }

    @Override
    public String getTitle() {
        return "定时查询交易明细";
    }

    @Override
    public boolean isUniqueConfig() {
        return false;
    }

    @Override
    public String getConfigDesc() {
        return "本定时任务（根据“定时表达式”）定期执行，自动下载交易明细（最好在非交易高峰期进行查询）。" + "配置格式为JSON，如：{\"bankName\": \"kft\"}（查询某个银行的所有账号） 或者 {\"accNo\": \"62260965123456\"}（查询某个账号）。";
    }
}
