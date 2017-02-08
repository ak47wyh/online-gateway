package com.iboxpay.settlement.gateway.common.web;

import java.io.File;
import java.util.Properties;

import javax.servlet.ServletContextEvent;

import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;
import org.logicalcobwebs.proxool.configuration.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 
 * 类名称：ListenerConfigurator
 * 类描述：proxool初始化
 * 创建人：weiyuanhua
 * 修改人：weiyuanhua
 * 修改时间：2014-1-12 上午11:34:48
 * 修改备注：
 * @version 1.0.0
 */
public class ListenerConfigurator implements javax.servlet.ServletContextListener {

    private final Logger logger = LoggerFactory.getLogger(ListenerConfigurator.class);

    private static final String XML_FILE_PROPERTY = "proxoolConfigLocation";

    private boolean autoShutdown = true;

    public void contextInitialized(ServletContextEvent servletConfig) {

        String appDir = servletConfig.getServletContext().getRealPath("/");

        Properties properties = new Properties();
        String value = servletConfig.getServletContext().getInitParameter(XML_FILE_PROPERTY);
        logger.info("------1------ proxoolConfigLocation:" + value);

        try {
            File file = new File(value);
            if (file.isAbsolute()) {
                JAXPConfigurator.configure(value, false);
            } else {
                JAXPConfigurator.configure(appDir + File.separator + value, false);
            }
        } catch (ProxoolException e) {
            logger.info("------2------ Problem configuring error:" + value, e);
        }

        if (properties.size() > 0) {
            try {
                PropertyConfigurator.configure(properties);
            } catch (ProxoolException e) {
                logger.info("------3------ Problem configuring using init properties error:", e);
            }
        }
    }

    public void contextDestroyed(ServletContextEvent s) {
        if (autoShutdown) {
            ProxoolFacade.shutdown(0);
        }
    }
}