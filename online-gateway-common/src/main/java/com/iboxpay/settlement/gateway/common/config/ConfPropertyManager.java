package com.iboxpay.settlement.gateway.common.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.cache.ICacheService;
import com.iboxpay.settlement.gateway.common.dao.PropertyDao;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;

/**
 * 静态配置属性管理.界面配置时，由这里读取要配置的项，生成界面。
 * @author jianbo_chen
 */
@Service
public class ConfPropertyManager {

    private static Logger logger = LoggerFactory.getLogger(ConfPropertyManager.class);

    //延迟初始化用。我们需要知道某个包名属于哪个银行，然后把属性与银行进行绑定。
    //而银行名称等信息又要在BankProfile扫描完成后才知道，属性可能在扫描过程中就注册了（先于其BankProfile注册），
    //所以要用tempConfProps延迟初始化banksConfProps
    private static List<PropertyRegisterInfo> tempConfProps = new LinkedList<PropertyRegisterInfo>();
    private static Map<String, List<Property>> banksConfProps;//key: bankName

    private final static String PROPERTY_PREFIX = "_confPro_";

    //spring无法注入静态字段,使用变通方式
    @Resource(name = "localCacheService")
    private ICacheService _cacheService;
    private static ICacheService cacheService;

    @Resource
    private PropertyDao _propertyDao;
    private static PropertyDao propertyDao;

    @PostConstruct
    void postConstruct() {
        cacheService = _cacheService;
        propertyDao = _propertyDao;
    }

    public static Map<String, List<Property>> getBanksConfProps() {
        return banksConfProps;
    }

    /**
     * 把静态配置项注册，受框架统一管理
     * @param p
     * @return
     */
    public final static Property register(Property property) {
        if (banksConfProps != null) {
            throw new RuntimeException("can not register config-property after init.");
        }
        String srcClassName = new Throwable().getStackTrace()[2].getClassName();
        if (logger.isDebugEnabled()) {
            logger.debug("register property：" + property + "; srcClassName：" + srcClassName);
        }
        property.setConfig(true);//是静态配置项
        property.setSourceClass(srcClassName);
        synchronized (ConfPropertyManager.class) {
            tempConfProps.add(new PropertyRegisterInfo(srcClassName, property));
        }
        return property;
    }

    private static String getPropertyKey(Property property) {
        return property.getOwner() + PROPERTY_PREFIX + property.getName();
    }

    /**
     * 刷新配置项值
     * @param property
     * @return
     */
    public static Property read(Property property) {
        //		ensureInit();
        if (!property.isConfig()) throw new UnsupportedOperationException("can not refresh no-config property.");

        if (property.isReadOnly()) return property;

        String key = getPropertyKey(property);
        String values[] = (String[]) cacheService.get(key);//银行名_confPro_xxxx
        if (values == null) {
            switch (property.getType()) {
                case plain:
                    String value = propertyDao.readProperty(property.getOwner(), property.getName());
                    property.setVal(value);
                    if (value != null) values = new String[] { value };
                    break;

                case array:
                    values = propertyDao.readPropertyArray(property.getOwner(), property.getName());
                    break;

                default:
                    throw new UnsupportedOperationException("Unsupported type : " + property.getType());
            }

            if (values == null) {
                values = new String[0];
            }

            cacheService.set(key, values);

        } else if (values.length == 0) {
            property.setVals((String[]) null);
        }
        property.setVals(values);
        return property;
    }

    /**
     * 保存属性值。由框架调用.
     * @param property
     * @return
     */
    public static Property save(Property property) {
        //		ensureInit();
        if (!property.isConfig()) throw new UnsupportedOperationException("can not save no-config property.");

        if (property.isReadOnly()) {
            logger.warn("can not save the readOnly property-config : " + property.getName());
            return property;
        }

        String key = getPropertyKey(property);
        String vals[] = property.getExactVals();
        vals = vals == null || vals.length == 0 || vals[0].length() == 0 ? null : vals;
        switch (property.getType()) {
            case plain:
                propertyDao.setProperty(property.getOwner(), property.getName(), vals != null && vals.length > 0 ? vals[0] : null);
                break;

            case array:
                propertyDao.setPropertyArray(property.getOwner(), property.getName(), vals);
                break;

            default:
                throw new UnsupportedOperationException("Unsupported type : " + property.getType());
        }

        cacheService.set(key, vals);

        return property;
    }

    public final static void init() {
        if (banksConfProps == null) {
            synchronized (ConfPropertyManager.class) {
                if (banksConfProps == null) {
                    Map<String, List<Property>> _banksConfProps = new HashMap<String, List<Property>>();
                    for (PropertyRegisterInfo regInfo : tempConfProps) {
                        String bankName = BankTransComponentManager.getBankNameByPackage(regInfo.srcClassName);
                        if (bankName == null) bankName = Property.OWNER_SYSTEM;//common包中为系统属性

                        List<Property> bankPropertyList = _banksConfProps.get(bankName);
                        if (bankPropertyList == null) {
                            bankPropertyList = new ArrayList<Property>();
                            _banksConfProps.put(bankName, bankPropertyList);
                        }
                        regInfo.property.setOwner(bankName);
                        //检查是否有重复配置属性
                        for (Property property : bankPropertyList) {
                            if (regInfo.property.getOwner().equals(property.getOwner()) && regInfo.property.getName().equals(property.getName())) {
                                throw new RuntimeException("dupplicate property-config : owner[" + property.getOwner() + "], name[" + property.getName() + "]");
                            }
                        }
                        bankPropertyList.add(regInfo.property);
                    }
                    List<Property> systemProps = _banksConfProps.get(Property.OWNER_SYSTEM);
                    //按顺序的列表，主要是把系统配置放在前面，主要用于界面显示时
                    Map<String, List<Property>> orderBanksConfProps = new LinkedHashMap<String, List<Property>>();
                    orderBanksConfProps.put(Property.OWNER_SYSTEM, systemProps);
                    Set<Entry<String, List<Property>>> entrys = _banksConfProps.entrySet();
                    for (Iterator<Entry<String, List<Property>>> itr = entrys.iterator(); itr.hasNext();) {
                        Entry<String, List<Property>> entry = itr.next();
                        if (!Property.OWNER_SYSTEM.equals(entry.getKey())) {
                            orderBanksConfProps.put(entry.getKey(), entry.getValue());
                        }
                    }
                    //////////////////
                    banksConfProps = orderBanksConfProps;
                }
            }
        }
    }

    private static class PropertyRegisterInfo {

        public final String srcClassName;
        public final Property property;

        public PropertyRegisterInfo(String srcClassName, Property property) {
            this.srcClassName = srcClassName;
            this.property = property;
        }
    }
}
