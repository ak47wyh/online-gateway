package com.iboxpay.settlement.gateway.common.util;

import java.lang.reflect.Field;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassUtil {

    private static Logger logger = LoggerFactory.getLogger(ClassUtil.class);

    /**
     * 封装性，设置属性值
     * @param o : 对象
     * @param fieldName : 字段名
     * @param value : 字段值
     */
    public static void set(Object o, String fieldName, Object value) {
        try {
            Field f = o.getClass().getField(fieldName);
            if (!f.isAccessible()) {
                f.setAccessible(true);
            }
            f.set(o, value);
        } catch (Exception e) {
            logger.warn("", e);
        }
    }

    public static String toString(Object o) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        java.lang.reflect.Field[] fields = o.getClass().getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) continue;
            field.setAccessible(true);
            Object fieldVal;
            try {
                fieldVal = field.get(o);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            String fieldStr = null;
            if (fieldVal instanceof Date) {
                fieldStr = DateTimeUtil.format((Date) fieldVal, "yyyy-MM-dd HH:mm:ss.SSS");
            } else if (fieldVal != null) {
                fieldStr = fieldVal.toString();
            }
            if (sb.length() > 1) sb.append(",");
            sb.append(field.getName()).append("=").append(fieldStr);
        }
        sb.append("}");
        return sb.toString();
    }
}
