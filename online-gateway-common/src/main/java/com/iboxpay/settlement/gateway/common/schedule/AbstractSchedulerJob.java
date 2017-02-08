package com.iboxpay.settlement.gateway.common.schedule;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public abstract class AbstractSchedulerJob implements Job {

    /**
     * job类别，每个实现类需要必须不同
     * @return
     */
    public abstract String getJobType();

    /**
     * 任务标题
     * @return
     */
    public abstract String getTitle();

    public abstract String getJobGroup();

    public abstract String getJobName();

    /**
     * 配置说明
     * @return
     */
    public abstract String getConfigDesc();

    /**
     * 是否只允许一个配置。若true 则只允许一个配置实例，即只有quartz里一个Job
     * @return
     */
    public abstract boolean isUniqueConfig();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String params = context.getMergedJobDataMap().getString("params");
        execute(params);
    }

    public abstract void execute(String params) throws JobExecutionException;
}
