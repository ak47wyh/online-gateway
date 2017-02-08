package com.iboxpay.settlement.gateway.common.dao;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;

@Component
public class CommonSessionFactory extends HibernateDaoSupport {

    private static CommonSessionFactory commonSessionFactory;

    @PostConstruct
    void postConstruct() {
        commonSessionFactory = this;
    }

    @Resource
    public void setMySessionFactory(SessionFactory sessionFactory) {
        super.setSessionFactory(sessionFactory);
    }

    public final static Session getHibernateSession() {
        return commonSessionFactory.getSession();
    }
}
