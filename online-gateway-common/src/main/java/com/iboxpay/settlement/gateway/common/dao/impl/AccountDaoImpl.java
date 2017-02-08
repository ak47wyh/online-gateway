package com.iboxpay.settlement.gateway.common.dao.impl;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.hibernate.EmptyInterceptor;
import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.property.ChainedPropertyAccessor;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.PropertyAccessorFactory;
import org.hibernate.property.Setter;
import org.hibernate.transform.ResultTransformer;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Component;

import com.iboxpay.settlement.gateway.common.config.Property;
import com.iboxpay.settlement.gateway.common.dao.AccountDao;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.AccountExtEntity;
import com.iboxpay.settlement.gateway.common.page.PageBean;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;

/**
 * 这里需要手动实现，虽然AccountExtInterceptor，但未确定怎样拦截并对所有扩展属性表进行操作。
 * @author jianbo_chen
 */
@Component("accountDao")
public class AccountDaoImpl extends HibernateDaoSupport implements AccountDao {

    public AccountDaoImpl() {

    }

    @Resource
    public void setMySessionFactory(SessionFactory sessionFactory) {
        super.setSessionFactory(sessionFactory);
    }

    private Class getEntityClass() {
        return AccountEntity.class;
    }

    /**
     * 注入Interceptor，用于账户的属性扩展
     */
    @Override
    protected HibernateTemplate createHibernateTemplate(SessionFactory sessionFactory) {
        HibernateTemplate hibernateTemplate = super.createHibernateTemplate(sessionFactory);
        hibernateTemplate.setEntityInterceptor(new AccountExtInterceptor());
        return hibernateTemplate;
    }

