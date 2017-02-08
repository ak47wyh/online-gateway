package com.iboxpay.settlement.gateway.common.dao.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.config.Property;
import com.iboxpay.settlement.gateway.common.dao.FrontEndDao;
import com.iboxpay.settlement.gateway.common.domain.FrontEndEntity;
import com.iboxpay.settlement.gateway.common.domain.FrontEndPropertyEntity;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;

@Component("frontEndDao")
public class FrontEndDaoImpl extends HibernateDaoSupport implements FrontEndDao {

    @Resource
    public void setMySessionFactory(SessionFactory sessionFactory) {
        super.setSessionFactory(sessionFactory);
    }

    @Transactional
    public boolean delete(FrontEndConfig feConfig) {
        return doDelete(feConfig.getId());
    }

    private boolean doDelete(Serializable id) {
        Session session = getSession();
        FrontEndEntity feEntity = (FrontEndEntity) session.get(FrontEndEntity.class, id);

        if (feEntity == null) return false;

        Query q = session.createQuery("delete from FrontEndPropertyEntity where parentId = :parentId");
        q.setInteger("parentId", feEntity.getId());
        q.executeUpdate();
        session.delete(feEntity);

        return true;
    }

    @Transactional
    public boolean delete(Serializable id) {
        return doDelete(id);
    }

    @Override
    public List<FrontEndConfig> findByBankName(String bankName) {
        Session session = getSession();
        try {
            Query q = session.createQuery("from FrontEndEntity where bankName = :bankName");
            q.setString("bankName", bankName);
            List<FrontEndEntity> frontEndEntityList = q.list();
            List<FrontEndConfig> resultList = new ArrayList<FrontEndConfig>();
            for (FrontEndEntity frontEndEntity : frontEndEntityList) {
                resultList.add(assemble(frontEndEntity, frontEndEntity.getPropertys()));
            }
            return resultList;
        } finally {
            session.close();
        }
    }

    @Override
    public FrontEndConfig get(Serializable id) {
        Session session = getSession();
        try {
            FrontEndEntity feEntity = (FrontEndEntity) session.get(FrontEndEntity.class, id);

            if (feEntity == null) return null;

            Query q = session.createQuery("from FrontEndPropertyEntity where parentId = :parentId");
            q.setInteger("parentId", feEntity.getId());
            List<FrontEndPropertyEntity> dbPropertysList = q.list();
            return assemble(feEntity, dbPropertysList);
        } finally {
            session.close();
        }
    }

    private FrontEndConfig assemble(FrontEndEntity feEntity, List<FrontEndPropertyEntity> dbPropertysList) {
        FrontEndConfig feConfig = BankTransComponentManager.getFrontEndConfigInstance(feEntity.getBankName());
        if (feConfig == null) {
            return null;
        }
        if (FrontEndEntity.STATUS_DISABLE.equals(feEntity.getStatus())) {
            feConfig.setEnable(false);
        } else {
            feConfig.setEnable(true);
        }
        feConfig.setName(feEntity.getName());
        feConfig.setId(feEntity.getId());
        if (dbPropertysList != null) {
            List<Property> propertys = feConfig.getAllPropertys();
            for (Property property : propertys) {
                for (FrontEndPropertyEntity fePropertyEntity : dbPropertysList) {
                    if (fePropertyEntity.getName().equals(property.getName())) {
                        property.setVal(fePropertyEntity.getValue());
                    }
                }
            }
        }
        return feConfig;
    }

    /**
     * @param session
     * @param accNo
     * @param extPropertys
     */
    private void saveFrontEndProperty(Session session, Integer parentId, List<Property> propertys) {
        for (Property property : propertys) {
            if (property.getExactVals() == null || property.getExactVals().length == 0 || property.getExactVals()[0] == null) {//值为null，删除

                logger.info("FrontEndEntity(id=" + parentId + ") 's property[" + property.getName() + "] value is null, try delete.");

                Query q = session.createQuery("delete from FrontEndPropertyEntity where parentId = :parentId and name = :name");
                q.setInteger("parentId", parentId);
                q.setString("name", property.getName());
                q.executeUpdate();
            } else {
                Query q = session.createQuery("update FrontEndPropertyEntity p set p.value = :value where p.parentId = :parentId and p.name = :name");
                q.setString("value", property.getExactVals()[0]);
                q.setInteger("parentId", parentId);
                q.setString("name", property.getName());
                if (q.executeUpdate() == 0) {//不存在
                    FrontEndPropertyEntity propertyEntity = new FrontEndPropertyEntity();
                    propertyEntity.setName(property.getName());
                    propertyEntity.setParentId(parentId);
                    propertyEntity.setValue(property.getExactVals()[0]);
                    session.save(propertyEntity);
                }
            }
        }
    }

    private void saveOrUpdate(FrontEndConfig feConfig, boolean update) {
        Date now = new Date();
        Session session = getSession();
        String bankName = BankTransComponentManager.getBankNameByPackage(feConfig.getClass().getName());
        if (bankName == null) throw new RuntimeException("bankName is null.");

        Integer id = feConfig.getId();
        //呃，别问我为什么要手工写了...ORA-01407: 无法更新 ("ROUTER_DEV"."T_EB_FRONTENDPROPERTY"."PARENT_ID") 为 NULL
        if (update) {
            Query q = session.createQuery("update FrontEndEntity set name=:name, bankName=:bankName, status=:status, updateTime=:updateTime where id=:id");
            q.setString("name", feConfig.getName());
            q.setString("bankName", bankName);
            q.setTimestamp("updateTime", now);
            q.setInteger("id", feConfig.getId());
            q.setString("status", feConfig.isEnable() ? FrontEndEntity.STATUS_ENABLE : FrontEndEntity.STATUS_DISABLE);
            q.executeUpdate();
        } else {
            FrontEndEntity entity = new FrontEndEntity();
            entity.setId(feConfig.getId());
            entity.setName(feConfig.getName());
            entity.setBankName(bankName);
            entity.setStatus(feConfig.isEnable() ? FrontEndEntity.STATUS_ENABLE : FrontEndEntity.STATUS_DISABLE);
            entity.setCreateTime(now);
            entity.setUpdateTime(now);
            session.save(entity);
            id = entity.getId();
        }
        feConfig.setId(id);
        List<Property> frontEndPropertys = feConfig.getAllPropertys();
        if (frontEndPropertys != null) {
            saveFrontEndProperty(session, id, frontEndPropertys);
        }

        logger.info("saveOrUpdate FrontEndConfig(" + feConfig.getClass().getName() + "): " + feConfig.toString());
    }

    @Transactional
    public void save(FrontEndConfig feConfig) {
        saveOrUpdate(feConfig, false);
    }

    @Transactional
    public void update(FrontEndConfig feConfig) {
        saveOrUpdate(feConfig, true);
    }

    @Override
    public List<FrontEndConfig> loadAllFrontEndConfig() {
        Session session = getSession();
        try {
            Query q = session.createQuery("from FrontEndEntity");
            List<FrontEndEntity> frontEndEntityList = q.list();
            List<FrontEndConfig> resultList = new ArrayList<FrontEndConfig>();
            for (FrontEndEntity frontEndEntity : frontEndEntityList) {
                try {
                    resultList.add(assemble(frontEndEntity, frontEndEntity.getPropertys()));
                } catch (Exception e) {
                    logger.warn("加载并配置前置机失败(可能数据库中前置机配置存在脏数据)", e);
                }
            }
            return resultList;
        } finally {
            session.close();
        }
    }

}
