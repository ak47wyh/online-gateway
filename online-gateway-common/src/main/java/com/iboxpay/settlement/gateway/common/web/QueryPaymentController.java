package com.iboxpay.settlement.gateway.common.web;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.iboxpay.settlement.gateway.common.config.Property;
import com.iboxpay.settlement.gateway.common.dao.PaymentDao;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.page.PageBean;
import com.iboxpay.settlement.gateway.common.service.AccountService;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

@Controller
@RequestMapping("/manage/query")
public class QueryPaymentController {

    @Resource
    PaymentDao paymentDao;

    @Resource
    AccountService accountService;

    @RequestMapping(value = "list.htm", method = RequestMethod.GET)
    public ModelAndView list(@RequestParam(value = "page", required = false) String page, @RequestParam(value = "transDate", required = false) String transDateStr,
            @RequestParam(value = "bankName", required = false) String bankName, @RequestParam(value = "accNo", required = false) String accNo) throws ParseMessageException {
        Date transDate = null;
        List<Object> params = new LinkedList<Object>();
        StringBuffer sql = new StringBuffer("from BatchPaymentEntity where 1=1 ");
        sql.append(" and transDate >= ? and transDate < ?");
        if (!StringUtils.isBlank(transDateStr)) {
            transDate = DateTimeUtil.parseDate(transDateStr, "yyyy-MM-dd");
        } else {
            transDate = new Date();
        }
        params.add(DateTimeUtil.truncateTime(transDate));
        params.add(DateTimeUtil.addDay(DateTimeUtil.truncateTime(transDate), 1));
        if (!StringUtils.isBlank(bankName)) {
            sql.append(" and bankName = ?");
            params.add(bankName);
        }
        if (!StringUtils.isBlank(accNo)) {
            sql.append(" and accNo = ?");
            params.add(accNo);
        }

        sql.append(" order by transDate desc,batch_seq_id desc");//bug fixed: 并发提交时，交易时间一样会导致翻页数据混乱，再添加个批次号就行了

        int pageNo = 1;
        try {
            pageNo = Integer.parseInt(page);
        } catch (Exception e) {}
        pageNo = pageNo <= 0 ? 1 : pageNo;
        PageBean pageBean = paymentDao.findPage(pageNo, 20, sql.toString(), params.toArray(new Object[0]));
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/views/query/list");
        mv.addObject("pageBean", pageBean);
        mv.addObject("transDate", DateTimeUtil.format(transDate, "yyyy-MM-dd"));
        mv.addObject("bankProfiles", BankTransComponentManager.getBankProfiles());
        mv.addObject("accounts", accountService.listAccount());
        return mv;
    }

