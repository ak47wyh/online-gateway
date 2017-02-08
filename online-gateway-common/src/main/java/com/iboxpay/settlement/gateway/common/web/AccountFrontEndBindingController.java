package com.iboxpay.settlement.gateway.common.web;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.dao.CommonDao;
import com.iboxpay.settlement.gateway.common.dao.FrontEndDao;
import com.iboxpay.settlement.gateway.common.dao.impl.CommonDaoImpl;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.AccountFrontEndBindingEntity;
import com.iboxpay.settlement.gateway.common.domain.FrontEndEntity;
import com.iboxpay.settlement.gateway.common.task.TaskScheduler;

@Controller
@RequestMapping("/manage/accfrontend")
public class AccountFrontEndBindingController {

    CommonDao accountFrontEndDao = CommonDaoImpl.getDao(AccountFrontEndBindingEntity.class);
    @Resource
    FrontEndDao frontEndDao;

    @RequestMapping(value = "list.htm", method = RequestMethod.GET)
    public ModelAndView list() throws Exception {
        List<AccountFrontEndBindingEntity> list = accountFrontEndDao.findByHQL("from AccountFrontEndBindingEntity order by updateTime desc");
        List<FrontEndConfig> frontEndList = frontEndDao.loadAllFrontEndConfig();
        Map<Integer, FrontEndConfig> frontEndMap = new HashMap<Integer, FrontEndConfig>();
        for (FrontEndConfig frontEndConfig : frontEndList) {
            frontEndMap.put(frontEndConfig.getId(), frontEndConfig);
        }
        ModelAndView mv = new ModelAndView();
        mv.addObject("list", list);
        mv.addObject("frontEndList", frontEndList);
        mv.addObject("frontEndMap", frontEndMap);
        mv.setViewName("/views/accfrontend/list");
        return mv;
    }

    @RequestMapping(value = "bind.htm", method = RequestMethod.POST)
    public String bind(@RequestParam(value = "accNo") String accNo, @RequestParam(value = "frontEndId") String frontEndId) throws Exception {
        Integer ifrontEndId = Integer.parseInt(frontEndId.trim());
        AccountFrontEndBindingEntity.Pk pk = getBindingPk(accNo, ifrontEndId);
        AccountFrontEndBindingEntity bindingEntity = (AccountFrontEndBindingEntity) accountFrontEndDao.get(pk);
        FrontEndConfig frontEndConfig = frontEndDao.get(ifrontEndId);
        Date now = new Date();
        if (bindingEntity == null) {
            bindingEntity = new AccountFrontEndBindingEntity();
            bindingEntity.setPk(pk);
            bindingEntity.setCreateTime(now);
            bindingEntity.setUpdateTime(now);
            accountFrontEndDao.save(bindingEntity);
            TaskScheduler.bind(frontEndConfig.getBankName(), pk.getFrontEnd().getId(), pk.getAccount().getAccNo());
        }
        return "redirect:list.htm";
    }

    @RequestMapping(value = "unbind.htm", method = RequestMethod.GET)
    public String unbind(@RequestParam(value = "accNo") String accNo, @RequestParam(value = "frontEndId") String frontEndId) throws Exception {
        Integer ifrontEndId = Integer.parseInt(frontEndId.trim());
        AccountFrontEndBindingEntity.Pk pk = getBindingPk(accNo, ifrontEndId);
        AccountFrontEndBindingEntity bindingEntity = (AccountFrontEndBindingEntity) accountFrontEndDao.get(pk);
        FrontEndConfig frontEndConfig = frontEndDao.get(ifrontEndId);
        if (bindingEntity != null) {
            accountFrontEndDao.delete(pk);
            TaskScheduler.unbind(frontEndConfig.getBankName(), pk.getFrontEnd().getId(), pk.getAccount().getAccNo());
        }
        return "redirect:list.htm";
    }

    private AccountFrontEndBindingEntity.Pk getBindingPk(String accNo, Integer frontEndId) {
        AccountEntity account = new AccountEntity();
        account.setAccNo(accNo);
        FrontEndEntity frontEndEntity = new FrontEndEntity();
        frontEndEntity.setId(frontEndId);
        return new AccountFrontEndBindingEntity.Pk(account, frontEndEntity);
    }
}
