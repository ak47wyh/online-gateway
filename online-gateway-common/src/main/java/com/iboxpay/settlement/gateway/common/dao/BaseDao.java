package com.iboxpay.settlement.gateway.common.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;

import com.iboxpay.settlement.gateway.common.page.PageBean;

public interface BaseDao<T> {

    /**
     * save or update entity
     * @param entity
     */
    public void saveOrUpdate(T entity);

    /**
     * save entity
     * @param entity
     */
    public void save(T entity);

    /**
     * update entity
     * @param entity
     * @return
     */
    public T update(T entity);

    /**
     * flush
     */
    public void flush();

    /**
     * get entity
     * @param id
     * @return
     */
    public T get(Serializable id);

    /**
     * get entity with lock
     * @param id
     * @param mode
     * @return
     */
    public T getWithLock(Serializable id, LockMode mode);

    /**
     * load entity
     * @param id
     * @return
     */
    public T load(Serializable id);

    /**
     * load entity with lock
     * @param id
     * @param mode
     * @return
     */
    public T loadWithLock(Serializable id, LockMode mode);

    /**
     * delete entity
     * @param entity
     */
    public void delete(T entity);

    /**
     * delete entity by id
     * @param entityId
     */
    public void delete(Serializable entityId);

    /**
     * find all
     * @return
     */
    public List<T> findAll();

    /**
     * find by criteria
     * @param firstResult
     * @param maxResults
     * @param order
     * @param criterions
     * @return
     */
    public List<T> findByCriteria(int firstResult, int maxResults, Order order, Criterion... criterions);

    /**
     * find by criteria
     * @param criterions
     * @return
     */
    public List<T> findByCriteria(Criterion... criterions);

    /**
     * find by hql
     * @param hql
     * @return
     */
    public List findByHQL(String hql);

    /**
     * find by id
     * @param id
     * @return
     */
    public T findById(Serializable id);

    /**
     * find by hql
     * @param hql
     * @param params
     * @return
     */
    public List<T> findByHQL(String hql, Object... params);

    /**
     * find by hql
     * @param hql
     * @param params
     * @return
     */
    public T findEntityByHQL(String hql, Object... params);

    /**
     * 按HQL分页查询.
     * 
     * @param pageNo 分页参数.
     * @param pageSize 分页参数.
     * @param hql hql语句.
     * @param params 数量可变的查询参数,按顺序绑定.
     * 
     * @return 分页查询结果, 附带结果列表及所有查询输入参数.
     */
    public PageBean findPage(int pageNo, int pageSize, String hql, Object... params);

    /**
     * get count row by hql
     * @param hql
     * @param params
     * @return
     */
    public Long getCountByHQL(String hql, Object... params);

    /**
     * 
     * @param startNum
     * @param pageSize
     * @param hql
     * @param params
     * @return
     */
    public List findItems(int startNum, int pageSize, String hql, Object... params);
}