    @RequestMapping(value = "stat-status.htm", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> statStatus(@RequestParam(value = "batchSeqId") String batchSeqId) {
        return getStatusMap(batchSeqId);
    }

    @RequestMapping(value = "stat-statuses.htm", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, String>> statStatuses(@RequestParam(value = "batchSeqId") String batchSeqIdStr) {
        String batchSeqIds[] = batchSeqIdStr.split(",");
        List<Map<String, String>> result = new LinkedList<Map<String, String>>();
        if (batchSeqIds != null && batchSeqIds.length > 0) {
            for (String batchSeqId : batchSeqIds) {
                result.add(getStatusMap(batchSeqId));
            }
        }
        return result;
    }

    private Map<String, String> getStatusMap(String batchSeqId) {
        Map<String, String> resultMap = new HashMap<String, String>();
        List result = paymentDao.findByHQL("select status,count(status) from PaymentEntity where batchSeqId=? group by status", batchSeqId);
        int init = 0, processing = 0, submmited = 0, success = 0, fail = 0, cancle = 0, other = 0;
        if (result != null) {
            for (int i = 0; i < result.size(); i++) {
                Object[] statusStats = (Object[]) result.get(i);
                int status = (Integer) statusStats[0];
                long statusCount = (Long) statusStats[1];
                switch (status) {
                    case PaymentStatus.STATUS_INIT:
                        init += statusCount;
                        break;
                    case PaymentStatus.STATUS_TO_SUBMIT:
                        processing += statusCount;
                        break;
                    case PaymentStatus.STATUS_SUBMITTED:
                        submmited += statusCount;
                        break;
                    case PaymentStatus.STATUS_SUCCESS:
                        success += statusCount;
                        break;
                    case PaymentStatus.STATUS_FAIL:
                        fail += statusCount;
                        break;
                    case PaymentStatus.STATUS_CANCEL:
                        cancle += statusCount;
                        break;
                    default:
                        other += statusCount;
                        break;
                }
            }
        }
        resultMap.put("batchSeqId", batchSeqId);
        resultMap.put("init", String.valueOf(init));
        resultMap.put("processing", String.valueOf(processing));
        resultMap.put("submmited", String.valueOf(submmited));
        resultMap.put("success", String.valueOf(success));
        resultMap.put("fail", String.valueOf(fail));
        resultMap.put("cancle", String.valueOf(cancle));
        resultMap.put("other", String.valueOf(other));
        return resultMap;
    }

    @RequestMapping(value = "detail.htm", method = RequestMethod.GET)
    public ModelAndView detail(HttpServletRequest request, @RequestParam(value = "batchSeqId", required = false) String batchSeqId, @RequestParam(value = "pageNo", required = false) Integer pageNo) {
        pageNo = pageNo == null || pageNo <= 0 ? 1 : pageNo;
        int pageSize = 500;
        PageBean pageBean = paymentDao.findPage(pageNo, pageSize, "from PaymentEntity where batchSeqId=? ", batchSeqId);
        List<PaymentEntity> payments = pageBean.getResult();
        int init = 0, processing = 0, submmited = 0, success = 0, fail = 0, cancle = 0, other = 0;
        for (PaymentEntity paymentEntity : payments) {
            switch (paymentEntity.getStatus()) {
                case PaymentStatus.STATUS_INIT:
                    init++;
                    break;
                case PaymentStatus.STATUS_TO_SUBMIT:
                    processing++;
                    break;
                case PaymentStatus.STATUS_SUBMITTED:
                    submmited++;
                    break;
                case PaymentStatus.STATUS_SUCCESS:
                    success++;
                    break;
                case PaymentStatus.STATUS_FAIL:
                    fail++;
                    break;
                case PaymentStatus.STATUS_CANCEL:
                    cancle++;
                    break;
                default:
                    other++;
                    break;
            }
        }
        ModelAndView mv = new ModelAndView();
        mv.addObject("init", String.valueOf(init));
        mv.addObject("processing", String.valueOf(processing));
        mv.addObject("submmited", String.valueOf(submmited));
        mv.addObject("success", String.valueOf(success));
        mv.addObject("fail", String.valueOf(fail));
        mv.addObject("cancle", String.valueOf(cancle));
        mv.addObject("other", String.valueOf(other));
        String query = request.getRequestURI() + "?" + request.getQueryString().replaceAll("&pageNo=\\d+", "");
        mv.addObject("priorPageHref", (pageBean.getPageNo() - 1) <= 0 ? null : query + "&pageNo=" + (pageBean.getPageNo() - 1));
        mv.addObject("nextPageHref", (pageBean.getPageNo() + 1) > pageBean.getTotalPages() ? null : query + "&pageNo=" + (pageBean.getPageNo() + 1));
        mv.addObject("pageNo", pageBean.getPageNo());
        mv.addObject("totalPage", pageBean.getTotalPages());
        mv.setViewName("/views/query/detail");
        mv.addObject("payments", payments);
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
    public ModelAndView doAddAccount(@RequestParam Map<String, String> params) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/views/account/edit");
        String oldAccNo = params.get("from_url_accNo");
        String bankName = params.get("bankName");
        String accNo = params.get("accNo");
        String accName = params.get("accName");
        AccountEntity accountEntity = BankTransComponentManager.getAccountEntityInstance(bankName);
        if (StringUtils.isBlank(accNo)) {
            fillPropertys(params, accountEntity);
            mv.addObject("account", accountEntity);
            mv.addObject("input_error", "账号必须填写");
            return mv;
        }
        if (StringUtils.isBlank(accName)) {
            fillPropertys(params, accountEntity);
            mv.addObject("account", accountEntity);
            mv.addObject("input_error", "账户名必须填写");
            return mv;
        }
        if (!StringUtils.isBlank(oldAccNo)) {
            if (!oldAccNo.equals(accNo)) { //账号改了
                //先删除旧的
                accountEntity = accountService.getAccountEntity(oldAccNo);
                accountService.deleteAccountEntity(accountEntity);
                //再添加新的
                accountEntity = BankTransComponentManager.getAccountEntityInstance(bankName);
                fillPropertys(params, accountEntity);
                accountService.addAccountEntity(accountEntity);
            } else {
                accountEntity = accountService.getAccountEntity(accNo);
                if (accountEntity == null) throw new RuntimeException("账号不存在：" + accNo);

                fillPropertys(params, accountEntity);
                accountService.updateAccountEntity(accountEntity);
            }
        } else {//新增
            AccountEntity existAccountEntity = accountService.getAccountEntity(accNo);
            if (existAccountEntity == null) {
                accountEntity = BankTransComponentManager.getAccountEntityInstance(bankName);
                fillPropertys(params, accountEntity);
                accountService.addAccountEntity(accountEntity);
            } else {//账号已存在
                fillPropertys(params, accountEntity);
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

    private void fillPropertys(Map<String, String> params, AccountEntity accountEntity) {
        String accNo = params.get("accNo");
        String accName = params.get("accName");
        String bankBranchName = params.get("bankBranchName");
        String areaCode = params.get("areaCode");
        String cnaps = params.get("cnaps");
        String currency = params.get("currency");
        String bankDefault = params.get("bankDefault");

        accountEntity.setAccNo(StringUtils.trim(accNo));
        accountEntity.setAccName(StringUtils.trim(accName));
        accountEntity.setBankBranchName(StringUtils.trim(bankBranchName));
        accountEntity.setAreaCode(StringUtils.trim(areaCode));
        accountEntity.setCnaps(StringUtils.trim(cnaps));
        accountEntity.setCurrency(StringUtils.trim(currency));
        accountEntity.setIsBankDefault(Boolean.valueOf(bankDefault));
        List<Property> extPropertys = accountEntity.getExtPropertys();
        if (extPropertys != null && extPropertys.size() > 0) {
            for (Property property : extPropertys) {
                if (property.isReadOnly()) continue;
                String value = params.get("ext_" + property.getName());
                property.setVal(StringUtils.trim(value));
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

    //	@Resource
    //	BankTransController bankTransController;
    //	
    //	@RequestMapping(value="balance.htm", method=RequestMethod.GET)
    //	public void balance(@RequestParam(value="accNo") String accNo, HttpServletResponse response) throws IOException{
    //		String resultStr = bankTransController.trans(TransCode.BALANCE.getCode(), "{\"accNo\": " + accNo + "}");
    //		response.getWriter().write(resultStr);
    //	}
}
