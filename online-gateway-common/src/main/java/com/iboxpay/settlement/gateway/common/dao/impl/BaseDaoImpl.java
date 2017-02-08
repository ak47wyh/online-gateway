package com.iboxpay.settlement.gateway.common.dao.impl;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.iboxpay.settlement.gateway.common.dao.BaseDao;
import com.iboxpay.settlement.gateway.common.dao.SessionJdbc;
import com.iboxpay.settlement.gateway.common.page.PageBean;

public abstract class BaseDaoImpl<T> extends HibernateDaoSupport implements BaseDao<T> {

    private static final Log log = LogFactory.getLog(BaseDaoImpl.class);

    private Class<T> entityClass;

    /**
     * 此方法用于 规避在 application-fullpay.xml中将sessionFactory注入到dao中
     * @param sessionFactory
     */
    @Resource
    public void setMySessionFactory(SessionFactory sessionFactory) {
        super.setSessionFactory(sessionFactory);
    }

    public BaseDaoImpl() {
        Class cl;
        if (getClass().getSuperclass() != BaseDao.class && getClass().getSuperclass() != BaseDaoImpl.class)
            cl = getClass().getSuperclass();
        else cl = getClass();

        this.entityClass = (Class<T>) ((ParameterizedType) cl.getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    /**
     * save or update entity
     * 
     * @param entity
     */
    public void saveOrUpdate(T entity) {
        this.getHibernateTemplate().saveOrUpdate(entity);
    }

    /**
     * save or update list entity
     * 
     * @param list 
     *void
     * @exception 
     * @since  1.0.0
    */
    public void saveOrUpdate(List<T> list) {
        this.getHibernateTemplate().saveOrUpdate(list);
    }

    /**
     * save entity
     * 
     * @param entity
     */
    public void save(T entity) {
        getHibernateTemplate().save(entity);
    }

    public T update(T entity) {
        getHibernateTemplate().update(entity);
        return entity;
    }

    public T get(Serializable id) {
        return (T) getHibernateTemplate().get(getEntityClass(), id);
    }

    public T getWithLock(Serializable id, LockMode mode) {
        return (T) getHibernateTemplate().get(getEntityClass(), id, mode);

    }

    public T load(Serializable id) {
        return (T) getHibernateTemplate().load(getEntityClass(), id);
    }

    public T loadWithLock(Serializable id, LockMode mode) {
        return (T) getHibernateTemplate().load(getEntityClass(), id, mode);
    }

    public void flush() {
        getHibernateTemplate().flush();
    }

    /**
     * delete entity
     * 
     * @param entity
     */
    public void delete(T entity) {

        getHibernateTemplate().delete(entity);

    }

    public void delete(Serializable entityId) {
        Object entity = getHibernateTemplate().load(getEntityClass(), entityId);
        getHibernateTemplate().delete(entity);
    }

    /**
     * find all entity
     */
    public List<T> findAll() {
        return findByCriteria();
    }

    public List<T> findByCriteria(final int firstResult, final int maxResults, final Order order, final Criterion... criterions) {
        return (List<T>) getHibernateTemplate().execute(new HibernateCallback() {

            public List<T> doInHibernate(Session session) throws HibernateException, SQLException {
                Criteria criteria = session.createCriteria(getEntityClass());
                for (Criterion c : criterions) {
                    criteria.add(c);
                }
                if (order != null) {
                    criteria.addOrder(order);
                }

                criteria.setFirstResult(firstResult);
                criteria.setMaxResults(maxResults);

                return criteria.list();
            }
        });
    }

    public List<T> findByCriteria(Criterion... criterions) {
        DetachedCriteria criteria = DetachedCriteria.forClass(getEntityClass());
        for (Criterion c : criterions) {
            criteria.add(c);
        }
        return getHibernateTemplate().findByCriteria(criteria);
    }

    public List findByHQL(String hql) {
        return getHibernateTemplate().find(hql);
    }

    public T findById(Serializable id) {
        return (T) getHibernateTemplate().get(getEntityClass(), id);
    }

    public List<T> findByHQL(String hql, Object... params) {
        return getHibernateTemplate().find(hql, params);
    }

    public T findEntityByHQL(String hql, Object... params) {
        List<T> entityList = getHibernateTemplate().find(hql, params);
        T entity = null;
        if (entityList != null && !entityList.isEmpty()) {
            entity = entityList.get(0);
        }
        return entity;
    }

    public PageBean findPage(int pageNo, int pageSize, String hql, Object... params) {

        Long totalCount = (Long) createQuery(prepareCountHql(hql), params).uniqueResult();

        PageBean pb = new PageBean(pageNo, pageSize, totalCount);
        Query query = createQuery(hql, params);
        query.setFirstResult(pb.getStartIndex());
        query.setMaxResults(pb.getPageSize());

        List result = query.list();
        pb.setResult(result);
        return pb;
    }

    public Long getCountByHQL(String hql, Object... params) {
        return (Long) createQuery(hql, params).uniqueResult();
    }

    private String prepareCountHql(String orgHql) {
        String countHql = "select count (*) " + removeSelect(removeOrders(orgHql));
        return countHql;
    }

    private static String removeSelect(String hql) {
        int beginPos = hql.toLowerCase().indexOf("from");
        return hql.substring(beginPos);
    }

    private static String removeOrders(String hql) {
        Pattern p = Pattern.compile("order\\s*by[\\w|\\W|\\s|\\S]*", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(hql);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, "");
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public Query createQuery(String queryString, Object... values) {
        Query query = getSession().createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        }
        return query;
    }

    /* (non-Javadoc)
     * @see com.iboxpay.core.dao.BaseDao#findItems(int, int, java.lang.String, java.lang.Object[])
     */
    @Override
    public List findItems(int startNum, int pageSize, String hql, Object... params) {
        Query query = createQuery(hql, params);
        query.setFirstResult(startNum);
        query.setMaxResults(pageSize);
        return query.list();
    }

    public int[] batchUpdate(String sql, List<Object[]> params) {
        int[] i = null;
        SessionJdbc db = null;
        try {
            Session session = this.getHibernateTemplate().getSessionFactory().openSession();
            db = new SessionJdbc(session);
            i = db.batchUpdate(sql, params);
        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            db.closeCon();
        }
        return i;
    }

    public int updateBySql(String sql) throws RuntimeException {
        int i = 0;
        SessionJdbc db = null;
        try {
            Session session = this.getHibernateTemplate().getSessionFactory().openSession();
            db = new SessionJdbc(session);
            i = db.updateDateBase(sql);
        } catch (Exception e) {
            throw new RuntimeException();
        } finally {
            db.closeCon();
        }
        return i;
    }
}
