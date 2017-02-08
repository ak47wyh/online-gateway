package com.iboxpay.settlement.gateway.common.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.cache.remote.MemcachedService;
import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.dao.FrontEndDao;
import com.iboxpay.settlement.gateway.common.task.TaskScheduler;

@Service
public class FrontEndService {

    @Resource
    FrontEndDao frontEndDao;

    @Resource
    MemcachedService memcachedService;

    public List<FrontEndConfig> listFrontEnd() {
        return frontEndDao.loadAllFrontEndConfig();
    }

    public void updateFrontEnd(FrontEndConfig feConfig) {
        frontEndDao.update(feConfig);
        if (feConfig.isEnable())
            TaskScheduler.addFrontEnd(feConfig);
        else TaskScheduler.deleteFrontEnd(feConfig);
    }

    public void addFrontEnd(FrontEndConfig feConfig) {
        frontEndDao.save(feConfig);
        if (feConfig.isEnable())
            TaskScheduler.addFrontEnd(feConfig);
        else TaskScheduler.deleteFrontEnd(feConfig);

    }

    public FrontEndConfig getFrontEnd(Integer id) {
        return frontEndDao.get(id);
    }

    public void deleteFrontEnd(FrontEndConfig feConfig) {
        frontEndDao.delete(feConfig);
        TaskScheduler.deleteFrontEnd(feConfig);
    }
}
