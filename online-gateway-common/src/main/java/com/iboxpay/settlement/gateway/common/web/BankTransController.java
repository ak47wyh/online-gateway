package com.iboxpay.settlement.gateway.common.web;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.Constant;
import com.iboxpay.settlement.gateway.common.IBankProfile;
import com.iboxpay.settlement.gateway.common.SystemManager;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.inout.CollectInfo;
import com.iboxpay.settlement.gateway.common.inout.CollectResultModel;
import com.iboxpay.settlement.gateway.common.inout.CommonRequestModel;
import com.iboxpay.settlement.gateway.common.inout.CommonResultModel;
import com.iboxpay.settlement.gateway.common.inout.payment.CollectQueryResultInfo;
import com.iboxpay.settlement.gateway.common.inout.payment.CollectRequestModel;
import com.iboxpay.settlement.gateway.common.inout.payment.PaymentOuterStatus;
import com.iboxpay.settlement.gateway.common.service.AccountService;
import com.iboxpay.settlement.gateway.common.task.TaskScheduler;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;
import com.iboxpay.settlement.gateway.common.trans.ErrorCode;
import com.iboxpay.settlement.gateway.common.trans.ITransDelegate;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

/**
 * 总业务业务请求入口
 * @author jianbo_chen
 */
@Path("")
@Service("bankTransController")
public class BankTransController {

    private final Logger logger = LoggerFactory.getLogger(BankTransController.class);
    private AtomicInteger requestCounter = new AtomicInteger(0);

    @Resource
    private AccountService accountService;

    @Context
    private MessageContext context;

    @Path("/online/{trans}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public String trans(@PathParam("trans") String transCode, String str) {
        HttpServletRequest request = context.getHttpServletRequest();
        return trans(transCode, str, request);
    }
    
