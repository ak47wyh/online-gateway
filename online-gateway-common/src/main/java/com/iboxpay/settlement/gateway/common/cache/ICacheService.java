package com.iboxpay.settlement.gateway.common.cache;

public interface ICacheService {

    public boolean delete(String key);

    public void set(String key, Object object);

    public Object get(String key);

    public boolean deleteWithType(String key, Class clazz);

    public void setWithType(String key, Object object);

    public void setWithType(String key, Object object, Class clazz);

    public Object getWithType(String key, Class clazz);

    public Object[] getWithType(String keys[], Class clazz);

    public void setWithType(String keys[], Class clazz, Object objects[]);

}
