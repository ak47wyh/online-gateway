package com.iboxpay.settlement.gateway.common.config;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.settlement.gateway.common.util.StringUtils;
import com.iboxpay.settlement.gateway.common.web.FileController;

/**
 * 配置项
 * @author jianbo_chen
 */
public class Property implements Serializable {

    private static final long serialVersionUID = 1L;
    private static Logger logger = LoggerFactory.getLogger(Property.class);

    public static File UPLOAD_FILE_DIR;

    static {
        File _file = new File(FileController.class.getResource("/").getFile()).getParentFile();
        UPLOAD_FILE_DIR = new File(_file, "upload");
        if (!UPLOAD_FILE_DIR.exists()) {
            UPLOAD_FILE_DIR.mkdirs();
        }
    }

    public enum Type {
        plain, //普通字符串
        array, //数组
        file,
    }

    public final static String OWNER_SYSTEM = "system";
    private final String name;
    private final Type type;
    private String vals[];
    private String defVals[];
    private final String candidateVals[];//候选值
    private final String candidateNames[];//候选值对应的名称
    private final String desc;
    private boolean readOnly;
    private String owner;//拥有者，框架使用
    private boolean isConfig;//是否静态配置项，静态为全局配置
    private String sourceClass;

    public Property(String name, Type type, String defVals[], String candidateVals[], String candidateNames[], String desc, boolean readOnly) {
        if (name == null || name.trim().length() == 0) throw new IllegalArgumentException("property name can not be null.");

        if (name.indexOf('[') != -1 || name.indexOf(']') != -1) throw new IllegalArgumentException("illegal property name.");

        if (readOnly && defVals == null) throw new IllegalArgumentException("defVals can not be null when readOnly is true.");

        if (candidateVals != null && defVals != null) {
            for (String defVal : defVals) {
                boolean exist = false;
                for (String canVal : candidateVals) {
                    if (defVal.equals(canVal)) {
                        exist = true;
                        break;
                    }
                }
                if (!exist) {
                    throw new IllegalArgumentException("defVals not match the candidateVals.");
                }
            }
        }
        if ((candidateVals != null && candidateNames == null) || (candidateVals == null && candidateNames != null)
                || (candidateVals != null && candidateNames != null && candidateVals.length != candidateNames.length)) {
            throw new IllegalArgumentException("candidateVals size not match the candidateNames size.");
        }
        this.name = name.intern();
        this.type = type == null ? Type.plain : type;
        this.defVals = defVals;
        this.candidateVals = candidateVals;
        this.candidateNames = candidateNames;
        this.desc = desc.intern();
        this.readOnly = readOnly;
    }

    public Property(String name, String defVals[], String candidateVals[], String candidateNames[], String desc) {
        this(name, Type.array, defVals, candidateVals, candidateNames, desc, false);
    }

    public Property(String name, Type type, String defVals[], String desc) {
        this(name, type, defVals, null, null, desc, false);
    }

    public Property(String name, Type type, String desc) {
        this(name, type, null, null, null, desc, false);
    }

    public Property(String name, String candidateVals[], String candidateNames[], String desc) {
        this(name, Type.plain, null, candidateVals, candidateNames, desc, false);
    }

    public Property(String name, Type type, String defVal, String desc, boolean readOnly) {
        this(name, type, new String[] { defVal }, null, null, desc, readOnly);
    }

    public Property(String name, String defVal, String desc, boolean readOnly) {
        this(name, Type.plain, new String[] { defVal }, null, null, desc, readOnly);
    }

    public Property(String name, Type type, String defVal, String desc) {
        this(name, type, new String[] { defVal }, null, null, desc, false);
    }

    public Property(String name, String defVal, String desc) {
        this(name, Type.plain, new String[] { defVal }, null, null, desc, false);
    }

    public Property(String name, String desc) {
        this(name, Type.plain, null, desc, false);
    }

