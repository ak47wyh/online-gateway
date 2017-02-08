package com.iboxpay.settlement.gateway.common.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.iboxpay.settlement.gateway.common.IBankProfile;
import com.iboxpay.settlement.gateway.common.config.ConfPropertyManager;
import com.iboxpay.settlement.gateway.common.config.Property;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

@Controller
@RequestMapping("/manage/config")
public class ConfigManagerController {

    @RequestMapping(value = "list.htm", method = RequestMethod.GET)
    public ModelAndView listConfig() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/views/config/list");
        mv.addObject("banksConfProps", ConfPropertyManager.getBanksConfProps());
        IBankProfile bankProfiles[] = BankTransComponentManager.getBankProfiles();
        Map<String, IBankProfile> bankProfileMap = new HashMap<String, IBankProfile>();
        for (IBankProfile bankProfile : bankProfiles) {
            bankProfileMap.put(bankProfile.getBankName(), bankProfile);
        }
        mv.addObject("bankProfileMap", bankProfileMap);
        return mv;
    }

    @RequestMapping(value = "set.htm", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> setConfig(@RequestParam(value = "bankName") String bankName, @RequestParam(value = "propertyName") String propertyName, @RequestParam(value = "value") String[] values) {
        Map<String, String> result = new HashMap<String, String>();
        List<Property> bankConfigs = ConfPropertyManager.getBanksConfProps().get(bankName);
        if (bankConfigs != null) {
            for (Property bankConfig : bankConfigs) {
                if (bankConfig.getName().equals(propertyName)) {
                    if (bankConfig.isArray()) {
                        if (values == null || values.length == 0) {
                            bankConfig.setVals((String[]) null);
                        } else {
                            List<String> valuesList = new ArrayList<String>();
                            for (String value : values)
                                if (!StringUtils.isBlank(value)) valuesList.add(value.trim());
                            bankConfig.setVals(valuesList.toArray(new String[0]));
                        }
                    } else {
                        bankConfig.setVal(values != null && values.length != 0 ? values[0] : null);
                    }
                    ConfPropertyManager.save(bankConfig);
                    result.put("status", "success");
                    result.put("statusMsg", "提交成功.");
                    return result;
                }
            }
        }
        result.put("status", "fail");
        result.put("statusMsg", "找不到银行或属性.");
        return result;
    }

}
