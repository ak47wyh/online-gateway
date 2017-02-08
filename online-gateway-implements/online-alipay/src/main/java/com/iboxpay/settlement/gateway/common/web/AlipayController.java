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
import com.iboxpay.settlement.gateway.alipay.AlipayFrontEndConfig;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;


@Controller
@RequestMapping("/alipay")
public class AlipayController {
	private static Logger logger = LoggerFactory.getLogger(AlipayController.class);
	@Resource
	private BankTransController bankTransController;
	
	@RequestMapping(value = "notify.do")
	@ResponseBody
	public void notify(HttpServletRequest request, HttpServletResponse response) {
		AlipayFrontEndConfig config = (AlipayFrontEndConfig) BankTransComponentManager.getFrontEndConfigInstance("alipay");
		try {
			request.setCharacterEncoding("utf-8");
			response.setCharacterEncoding("utf-8");
			response.setHeader("Content-type", "text/html;charset=UTF-8");
			Map map=request.getParameterMap();
			logger.info("NOTIFY DO =>异步通知内容："+JsonUtil.toJson(map));
			/**
			notify_time	通知时间	Date	是	通知的发送时间。格式为yyyy-MM-dd HH:mm:ss	2015-14-27 15:45:58
			notify_type	通知类型	String(64)	是	通知的类型	trade_status_sync
			notify_id	通知校验ID	String(128)	是	通知校验ID	ac05099524730693a8b330c5ecf72da9786
			sign_type	签名类型	String(10)	是	签名算法类型，目前支持RSA	RSA
			sign	签名	String(256)	是	请参考签名	601510b7970e52cc63db0f44997cf70e
			trade_no	支付宝交易号	String(64)	是	支付宝交易凭证号	2013112011001004330000121536
			app_id	开发者的app_id	String(32)	是	支付宝分配给开发者的应用Id	2014072300007148
			out_trade_no	商户订单号	String(64)	是	原支付请求的商户订单号	6823789339978248
			out_biz_no	商户业务号	String(64)	否	商户业务ID，主要是退款通知中返回退款申请的流水号	HZRF001
			buyer_id	买家支付宝用户号	String(16)	否	买家支付宝账号对应的支付宝唯一用户号。以2088开头的纯16位数字	2088102122524333
			buyer_logon_id	买家支付宝账号	String(100)	否	买家支付宝账号	15901825620
			seller_id	卖家支付宝用户号	String(30)	否	卖家支付宝用户号	2088101106499364
			seller_email	卖家支付宝账号	String(100)	否	卖家支付宝账号	zhuzhanghu@alitest.com
			trade_status	交易状态	String(32)	否	交易目前所处的状态	TRADE_CLOSED
			total_amount	订单金额	Number(9,2)	否	本次交易支付的订单金额，单位为人民币（元）	20
			receipt_amount	实收金额	Number(9,2)	否	商家在交易中实际收到的款项，单位为元	15
			invoice_amount	开票金额	Number(9,2)	否	用户在交易中支付的可开发票的金额	10.00
			buyer_pay_amount	付款金额	Number(9,2)	否	用户在交易中支付的金额	13.88
			point_amount	集分宝金额	Number(9,2)	否	使用集分宝支付的金额	12.00
			refund_fee	总退款金额	Number(9,2)	否	退款通知中，返回总退款金额，单位为元，支持两位小数	2.58
			send_back_fee	实际退款金额	Number(9,2)	否	商户实际退款给用户的金额，单位为元，支持两位小数	2.08
			subject	订单标题	String(256)	否	商品的标题/交易标题/订单标题/订单关键字等，是请求时对应的参数，原样通知回来	当面付交易
			body	商品描述	String(400)	否	该订单的备注、描述、明细等。对应请求时的body参数，原样通知回来	当面付交易内容
			gmt_create	交易创建时间	Date	否	该笔交易创建的时间。格式为yyyy-MM-dd HH:mm:ss	2015-04-27 15:45:57
			gmt_payment	交易付款时间	Date	否	该笔交易的买家付款时间。格式为yyyy-MM-dd HH:mm:ss	2015-04-27 15:45:57
			gmt_refund	交易退款时间	Date	否	该笔交易的退款时间。格式为yyyy-MM-dd HH:mm:ss	2015-04-28 15:45:57
			gmt_close	交易结束时间	Date	否	该笔交易结束时间。格式为yyyy-MM-dd HH:mm:ss	2015-04-29 15:45:57
			fund_bill_list	支付金额信息	String(512)	否	支付成功的各个渠道金额信息，详见资金明细信息说明	参见资金明细信息
			*/
			
			String tradeStatus= request.getParameter("trade_status");
			String outTradeNo=request.getParameter("out_trade_no");
			String notifyId=request.getParameter("notify_id");
			String buyerId=request.getParameter("buyer_id");
			String buyerEmail=request.getParameter("buyer_email");
			Map<String,Object> resultMap=new HashMap<String,Object>();
			resultMap.put("tradeStatus", tradeStatus);
			resultMap.put("outTradeNo", outTradeNo);
			resultMap.put("notifyId", notifyId);
			resultMap.put("buyerId", buyerId);
			resultMap.put("buyerEmail", buyerEmail);
			
			Map<String,Object> param=new HashMap<String,Object>();
			param.put("appCode", "alpy");
			param.put("type", "online");
			param.put("requestSystem", "online_sys");
			param.put("outTradeNo", outTradeNo);
			param.put("resultMap", resultMap);
			
			String reqContext=JsonUtil.toJson(param);
			String transCode="callback";
			String result=bankTransController.trans(transCode,reqContext,request);
			
			//解析处理反馈结果
			String respString="fail";
			Map appResultMap=JsonUtil.parseJSON2Map(result);
			String rsStatus=(String) appResultMap.get("status");
			if(rsStatus.equals("success")){
				List list= (List) appResultMap.get("data");
				Map dataMap= (Map) list.get(0);
				String status=(String) dataMap.get("status");
				if(status.equals("success")){
					respString = "success";
				}
			}
			response.getWriter().write(respString);			
		} catch (Exception e) {
			logger.error("微信异步通知消息异常："+e.getMessage());
		}
	}
	
