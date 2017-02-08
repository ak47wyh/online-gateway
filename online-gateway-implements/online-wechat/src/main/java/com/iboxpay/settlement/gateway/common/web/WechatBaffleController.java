package com.iboxpay.settlement.gateway.common.web;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.iboxpay.settlement.gateway.wechat.service.utils.XmlUtils;

/**
 * 微信模拟挡板压力测试
 * @author liaoxiongjian
 * @date 2016-04-13 11:33
 */
@Controller
@RequestMapping("/wechatbaffle")
public class WechatBaffleController {
	private static Logger logger = LoggerFactory.getLogger(WechatBaffleController.class);
	
	@RequestMapping(value = "pay.do")
	@ResponseBody
	public void payment(HttpServletRequest request, HttpServletResponse response) {
		
		try {
			request.setCharacterEncoding("utf-8");
			response.setCharacterEncoding("utf-8");
			response.setHeader("Content-type", "text/html;charset=UTF-8");
			String resString = XmlUtils.parseRequst(request);
			logger.info("【WechatBaffleController.payment支付报文请求：】"+resString);
			Map<String, String> resultMap = XmlUtils.toMap(resString.getBytes(), "utf-8");
			/**
			 * 		
			<xml><appid>wx92f6dbb646e17dc5</appid>
			<body>钱盒微信支付订单</body>
			<mch_id>10012102</mch_id>
			<nonce_str>1460516664804</nonce_str>
			<notify_url>http://api.iboxpay.com/online-gateway/wechat/notify.htm</notify_url>
			<out_trade_no>99159029133236942156</out_trade_no>
			<sign>f9de7c59cfadb425b43439078efd9483</sign>
			<spbill_create_ip>172.16.1.215</spbill_create_ip>
			<sub_appid>wx04473f64e135e0c0</sub_appid>
			<sub_mch_id>1326506201</sub_mch_id>
			<sub_openid>omjz8sgD-zmJ3uL0FXw7yudBPqCQ</sub_openid>
			<total_fee>1</total_fee>
			<trade_type>JSAPI</trade_type>
			</xml>
			 */
			String appid = String.valueOf(resultMap.get("appid"));
			String mch_id = String.valueOf(resultMap.get("mch_id"));
			String notify_url = String.valueOf(resultMap.get("notify_url"));
			String out_trade_no = String.valueOf(resultMap.get("out_trade_no"));
			String spbill_create_ip =String.valueOf(resultMap.get("spbill_create_ip"));
			String sub_appid = String.valueOf(resultMap.get("sub_appid"));
			String sub_mch_id = String.valueOf(resultMap.get("sub_mch_id"));
			String sub_openid = String.valueOf(resultMap.get("sub_openid"));
			
			long start = System.currentTimeMillis();
			Thread.sleep(2000);
			logger.info("Sleep time in ms = " + (System.currentTimeMillis() - start));
			
			Map<String, String> respMap= new HashMap<String, String>();
			respMap.put("return_code", "SUCCESS");
			respMap.put("return_msg", "OK");
			respMap.put("appid", appid);
			respMap.put("mch_id", mch_id);
			respMap.put("sub_mch_id", sub_mch_id);
			respMap.put("nonce_str", "x4BX4C75FjcZHtYT");
			respMap.put("sign", "0144D09F17B3982D16B074D07B244F68");
			respMap.put("prepay_id", "wx2016041311042616a57e2f4a0053072411");
			respMap.put("trade_type", "JSAPI");
			respMap.put("sub_appid", sub_appid);
			respMap.put("result_code", "SUCCESS");
			
			String respString = XmlUtils.toXml(respMap);
			logger.info("【WechatBaffleController.payment支付报文响应】："+respString);
			response.getWriter().write(respString);
			
			

			/**
			2016-04-13 11:04:26.686 [task-worker-19] INFO  [AbstractPayment.java:72] - 接收【支付】返回报文(paymentId:[6250], batchSeqId:160413110424149111): 
			<xml><return_code><![CDATA[SUCCESS]]></return_code>
			<return_msg><![CDATA[OK]]></return_msg>
			<appid><![CDATA[wx92f6dbb646e17dc5]]></appid>
			<mch_id><![CDATA[10012102]]></mch_id>
			<sub_mch_id><![CDATA[1326506201]]></sub_mch_id>
			<nonce_str><![CDATA[x4BX4C75FjcZHtYT]]></nonce_str>
			<sign><![CDATA[0144D09F17B3982D16B074D07B244F68]]></sign>
			<result_code><![CDATA[SUCCESS]]></result_code>
			<prepay_id><![CDATA[wx2016041311042616a57e2f4a0053072411]]></prepay_id>
			<trade_type><![CDATA[JSAPI]]></trade_type>
			<sub_appid><![CDATA[wx04473f64e135e0c0]]></sub_appid>
			</xml>		
			 */
		} catch (UnsupportedEncodingException e) {
			logger.error("支付报文异常："+e);
		} catch (IOException e) {
			logger.error("支付报文异常："+e);
		} catch (Exception e) {
			logger.error("支付报文异常："+e);
		}
	}
	
	
	@RequestMapping(value = "query.do")
	@ResponseBody
	public void queryPayment(HttpServletRequest request, HttpServletResponse response) {
		try {
			request.setCharacterEncoding("utf-8");
			response.setCharacterEncoding("utf-8");
			response.setHeader("Content-type", "text/html;charset=UTF-8");
			String resString = XmlUtils.parseRequst(request);
			logger.info("【WechatBaffleController.queryPayment查询报文请求：】"+resString);
			
			
			/**
			 *
		<xml><appid>wx92f6dbb646e17dc5</appid>
		<mch_id>10012102</mch_id>
		<nonce_str>1460516671955</nonce_str>
		<out_trade_no>99159029133236942156</out_trade_no>
		<sign>E2DA166931A95E92BC04129EBA8B6CFF</sign>
		<sub_appid>wx04473f64e135e0c0</sub_appid>
		<sub_mch_id>1326506201</sub_mch_id>
		</xml>
			 */
			
			Map<String, String> resultMap = XmlUtils.toMap(resString.getBytes(), "utf-8");
			String appid = String.valueOf(resultMap.get("appid"));
			String mch_id = String.valueOf(resultMap.get("mch_id"));
			String out_trade_no = String.valueOf(resultMap.get("out_trade_no"));
			String sub_mch_id = String.valueOf(resultMap.get("sub_mch_id"));
			String sub_appid = String.valueOf(resultMap.get("sub_appid"));
			

			
			/**
			2016-04-13 11:04:32.141 [task-worker-20] INFO  [AbstractQueryPayment.java:44] - 接收【查询交易状态】返回报文(paymentId:[6250], batchSeqId:160413110424149111): 
			<xml><return_code><![CDATA[SUCCESS]]></return_code>
			<return_msg><![CDATA[OK]]></return_msg>
			<appid><![CDATA[wx92f6dbb646e17dc5]]></appid>
			<mch_id><![CDATA[10012102]]></mch_id>
			<sub_mch_id><![CDATA[1326506201]]></sub_mch_id>
			<nonce_str><![CDATA[qhZWnhhFIFulpOG0]]></nonce_str>
			<sign><![CDATA[8DCF986E68A8FB47F78B8EE8EC5914EE]]></sign>
			<result_code><![CDATA[SUCCESS]]></result_code>
			<out_trade_no><![CDATA[99159029133236942156]]></out_trade_no>
			<trade_state><![CDATA[NOTPAY]]></trade_state>
			<sub_appid><![CDATA[wx04473f64e135e0c0]]></sub_appid>
			<trade_state_desc><![CDATA[订单未支付]]></trade_state_desc>
			</xml>
			*/
			long start = System.currentTimeMillis();
			Thread.sleep(2000);
			logger.info("Sleep time in ms = " + (System.currentTimeMillis() - start));
			
			
			Map<String, String> respMap= new HashMap<String, String>();
			respMap.put("return_code", "SUCCESS");
			respMap.put("return_msg", "OK");
			respMap.put("appid", appid);
			respMap.put("mch_id", mch_id);
			respMap.put("sub_mch_id", sub_mch_id);
			respMap.put("nonce_str", "x4BX4C75FjcZHtYT");
			respMap.put("sign", "0144D09F17B3982D16B074D07B244F68");
			respMap.put("result_code", "SUCCESS");
			respMap.put("trade_type", "JSAPI");
			respMap.put("out_trade_no", out_trade_no);
			respMap.put("trade_state", "SUCCESS");
			respMap.put("sub_appid", sub_appid);
			
//		
			String respString = XmlUtils.toXml(respMap);
			logger.info("【WechatBaffleController.queryPayment查询报文响应】："+respString);
			response.getWriter().write(respString);
		} catch (UnsupportedEncodingException e) {
			logger.error("查询报文异常："+e);
		} catch (IOException e) {
			logger.error("查询报文异常："+e);
		} catch (Exception e) {
			logger.error("查询报文异常："+e);
		}
		
	}
	
	
	
