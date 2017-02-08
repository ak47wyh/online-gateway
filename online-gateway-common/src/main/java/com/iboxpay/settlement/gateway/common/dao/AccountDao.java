package com.iboxpay.settlement.gateway.common.dao;

import java.io.Serializable;
import java.util.List;

import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.page.PageBean;

/**
 * 账号DAO
 * @author jianbo_chen
 */
public interface AccountDao {

    /**
     * save entity
     * @param entity
     */
    public void save(AccountEntity entity);

    /**
     * update entity
     * @param entity
     * @return
     */
    public void update(AccountEntity entity);

    /**
     * get entity
     * @param id
     * @return
     */
    public AccountEntity get(Serializable id);

    /**
     * delete entity
     * @param entity
     */
    public void delete(AccountEntity entity);

    /**
     * 按HQL分页查询.
     * 
     * @param pageNo 分页参数.
     * @param pageSize 分页参数.
     * @param where 条件
     * @param params 条件参数
     * 
     * @return 分页查询结果, 附带结果列表及所有查询输入参数.
     */
    public PageBean findPage(int pageNo, int pageSize, String where, Object... params);

    public List<AccountEntity> find(String where, Object... params);

    /**
     * get count row by hql
     * @param hql
     * @param params
     * @return
     */
    public Long getCountByHQL(String hql, Object... params);
}
