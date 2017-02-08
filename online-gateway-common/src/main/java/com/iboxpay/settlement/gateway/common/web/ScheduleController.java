package com.iboxpay.settlement.gateway.common.web;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.quartz.CronExpression;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.iboxpay.settlement.gateway.common.dao.CommonDao;
import com.iboxpay.settlement.gateway.common.dao.impl.CommonDaoImpl;
import com.iboxpay.settlement.gateway.common.domain.ScheduleEntity;
import com.iboxpay.settlement.gateway.common.schedule.AbstractSchedulerJob;
import com.iboxpay.settlement.gateway.common.schedule.ScheduleCenter;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

@Controller
@RequestMapping("/manage/schedule")
public class ScheduleController {

    private CommonDao scheduleEntityDao = CommonDaoImpl.getDao(ScheduleEntity.class);

    @RequestMapping(value = "list.htm", method = RequestMethod.GET)
    public ModelAndView list() {
        List<ScheduleEntity> scheduleList = scheduleEntityDao.findAll();
        List<Map<String, Object>> list = new LinkedList<Map<String, Object>>();
        for (ScheduleEntity scheduleEntity : scheduleList) {
            Map<String, Object> scheMap = convertToMap(scheduleEntity);
            if (scheMap != null) list.add(scheMap);
        }
        ModelAndView mv = new ModelAndView();
        mv.addObject("scheduleList", list);
        mv.setViewName("/views/schedule/list");
        return mv;
    }