	@RequestMapping(value = "notify.htm",method = RequestMethod.POST)
	@ResponseBody
	public void notifyUrl(HttpServletRequest request, HttpServletResponse response) {
		try {
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		Map map=request.getParameterMap();
		logger.info("NOTIFY HTM POST =>异步通知内容："+JsonUtil.toJson(map));
		
		String tradeStatus= request.getParameter("trade_status");
		String outTradeNo=request.getParameter("out_trade_no");
		String notifyId=request.getParameter("notify_id");
		String buyerId=request.getParameter("buyer_id");
		String buyerEmail=request.getParameter("buyer_email");
		Map<String,Object> resultMap=new HashMap<String,Object>();
		resultMap.put("tradeStatus", tradeStatus);
		resultMap.put("outTradeNo", outTradeNo);
		resultMap.put("notifyId", notifyId);
		resultMap.put("buyerId", buyerId);
		resultMap.put("buyerEmail", buyerEmail);
		
		Map<String,Object> param=new HashMap<String,Object>();
		param.put("appCode", "alpy");
		param.put("type", "online");
		param.put("requestSystem", "online_sys");
		param.put("outTradeNo", outTradeNo);
		param.put("resultMap", resultMap);
		
		String reqContext=JsonUtil.toJson(param);
		String transCode="callback";
		String result=bankTransController.trans(transCode,reqContext,request);
		
		//解析处理反馈结果
		String respString="fail";
		Map appResultMap=JsonUtil.parseJSON2Map(result);
		String rsStatus=(String) appResultMap.get("status");
		if(rsStatus.equals("success")){
			List list= (List) appResultMap.get("data");
			Map dataMap= (Map) list.get(0);
			String status=(String) dataMap.get("status");
			if(status.equals("success")){
				respString = "success";
			}
		}
		response.getWriter().write(respString);			
	} catch (Exception e) {
		logger.error("微信异步通知消息异常："+e.getMessage());
	}
		
	}
}