    @Path("/collect/{trans}")
    @POST
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public String collect(@PathParam("trans") String transCode, String str) {
    	logger.info("online " + transCode + " json:" + str);
    	CollectRequestModel requestModel = null;
    	try {
    		requestModel = (CollectRequestModel) JsonUtil.jsonToObject(str, "UTF-8", CollectRequestModel.class);
        } catch (Exception e) {
            logger.error("解析请求输入串失败.", e);
            return JsonUtil.toJson(new CommonResultModel().fail(ErrorCode.input_error, "业务请求报文格式有误."));
        }
    	
    	Map<String, Object> requestMap = new HashMap<String, Object>();
		//批次号
    	requestMap.put("batchSeqId", requestModel.getBatchSeqId());
		//出款账号
    	requestMap.put("appCode", requestModel.getAppCode());
		if("pay".equals(transCode)) {
			requestMap.put("requestSystem", "online_sys");
			requestMap.put("type", "pay");
			//以下添加收款信息
			List<Map<String, Object>> customerInfos = new ArrayList<Map<String, Object>>();
			requestMap.put("data", customerInfos);
			Map<String, Object> customerInfo = new HashMap<String, Object>();
			customerInfos.add(customerInfo);
			customerInfo.put("seqId", requestModel.getOrderSerial());
			customerInfo.put("amount", new BigDecimal(requestModel.getAmount()).divide(new BigDecimal(100)).setScale(2).toString());
			customerInfo.put("accNo", requestModel.getAccountNo());
			customerInfo.put("accName", requestModel.getAccountName());
			customerInfo.put("accType", requestModel.getAccountType());
			//		customerInfo.put("bankName" , bank);
			customerInfo.put("cnaps", requestModel.getUnionNo());
			customerInfo.put("bankBranchName", requestModel.getUnionName());
			customerInfo.put("bankFullName", requestModel.getBankName());
			customerInfo.put("cnapsBankNo", requestModel.getNetPayNo());//网银支付号
			Map<String, String> extProperties = new HashMap<String, String>();
			Map<String, Object> extPropertiesMap = new HashMap<String, Object>();
			if (requestModel.getExtProperties() != null && StringUtils.isNotBlank(requestModel.getExtProperties())) {
	            try {
	                extPropertiesMap = (Map) JsonUtil.jsonToObject(requestModel.getExtProperties(), "UTF-8", Map.class);
	            } catch (Exception e) {
	            	 logger.error("解析请求输入串失败.", e);
	                 return JsonUtil.toJson(new CommonResultModel().fail(ErrorCode.input_error, "业务请求报文格式有误."));
	            }
	        }
			
			String identityCode = (String) extPropertiesMap.get("identityCode");
			extProperties.put("certType", "0");
			extProperties.put("certNo", identityCode);
			String mobileNo = (String) extPropertiesMap.get("mobileNo");
			extProperties.put("mobileNo", mobileNo);
			
			customerInfo.put("extProperties",extProperties);
		}
		
		String json = JsonUtil.toJson(requestMap);
    	
        HttpServletRequest request = context.getHttpServletRequest();
        String responseJson = trans(transCode, json, request);
        logger.info("请求返回json：" + responseJson);
        Map resultMap;
		try {
			resultMap = JsonUtil.parseJSON2Map(responseJson);
		} catch (IOException e) {
			logger.error("解析请求输入串失败.", e);
            return JsonUtil.toJson(new CommonResultModel().fail(ErrorCode.sys_internal_err, "系统内部错误"));
		}
		
        String accNo = (String)resultMap.get("accNo");
        String status = (String)resultMap.get("status");
		String statusMsg = (String)resultMap.get("statusMsg");
		String errorCode = (String)resultMap.get("errorCode");
		String errorMsg = String.valueOf(resultMap.get("errorMsg"));
		
		String resultJson = "";
		if("pay".equals(transCode)) {
			CollectResultModel crm = new CollectResultModel();
			crm.setAppCode(accNo);
			if(PaymentOuterStatus.STATUS_SUCCESS.equals(status)) {
				List<Map> customerResults = (List)resultMap.get("data");
				if(customerResults == null) {
					crm.setStatus("3");
				}
				else {
					StringBuilder bankMsg = new StringBuilder();
					Map customerResult = customerResults.get(0);
					String payStatus = (String)customerResult.get("status");
					String payStatusMsg = (String) customerResult.get("statusMsg");
    				String payBankStatusMsg = (String)customerResult.get("payBankStatusMsg");
    				String bankStatusMsg = (String)customerResult.get("bankStatusMsg");
    				if(PaymentOuterStatus.STATUS_SUCCESS.equals(payStatus)){
    					crm.setStatus("1");
    				}else if(PaymentOuterStatus.STATUS_FAIL.equals(payStatus)){
    					crm.setStatus("2");
    					bankMsg.append(payStatusMsg);
    				}else{
    					crm.setStatus("3");
    				}
    				
    				if (org.apache.commons.lang.StringUtils.isNotBlank(bankStatusMsg)) { 
    					bankMsg.append(bankStatusMsg);
    				}
    				
    				if(StringUtils.isBlank(bankMsg.toString()) && org.apache.commons.lang.StringUtils.isNotBlank(payBankStatusMsg)){
    					if(!StringUtils.isBlank(payBankStatusMsg))
    						bankMsg.append(payBankStatusMsg);
    				}
    				
    				String reservedMsg = bankMsg.toString();
    				
    				crm.setErrorMsg(reservedMsg);
				}
			}
			else if(PaymentOuterStatus.STATUS_FAIL.equals(status)) {
				crm.setStatus("2");
				if(!StringUtils.isEmpty(errorMsg)){
					crm.setErrorMsg(errorMsg);
				}else if(!StringUtils.isEmpty(statusMsg)){
					crm.setErrorMsg(statusMsg);
				}
				
				crm.setErrorCode(errorCode);
			}
			else {
				crm.setStatus("3");
				crm.setErrorMsg(statusMsg);
				crm.setErrorCode(errorCode);
			}
			
			
			resultJson = JsonUtil.toJson(crm);
		}
        else if("query".equals(transCode)) {// 返回结果对查询业务做特殊处理
        	CollectQueryResultInfo cqri = new CollectQueryResultInfo();
        	cqri.setAppCode(accNo);
        	if(PaymentOuterStatus.STATUS_SUCCESS.equals(status)) {
        		cqri.setStatus("1");
			}
			else if(PaymentOuterStatus.STATUS_FAIL.equals(status)) {
				cqri.setStatus("2");
			}
			else {
				cqri.setStatus("3");
			}
        	cqri.setErrorMsg(statusMsg);
        	cqri.setErrorCode(errorCode);
        	
        	if(PaymentOuterStatus.STATUS_SUCCESS.equals(status)) {
        		List<Map> customerResults = (List)resultMap.get("data");
        		List<CollectInfo> resultList = new ArrayList<CollectInfo>();
        		for(int i=0; i<customerResults.size(); i++){
        			StringBuilder bankMsg = new StringBuilder();
        			Map customerResult = customerResults.get(i);
    				CollectInfo ci = new CollectInfo();
    				resultList.add(ci);
    				String payStatus = (String)customerResult.get("status");
    				String payStatusMsg = (String) customerResult.get("statusMsg");
    				String payBankStatusMsg = (String)customerResult.get("payBankStatusMsg");
    				String bankStatusMsg = (String)customerResult.get("bankStatusMsg");
    				String seqId = (String)customerResult.get("seqId");
    				ci.setOrderSerial(seqId);
    				if(PaymentOuterStatus.STATUS_SUCCESS.equals(payStatus)){
    					ci.setStatus("1");
    				}else if(PaymentOuterStatus.STATUS_FAIL.equals(payStatus)){
    					ci.setStatus("2");
    					bankMsg.append(payStatusMsg);
    				}else{
    					ci.setStatus("3");
    				}
    				if (org.apache.commons.lang.StringUtils.isNotBlank(bankStatusMsg)) { 
    					bankMsg.append(bankStatusMsg);
    				}
    				
    				if(StringUtils.isBlank(bankMsg.toString()) && org.apache.commons.lang.StringUtils.isNotBlank(payBankStatusMsg)){
    					if(!StringUtils.isBlank(payBankStatusMsg))
    						bankMsg.append(payBankStatusMsg);
    				}
    				
    				String reservedMsg = bankMsg.toString();
    				
    				ci.setStatusMsg(reservedMsg);
    			}
        		
        		cqri.setData(resultList);
        	}
        	
        	resultJson = JsonUtil.toJson(cqri);
        }
        
        return resultJson;
    }

