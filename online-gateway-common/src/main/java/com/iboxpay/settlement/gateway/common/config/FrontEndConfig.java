package com.iboxpay.settlement.gateway.common.config;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 前置机配置.支持多个前置机实例.
 * @author jianbo_chen
 */
public abstract class FrontEndConfig implements Serializable {

    private static Logger logger = LoggerFactory.getLogger(FrontEndConfig.class);
    private static final long serialVersionUID = 1L;

    /**通信协议*/
    protected final static String PROPERTY_PROTOCOL = "protocol";
    /**前置机IP*/
    protected final static String PROPERTY_IP = "ip";
    /**前置机端口*/
    protected final static String PROPERTY_PORT = "port";
    /**超时*/
    protected final static String PROPERTY_TIMEOUT = "connect_timeout";
    /**字符集*/
    protected final static String PROPERTY_CHARSET = "charset";
    /**前置机并发数*/
    protected static final String PROPERTY_totalConcurrentNum = "totalConcurrentNum";

    //配置ID
    private int id;

    private String name;

    private String bankName;
    private String bankFullName;

    /**
     * 配置项: 通信协议
     */
    protected Property protocal;
    /**
     * 配置项: IP
     */
    protected Property ip;
    /**
     * 配置项: 端口
     */
    protected Property port;
    /**
     * 配置项: 超时时间
     */
    protected Property timeout;
    /**
     * 配置项: 字符集
     */
    protected Property charset;

    protected Property totalConcurrentNum;//总的并发数
    private volatile int concurrentNum;//当前并发数
    private volatile boolean deprecated;
    private boolean enable;

    private transient List<Property> allProperties;

    public FrontEndConfig() {
        protocal = new Property(PROPERTY_PROTOCOL, "通信协议");
        ip = new Property(PROPERTY_IP, "前置机IP");
        port = new Property(PROPERTY_PORT, "前置机端口");
        timeout = new Property(PROPERTY_TIMEOUT, "60", "网络超时（秒）");
        charset = new Property(PROPERTY_CHARSET, "字符集");
        totalConcurrentNum = new Property(PROPERTY_totalConcurrentNum, "1", "前置机并发数");
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setBankName(String bank) {
        this.bankName = bank;
    }

    public String getBankName() {
        return bankName;
    }

    public String getBankFullName() {
        return bankFullName;
    }

    public void setBankFullName(String bankFullName) {
        this.bankFullName = bankFullName;
    }

    public void setTotalConcurrentNum(int totalConcurrentNum) {
        if (totalConcurrentNum <= 0) throw new IllegalArgumentException("并发数必须大于0");

        if (totalConcurrentNum > 100) throw new IllegalArgumentException("并发数过大");

        //		logger.info("为前置机【" + ip.getVal() + ":" + port.getVal() + "】设置并发数："+totalConcurrentNum);

        this.totalConcurrentNum.setVal(totalConcurrentNum);
    }

    public int getTotalConcurrentNumVal() {
        return totalConcurrentNum.getIntVal();
    }

    public Property getTotalConcurrentNum() {
        return totalConcurrentNum;
    }

    /**
     * 注：不允许银行调用
     */
    public void increaseConcurrentNum() {
        this.concurrentNum++;
    }

    /**
     * 注：不允许银行调用
     */
    public int decreaseConcurrentNum() {
        return concurrentNum--;
    }

    public int getConcurrentNum() {
        return concurrentNum;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public Property getProtocal() {
        return protocal;
    }

    public Property getIp() {
        return ip;
    }

    public Property getPort() {
        return port;
    }

    public Property getTimeout() {
        return timeout;
    }

    public Property getCharset() {
        return charset;
    }

    /**
     * 设置为只读.只限子类在初始化时调用.
     * @param p
     */
    protected static void setReadOnly(Property p) {
        p.setReadOnly(true);
    }

    /**
     * 设置默认值.只限子类在初始化时调用.
     * @param p
     * @param defVal
     */
    protected static void setDefVal(Property p, String defVal) {
        p.setDefVal(defVal);
    }

    /**
     * hardCode前置机配置，测试用。
     * @deprecated
     */
    public List<FrontEndConfig> getHardCodeFrontMachineConfigs() {
        return null;
    }

    /**
     * 获取所有属性.
     * @return
     */
    public List<Property> getAllPropertys() {
        if (allProperties == null) {
            try {
                allProperties = Property.findExtPropertys(this);
            } catch (Exception e) {
                throw new RuntimeException("find ext-propertys error", e);
            }
        }
        return allProperties;
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public boolean equals(Object obj) {
        return this.id == ((FrontEndConfig) obj).id;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isEnable() {
        return enable;
    }

    @Override
    public String toString() {
        List<Property> allProperties = getAllPropertys();
        if (allProperties != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("{id=").append(this.id).append(", name=").append(this.name).append(", bank=").append(this.bankName).append(", totalConcurrentNum(并发数)=").append(this.totalConcurrentNum);

            for (Property property : allProperties) {
                sb.append(", ").append(property.toString());
            }
            sb.append("}");
            return sb.toString();
        }
        return super.toString();
    }
}
