package com.iboxpay.settlement.gateway.common.web;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.iboxpay.settlement.gateway.common.IBankProfile;
import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.config.Property;
import com.iboxpay.settlement.gateway.common.dao.CommonDao;
import com.iboxpay.settlement.gateway.common.dao.impl.CommonDaoImpl;
import com.iboxpay.settlement.gateway.common.domain.AccountFrontEndBindingEntity;
import com.iboxpay.settlement.gateway.common.service.FrontEndService;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

@Controller("frontEndManagerController")
@RequestMapping("/manage/frontend")
public class FrontEndManagerController {

    private final Logger logger = LoggerFactory.getLogger(FrontEndManagerController.class);

    CommonDao accountFrontEndDao = CommonDaoImpl.getDao(AccountFrontEndBindingEntity.class);

    @Resource
    private FrontEndService frontEndService;

    @RequestMapping(value = "list.htm", method = RequestMethod.GET)
    public ModelAndView listFrontend() {
        IBankProfile[] bankProfiles = BankTransComponentManager.getBankProfiles();
        List<FrontEndConfig> list = frontEndService.listFrontEnd();
        if (list != null && list.size() > 0) Collections.sort(list, new Comparator<FrontEndConfig>() {

            @Override
            public int compare(FrontEndConfig o1, FrontEndConfig o2) {
                int r = o1.getBankName().compareTo(o2.getBankName());
                if (r == 0)
                    return o1.getName().compareTo(o2.getName());
                else return r;

            }
        });
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/views/frontend/list");
        mv.addObject("list", list);
        mv.addObject("bankProfiles", bankProfiles);
        return mv;
    }

    @RequestMapping(value = "edit.htm", method = RequestMethod.GET)
    public ModelAndView addFrontend(@RequestParam(value = "bankName", required = false) String bankName, @RequestParam(value = "id", required = false) String id) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/views/frontend/edit");
        if (StringUtils.isBlank(bankName) && StringUtils.isBlank(id)) {
            mv.addObject("errorMsg", "缺少前置机配置ID 或 银行信息.");
            return mv;
        }

        if (!StringUtils.isBlank(bankName)) {
            FrontEndConfig frontEndConfig = BankTransComponentManager.getFrontEndConfigInstance(bankName);
            mv.addObject("frontEndConfig", frontEndConfig);
        } else if (!StringUtils.isBlank(id)) {
            FrontEndConfig frontEndConfig = frontEndService.getFrontEnd(Integer.parseInt(id));
            mv.addObject("frontEndConfig", frontEndConfig);
        } else {
            throw new RuntimeException("");
        }
        return mv;

    }

    @RequestMapping(value = "edit.htm", method = RequestMethod.POST)
    public ModelAndView doAddFrontend(@RequestParam Map<String, String> params) {
        String id = params.remove("frontend_id");
        String bankName = params.remove("frontend_bankName");
        String name = params.remove("frontend_name");
        FrontEndConfig frontEndConfig;
        boolean isNew = true;

        if (!StringUtils.isBlank(id)) {
            frontEndConfig = frontEndService.getFrontEnd(Integer.parseInt(id));
            isNew = false;
        } else {
            frontEndConfig = BankTransComponentManager.getFrontEndConfigInstance(bankName);
        }
        frontEndConfig.setName(name);
        List<Property> propertys = frontEndConfig.getAllPropertys();

        if (!StringUtils.isBlank(bankName)) frontEndConfig.setBankName(bankName);

        for (Property property : propertys) {
            if (property.isReadOnly()) continue;
            String value = params.get(property.getName());
            property.setVal(value);
        }
        if (isNew)
            frontEndService.addFrontEnd(frontEndConfig);
        else frontEndService.updateFrontEnd(frontEndConfig);

        ModelAndView mv = new ModelAndView();
        mv.setViewName("/views/frontend/edit");
        mv.addObject("frontEndConfig", frontEndConfig);
        mv.addObject("success", true);
        return mv;
    }

    @RequestMapping(value = "delete.htm", method = RequestMethod.GET)
    public String deleteFrontend(@RequestParam(value = "id", required = false) String id) {
        int frontEndId = Integer.parseInt(id);
        FrontEndConfig frontEndConfig = frontEndService.getFrontEnd(Integer.parseInt(id));
        List list = accountFrontEndDao.findByHQL("from AccountFrontEndBindingEntity where pk.frontEnd.id = ?", frontEndId);
        String msg = null;
        if (list == null || list.size() == 0)
            frontEndService.deleteFrontEnd(frontEndConfig);
        else msg = "binding";
        return "redirect:list.htm?msg=" + (msg == null ? "" : msg);
    }

    @RequestMapping(value = "enable.htm", method = RequestMethod.GET)
    public String enable(@RequestParam(value = "id", required = true) String id, HttpServletRequest request) {
        String Referer = request.getHeader("Referer");
        FrontEndConfig frontEndConfig = frontEndService.getFrontEnd(Integer.parseInt(id));
        frontEndConfig.setEnable(true);
        frontEndService.updateFrontEnd(frontEndConfig);
        if (StringUtils.isBlank(Referer))
            return "redirect:list.htm";
        else return "redirect:" + Referer;
    }

    @RequestMapping(value = "disable.htm", method = RequestMethod.GET)
    public String disable(@RequestParam(value = "id", required = true) String id, HttpServletRequest request) {
        String Referer = request.getHeader("Referer");
        FrontEndConfig frontEndConfig = frontEndService.getFrontEnd(Integer.parseInt(id));
        frontEndConfig.setEnable(false);
        frontEndService.updateFrontEnd(frontEndConfig);
        if (StringUtils.isBlank(Referer))
            return "redirect:list.htm";
        else return "redirect:" + Referer;
    }
}