    public String trans(String transCode, String str, HttpServletRequest request) {
    	logger.info("支付处理开始【trans】"+transCode);
    	logger.info("支付请求报文【trans】"+str);
    	long startTime = System.currentTimeMillis();
    	
        MDC.put("bankName", "common");
        if (request != null) {//内部调用不需要记录日志
            logger.info("(" + transCode + ")客户端(ip=" + getRemoteAddress(request) + ")请求 : \n" + str);
        }
        
        try {
            String resultJSON = processTrans(transCode, str);
            if (request != null) {//内部调用不需要记录日志
                logger.info("(" + transCode + ")响应客户端 : \n" + resultJSON);
            }
            long endTime = System.currentTimeMillis();
            logger.info("支付处理结束【trans】"+(endTime-startTime));
            
            return resultJSON;
        } catch (RuntimeException e) {
            logger.error("[系统错误]未处理交易异常", e);
            throw e;
        } finally {
            MDC.put("bankName", "common");
        }
        
        
    }

    private String processTrans(String transCode, String str) {
        if (!SystemManager.isRunning()) {
            String systemStatusMsg = SystemManager.getCurrentStatus().getMessage();
            logger.warn("拒绝请求: " + systemStatusMsg);
            return JsonUtil.toJson(new CommonResultModel().fail(ErrorCode.sys_not_running, SystemManager.getCurrentStatus().getMessage()));
        }
        requestCounter.incrementAndGet();
        try {
            ITransDelegate trans = BankTransComponentManager.getTransDelegate(transCode);
            if (trans == null) {
                String message = "不支持的交易指令: " + transCode;
                logger.warn(message);
                return JsonUtil.toJson(new CommonResultModel().fail(ErrorCode.sys_not_support, message));
            }
            CommonRequestModel requestModel = null;
            try {
                requestModel = trans.parseInput(str);
            } catch (Exception e) {
                logger.error("解析请求输入串失败.", e);
                return JsonUtil.toJson(new CommonResultModel().fail(ErrorCode.input_error, "业务请求报文格式有误."));
            }
            
            AccountEntity accountEntity;
            if (StringUtils.isBlank(requestModel.getAppCode())) {
                if (StringUtils.isBlank(requestModel.getBankName()))
                    return JsonUtil.toJson(new CommonResultModel().fail(ErrorCode.input_error, "缺少主账号 或 银行信息"));
                else {
                    accountEntity = accountService.getAccountEntityByBank(requestModel.getBankName());
                    if (accountEntity == null) return JsonUtil.toJson(new CommonResultModel().fail(ErrorCode.input_error, "银行[" + requestModel.getBankName() + "]的默认账号不存在."));
                    requestModel.setAppCode(accountEntity.getAccNo());
                }
            } else {
                accountEntity = accountService.getAccountEntity(requestModel.getAppCode());
                if (accountEntity == null) return JsonUtil.toJson(new CommonResultModel().fail(ErrorCode.input_error, "账号不存在: " + requestModel.getAppCode()));
            }
            IBankProfile bankProfile = BankTransComponentManager.getBankProfile(accountEntity.getBankName());
            if (bankProfile == null) {
                return JsonUtil.toJson(new CommonResultModel().fail(ErrorCode.sys_not_support, "未找到银行实现: " + accountEntity.getBankName()));
            }
            TransContext context = new TransContext(bankProfile, accountEntity);
            if (!TaskScheduler.existFrontEnd(accountEntity)) {
                return JsonUtil.toJson(new CommonResultModel().fail(ErrorCode.sys_internal_err, "没有可用前置机（银行前置机尚未配置 或 并发数设置为0 或 已禁用）"));
            }
            context.setMainAccount(accountEntity);
            //hardcode币别
            context.setIsoCurrency(Constant.CURRENCY_CNY);
            context.setTransCode(trans.getTransCode());
            context.setRequestSystem(requestModel.getRequestSystem());
            TransContext.setContext(context);
            try {
            	CommonResultModel comResult=trans.trans(context, requestModel);
                String resultJSON = JsonUtil.toJson(comResult);
                MDC.put("bankName", "common");
                return resultJSON;
            } finally {
                TransContext.setContext(null);
            }
        } finally {
            requestCounter.decrementAndGet();
        }
    }

    public void stop() {
        while (requestCounter.get() > 0) {//等待请求处理完毕
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {}
        }
        logger.info("服务入口已停止.");
    }

    //客户端ip
    private final String getRemoteAddress(HttpServletRequest request) {
        if (request == null) {
            return "self";
        }
        try {
            String ip = request.getHeader("x-forwarded-for");
            if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) ip = request.getHeader("Proxy-Client-IP");
            if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) ip = request.getHeader("WL-Proxy-Client-IP");
            if (ip == null || ip.length() == 0 || ip.equalsIgnoreCase("unknown")) ip = request.getRemoteAddr();
            return ip;
        } catch (Throwable e) {
            logger.error("获取客户端IP异常", e);
            return "unkown";
        }
    }
}
