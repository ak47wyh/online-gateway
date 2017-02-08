package com.iboxpay.settlement.gateway.common.web;

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
import com.iboxpay.settlement.gateway.common.dao.PaymentDao;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.wft.WftFrontEndConfig;
import com.iboxpay.settlement.gateway.wft.service.payment.Payment_Native;
import com.iboxpay.settlement.gateway.wft.service.utils.SignUtils;
import com.iboxpay.settlement.gateway.wft.service.utils.XmlUtils;

@Controller
@RequestMapping("/wechatWFT")
public class WechatController {
	private static Logger logger = LoggerFactory.getLogger(WechatController.class);
	@Resource
	private PaymentDao paymentDao;
	
	@RequestMapping(value = "notify.htm", method = RequestMethod.POST)
	@ResponseBody
	public void notify(HttpServletRequest request, HttpServletResponse response) {
		WftFrontEndConfig frontEndConfig = (WftFrontEndConfig) BankTransComponentManager.getFrontEndConfigInstance("wechatWFT");
		try {
			request.setCharacterEncoding("utf-8");
			response.setCharacterEncoding("utf-8");
			response.setHeader("Content-type", "text/html;charset=UTF-8");
			String resString = XmlUtils.parseRequst(request);
			System.out.println("通知内容：" + resString);

			String respString = "fail";
			if (resString != null && !"".equals(resString)) {
				Map<String, String> map = XmlUtils.toMap(resString.getBytes(), "utf-8");
				String res = XmlUtils.toXml(map);
				System.out.println("通知内容：" + res);
				
				if (map.containsKey("sign")) {
					if (!SignUtils.checkParam(map, "9d101c97133837e13dde2d32a5054abb")) {
						logger.info("验证签名不通过");
						respString = "fail";
					} else {
						String status = map.get("status");
						if (status != null && "0".equals(status)) {
							String result_code = map.get("result_code");
							if (result_code != null && "0".equals(result_code)) {
								String pay_result = map.get("pay_result");
								String out_trade_no = map.get("out_trade_no");// 商户订单号
								String total_fee = map.get("total_fee");// 总金额
								if (pay_result != null && "0".equals(pay_result)) {
									// 修改支付状态为成功
									PaymentEntity payment=paymentDao.getPaymentsByBankSeqId(out_trade_no);
									if(payment!=null){
					                    payment.setStatus(PaymentStatus.STATUS_SUCCESS);
					                    payment.setStatusMsg("支付成功");
					                    payment.setPayBankStatus("0");
					                    payment.setPayBankStatusMsg("支付成功");
					                    payment.setPayErrorCode(0);
					                    payment.setBankStatus("0");
					                    payment.setBankStatusMsg("支付成功");
					                    payment.setErrorCode(0);
					                    
					                    PaymentEntity[] paymentEntitys=new PaymentEntity[] { payment };
					                    paymentDao.updatePaymentStatus(paymentEntitys, false);
//										PaymentStatus.setStatus(payment, PaymentStatus.STATUS_SUCCESS, "支付成功", "0", "支付成功",false);
										respString = "success";
									}

								} else {
									String errCode = map.get("err_code");
									String errMsg = map.get("err_msg");
									String payInfo = map.get("pay_info");
									// 修改支付状态为失败
									PaymentEntity payment=paymentDao.getPaymentsByBankSeqId(out_trade_no);
									if(payment!=null){
					                    payment.setStatus(PaymentStatus.STATUS_FAIL);
					                    payment.setStatusMsg("支付失败");
					                    payment.setPayBankStatus(pay_result);
					                    payment.setPayBankStatusMsg(payInfo);
					                    payment.setPayErrorCode(0);
					                    payment.setBankStatus(errCode);
					                    payment.setBankStatusMsg(errMsg);
					                    payment.setErrorCode(0);
					                    PaymentEntity[] paymentEntitys=new PaymentEntity[] { payment };
					                    paymentDao.updatePaymentStatus(paymentEntitys, false);
//					                    PaymentStatus.setStatus(payment, PaymentStatus.STATUS_FAIL, "支付失败", errCode, errMsg);
					                    respString = "fail";
									}
								}
							}
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
