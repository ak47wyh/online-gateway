package com.iboxpay.settlement.gateway.common.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.iboxpay.settlement.gateway.common.IBankProfile;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;
import com.iboxpay.settlement.gateway.common.trans.IBankTrans;
import com.iboxpay.settlement.gateway.common.trans.balance.IBalance;
import com.iboxpay.settlement.gateway.common.trans.payment.IPayment;
import com.iboxpay.settlement.gateway.common.trans.payment.PaymentNavigation;
import com.iboxpay.settlement.gateway.common.trans.query.IQueryPayment;

@Controller("indexController")
@RequestMapping("/manage")
public class IndexController {

    @RequestMapping(value = "indexb.htm", method = RequestMethod.GET)
    public ModelAndView index() {
        IBankProfile[] bankProfiles = BankTransComponentManager.getBankProfiles();
        Map<IBankProfile, Map<String, TransInfo[]>> bankTransMap = new HashMap<IBankProfile, Map<String, TransInfo[]>>();
        for (IBankProfile bankProfile : bankProfiles) {
            Map<String, TransInfo[]> bankTrans = new HashMap<String, TransInfo[]>();
            IBankTrans[] balanceTrans = BankTransComponentManager.getBankComponent(bankProfile.getBankName(), IBalance.class);
            IBankTrans[] paymentTrans = BankTransComponentManager.getBankComponent(bankProfile.getBankName(), IPayment.class);
            if (balanceTrans != null && balanceTrans.length > 0) {
                TransInfo balanceInfo = new TransInfo(balanceTrans[0].getBankTransCode(), balanceTrans[0].getBankTransDesc());
                bankTrans.put("balance", new TransInfo[] { balanceInfo });
            }
            if (paymentTrans != null && paymentTrans.length > 0) {
                TransInfo paymentInfos[] = new TransInfo[paymentTrans.length];
                for (int i = 0; i < paymentTrans.length; i++) {
                    IPayment paymentImpl = (IPayment) paymentTrans[i];
                    TransInfo paymentInfo = new TransInfo(paymentImpl.getBankTransCode(), paymentImpl.getBankTransDesc());
                    PaymentNavigation paymentNavigation = paymentImpl.navigate();
                    paymentInfo.setNavigationInfo(paymentNavigation == null ? null : paymentNavigation.toString());
                    Class<? extends IQueryPayment> queryClass = paymentImpl.getQueryClass();
                    if (queryClass != null) {
                        IQueryPayment queryPaymentImpl = (IQueryPayment) BankTransComponentManager.getBankComponent(queryClass);
                        paymentInfo.setQueryCode(queryPaymentImpl.getBankTransCode());
                        paymentInfo.setQueryDesc(queryPaymentImpl.getBankTransDesc());
                    }
                    paymentInfos[i] = paymentInfo;
                    bankTrans.put("pay", paymentInfos);
                }
            }
            bankTransMap.put(bankProfile, bankTrans);
        }

        ModelAndView mv = new ModelAndView();
        mv.setViewName("/views/indexb");
        mv.addObject("bankTransMap", bankTransMap);
        mv.addObject("bankProfiles", bankProfiles);
        return mv;
    }

    @RequestMapping(value = "index.htm", method = RequestMethod.GET)
    public ModelAndView info() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/views/index");
        return mv;
    }

    public static class TransInfo {

        private String code;
        private String desc;

        private String queryCode;
        private String queryDesc;

        private String navigationInfo;

        public TransInfo(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }

        public String getQueryCode() {
            return queryCode;
        }

        public void setQueryCode(String queryCode) {
            this.queryCode = queryCode;
        }

        public void setQueryDesc(String queryDesc) {
            this.queryDesc = queryDesc;
        }

        public String getQueryDesc() {
            return queryDesc;
        }

        public void setNavigationInfo(String navigationInfo) {
            this.navigationInfo = navigationInfo;
        }

        public String getNavigationInfo() {
            return navigationInfo;
        }
    }
}
