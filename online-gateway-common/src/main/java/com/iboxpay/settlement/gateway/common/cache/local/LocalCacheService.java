package com.iboxpay.settlement.gateway.common.cache.local;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.cache.ICacheService;

/***
 * 本地缓存实现
 * @author jianbo_chen
 */
@Service("localCacheService")
public class LocalCacheService implements ICacheService {

    private static ConcurrentHashMap<String, Object> cacheMap = new ConcurrentHashMap<String, Object>();

    public boolean delete(String key) {
        return cacheMap.remove(key) != null;
    }

    public void set(String key, Object object) {
        if (object != null)
            cacheMap.put(key, object);
        else cacheMap.remove(key);
    }

    public Object get(String key) {
        return cacheMap.get(key);
    }

    public boolean deleteWithType(String key, Class clazz) {
        return cacheMap.remove(getKeyWithType(key, clazz)) != null;
    }

    public Object getWithType(String key, Class clazz) {
        return cacheMap.get(getKeyWithType(key, clazz));
    }

    public void setWithType(String key, Object object) {
        cacheMap.put(getKeyWithType(key, object.getClass()), object);
    }

    public void setWithType(String key, Object object, Class clazz) {
        if (object != null)
            cacheMap.put(getKeyWithType(key, clazz), object);
        else cacheMap.remove(getKeyWithType(key, clazz));
    }

    protected String getKeyWithType(String key, Class clazz) {
        return clazz.getSimpleName() + "-" + key;
    }

    @Override
    public Object[] getWithType(String[] keys, Class clazz) {
        for (int i = 0; i < keys.length; i++) {
            keys[i] = getKeyWithType(keys[i], clazz);
        }
        Object[] cacheObjects = new Object[keys.length];
        for (int i = 0; i < keys.length; i++) {
            cacheObjects[i] = cacheMap.get(keys[i]);
        }
        return cacheObjects;
    }

    @Override
    public void setWithType(String[] keys, Class clazz, Object[] objects) {
        for (int i = 0; i < keys.length; i++) {
            cacheMap.put(getKeyWithType(keys[i], clazz), objects[i]);
        }
    }

}
