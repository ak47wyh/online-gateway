package com.iboxpay.settlement.gateway.common.web;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.iboxpay.settlement.gateway.common.IBankProfile;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.service.AccountService;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

@Controller
@RequestMapping("/manage/payment")
public class PaymentController {

    @Resource
    AccountService accountService;

    @RequestMapping(value = "list.htm", method = RequestMethod.GET)
    public ModelAndView listAccount() {
        ModelAndView mv = new ModelAndView();
        List<AccountEntity> list = accountService.listAccount();
        IBankProfile[] bankProfiles = BankTransComponentManager.getBankProfiles();
        mv.addObject("bankProfiles", bankProfiles);
        mv.addObject("list", list);
        mv.setViewName("/views/payment/list");
        return mv;
    }

    @RequestMapping(value = "pay.htm", method = RequestMethod.GET)
    public ModelAndView payMent(@RequestParam(value = "bankName", required = false) String bankName, @RequestParam(value = "accNo", required = false) String accNo) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/views/payment/pay");
        mv.addObject("from_url_accNo", accNo);
        if (StringUtils.isBlank(bankName) && StringUtils.isBlank(accNo)) {
            mv.addObject("errorMsg", "缺少账号 或 银行信息.");
            return mv;
        }
        if (!StringUtils.isBlank(accNo)) {
            AccountEntity account = accountService.getAccountEntity(accNo);
            mv.addObject("account", account);
        } else if (!StringUtils.isBlank(bankName)) {
            AccountEntity account = BankTransComponentManager.getAccountEntityInstance(bankName);
            mv.addObject("account", account);
        } else {
            throw new RuntimeException("");
        }
        return mv;
    }

    //	@Resource
    //	BankTransController bankTransController;
    //	
    //	@RequestMapping(value="balance.htm", method=RequestMethod.GET)
    //	public void balance(@RequestParam(value="accNo") String accNo, HttpServletResponse response) throws IOException{
    //		String resultStr = bankTransController.trans(TransCode.BALANCE.getCode(), "{\"accNo\": " + accNo + "}");
    //		response.getWriter().write(resultStr);
    //	}
}
