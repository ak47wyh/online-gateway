package com.iboxpay.settlement.gateway.common.web;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSONObject;
import com.iboxpay.common.json.MapperUtils;
import com.iboxpay.settlement.gateway.common.cache.remote.MemcachedService;
import com.iboxpay.settlement.gateway.common.dao.PaymentMerchantDao;
import com.iboxpay.settlement.gateway.common.domain.PaymentMerchantEntity;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.PropertyReader;
import com.iboxpay.settlement.gateway.common.util.StringUtils;
import com.iboxpay.settlement.gateway.wechat.service.sign.utils.EncrytUtils;
import com.iboxpay.settlement.gateway.wechat.service.sign.utils.JsTicket;
import com.iboxpay.settlement.gateway.wechat.service.sign.utils.WeixinJsTicketInfoService;
import com.iboxpay.settlement.gateway.wechat.service.utils.WechatUtils;
import com.iboxpay.settlement.gateway.wechat.service.utils.XmlUtils;


@Controller
@RequestMapping("/wechat")
public class WechatController {
	private static Logger logger = LoggerFactory.getLogger(WechatController.class);
	@Resource
	private BankTransController bankTransController;
	
	@Resource 
	private PaymentMerchantDao paymentMerchantDao;
	
	@Resource
    private WeixinJsTicketInfoService weixinJsTicketInfoService;
	
    @Resource
    private MemcachedService memcachedService;
	
	@RequestMapping(value = "notify.htm", method = RequestMethod.POST)
	@ResponseBody
	public void notify(HttpServletRequest request, HttpServletResponse response) {
		try {
			request.setCharacterEncoding("utf-8");
			response.setCharacterEncoding("utf-8");
			response.setHeader("Content-type", "text/html;charset=UTF-8");
			String resString = XmlUtils.parseRequst(request);
			logger.info("异步通知内容："+resString);

			String respString = "fail";
			if (resString != null && !"".equals(resString)) {
				Map<String, String> resultMap = XmlUtils.toMap(resString.getBytes(), "utf-8");
				String res = XmlUtils.toXml(resultMap);
				
				String returnCode=resultMap.get("return_code");
				String returnMsg= resultMap.get("return_msg");
				if (returnCode.equals("SUCCESS")) {
					String resultCode=resultMap.get("result_code");
					String errCode=resultMap.get("err_code");
					String errCodeDes=resultMap.get("err_code_des");
					String outTradeNo=resultMap.get("out_trade_no");
					Map<String,Object> param=new HashMap<String,Object>();
					param.put("appCode", "wcht");
					param.put("type", "online");
					param.put("requestSystem", "online_sys");
					param.put("outTradeNo", outTradeNo);
					param.put("resultCode", resultCode);
					param.put("errCode", errCode);
					param.put("errCodeDes", errCodeDes);
					param.put("resultMap", resultMap);
					
					String reqContext=JsonUtil.toJson(param);
					String transCode="callback";
					String result=bankTransController.trans(transCode,reqContext,request);
					
					//解析处理反馈结果
					Map appResultMap=JsonUtil.parseJSON2Map(result);
					String rsStatus=(String) appResultMap.get("status");
					if(rsStatus.equals("success")){
						List list= (List) appResultMap.get("data");
						Map dataMap= (Map) list.get(0);
						String status=(String) dataMap.get("status");
						if(status.equals("success")){
							respString = "SUCCESS";
						}else{
							respString = "FAIL";
						}
					}
				}
			}
			response.getWriter().write(respString);
		} catch (Exception e) {
			logger.error("微信异步通知消息异常："+e);
		}
	}
	
	
	
