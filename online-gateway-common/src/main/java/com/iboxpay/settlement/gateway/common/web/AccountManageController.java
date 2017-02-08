package com.iboxpay.settlement.gateway.common.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.iboxpay.settlement.gateway.common.IBankProfile;
import com.iboxpay.settlement.gateway.common.config.Property;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.AccountTransConfig;
import com.iboxpay.settlement.gateway.common.service.AccountService;
import com.iboxpay.settlement.gateway.common.service.AccountTransConfigService;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;
import com.iboxpay.settlement.gateway.common.trans.IBankTrans;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.balance.IBalance;
import com.iboxpay.settlement.gateway.common.trans.detail.IDetail;
import com.iboxpay.settlement.gateway.common.trans.payment.IPayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

@Controller
@RequestMapping("/manage/account")
public class AccountManageController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountManageController.class);
    
    @Resource
    AccountService accountService;
    @Resource
    AccountTransConfigService accountTransConfigService;

    @RequestMapping(value = "list.htm", method = RequestMethod.GET)
    public ModelAndView listAccount(@RequestParam(value = "type", required = false) String type, HttpServletResponse response) throws Exception {
        List<AccountEntity> list = accountService.listAccount();
        //按银行排下序
        if (list != null && list.size() > 0) {
            Collections.sort(list, new Comparator<AccountEntity>() {

                @Override
                public int compare(AccountEntity o1, AccountEntity o2) {
                    return o1.getBankName().compareTo(o2.getBankName());
                }
            });
        }
        if ("json".equalsIgnoreCase(type)) {
            List<Map<String, Object>> accounts = new ArrayList<Map<String, Object>>();
            for (AccountEntity accountEntity : list) {
                Map<String, Object> account = new HashMap<String, Object>();
                accounts.add(account);
                account.put("accNo", accountEntity.getAccNo());
                account.put("accName", accountEntity.getAccName());
                account.put("bankName", accountEntity.getBankName());
                account.put("bankFullName", accountEntity.getBankFullName());
                account.put("cnaps", accountEntity.getCnaps());
            }
            response.setContentType("application/json; charset=utf-8");
            response.getOutputStream().write(JsonUtil.toJson(accounts).getBytes("utf-8"));
            return null;
        } else {
            ModelAndView mv = new ModelAndView();
            IBankProfile[] bankProfiles = BankTransComponentManager.getBankProfiles();
            mv.addObject("bankProfiles", bankProfiles);
            mv.addObject("list", list);
            mv.setViewName("/views/account/list");
            return mv;
        }
    }

    @RequestMapping(value = "edit.htm", method = RequestMethod.GET)
    public ModelAndView addAccount(@RequestParam(value = "bankName", required = false) String bankName, @RequestParam(value = "accNo", required = false) String accNo) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/views/account/edit");
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

    @RequestMapping(value = "edit.htm", method = RequestMethod.POST)
    public ModelAndView doAddAccount(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/views/account/edit");
        String oldAccNo = request.getParameter("from_url_accNo");
        String bankName = request.getParameter("bankName");
        String accNo = request.getParameter("accNo");
        String accName = request.getParameter("accName");
        String cnaps = request.getParameter("cnaps");
        AccountEntity accountEntity = BankTransComponentManager.getAccountEntityInstance(bankName);
        if (StringUtils.isBlank(accNo)) {
            fillPropertys(request, accountEntity);
            mv.addObject("account", accountEntity);
            mv.addObject("input_error", "账号必须填写");
            return mv;
        }
        if (StringUtils.isBlank(accName)) {
            fillPropertys(request, accountEntity);
            mv.addObject("account", accountEntity);
            mv.addObject("input_error", "账户名必须填写");
            return mv;
        }
        if (StringUtils.isBlank(cnaps) || !cnaps.trim().matches("\\d{12}")) {
            fillPropertys(request, accountEntity);
            mv.addObject("account", accountEntity);
            mv.addObject("input_error", "联行号未填写 或 格式不正确(必须12位数字)");
            return mv;
        }
        if (!StringUtils.isBlank(oldAccNo)) {
            if (!oldAccNo.equals(accNo)) { //账号改了
                //先删除旧的
                accountEntity = accountService.getAccountEntity(oldAccNo);
                accountService.deleteAccountEntity(accountEntity);
                //再添加新的
                accountEntity = BankTransComponentManager.getAccountEntityInstance(bankName);
                fillPropertys(request, accountEntity);
                accountService.addAccountEntity(accountEntity);
            } else {
                accountEntity = accountService.getAccountEntity(accNo);
                if (accountEntity == null) throw new RuntimeException("账号不存在：" + accNo);

                fillPropertys(request, accountEntity);
                accountService.updateAccountEntity(accountEntity);
            }
        } else {//新增
            AccountEntity existAccountEntity = accountService.getAccountEntity(accNo);
            if (existAccountEntity == null) {
                accountEntity = BankTransComponentManager.getAccountEntityInstance(bankName);
                fillPropertys(request, accountEntity);
                accountService.addAccountEntity(accountEntity);
            } else {//账号已存在
                fillPropertys(request, accountEntity);
                mv.addObject("account", accountEntity);
                mv.addObject("input_error", "账号已存在");
                return mv;
            }
        }
        mv.addObject("from_url_accNo", accountEntity.getAccNo());
        mv.addObject("account", accountEntity);
        mv.addObject("success", true);
        return mv;
    }

    private void fillPropertys(HttpServletRequest request, AccountEntity accountEntity) {
        String accNo = request.getParameter("accNo");
        String accName = request.getParameter("accName");
        String bankBranchName = request.getParameter("bankBranchName");
        String areaCode = request.getParameter("areaCode");
        String cnaps = request.getParameter("cnaps");
        String currency = request.getParameter("currency");
        String bankDefault = request.getParameter("bankDefault");
        String transConfigEnabled = request.getParameter("transConfigEnabled");

        accountEntity.setAccNo(StringUtils.trim(accNo));
        accountEntity.setAccName(StringUtils.trim(accName));
        accountEntity.setBankBranchName(StringUtils.trim(bankBranchName));
        accountEntity.setAreaCode(StringUtils.trim(areaCode));
        accountEntity.setCnaps(StringUtils.trim(cnaps));
        accountEntity.setCurrency(StringUtils.trim(currency));
        accountEntity.setIsBankDefault(Boolean.valueOf(bankDefault));
        accountEntity.setTransConfigEnabled(Boolean.valueOf(transConfigEnabled));
        List<Property> extPropertys = accountEntity.getExtPropertys();
        if (extPropertys != null && extPropertys.size() > 0) {
            for (Property property : extPropertys) {
                if (property.isReadOnly()) continue;
                if (property.isArray()) {
                    String values[] = request.getParameterValues("ext_" + property.getName());
                    List<String> availableList = new LinkedList<String>();
                    for (String value : values) {
                        if (!StringUtils.isBlank(value)) availableList.add(StringUtils.trim(value));
                    }
                    property.setVals(availableList.toArray(new String[0]));
                } else {
                    String value = request.getParameter("ext_" + property.getName());
                    property.setVal(StringUtils.trim(value));
                }
            }
        }
    }

    @RequestMapping(value = "delete.htm", method = RequestMethod.GET)
    //POST请求有点麻烦，先用GET吧
    public String deleteAccount(@RequestParam(value = "accNo") String accNo) {
        AccountEntity accountEntity = accountService.getAccountEntity(accNo);
        accountService.deleteAccountEntity(accountEntity);
        return "redirect:list.htm";
    }

    @Resource
    BankTransController bankTransController;

    @RequestMapping(value = "balance.htm", method = RequestMethod.GET)
    public void balance(@RequestParam(value = "accNo") String accNo, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String resultStr = bankTransController.trans(TransCode.BALANCE.getCode(), "{\"appCode\": \"" + accNo + "\"}", request);
        response.getWriter().write(resultStr);
    }

    @RequestMapping(value = "trans-config.htm", method = RequestMethod.GET)
    public ModelAndView transConfig(@RequestParam(value = "accNo") String accNo, @RequestParam(value = "type", required = false) String type) {
        ModelAndView mv = new ModelAndView("/views/account/trans-config");
        AccountEntity accountEntity = accountService.getAccountEntity(accNo);
        mv.addObject("transComponentType", IPayment.class.getName());
        mv.addObject("IPayment", IPayment.class);
        mv.addObject("IBalance", IBalance.class);
        mv.addObject("IDetail", IDetail.class);
        if ("default".equals(type)) {
            Map<Class<? extends IBankTrans>, IBankTrans[]> defTransMap = new LinkedHashMap<Class<? extends IBankTrans>, IBankTrans[]>(); 
            IBankTrans[] paymentTrans = BankTransComponentManager.getBankComponent(accountEntity.getBankName(), IPayment.class);
            defTransMap.put(IPayment.class, paymentTrans);
            IBankTrans[] balanceTrans = BankTransComponentManager.getBankComponent(accountEntity.getBankName(), IBalance.class);
            defTransMap.put(IBalance.class, balanceTrans);
            IBankTrans[] detailTrans = BankTransComponentManager.getBankComponent(accountEntity.getBankName(), IDetail.class);
            defTransMap.put(IDetail.class, detailTrans);
            mv.addObject("defTransMap", defTransMap);
        } else {
            Map<Class<? extends IBankTrans>, AccountTransConfig[]> accountTransConfigsMap = accountTransConfigService.getTransConfigsByAccount(accountEntity);
//            AccountTransConfig[] transConfigs = accountTransConfigsMap.get(IPayment.class);
            mv.addObject("accountTransConfigsMap", accountTransConfigsMap);
        }
        return mv;
    }

    @RequestMapping(value = "trans-config.htm", method = RequestMethod.POST, produces = "application/json")
    public @ResponseBody Map<String, String> submitTransConfig(@RequestParam(value = "accNo") String accNo,
            @RequestBody String reqBody) {
        LOGGER.info("修改账号{}的交易接口配置: {}", accNo, reqBody);
        Map<String, String> respMap = new HashMap<String, String>();
        if(accNo == null || accNo.length() == 0){
            respMap.put("status", "fail");
            respMap.put("statusMsg", "accNo参数为空");
            return respMap;
        }
        AccountEntity accountEntity = accountService.getAccountEntity(accNo);
        Object[] transConfigs;
        try {
            transConfigs = (Object[]) JsonUtil.jsonToObject(reqBody, "UTF-8", Object[].class);
        } catch (Exception e) {
            respMap.put("status", "fail");
            respMap.put("statusMsg", "解析请求参数异常");
            return respMap;
        }
        List<AccountTransConfig> accountTransConfigList = new LinkedList<AccountTransConfig>();
        int transOrder = transConfigs.length;
        if (transConfigs != null && transConfigs.length > 0) {
            for (Object o : transConfigs) {
                Map transConfig = (Map) o;
                String componentName = (String) transConfig.get("name");
                Boolean componentEnabled = (Boolean) transConfig.get("componentEnabled");
                String transComponentType = (String) transConfig.get("transComponentType");
                AccountTransConfig accountTransConfig = new AccountTransConfig();
                accountTransConfig.setPk(new AccountTransConfig.Pk(accountEntity, componentName));
                accountTransConfig.setComponentEnabled(componentEnabled == true ? true : false);
                accountTransConfig.setTransComponentType(transComponentType);
                accountTransConfig.setTransOrder(transOrder);//值越大优先级越高
                accountTransConfigList.add(accountTransConfig);
                transOrder--;
            }
            accountTransConfigService.saveAccountTransConfig(accountEntity, accountTransConfigList);
        }
        respMap.put("status", "success");
        respMap.put("statusMsg", "保存成功");

        return respMap;
    }
    @RequestMapping(value = "delete-trans-config.htm", method = RequestMethod.GET)
    public String deleteTransConfig(@RequestParam(value = "accNo") String accNo, @RequestParam(value = "transComponent")String transComponent) {
        LOGGER.info("删除接口配置：账号-{}， 接口-{}", accNo, transComponent);
        AccountEntity accountEntity = accountService.getAccountEntity(accNo);
        AccountTransConfig accountTransConfig = new AccountTransConfig();
        accountTransConfig.setPk(new AccountTransConfig.Pk(accountEntity, transComponent));
        accountTransConfigService.deleteAccountTransConfig(accountTransConfig);
        return "redirect: trans-config.htm?accNo=" + accNo;
    }

}
