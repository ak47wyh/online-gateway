package com.iboxpay.settlement.gateway.common.config;

import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.config.Property.Type;

/**
 * 系统配置@Resource目的是让Spring扫描配置类
 * @author jianbo_chen
 */
@Service
public class SystemConfig {

    public final static Property threadPoolConfig = new Property("threadPoolSize", "40", "工作线程池线程数量（用于发送网络请求，重启生效）。【注：请勿随意修改！！！】").asConfig();

    public final static Property blackListTestInterval = new Property("blackListTestIntvl", "2", "前置机黑名单探测恢复可用的时间间隔(分钟，默认为2分钟)").asConfig();

    public final static Property payForbdOnConFail = new Property("payForbdOnConFail", "true", "在支付时若网络连接打不开，是否禁用该前置机(true：是, false：否)，如果没有可用前置机后续支付请求将会挂起。").asConfig();

    public final static Property ignoreURIsProperties = new Property("ignoreURIs", Type.array, new String[] { 
																					    		"/services/online", 
																					    		"/services/collect", 
																					    		"/manage/query/stat-statuses.htm", 
																					    		"/manage/user/login",
																					            "/manage/account/list.htm", 
																					            "/manage/system/init.htm",
																					            "/wechat/notify.htm",
																					            "/alipay/notify.htm",
																					            "/wechatwft/notify.htm",
																					            "/jd/pay/notify.htm",
																					            "/jd/refund/notify.htm"}, "不过滤登记的URL").asConfig();

    public final static Property MAX_PAYMENT_SIZE = new Property("maxPaymentSize", "5000", "对外接口最大的批量数为多少笔（之前是500一批，但王府井这个渠道一天只能提交一批...）").asConfig();

}
