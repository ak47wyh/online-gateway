package com.iboxpay.settlement.gateway.common.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.wft.service.utils.XmlUtils;

@Controller
@RequestMapping("/wechatwft")
public class WechatWftController {
	private static Logger logger = LoggerFactory.getLogger(WechatWftController.class);
	@Resource
	private BankTransController bankTransController;
	
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
				Map<String, String> map = XmlUtils.toMap(resString.getBytes(), "utf-8");	
				String status = map.get("status");
				if (status != null && "0".equals(status)) {					
					String resultCode=map.get("result_code");
					String errCode=map.get("err_code");
					String errCodeDes=map.get("err_msg");
					String outTradeNo=map.get("out_trade_no");
					if("0".equals(resultCode)){
						outTradeNo=map.get("out_trade_no");
					}
					Map<String,Object> param=new HashMap<String,Object>();
					param.put("appCode", "zxwt");
					param.put("type", "online");
					param.put("requestSystem", "online_sys");
					param.put("outTradeNo", outTradeNo);
					param.put("resultCode", resultCode);
					param.put("errCode", errCode);
					param.put("errCodeDes", errCodeDes);
					param.put("resultMap", map);
					
					String reqContext=JsonUtil.toJson(param);
					String transCode="callback";
					String result=bankTransController.trans(transCode,reqContext,request);
					
					//解析处理反馈结果
					Map appResultMap=JsonUtil.parseJSON2Map(result);
					String rsStatus=(String) appResultMap.get("status");
					if(rsStatus.equals("success")){
						List list= (List) appResultMap.get("data");
						Map dataMap= (Map) list.get(0);
						String rs_status=(String) dataMap.get("status");
						if(rs_status.equals("success")){
							respString = "SUCCESS";
						}else{
							respString = "FAIL";
						}
					}
				}	
			}
			response.getWriter().write(respString);
		} catch (Exception e) {
			logger.error("微信异步通知消息异常："+e.getMessage());
		}
	}
}
