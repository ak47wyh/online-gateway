package com.iboxpay.settlement.gateway.common.util;

import java.beans.Introspector;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.util.ClassUtils;

/**
 * 默认的Spring bean名称是用类名的，不同银行实现时，不小心会造成bean名称相同.
 * 银行组件的bean名称对我们来说是不重要的，我们只用到Spring的组件扫描功能.这里会根据银行名称自动组装bean名称.
 * @author jianbo_chen
 */
public class CustomAnnotationBeanNameGenerator extends AnnotationBeanNameGenerator {

    private final static String PACKAGE_BANK = "com.iboxpay.settlement.gateway";
    private final static String PACKAGE_BANK_PATTERN = "com\\.iboxpay\\.settlement\\.gateway\\.([^\\.]+)\\..*";
    private final static String PACKAGE_COMMON = "com.iboxpay.settlement.gateway.common";

    protected String buildDefaultBeanName(BeanDefinition definition) {
        String beanClassName = definition.getBeanClassName();
        String bankName = null;
        if (beanClassName.startsWith(PACKAGE_BANK) && !beanClassName.startsWith(PACKAGE_COMMON)) {
            bankName = beanClassName.replaceAll(PACKAGE_BANK_PATTERN, "$1");
            String shortClassName = ClassUtils.getShortName(beanClassName);
            return bankName + "_" + Introspector.decapitalize(shortClassName);
        } else {
            return super.buildDefaultBeanName(definition);
        }
    }
}
