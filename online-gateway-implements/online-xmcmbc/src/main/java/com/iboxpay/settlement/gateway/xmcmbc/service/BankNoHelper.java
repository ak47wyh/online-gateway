/*
 * Copyright (C) 2011-2015 ShenZhen iBOXPAY Information Technology Co.,Ltd.
 * 
 * All right reserved.
 * 
 * This software is the confidential and proprietary
 * information of iBoxPay Company of China. 
 * ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only
 * in accordance with the terms of the contract agreement 
 * you entered into with iBoxpay inc.
 *
 */
package com.iboxpay.settlement.gateway.xmcmbc.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The class BankNoHelper.
 *
 * Description: 根据银行名称将其转变为对应的银联机构号
 *
 * @author: weiyuanhua
 * @since: 2016年2月29日 下午5:06:54 	
 * @version: $Revision$ $Date$ $LastChangedBy$
 *
 */
public class BankNoHelper {

    private static Map<String, String> bankNoMap;

    private static class BankNo {

        public final String bankno;
        public final String bankName;

        public BankNo(String bankno, String bankName) {
            this.bankno = bankno;
            this.bankName = bankName;
        }
    }

    /**
     * 银行代码转换
     * @param bankFullName
     * @return
     */
    public static String convertBankNo(String bankFullName) {
        if (bankNoMap == null) {
            synchronized (BankNoHelper.class) {
                if (bankNoMap == null) {
                	bankNoMap = new HashMap<String, String>();
                    List<BankNo> list = init();
                    for (BankNo bankNo : list) {
                        String shortNames[] = bankNo.bankName.split(",");
                        for (String shortName : shortNames)
                        	bankNoMap.put(shortName, bankNo.bankno);
                    }
                    bankNoMap = Collections.unmodifiableMap(bankNoMap);
                }
            }
        }
        
        if (bankFullName != null) {
            for (Map.Entry<String, String> entry : bankNoMap.entrySet()) {
                if (bankFullName.indexOf(entry.getKey()) != -1) return entry.getValue();
            }
        }
        
        return null;
    }

    /**
     * 银行代码列表
     * @return
     */
    private static List<BankNo> init() {
        List<BankNo> list = new LinkedList<BankNo>();
        list.add(new BankNo("01020000","工商银行,工商"));
        list.add(new BankNo("01030000","农业银行,农业"));
        list.add(new BankNo("01040000","中国银行"));
        list.add(new BankNo("01050000","建设银行,建设"));
        list.add(new BankNo("01000000","邮储银行,邮政储蓄"));
        list.add(new BankNo("03080000","招商银行,招商"));
        list.add(new BankNo("03030000","光大银行,光大"));
        list.add(new BankNo("03060000","广发银行,广发"));
        list.add(new BankNo("03040000","华夏银行,华夏"));
        list.add(new BankNo("03090000","兴业银行,兴业"));
        list.add(new BankNo("03020000","中信银行,中信"));
        list.add(new BankNo("03070000","平安银行,平安"));
        list.add(new BankNo("03010000","交通银行,交通"));
        list.add(new BankNo("03100000","浦东发展银行,浦发银行,浦发"));
		list.add(new BankNo("04470000","兰州银行,兰州"));
		
        return list;
    }
}
