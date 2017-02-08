package com.iboxpay.settlement.gateway.common.dao.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.iboxpay.settlement.gateway.common.dao.PropertyDao;
import com.iboxpay.settlement.gateway.common.domain.PropertyEntity;

/**
 * 配置扩展属性相关
 * @author jianbo_chen
 */
@Component("propertyDao")
public class PropertyDaoImpl extends BaseDaoImpl<PropertyEntity> implements PropertyDao {

    private static Logger logger = LoggerFactory.getLogger(PropertyDaoImpl.class);

    @Override
    public String[] readPropertyArray(String owner, String name) {
        List<PropertyEntity> propertyEntityList = findByHQL("from PropertyEntity where pk.owner=? and pk.name like ?", owner, name + "[%");
        if (propertyEntityList != null) {
            String[] values = new String[propertyEntityList.size()];
            Collections.sort(propertyEntityList);//按数组序号[i]排序
            for (int i = 0; i < propertyEntityList.size(); i++) {
                values[i] = propertyEntityList.get(i).getValue();
            }
            return values;
        }
        return null;
    }

    private void delete(Session session, String owner, String name) {
        String nameOperator = name.indexOf('[') != -1 ? "like" : "=";
        Query q = session.createQuery("delete from PropertyEntity where pk.owner = :owner and pk.name " + nameOperator + " :name");
        q.setString("owner", owner);
        q.setString("name", name);
        int size = q.executeUpdate();
        if (logger.isDebugEnabled()) {
            logger.debug("delete PropertyEntity(owner=" + owner + ", name=" + name + ") count : " + size);
        }
    }

    @Override
    public void setPropertyArray(String owner, String name, String[] values) {
        Date now = new Date();
        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            if (values == null) {
                delete(session, owner, name + "[%");
            } else {
                Query updateQuery = session.createQuery("update PropertyEntity " + "set value = :value, updateTime = :updateTime " + "where pk.owner = :owner and pk.name = :name");
                int updateIndex = -1;
                for (int i = 0; i < values.length; i++) {//数组值依次更新，直到更新失败的
                    updateQuery.setString("value", values[i]);
                    updateQuery.setTimestamp("updateTime", now);
                    updateQuery.setString("owner", owner);
                    updateQuery.setString("name", name + "[" + i + "]");
                    if (updateQuery.executeUpdate() == 1)//更新失败，没有该值
                        updateIndex = i;
                    else break;
                }
                if (updateIndex < values.length - 1) {
                    for (int i = updateIndex + 1; i < values.length; i++) {//没有存在旧值的index,插入
                        PropertyEntity entity = new PropertyEntity();
                        PropertyEntity.Pk pk = new PropertyEntity.Pk();
                        pk.setOwner(owner);
                        pk.setName(name + "[" + i + "]");
                        entity.setPk(pk);
                        entity.setValue(values[i]);
                        entity.setCreateTime(now);
                        entity.setUpdateTime(now);
                        session.save(entity);
                    }
                } else if (updateIndex == values.length - 1) {//能用旧值更新完，后面可能还有其他旧值，要执行删除.如： 一个数组原来有[0],[1],[2]三个值，更新值[0],[1]，则[2]要删除
                    Query deleteQuery = session.createQuery("delete from PropertyEntity where pk.owner=:owner and pk.name=:name");
                    for (int i = updateIndex + 1;; i++) {
                        deleteQuery.setString("owner", owner);
                        deleteQuery.setString("name", name + "[" + i + "]");
                        if (deleteQuery.executeUpdate() != 1) {//删除完了
                            break;
                        }
                    }
                }
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            logger.error("setPropertyArray error: owner[" + owner + "], name[" + name + "]", e);
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }

    @Override
    public String readProperty(String owner, String name) {
        PropertyEntity entity = get(new PropertyEntity.Pk(owner, name));
        if (entity != null) {
            return entity.getValue();
        }
        return null;
    }

    @Override
    public void setProperty(String owner, String name, String value) {
        Date now = new Date();
        Session session = getSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            if (value == null) {
                delete(session, owner, name);
            } else {
                Query updateQuery = session.createQuery("update PropertyEntity " + "set value = :value, updateTime = :updateTime " + "where pk.owner = :owner and pk.name = :name");
                updateQuery.setString("value", value);
                updateQuery.setTimestamp("updateTime", now);
                updateQuery.setString("owner", owner);
                updateQuery.setString("name", name);

                if (updateQuery.executeUpdate() == 0) {//不存在，插入
                    PropertyEntity entity = new PropertyEntity();
                    PropertyEntity.Pk pk = new PropertyEntity.Pk();
                    pk.setName(name);
                    pk.setOwner(owner);
                    entity.setPk(pk);
                    entity.setValue(value);
                    entity.setCreateTime(now);
                    entity.setUpdateTime(now);
                    session.save(entity);
                }
            }

            tx.commit();

        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            logger.error("setProperty error : owner[" + owner + "], name[" + name + "]");
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }

}
