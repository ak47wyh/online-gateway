package com.iboxpay.settlement.gateway.common.dao.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Component;

import com.iboxpay.settlement.gateway.common.dao.CommonDao;

/**
 * 呃...超级懒人专用...
 * @author jianbo_chen
 */
@Component("commonDao")
public class CommonDaoImpl extends BaseDaoImpl<Object> implements CommonDao<Object> {

    private Class entityClass;

    private static SessionFactory _sessionFactory;

    @Resource
    private SessionFactory sessionFactory;

    @PostConstruct
    void postConstruct() {
        _sessionFactory = sessionFactory;
    }

    /**!!!禁止直接使用!!!*/
    public CommonDaoImpl() {
    }

    private CommonDaoImpl(Class entityClass) {
        this.entityClass = entityClass;
    }

    public static CommonDao<Object> getDao(Class entityClass) {
        return (CommonDao<Object>) Proxy.newProxyInstance(CommonDao.class.getClassLoader(), new Class[] { CommonDao.class }, new CommonDaoHandler(new CommonDaoImpl(entityClass)));
    }

    public Class getEntityClass() {
        return entityClass;
    }

    public final static class CommonDaoHandler implements InvocationHandler {

        CommonDao<Object> commonDao;

        public CommonDaoHandler(CommonDao<Object> commonDao) {
            this.commonDao = commonDao;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            BaseDaoImpl daoImpl = ((BaseDaoImpl) this.commonDao);
            if (daoImpl.getSessionFactory() == null) {//运行时由容器拿到并手动set进去。。。。有点丑，但试了无数种方式都不好使。其实比较好的是用@Configurable，但aspectJ包没加入
            //				SessionFactory sessionFactory = (SessionFactory)ContextLoader
            //													.getCurrentWebApplicationContext()
            //													.getBean("sessionFactory");
                daoImpl.setSessionFactory(_sessionFactory);
            }
            return method.invoke(commonDao, args);
        }

    }
}
