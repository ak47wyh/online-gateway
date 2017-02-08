package com.iboxpay.settlement.gateway.common.web;

import java.util.Date;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;

@Controller
@RequestMapping("/manage/check")
public class PaymentCheckerController {

    @RequestMapping(value = "list.htm", method = RequestMethod.GET)
    public ModelAndView list() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/views/check/list");
        mv.addObject("today", DateTimeUtil.format(new Date(), "yyyy-MM-dd"));
        return mv;
    }
}
