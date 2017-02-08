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
package com.iboxpay.settlement.gateway.kq.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
/**
 * 根据银行名称将其转变为对应的银行简码
 * @author liaoxiongjian
 * @date 2015-12-22 14:45
 */
public class KQBankNoHelper {

    private static Map<String, String> KQBankNoMap;

    private static class BankNo {

        public final String bankno;
        public final String shortName;

        public BankNo(String bankno, String shortName) {
            this.bankno = bankno;
            this.shortName = shortName;
        }
    }

    /**
     * 银行代码转换
     * @param bankFullName
     * @return
     */
    public static String convertBankNo(String bankFullName) {
        if (KQBankNoMap == null) {
            synchronized (KQBankNoHelper.class) {
                if (KQBankNoMap == null) {
                	KQBankNoMap = new HashMap<String, String>();
                    List<BankNo> list = init();
                    for (BankNo bankNo : list) {
                        String shortNames[] = bankNo.shortName.split(",");
                        for (String shortName : shortNames)
                        	KQBankNoMap.put(shortName, bankNo.bankno);
                    }
                    KQBankNoMap = Collections.unmodifiableMap(KQBankNoMap);
                }
            }
        }
        
        if (bankFullName != null) {
            for (Map.Entry<String, String> entry : KQBankNoMap.entrySet()) {
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
        list.add(new BankNo("ICBC","工商银行"));
        list.add(new BankNo("CCB","建设银行"));
        list.add(new BankNo("CMB","招商银行"));
        list.add(new BankNo("CEB","光大银行"));
		list.add(new BankNo("ABC","农业银行"));
		list.add(new BankNo("RCB","农信银,农村商业银行,农村信用社"));
		list.add(new BankNo("BOC","中国银行"));
		list.add(new BankNo("BCOM","交通银行"));
		list.add(new BankNo("POST","邮储银行,邮政储蓄"));
		list.add(new BankNo("SDB","深圳发展银行"));
		list.add(new BankNo("CGB","广州发展银行"));
		list.add(new BankNo("CIB","兴业银行"));
		list.add(new BankNo("CITIC","中信银行"));
		list.add(new BankNo("SPDB","浦东发展银行,浦发银行,浦发"));
		list.add(new BankNo("CMBC","民生银行"));
		list.add(new BankNo("PAB","平安银行"));
		list.add(new BankNo("HXB","华夏银行"));
		list.add(new BankNo("SHB","上海银行"));
		list.add(new BankNo("GZRCB","广州农村商业银行"));
		list.add(new BankNo("HSB","徽商银行"));
		list.add(new BankNo("BHB","渤海银行"));
		list.add(new BankNo("JSB","江苏银行"));
        return list;
    }
}
