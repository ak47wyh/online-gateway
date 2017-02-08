package com.iboxpay.settlement.gateway.common.trans.fetest;

import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.settlement.gateway.common.SystemManager;
import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;

public class FrontEndTestDelegateService {

    private static Logger logger = LoggerFactory.getLogger(FrontEndTestDelegateService.class);
    private final static IFrontEndTest defaultFrontEndTestImpl = new DefaultFrontEndTestImpl();

    public static boolean testFeConnection(FrontEndConfig feConfig) {
        try {
            logger.info("探测前置机是否可用：" + feConfig);
            IFrontEndTest feTest = getBankFeTest(feConfig);
            if (feTest == null) feTest = defaultFrontEndTestImpl;

            return feTest.testConnection(feConfig);
        } catch (Exception e) {
            logger.error("", e);
        }
        return false;
    }

    private static IFrontEndTest getBankFeTest(FrontEndConfig feConfig) {
        Map<String, IFrontEndTest> feTestImplsMap = SystemManager.getSpringContext().getBeansOfType(IFrontEndTest.class);
        if (feTestImplsMap != null && feTestImplsMap.size() > 0) {
            for (Iterator<IFrontEndTest> itr = feTestImplsMap.values().iterator(); itr.hasNext();) {
                IFrontEndTest feTest = itr.next();
                String bankName = BankTransComponentManager.getBankNameByPackage(feTest.getClass().getName());
                if (feConfig.getBankName().equals(bankName)) {
                    return feTest;
                }
            }
        }
        return null;
    }

}