	@RequestMapping(value = "notify.do")
	@ResponseBody
	public void notifyPayment(HttpServletRequest request, HttpServletResponse response) {
		
		//<xml><appid><![CDATA[wx92f6dbb646e17dc5]]></appid><bank_type><![CDATA[CFT]]></bank_type><cash_fee><![CDATA[1]]></cash_fee><fee_type><![CDATA[CNY]]></fee_type><is_subscribe><![CDATA[N]]></is_subscribe><mch_id><![CDATA[10012102]]></mch_id><nonce_str><![CDATA[1460516664804]]></nonce_str><openid><![CDATA[owqpSuD5Va5MSLgTNeqg7cZn4r4U]]></openid><out_trade_no><![CDATA[99159029133236942156]]></out_trade_no><result_code><![CDATA[SUCCESS]]></result_code><return_code><![CDATA[SUCCESS]]></return_code><sign><![CDATA[69E2A5B3C0E861CEDFD7F8453D3401A3]]></sign><sub_appid><![CDATA[wx04473f64e135e0c0]]></sub_appid><sub_is_subscribe><![CDATA[Y]]></sub_is_subscribe><sub_mch_id><![CDATA[1326506201]]></sub_mch_id><sub_openid><![CDATA[omjz8sgD-zmJ3uL0FXw7yudBPqCQ]]></sub_openid><time_end><![CDATA[20160413110434]]></time_end><total_fee>1</total_fee><trade_type><![CDATA[JSAPI]]></trade_type><transaction_id><![CDATA[4009762001201604134790060583]]></transaction_id></xml>
		
		
		
		
	}
	

}

