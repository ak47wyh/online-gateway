package com.iboxpay.settlement.gateway.common.web;


import java.util.HashMap;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import com.iboxpay.settlement.gateway.common.dao.PaymentMerchantDao;
import com.iboxpay.settlement.gateway.common.domain.PaymentMerchantEntity;
import com.iboxpay.settlement.gateway.common.page.PageBean;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

@Controller
@RequestMapping("/manage/merchant")
public class PaymentMerchantControler {
	
	@Resource
	private PaymentMerchantDao paymentMerchantDao;

    @RequestMapping(value = "list.htm", method = RequestMethod.GET)
    public ModelAndView listMerchant(HttpServletRequest request,HttpServletResponse response,@RequestParam(value = "page", required = false) String page) {
        String appCode =request.getParameter("appCode");
        String payMerchantNo = request.getParameter("payMerchantNo");
        
        
        
        Map<String,Object> params = new HashMap<String,Object>();
        if (!StringUtils.isBlank(appCode)) {
            params.put("appCode",appCode);
        }
        if (!StringUtils.isBlank(payMerchantNo)) {
            params.put("payMerchantNo",payMerchantNo);
        }
        int pageNo = 1;
        try {
        	if(page!=null){
        		pageNo = Integer.parseInt(page);
        	}
        } catch (Exception e) {}
        pageNo = pageNo <= 0 ? 1 : pageNo;
        PageBean pageBean= paymentMerchantDao.findPage(pageNo, 20, params);
        
        ModelAndView mv = new ModelAndView();
        mv.addObject("pageBean", pageBean);
        mv.addObject("appCode", appCode);
        mv.addObject("payMerchantNo", payMerchantNo);
        mv.setViewName("/views/merchant/list");
        return mv;
    }
    
    
    
    @RequestMapping(value = "add.htm", method = RequestMethod.GET)
    public ModelAndView toAddMerchant() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/views/merchant/add");
        return mv;
    }
    
    
    @RequestMapping(value = "add.htm", method = RequestMethod.POST)
    @ResponseBody
    public ModelAndView addMerchant(HttpServletRequest request,HttpServletResponse response) {
    	String appId= request.getParameter("appId");
    	String subAppId= request.getParameter("subAppId");
    	String appSecret= request.getParameter("appSecret");
    	String subAppSecret= request.getParameter("subAppSecret");
    	String appCode= request.getParameter("appCode");
    	String payMerchantName= request.getParameter("payMerchantName");
    	String payMerchantNo= request.getParameter("payMerchantNo");
    	String payMerchantSubNo= request.getParameter("payMerchantSubNo");
    	String payMerchantKey= request.getParameter("payMerchantKey");
    	
    	
    	
    	PaymentMerchantEntity paymentMerchant=new PaymentMerchantEntity();
    	paymentMerchant.setPayMerchantName(payMerchantName);
    	paymentMerchant.setAppId(appId);
    	paymentMerchant.setSubAppId(subAppId);
    	paymentMerchant.setAppSecret(appSecret);
    	paymentMerchant.setSubAppSecret(subAppSecret);
    	paymentMerchant.setAppCode(appCode);
    	paymentMerchant.setPayMerchantNo(payMerchantNo);
    	paymentMerchant.setPayMerchantSubNo(payMerchantSubNo);
    	paymentMerchant.setPayMerchantKey(payMerchantKey);
    	
    	// 判断同一交易通道账号下的交易子商户号是否重复
    	PaymentMerchantEntity paymentMerchantResult= paymentMerchantDao.findByAppCode(appCode, payMerchantSubNo);
    	if(paymentMerchantResult==null){
    		paymentMerchantDao.save(paymentMerchant);
    		return new ModelAndView("redirect:list.htm");
    	}else{
    		ModelAndView mv = new ModelAndView();
    		mv.addObject("errorMsg", "该子商户号已经存在,请重新输入");
    		mv.addObject("paymentMerchant", paymentMerchant);
    		mv.setViewName("/views/merchant/add");
    		return mv;
    	}
    }
    
    
    
    
    @RequestMapping(value = "edit.htm", method = RequestMethod.GET)
    public ModelAndView toEditMerchant(@RequestParam(value = "id") Long id) {
        ModelAndView mv = new ModelAndView();
        PaymentMerchantEntity paymentMerchant=paymentMerchantDao.load(id);
        mv.addObject("paymentMerchant", paymentMerchant);
        mv.setViewName("/views/merchant/edit");
        return mv;
    }
    
    
    @RequestMapping(value = "edit.htm", method = RequestMethod.POST)
    @ResponseBody
    public ModelAndView editMerchant(HttpServletRequest request,HttpServletResponse response) {
    	String id= request.getParameter("id");
    	String appCode= request.getParameter("appCode");
    	String appId= request.getParameter("appId");
    	String subAppId= request.getParameter("subAppId");
    	String appSecret= request.getParameter("appSecret");
    	String subAppSecret= request.getParameter("subAppSecret");
    	String payMerchantName= request.getParameter("payMerchantName");
    	String payMerchantNo= request.getParameter("payMerchantNo");
    	String payMerchantSubNo= request.getParameter("payMerchantSubNo");
    	String payMerchantKey= request.getParameter("payMerchantKey");
    	
    	PaymentMerchantEntity paymentMerchant=new PaymentMerchantEntity();
    	paymentMerchant.setId(Long.valueOf(id));
    	paymentMerchant.setAppId(appId);
    	paymentMerchant.setSubAppId(subAppId);
    	paymentMerchant.setAppSecret(appSecret);
    	paymentMerchant.setSubAppSecret(subAppSecret);
    	paymentMerchant.setPayMerchantName(payMerchantName);
    	paymentMerchant.setAppCode(appCode);
    	paymentMerchant.setPayMerchantNo(payMerchantNo);
    	paymentMerchant.setPayMerchantSubNo(payMerchantSubNo);
    	paymentMerchant.setPayMerchantKey(payMerchantKey);
    	paymentMerchantDao.update(paymentMerchant);
       
        return new ModelAndView("redirect:list.htm");
    }
    
     
    
    @RequestMapping(value = "delete.htm", method = RequestMethod.GET)
    public ModelAndView deleteMerchant(@RequestParam(value = "id") Long id) {
        ModelAndView mv = new ModelAndView();
        Boolean flag=paymentMerchantDao.delete(id);
        return new ModelAndView("redirect:list.htm");
    }    
    
}

