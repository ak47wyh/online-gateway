package com.iboxpay.settlement.gateway.common.cache.remote;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.danga.MemCached.MemCachedClient;
import com.iboxpay.settlement.gateway.common.cache.ICacheService;

@Service("memcachedService")
public class MemcachedServiceImpl implements MemcachedService, ICacheService {

    private static final Log log = LogFactory.getLog(MemcachedServiceImpl.class);
    private static final long serialVersionUID = 1;

    @Resource
    MemCachedClient memCachedClient;

    public void setMemCachedClient(MemCachedClient memCachedClient) {
        this.memCachedClient = memCachedClient;

        // 序列化
        if (this.memCachedClient != null) {
            this.memCachedClient.setPrimitiveAsString(true);
        }
    }

    public boolean delete(String key) {
        return memCachedClient.delete(key);
    }

    public void set(String key, Object object) {
        if (object != null)
            memCachedClient.set(key, object);
        else memCachedClient.delete(key);
    }

    public Object get(String key) {
        return memCachedClient.get(key);
    }

    public boolean deleteWithType(String key, Class clazz) {
        return memCachedClient.delete(getKeyWithType(key, clazz));
    }

    public Object getWithType(String key, Class clazz) {
        return memCachedClient.get(getKeyWithType(key, clazz));
    }

    public void setWithType(String key, Object object) {
        memCachedClient.set(getKeyWithType(key, object.getClass()), object);
    }

    public void setWithType(String key, Object object, Class clazz) {
        if (object != null)
            memCachedClient.set(getKeyWithType(key, clazz), object);
        else memCachedClient.delete(getKeyWithType(key, clazz));
    }

    protected String getKeyWithType(String key, Class clazz) {
        return clazz.getSimpleName() + "-" + key;
    }

    @Override
    public Object[] getWithType(String[] keys, Class clazz) {
        for (int i = 0; i < keys.length; i++) {
            keys[i] = getKeyWithType(keys[i], clazz);
        }
        //不知为啥会报个
        //		java.lang.NullPointerException: null
        //		at com.schooner.MemCached.AscIIClient$NIOLoader.doMulti(Unknown Source) ~[memcached-2.6.6.jar:na]
        //		at com.schooner.MemCached.AscIIClient.getMulti(Unknown Source) ~[memcached-2.6.6.jar:na]
        try {
            return memCachedClient.getMultiArray(keys);
        } catch (Exception e) {}
        return new Object[0];
    }

    @Override
    public void setWithType(String[] keys, Class clazz, Object[] objects) {
        for (int i = 0; i < keys.length; i++) {
            memCachedClient.set(getKeyWithType(keys[i], clazz), objects[i]);
        }
    }

    //	/**
    //	 * 
    //	 * 从memcached 中检索以指定key为开头的一组key值
    //	 */
    //	@SuppressWarnings("rawtypes")
    //	public Set filterCachedKey(String keyStart) {
    //		Set<String> keys = new HashSet<String>();
    //		try {
    //			int limit = 0;
    //
    //			Map<String, Integer> dumps = new HashMap<String, Integer>();
    //
    //			Map slabs = memCachedClient.statsItems();
    //			if (slabs != null && slabs.keySet() != null) {
    //				Iterator itemsItr = slabs.keySet().iterator();
    //				while (itemsItr.hasNext()) {
    //					String server = itemsItr.next().toString();
    //					Map itemNames = (Map) slabs.get(server);
    //					Iterator itemNameItr = itemNames.keySet().iterator();
    //					while (itemNameItr.hasNext()) {
    //						String itemName = itemNameItr.next().toString();
    //						// itemAtt[0]=itemname
    //						// itemAtt[1]=number
    //						// itemAtt[2]=field
    //						String[] itemAtt = itemName.split(":");
    //
    //						if (itemAtt[2].startsWith("number"))
    //							dumps.put(itemAtt[1], Integer.parseInt(itemAtt[1]));
    //
    //					}
    //				}
    //				if (!dumps.values().isEmpty()) {
    //					Iterator<Integer> dumpIter = dumps.values().iterator();
    //					while (dumpIter.hasNext()) {
    //						int dump = dumpIter.next();
    //
    //						Map cacheDump = memCachedClient.statsCacheDump(dump,
    //								limit);
    //						Iterator entryIter = cacheDump.values().iterator();
    //						while (entryIter.hasNext()) {
    //							Map items = (Map) entryIter.next();
    //							Iterator ks = items.keySet().iterator();
    //							while (ks.hasNext()) {
    //								String k = (String) ks.next();
    //								if (k.startsWith(keyStart)
    //										&& this.memCachedClient.keyExists(k)) {
    //									keys.add(k);
    //								}
    //							}
    //						}
    //					}
    //				}
    //
    //			}
    //		} catch (Exception ex) {
    //			log.debug(ex.toString());
    //		}
    //		return keys;
    //	}

}