    @Override
    public void delete(AccountEntity entity) {
        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Query deleteQuery = session.createQuery("delete from AccountExtEntity where pk.accNo = :accNo");
            deleteQuery.setString("accNo", entity.getAccNo());
            int deleteCount = deleteQuery.executeUpdate();
            session.delete(entity);
            tx.commit();
            logger.info("delete accNo: " + entity.getAccNo());
            if (deleteCount > 0) logger.info("delete accNo[" + entity.getAccNo() + "] ext-property count : " + deleteCount);

        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    @Override
    public AccountEntity get(Serializable id) {
        Session session = getSession();
        Transaction tx = null;
        try {
            session.setFlushMode(FlushMode.MANUAL);//使用oracle read-only 事务
            tx = session.beginTransaction();
            Query query = createQuery(session, "select " + getAccountEntityColumns() + " from AccountEntity where accNo = ? ", null);
            query.setString(0, (String) id);
            query.setResultTransformer(new AccountResultTransformer(AccountEntity.class));
            List<AccountEntity> resultList = query.list();

            if (resultList.size() > 0 && !resultList.get(0).getClass().equals(AccountEntity.class)) {//扩展了
                fetchExtAccountProperty(session, resultList);
            }

            tx.commit();

            if (resultList.size() > 0) return resultList.get(0);

            return null;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    /**
     * 保存扩展属性. 本来想用@OneToMany级联来做，但真的不好做，只能手工来了.
     * @param session
     * @param accNo
     * @param extPropertys
     */
    private void _saveExtProperty(Session session, String accNo, List<Property> extPropertys) {
        for (Property extProperty : extPropertys) {
            if (extProperty.getExactVals() == null || extProperty.getExactVals().length == 0 || extProperty.getExactVals()[0] == null) {//值为null，删除

                logger.info(accNo + "'s property[" + extProperty.getName() + "] value is null, try delete.");

                Query q = session.createQuery("delete from AccountExtEntity where pk.accNo = :accNo and pk.name = :name");
                q.setString("accNo", accNo);
                q.setString("name", extProperty.getName());
                q.executeUpdate();

            } else {
                Query q = session.createQuery("update AccountExtEntity set value = :value where pk.accNo = :accNo and pk.name = :name");
                q.setString("value", extProperty.getExactVals()[0]);
                q.setString("accNo", accNo);
                q.setString("name", extProperty.getName());
                if (q.executeUpdate() == 0) {//不存在
                    AccountExtEntity extEntity = new AccountExtEntity();
                    extEntity.setPk(new AccountExtEntity.Pk(accNo, extProperty.getName()));
                    extEntity.setValue(extProperty.getExactVals()[0]);
                    session.save(extEntity);
                }
            }
        }
    }

    private void deleteExtProperty(Session session, String accNo, String name) {
        String nameOperator = name.indexOf('[') != -1 ? "like" : "=";
        Query q = session.createQuery("delete from AccountExtEntity where pk.accNo = :accNo and pk.name " + nameOperator + " :name");
        q.setString("accNo", accNo);
        q.setString("name", name);
        int size = q.executeUpdate();
        if (logger.isDebugEnabled()) {
            logger.debug("delete AccountExtEntity(accNo=" + accNo + ", name=" + name + ") count : " + size);
        }
    }

    private void saveExtProperty(Session session, String accNo, List<Property> extPropertys) {
        Query updateQuery = session.createQuery("update AccountExtEntity " + "set value = :value " + "where pk.accNo = :accNo and pk.name = :name");
        Query deleteQuery = session.createQuery("delete from AccountExtEntity where pk.accNo = :accNo and pk.name = :name");
        for (Property extProperty : extPropertys) {
            String values[] = extProperty.getVals();
            String name = extProperty.getName();
            if (extProperty.isArray()) {
                if (values == null) {
                    deleteExtProperty(session, accNo, name + "[%");
                } else {
                    int updateIndex = -1;
                    for (int i = 0; i < values.length; i++) {//数组值依次更新，直到更新失败的
                        updateQuery.setString("value", values[i]);
                        updateQuery.setString("accNo", accNo);
                        updateQuery.setString("name", name + "[" + i + "]");
                        if (updateQuery.executeUpdate() == 1)//更新失败，没有该值
                            updateIndex = i;
                        else break;
                    }
                    if (updateIndex < values.length - 1) {
                        for (int i = updateIndex + 1; i < values.length; i++) {//没有存在旧值的index,插入
                            AccountExtEntity entity = new AccountExtEntity();
                            AccountExtEntity.Pk pk = new AccountExtEntity.Pk();
                            pk.setAccNo(accNo);
                            pk.setName(name + "[" + i + "]");
                            entity.setPk(pk);
                            entity.setValue(values[i]);
                            session.save(entity);
                        }
                    } else if (updateIndex == values.length - 1) {//能用旧值更新完，后面可能还有其他旧值，要执行删除.如： 一个数组原来有[0],[1],[2]三个值，更新值[0],[1]，则[2]要删除
                        for (int i = updateIndex + 1;; i++) {
                            deleteQuery.setString("accNo", accNo);
                            deleteQuery.setString("name", name + "[" + i + "]");
                            if (deleteQuery.executeUpdate() != 1) {//删除完了
                                break;
                            }
                        }
                    }
                }
            } else {
                String value = values != null && values.length > 0 ? values[0] : "";
                updateQuery.setString("value", value);
                updateQuery.setString("accNo", accNo);
                updateQuery.setString("name", name);
                if (updateQuery.executeUpdate() == 0) {//不存在
                    AccountExtEntity extEntity = new AccountExtEntity();
                    extEntity.setPk(new AccountExtEntity.Pk(accNo, name));
                    extEntity.setValue(value);
                    session.save(extEntity);
                }
            }
        }
    }

    private void saveOrUpdate(AccountEntity entity, boolean update) {
        Date now = new Date();
        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            if (entity.isBankDefault()) {//设置其他为非默认
                Query notDefQuery = session.createQuery("update AccountEntity set bankDefault=:bankDefault where bankName=:bankName and bankDefault=:oldBankDefault");//设置所有的账号为非默认
                notDefQuery.setInteger("bankDefault", 0);
                notDefQuery.setInteger("oldBankDefault", 1);
                notDefQuery.setString("bankName", entity.getBankName());
                notDefQuery.executeUpdate();
            }
            entity.setUpdateTime(now);
            List<Property> extPropertys = entity.getExtPropertys();
            if (extPropertys != null) {
                saveExtProperty(session, entity.getAccNo(), extPropertys);
            }
            if (update) {
                session.update(entity);
            } else {
                entity.setCreateTime(now);
                session.save(entity);
            }

            logger.info("saveOrUpdate AccountEntity(" + entity.getClass().getName() + "): " + entity.toString());
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    @Override
    public void save(AccountEntity entity) {
        saveOrUpdate(entity, false);
    }

    @Override
    public void update(AccountEntity entity) {
        saveOrUpdate(entity, true);
    }

    @Override
    public List<AccountEntity> find(String where, Object... params) {
        Session session = getSession();
        Transaction tx = null;
        try {
            session.setFlushMode(FlushMode.MANUAL);//使用oracle read-only 事务
            tx = session.beginTransaction();
            if (where != null && (where = where.trim()).length() != 0) {
                where = where.toLowerCase().startsWith("where") ? where : ("where " + where);
            }
            where = where == null || where.length() == 0 ? "" : where;

            Query query = createQuery(session, "select " + getAccountEntityColumns() + " from AccountEntity " + where, params);
            query.setResultTransformer(new AccountResultTransformer(AccountEntity.class));
            List<AccountEntity> resultList = query.list();

            fetchExtAccountProperty(session, resultList);

            tx.commit();

            return resultList;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public PageBean findPage(int pageNo, int pageSize, String where, Object... params) {
        Session session = getSession();
        Transaction tx = null;
        try {
            session.setFlushMode(FlushMode.MANUAL);//使用oracle read-only 事务
            tx = session.beginTransaction();
            if (where != null && (where = where.trim()).length() != 0) {
                where = where.toLowerCase().startsWith("where") ? where : ("where " + where);
            }
            where = where == null || where.length() == 0 ? "" : where;
            Long totalCount = (Long) createQuery(session, prepareCountHql("from AccountEntity " + where), params).uniqueResult();

            PageBean pageBean = new PageBean(pageNo, pageSize, totalCount);

            Query query = createQuery(session, "select " + getAccountEntityColumns() + " from AccountEntity " + where, params);
            query.setFirstResult(pageBean.getStartIndex());
            query.setMaxResults(pageBean.getPageSize());
            query.setResultTransformer(new AccountResultTransformer(AccountEntity.class));
            List result = query.list();
            pageBean.setResult(result);

            List<AccountEntity> resultList = pageBean.getResult();

            fetchExtAccountProperty(session, resultList);
            tx.commit();
            return pageBean;
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    private String getAccountEntityColumns() {
        AbstractEntityPersister entityPersister = (AbstractEntityPersister) getSessionFactory().getClassMetadata(AccountEntity.class);
        String bankName = "bankName";
        StringBuilder sb = new StringBuilder();
        sb.append(bankName).append(" as ").append(bankName).append(", ");
        sb.append(entityPersister.getIdentifierPropertyName()).append(" as ").append(entityPersister.getIdentifierPropertyName());
        String propertyNames[] = entityPersister.getPropertyNames();
        for (String propertyName : propertyNames) {
            if (bankName.equals(propertyName)) continue;

            sb.append(",");
            sb.append(propertyName).append(" as ").append(propertyName);
        }
        return sb.toString();
    }

    /**
     * 加载账号扩展属性
     * @param session
     * @param resultList
     */
    private void fetchExtAccountProperty(Session session, List<AccountEntity> resultList) {
        //先查找哪些是扩展过的账户类
        if (resultList.size() > 0) {
            for (AccountEntity entity : resultList) {

                if (AccountEntity.class.equals(entity.getClass())) //账户类没扩展
                    continue;

                Query query = session.createQuery("from AccountExtEntity where pk.accNo = :accNo");
                query.setString("accNo", entity.getAccNo());
                List<AccountExtEntity> extPropertyValues = query.list();
                List<Property> extPropertys = entity.getExtPropertys();

                if (extPropertys != null && extPropertyValues != null) for (Property extProperty : extPropertys) {
                    Map<Integer, String> extractValues = new TreeMap<Integer, String>();
                    for (AccountExtEntity extPropertyValue : extPropertyValues) {
                        String value = extPropertyValue.getValue();
                        if (extProperty.isArray()) {
                            if (extPropertyValue.getPk().getName().matches(extProperty.getName() + "\\[\\d+\\]")) {
                                int index = Integer.parseInt(extPropertyValue.getPk().getName().replaceAll(extProperty.getName() + "\\[(\\d+)\\]", "$1"));
                                extractValues.put(index, value);
                            }
                        } else if (extPropertyValue.getPk().getName().equals(extProperty.getName())) {
                            extractValues.put(0, value);
                        }
                    }
                    String[] values = new String[extractValues.size()];
                    for (Iterator<Entry<Integer, String>> itr = extractValues.entrySet().iterator(); itr.hasNext();) {
                        Entry<Integer, String> entry = itr.next();
                        values[entry.getKey()] = entry.getValue();
                    }
                    extProperty.setVals(values);
                }
            }
        }
    }

    public Long getCountByHQL(String hql, Object... params) {
        return (Long) getCountByHQL(getSession(), hql, params);
    }

    private Long getCountByHQL(Session session, String hql, Object... params) {
        return (Long) createQuery(session, hql, params).uniqueResult();
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

    protected Query createQuery(Session session, String queryString, Object... values) {
        Query query = session.createQuery(queryString);
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                query.setParameter(i, values[i]);
            }
        }
        return query;
    }

    /**
     * 从AliasToBeanResultTransformer抄过来改的
     * @author jianbo_chen
     */
    private static class AccountResultTransformer implements ResultTransformer {

        private static final long serialVersionUID = 1L;
        private Class resultClass;
        private Setter[] setters;
        private PropertyAccessor propertyAccessor;

        public AccountResultTransformer(Class resultClass) {
            if (resultClass == null) throw new IllegalArgumentException("resultClass cannot be null");
            this.resultClass = resultClass;
            propertyAccessor =
                    new ChainedPropertyAccessor(new PropertyAccessor[] { PropertyAccessorFactory.getPropertyAccessor(resultClass, null), PropertyAccessorFactory.getPropertyAccessor("field") });
        }

        public Object transformTuple(Object[] tuple, String[] aliases) {
            AccountEntity result = BankTransComponentManager.getAccountEntityInstance((String) tuple[0]);

            if (setters == null) {
                setters = new Setter[aliases.length];
                for (int i = 0; i < aliases.length; i++) {
                    String alias = aliases[i];
                    if (alias != null) {
                        setters[i] = propertyAccessor.getSetter(resultClass, alias);
                    }
                }
            }

            for (int i = 0; i < aliases.length; i++) {
                if (setters[i] != null) {
                    setters[i].set(result, tuple[i], null);
                }
            }

            return result;
        }

        public List transformList(List collection) {
            return collection;
        }

    }

    /**
     * 账户操作拦截器，各个银行可能扩展账户的属性，并存到数据库的扩展表中.
     * @author jianbo_chen
     */
    private static class AccountExtInterceptor extends EmptyInterceptor {

        private static final long serialVersionUID = 1L;

        public String getEntityName(Object object) {
            Class entityClass = object.getClass();
            if (entityClass != AccountEntity.class && AccountEntity.class.isAssignableFrom(entityClass)) {
                return object.getClass().getSuperclass().getName();
            } else {
                return super.getEntityName(object);
            }
        }
    }

}