    private Map<String, Object> convertToMap(ScheduleEntity scheduleEntity) {
        AbstractSchedulerJob schedulerJob = ScheduleCenter.getSchedulerByType(scheduleEntity.getJobType());
        if (schedulerJob != null) {
            Map<String, Object> scheMap = new HashMap<String, Object>();
            scheMap.put("id", scheduleEntity.getId());
            scheMap.put("title", schedulerJob.getTitle());
            scheMap.put("jobType", schedulerJob.getJobType());
            scheMap.put("jobGroup", schedulerJob.getJobGroup());
            scheMap.put("jobName", schedulerJob.getJobName());
            scheMap.put("cron", scheduleEntity.getCron());
            scheMap.put("params", scheduleEntity.getParams());
            scheMap.put("status", scheduleEntity.getStatus());
            if (scheduleEntity.getCreateTime() != null) scheMap.put("createTime", DateTimeUtil.format(scheduleEntity.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
            if (scheduleEntity.getUpdateTime() != null) scheMap.put("updateTime", DateTimeUtil.format(scheduleEntity.getUpdateTime(), "yyyy-MM-dd HH:mm:ss"));
            return scheMap;
        }
        return null;
    }

    @RequestMapping(value = "edit.htm", method = RequestMethod.GET)
    public ModelAndView edit(@RequestParam(value = "id", required = false) String id) throws Exception {
        ModelAndView mv = new ModelAndView();
        if (!StringUtils.isBlank(id)) {
            ScheduleEntity scheduleEntity = (ScheduleEntity) scheduleEntityDao.get(Integer.parseInt(id));
            Map<String, Object> scheMap = convertToMap(scheduleEntity);
            if (scheMap != null)
                mv.addObject("schedule", scheMap);
            else throw new Exception("找不到定时配置id=" + id);
        }
        Collection<AbstractSchedulerJob> scheduleJobs = ScheduleCenter.getAllSchedulerJob();
        mv.addObject("scheduleJobs", scheduleJobs);
        mv.setViewName("/views/schedule/edit");
        return mv;
    }

    @RequestMapping(value = "edit.htm", method = RequestMethod.POST)
    public ModelAndView saveEdit(@RequestParam(value = "id", required = false) String id, @RequestParam(value = "cron", required = false) String cron,
            @RequestParam(value = "jobType", required = false) String jobType, @RequestParam(value = "params", required = false) String params) throws Exception {
        Map<String, Object> scheMap = new HashMap<String, Object>();
        scheMap.put("id", id);
        scheMap.put("cron", cron);
        scheMap.put("jobType", jobType);
        scheMap.put("params", params);
        ModelAndView mv = new ModelAndView();
        mv.addObject("schedule", scheMap);
        mv.setViewName("/views/schedule/edit");
        Collection<AbstractSchedulerJob> scheduleJobs = ScheduleCenter.getAllSchedulerJob();
        mv.addObject("scheduleJobs", scheduleJobs);
        if (StringUtils.isBlank(cron)) {
            mv.addObject("input_error", "定时表达式为空");
            return mv;
        }
        cron = cron.trim();
        if (cron.matches("f:\\d+[smh]")) {

        } else {
            try {
                new CronExpression(cron);
            } catch (Exception e) {
                mv.addObject("input_error", "定时表达式格式不正确");
                return mv;
            }
        }
        if (!StringUtils.isBlank(params)) {
            try {
                JsonUtil.jsonToObject(params, "UTF-8", Map.class);
            } catch (Exception e) {
                mv.addObject("input_error", "定时参数格式有误，不是JSON格式");
                return mv;
            }
        }
        Date now = new Date();
        id = StringUtils.trim(id);
        ScheduleEntity scheduleEntity = null;
        if (!StringUtils.isBlank(id)) {
            scheduleEntity = (ScheduleEntity) scheduleEntityDao.get(Integer.parseInt(id));
            if (scheduleEntity == null) {
                mv.addObject("input_error", "定时配置不存在，id=" + id);
                return mv;
            } else {
                scheduleEntity.setCron(cron);
                scheduleEntity.setParams(params);
                scheduleEntity.setUpdateTime(now);
                scheduleEntityDao.update(scheduleEntity);
            }
        } else {
            if (StringUtils.isBlank(jobType)) {
                mv.addObject("input_error", "请选择定时类别");
                return mv;
            }
            AbstractSchedulerJob schedulerJob = ScheduleCenter.getSchedulerByType(jobType);
            if (schedulerJob == null) {
                mv.addObject("input_error", "没有找对应的定时类别：" + jobType);
                return mv;
            }
            List existScheduleJobs = scheduleEntityDao.findByHQL("from ScheduleEntity where jobType = ?", jobType);
            if (schedulerJob.isUniqueConfig() && existScheduleJobs.size() > 0) {
                mv.addObject("input_error", "该定时类别只允许一个定时配置");
                return mv;
            }
            scheduleEntity = new ScheduleEntity();
            scheduleEntity.setCron(cron);
            scheduleEntity.setParams(params);
            scheduleEntity.setJobType(jobType);
            scheduleEntity.setCreateTime(now);
            scheduleEntity.setUpdateTime(now);
            scheduleEntityDao.save(scheduleEntity);
        }
        if (scheduleEntity != null) ScheduleCenter.schedule(scheduleEntity);
        return new ModelAndView("redirect:list.htm");
    }

    @RequestMapping(value = "enable.htm", method = RequestMethod.GET)
    public ModelAndView enable(@RequestParam(value = "id", required = false) String id) throws Exception {
        ScheduleEntity scheduleEntity = (ScheduleEntity) scheduleEntityDao.get(Integer.parseInt(id));
        scheduleEntity.setStatus(ScheduleEntity.STATUS_ENABLE);
        ScheduleCenter.schedule(scheduleEntity);
        scheduleEntityDao.update(scheduleEntity);
        return new ModelAndView("redirect:list.htm");

    }

    @RequestMapping(value = "disable.htm", method = RequestMethod.GET)
    public ModelAndView disable(@RequestParam(value = "id", required = false) String id) throws Exception {
        ScheduleEntity scheduleEntity = (ScheduleEntity) scheduleEntityDao.get(Integer.parseInt(id));
        scheduleEntity.setStatus(ScheduleEntity.STATUS_DISABLE);
        ScheduleCenter.deleteSchedule(scheduleEntity);
        scheduleEntityDao.update(scheduleEntity);
        return new ModelAndView("redirect:list.htm");
    }
}
