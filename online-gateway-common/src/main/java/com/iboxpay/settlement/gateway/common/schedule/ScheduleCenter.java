package com.iboxpay.settlement.gateway.common.schedule;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.settlement.gateway.common.SystemManager;
import com.iboxpay.settlement.gateway.common.dao.CommonDao;
import com.iboxpay.settlement.gateway.common.dao.impl.CommonDaoImpl;
import com.iboxpay.settlement.gateway.common.domain.ScheduleEntity;

/**
 * 定时任务中心
 * @author jianbo_chen
 */
public class ScheduleCenter {

    private static Logger logger = LoggerFactory.getLogger(ScheduleCenter.class);
    private static Scheduler scheduler;
    private static CommonDao scheduleEntityDao = CommonDaoImpl.getDao(ScheduleEntity.class);

    public static void start() {
        if (scheduler == null) {
            try {
                scheduler = StdSchedulerFactory.getDefaultScheduler();
                init();
                scheduler.start();
            } catch (SchedulerException e) {
                logger.error("", e);
            }
        }
    }

    private static void init() {
        List<ScheduleEntity> scheduleList = scheduleEntityDao.findAll();
        for (ScheduleEntity scheduleEntity : scheduleList) {
            schedule(scheduleEntity);
        }
    }

    public static void stop() {
        try {
            scheduler.shutdown();
        } catch (SchedulerException e) {
            logger.warn("", e);
        }
    }

    public static Collection<AbstractSchedulerJob> getAllSchedulerJob() {
        Map<String, AbstractSchedulerJob> scheduleJobs = SystemManager.getSpringContext().getBeansOfType(AbstractSchedulerJob.class);
        return scheduleJobs.values();
    }

    public static AbstractSchedulerJob getSchedulerByType(String jobType) {
        Collection<AbstractSchedulerJob> allSchedulerJob = getAllSchedulerJob();
        for (Iterator<AbstractSchedulerJob> itr = allSchedulerJob.iterator(); itr.hasNext();) {
            AbstractSchedulerJob scheduleJob = itr.next();
            if (scheduleJob.getJobType().equals(jobType)) {
                return scheduleJob;
            }
        }
        return null;
    }

    public synchronized static void schedule(ScheduleEntity scheduleEntity) {
        AbstractSchedulerJob scheduleJob = getSchedulerByType(scheduleEntity.getJobType());
        try {
            Trigger trigger;
            if (scheduleEntity.getCron().startsWith("f:")) {
                SimpleTrigger simpleTrigger = new SimpleTrigger();
                simpleTrigger.setStartTime(new Date());
                long interval = Integer.parseInt(scheduleEntity.getCron().substring(2, scheduleEntity.getCron().length() - 1));
                String unit = scheduleEntity.getCron().substring(scheduleEntity.getCron().length() - 1);
                if ("s".equalsIgnoreCase(unit)) {
                    interval *= 1000;
                } else if ("m".equalsIgnoreCase(unit)) {
                    interval *= 60 * 1000;
                } else if ("h".equalsIgnoreCase(unit)) {
                    interval *= 60 * 60 * 1000;
                } else {
                    throw new Exception("无法识别的单位：" + unit);
                }
                simpleTrigger.setRepeatInterval(interval);
                simpleTrigger.setRepeatCount(Integer.MAX_VALUE);
                simpleTrigger.setJobName(scheduleJob.getJobName());
                trigger = simpleTrigger;
            } else {
                CronTrigger cronTrigger = new CronTrigger();
                cronTrigger.setCronExpression(scheduleEntity.getCron());
                trigger = cronTrigger;
            }
            trigger.setName(scheduleJob.getJobName());
            trigger.setGroup(scheduleJob.getJobGroup());
            trigger.setJobName(scheduleJob.getJobName());
            trigger.setJobGroup(scheduleJob.getJobGroup());
            JobDetail jobDetail = new JobDetail(scheduleJob.getJobName(), scheduleJob.getJobGroup(), scheduleJob.getClass());
            jobDetail.getJobDataMap().put("params", scheduleEntity.getParams());
            Trigger oldTrigger = scheduler.getTrigger(scheduleJob.getJobName(), scheduleJob.getJobGroup());
            if (oldTrigger == null) {
                scheduler.scheduleJob(jobDetail, trigger);
                logger.info("启动定时任务:" + scheduleEntity.toString());
            } else {
                scheduler.addJob(jobDetail, true);//覆盖旧的JobDetail
                scheduler.rescheduleJob(scheduleJob.getJobName(), scheduleJob.getJobGroup(), trigger);
                logger.info("重设定时任务:" + scheduleEntity.toString());
            }
        } catch (Exception e) {
            logger.warn("配置调度任务异常", e);
        }
    }

    public synchronized static void deleteSchedule(ScheduleEntity scheduleEntity) {
        AbstractSchedulerJob scheduleJob = getSchedulerByType(scheduleEntity.getJobType());
        try {
            scheduler.deleteJob(scheduleJob.getJobName(), scheduleJob.getJobGroup());
            logger.info("禁用定时任务:" + scheduleEntity.toString());
        } catch (SchedulerException e) {
            logger.warn("禁用调度任务异常", e);
        }
    }
}