    public String getName() {
        return name;
    }

    /**
     * 直接从内存中读取
     * @return
     */
    public String[] getExactVals() {
        return vals;
    }

    public String[] getVals() {
        if (this.isConfig) ConfPropertyManager.read(this);

        if (vals == null || vals.length == 0)
            return defVals;
        else return vals;
    }

    public Property setVals(String[] vals) {
        if (readOnly) {
            logger.warn("can not modify the readOnly property : " + this.name);
            return this;
        }
        if (this.candidateVals != null) {
            for (String val : vals) {
                boolean exist = false;
                for (String candidateVal : candidateVals) {
                    if (candidateVal.equals(val)) {
                        exist = true;
                        break;
                    }
                }
                if (!exist) throw new IllegalArgumentException("vals not match the candidateVals.");
            }
        }
        this.vals = vals;
        return this;
    }

    public Property setVals(int[] vals) {
        String[] _vals = new String[vals.length];
        for (int i = 0; i < vals.length; i++) {
            _vals[i] = String.valueOf(vals[i]);
        }
        return setVals(_vals);
    }

    public String getVal() {
        String vals[] = getVals();
        if (vals != null)
            return vals[0];
        else return null;
    }

    public Integer getIntVal() {
        String v = getVal();
        if (v != null) {
            try {
                return Integer.parseInt(v);
            } catch (RuntimeException e) {
                if (defVals != null && defVals.length > 0) return Integer.parseInt(defVals[0]);
                throw e;
            }
        }
        return null;
    }

    public Property setVal(String val) {
        if (val == null)
            setVals((String[]) null);
        else setVals(new String[] { val });

        return this;
    }

    public Property setVal(int val) {
        return setVal(String.valueOf(val));
    }

    public String[] getCandidateNames() {
        return candidateNames;
    }

    public String[] getCandidateVals() {
        return candidateVals;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public Type getType() {
        return type;
    }

    public boolean isArray() {
        return this.type == Type.array;
    }

    public boolean isFile() {
        return this.type == Type.file;
    }

    protected String getOwner() {
        return owner;
    }

    protected void setOwner(String owner) {
        this.owner = owner;
    }

    protected boolean isConfig() {
        return isConfig;
    }

    protected void setConfig(boolean isConfig) {
        this.isConfig = true;
    }

    /**
     * 设置为静态配置，由框架完成配置值处理。
     * @return
     */
    public Property asConfig() {
        ConfPropertyManager.register(this);
        return this;
    }

    protected Property setDefVal(String defVal) {
        setDefVal(new String[] { defVal });
        return this;
    }

    protected Property setDefVal(String[] defVals) {
        this.defVals = defVals;
        return this;
    }

    protected Property setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    protected void setSourceClass(String sourceClass) {
        this.sourceClass = sourceClass;
    }

    public String getSourceClass() {
        return sourceClass;
    }

    public File getFileVal() {
        if (UPLOAD_FILE_DIR == null) return null;
        String val = getVal();
        if (!StringUtils.isBlank(val)) {
            return new File(UPLOAD_FILE_DIR, val);
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        Property target = (Property) obj;
        return this.owner.equals(target.owner) && this.name.equals(target.name);
    }

    public String toString() {
        return name + "=" + Arrays.toString(this.vals) + "(defVals=" + Arrays.toString(defVals) + ")";
    }

    public static List<Property> findExtPropertys(Object o) throws Exception {
        Method[] methods = o.getClass().getMethods();
        List<Property> extPropertys = new ArrayList<Property>();
        for (Method method : methods) {
            if (Property.class.isAssignableFrom(method.getReturnType()) && method.getParameterTypes().length == 0 && method.getName().startsWith("get")) {
                Property p = (Property) method.invoke(o, null);
                extPropertys.add(p);
            }
        }
        if (extPropertys.size() > 0) {
            return extPropertys;
        }
        return null;
    }
}