	/**
	 * 获取授权code，用于测试
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "getCode.do")
	public void getCode(HttpServletRequest request, HttpServletResponse response){
		String code = request.getParameter("code");
		logger.info("获取微信授权code："+code);
	}
	
	
	
	/**
	 * 根据code获取AccessToken
	 * 
	 * 通过跳转获取用户的openid，跳转流程如下：
	 * 1、设置自己需要调回的url及其其他参数，跳转到微信服务器https://open.weixin.qq.com/connect/oauth2/authorize
	 * 2、微信服务处理完成之后会跳转回用户redirect_uri地址，此时会带上一些参数，如：code
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "getAccessToken.do")
	public void getAccessToken(HttpServletRequest request, HttpServletResponse response){
		try {
			String resString = XmlUtils.parseRequst(request);
			Map<String, Object> reqMap = JsonUtil.parseJSON2Map(resString);
			String code =String.valueOf(reqMap.get("code"));
			logger.info("code="+code);
			
			String result=WechatUtils.getAccessToken(code);
			logger.info("获取微信getAccessToken 结果："+result);
			JSONObject result_ = JSONObject.parseObject(result);
			String errcode = result_.getString("errcode");
			String errmsg = result_.getString("errmsg");
			Map<String,Object> param = new HashMap<String,Object>();
			if(StringUtils.isEmpty(errcode)){
				param.put("status", "success");
				param.put("statusMsg", "获取成功");
				String openid = result_.getString("openid");
				if(!StringUtils.isEmpty(openid)){
					param.put("openid", openid);
				}
				String unionid = result_.getString("unionid");
				if(!StringUtils.isEmpty(unionid)){
					param.put("unionid", unionid);
				}
				String accessToken = result_.getString("access_token");
				if(!StringUtils.isEmpty(accessToken)){
					param.put("accessToken", accessToken);
				}
				String expiresIn = result_.getString("expires_in");
				if(!StringUtils.isEmpty(expiresIn)){
					param.put("expiresIn", expiresIn);
				}
				String scope = result_.getString("scope");
				if(!StringUtils.isEmpty(scope)){
					param.put("scope", scope);
				}
			}else{
				param.put("status", "fail");
				param.put("statusMsg", errmsg);
				logger.error("获取微信OpenID失败：错误编号="+errcode +" ,错误原因="+errmsg);
			}
			String responseContext=JsonUtil.toJson(param);
			logger.info("获取微信OpenID："+responseContext);
			response.getWriter().write(responseContext);
		} catch (Exception e) {
			logger.error("微信获取微信openid异常："+e);
		}
	}
	

	/**
	 * 刷新access_token
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "refreshAccessToken.do")
	public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response){
		try {
    		String resString = XmlUtils.parseRequst(request);
    		Map<String, Object> reqMap = JsonUtil.parseJSON2Map(resString);
			String appid =String.valueOf(reqMap.get("appid"));
			String refreshToken = String.valueOf(reqMap.get("refreshToken"));	
		    logger.info("appid="+appid +",refreshToken="+refreshToken);
	
			String result=WechatUtils.refreshAccessToken(appid, refreshToken);
			JSONObject result_ = JSONObject.parseObject(result);
			String errcode = result_.getString("errcode");
			String errmsg = result_.getString("errmsg");
			Map<String,Object> param = new HashMap<String,Object>();
			if(StringUtils.isEmpty(errcode)){
				param.put("status", "success");
				param.put("statusMsg", "获取成功");
				String access_token = result_.getString("access_token");
				if(!StringUtils.isEmpty(access_token)){
					param.put("access_token", access_token);
				}
				String expiresIn = result_.getString("expires_in");
				if(!StringUtils.isEmpty(expiresIn)){
					param.put("expiresIn", expiresIn);
				}
				String refreshTokenRes = result_.getString("refresh_token");
				if(!StringUtils.isEmpty(refreshTokenRes)){
					param.put("refreshToken", refreshTokenRes);
				}
				String openidRes = result_.getString("openid");
				if(!StringUtils.isEmpty(openidRes)){
					param.put("openid", openidRes);
				}
				String scope = result_.getString("scope");
				if(!StringUtils.isEmpty(scope)){
					param.put("scope", scope);
				}
			}else{
				param.put("status", "fail");
				param.put("statusMsg", errmsg);
				logger.error("刷新access_token失败：错误编号="+errcode +" ,错误原因="+errmsg);
			}
			String responseContext=JsonUtil.toJson(param);
			logger.info("刷新access_token："+responseContext);
			response.getWriter().write(responseContext);
		} catch (IOException e) {
			logger.error("刷新access_token异常："+e);
		} catch (Exception e) {
			logger.error("刷新access_token异常："+e);
		}
		
	}
	
	
	
	

	/**
	 * 拉取用户信息
	 * 参数格式：{"openid":"wcht","accessToken":"1326506201"}
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "pullUserInfo.do")
	public void pullUserInfo(HttpServletRequest request, HttpServletResponse response){		
		try {			
    		String resString = XmlUtils.parseRequst(request);
    		Map<String, Object> reqMap = JsonUtil.parseJSON2Map(resString);
			String openid =String.valueOf(reqMap.get("openid"));
			String accessToken = String.valueOf(reqMap.get("accessToken"));
			String lang = String.valueOf(reqMap.get("lang"));
    		logger.info("openid="+openid +",accessToken="+accessToken);
    		
			String result=WechatUtils.pullUserInfo(accessToken, openid, lang);
			JSONObject result_ = JSONObject.parseObject(result);
			String errcode = result_.getString("errcode");
			String errmsg = result_.getString("errmsg");
			Map<String,Object> param = new HashMap<String,Object>();
			if(StringUtils.isEmpty(errcode)){
				param.put("status", "success");
				param.put("statusMsg", "获取成功");
				String openidRes = result_.getString("openid");
				if(!StringUtils.isEmpty(openidRes)){
					param.put("openid", openidRes);
				}
				String nickname = result_.getString("nickname");
				if(!StringUtils.isEmpty(nickname)){
					param.put("nickname", nickname);
				}
				int sex = result_.getIntValue("sex");
				param.put("sex", sex);
				String province = result_.getString("province");
				if(!StringUtils.isEmpty(province)){
					param.put("province", province);
				}
				String city = result_.getString("city");
				if(!StringUtils.isEmpty(city)){
					param.put("city", city);
				}
				String country = result_.getString("country");
				if(!StringUtils.isEmpty(country)){
					param.put("country", country);
				}
				String headimgurl = result_.getString("headimgurl");
				if(!StringUtils.isEmpty(headimgurl)){
					param.put("headimgurl", headimgurl);
				}
				String unionid = result_.getString("unionid");
				if(!StringUtils.isEmpty(unionid)){
					param.put("unionid", unionid);
				}
			}else{
				param.put("status", "fail");
				param.put("statusMsg", errmsg);
				logger.error("拉取用户信息失败：错误编号="+errcode +" ,错误原因="+errmsg);
			}
			
			String responseContext=JsonUtil.toJson(param);
			logger.info("拉取用户信息："+responseContext);
			response.getWriter().write(responseContext);
		} catch (IOException e) {
			logger.error("拉取用户信息异常："+e);
		} catch (Exception e) {
			logger.error("拉取用户信息异常："+e);
		}
	}
	
	
	
	/**
	 * H5payJs 微信H5支付付款请求
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = "H5payJs.do")
    public ModelAndView H5payJs(HttpServletRequest request, HttpServletResponse response) {
		PropertyReader reader = PropertyReader.getInstance();
    	String appId = reader.getPropertyValue("/wechat.properties", "wechat.appid");
    	String merchantKey = reader.getPropertyValue("/wechat.properties", "wechat.appsecret");

    	//获取预付款标示
		String prepayId =request.getParameter("prepay_id");
		String url =request.getParameter("url");
		logger.info("appId="+appId +",secret="+merchantKey+",prepay_id="+prepayId);
		String timeStamp =String.valueOf(new Date().getTime());
		String nonceStr= String.valueOf(new Date().getTime());
		String packageStr="prepay_id="+prepayId;
		// 付款请求签名
	   	String paySign = WechatUtils.createPaySign(appId, merchantKey, prepayId, timeStamp, nonceStr);
	   	// 公共签名
	   	String signature = WechatUtils.createSignature(url,appId, merchantKey , timeStamp, nonceStr);
	   	
        ModelAndView mv = new ModelAndView();
        mv.addObject("appId",appId);
        mv.addObject("timeStamp",timeStamp);
        mv.addObject("package",packageStr);
        mv.addObject("paySign",paySign);
        mv.addObject("nonceStr",nonceStr);
        mv.addObject("signature",signature);
        mv.setViewName("/views/wechat/H5payJs");
        return mv;
    }
	
	/**
	 * 微信支付公共签名方法
	 * 参数格式：{"appCode":"wcht","payMerchantNo":"1326506201"}
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "getSignature.do")
    public void getSignature(HttpServletRequest request, HttpServletResponse response) {
		HashMap<String,String> mapInit=new HashMap<String, String>(); 
	    SortedMap<String,String> param = new TreeMap<String,String>(mapInit);
        try {
    		String resString = XmlUtils.parseRequst(request);
    		Map<String, Object> reqMap = JsonUtil.parseJSON2Map(resString);
			String appCode =String.valueOf(reqMap.get("appCode"));
			String payMerchantNo = String.valueOf(reqMap.get("payMerchantNo"));
			String url = String.valueOf(reqMap.get("url"));
			logger.info("appCode="+appCode +",payMerchantNo="+payMerchantNo);
			
			PropertyReader reader = PropertyReader.getInstance();
			String appId = reader.getPropertyValue("/wechat.properties", "wechat.appid");
	    	String appsecret = reader.getPropertyValue("/wechat.properties", "wechat.appsecret");
	    	logger.info("appId="+appId +",secret="+appsecret);
	    	
			PaymentMerchantEntity merchant = paymentMerchantDao.findByAppCode(appCode, payMerchantNo);
			if(merchant!=null){
				JsTicket jsticket = (JsTicket) memcachedService.get("wxOfficalJsTicket");
				String jsapi_ticket=null;
				if(jsticket!=null){
				     long createTimeMs = jsticket.getCreateTime().getTime();
				     long createInterval = new Date().getTime() - createTimeMs;
					 // 判断失效时间
					 if(createInterval<=jsticket.getExpiresIn()*1000){
						 jsapi_ticket = jsticket.getTicket(); 
					 }else{
						 jsapi_ticket=null;
					 }
				}
		        if (null == jsapi_ticket) {
		            //如果JsTicket从redis缓存中取不到，代表其已过期或者redis异常，则重新从微信获取一次     
		            try {
		                jsapi_ticket = weixinJsTicketInfoService.getJsTicketInfo(appId,appsecret);
		            } catch (Exception e) {
		            	logger.info("-------get jsapi_ticket from weixin fail!");
		            	param.put("status", "fail");
						param.put("statusMsg", "get jsapi_ticket fail");
		            }
		            //如果还没有成功取到，则抛出错误给客户端提示
		            if (null == jsapi_ticket) {
		            	logger.info("-------get jsapi_ticket fail!");
		            	param.put("status", "fail");
						param.put("statusMsg", "get jsapi_ticket fail");
		            }
		        }
		        
		        
            	String nonceStr = UUID.randomUUID().toString();
		        String timeStamp = Long.toString(System.currentTimeMillis() / 1000);
		        Map<String, Object> ticketMap = new HashMap<String, Object>();
		        ticketMap.put("noncestr", nonceStr);
		        ticketMap.put("jsapi_ticket", jsapi_ticket);
		        ticketMap.put("timestamp", timeStamp);
		        ticketMap.put("url", url);
		        logger.info("-------the sign paramMap:{}", MapperUtils.toJson(ticketMap));
		        //按字母排列顺序生成加密字符串
		        String signStr = EncrytUtils.buildSignParms(ticketMap);
		        //进行SHA1签名
		        String signature = EncrytUtils.SHA1Encrypt(signStr);
		        logger.info("-------the sign signature:{}", signature);
				
				
				param.put("url", url);
				param.put("appId", appId);
				param.put("timeStamp", timeStamp);
				param.put("nonceStr", nonceStr);
				param.put("signType", "SHA-1");//
//				param.put("signType", "MD5");//SHA-1
//				String signature = WechatUtils.createSignature(url, appId, appsecret , timeStamp, nonceStr);
				param.put("signature", signature);
				
				param.put("status", "success");
				param.put("statusMsg", "获取成功");
			}else{
				param.put("status", "fail");
				param.put("statusMsg", "通道名称或者交易商品号匹配错误");
			}
			
			
			String responseContext=JsonUtil.toJson(param);
			logger.info("微信支付公共签名 getSignature ："+responseContext);
			response.getWriter().write(responseContext);
			
		} catch (IOException e) {
			logger.error("微信支付公共签名异常："+e);
		} catch (Exception e) {
			logger.error("微信支付公共签名异常："+e);
		}
        

	}
}

