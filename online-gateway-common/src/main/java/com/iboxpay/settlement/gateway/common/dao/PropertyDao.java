package com.iboxpay.settlement.gateway.common.dao;

import com.iboxpay.settlement.gateway.common.domain.PropertyEntity;

/**
 * 属性配置DAO
 * @author jianbo_chen
 */
public interface PropertyDao extends BaseDao<PropertyEntity> {

    /**
     * 从数据库读取属性值，该属性是数组
     * @param owner : 拥有者. 如果是银行定义的，则这个属性属于这个银行的.
     * @param name : 属性名
     * @return
     */
    public String[] readPropertyArray(String owner, String name);

    /**
     * 从数据库读取属性值
     * @param owner : 拥有者. 如果是银行定义的，则这个属性属于这个银行的.
     * @param name : 属性名
     * @return 
     */
    public String readProperty(String owner, String name);

    /**
     * 把属性数组值设置到数据库中
     * @param owner : 拥有者. 如果是银行定义的，则这个属性属于这个银行的.
     * @param name : 属性名
     * @param values
     */
    public void setPropertyArray(String owner, String name, String values[]);

    /**
     * 把属性值设置到数据库中
     * @param owner : 拥有者. 如果是银行定义的，则这个属性属于这个银行的.
     * @param name : 属性名
     * @param values 支持数组
     */
    public void setProperty(String owner, String name, String value);

}
