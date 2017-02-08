/*
 * Copyright (C) 2011-2015 ShenZhen iBOXPAY Information Technology Co.,Ltd.
 * 
 * All right reserved.
 * 
 * This software is the confidential and proprietary
 * information of iBoxPay Company of China. 
 * ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only
 * in accordance with the terms of the contract agreement 
 * you entered into with iBoxpay inc.
 *
 */
package com.iboxpay.settlement.gateway.jz.service.balance;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.BalanceEnity;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.trans.balance.AbstractBalance;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.Sequence;
import com.iboxpay.settlement.gateway.common.util.StringUtils;
import com.iboxpay.settlement.gateway.jz.Constants;
import com.iboxpay.settlement.gateway.jz.JzAccountEntityExt;
import com.iboxpay.settlement.gateway.jz.JzFrontEndConfig;
import com.iboxpay.settlement.gateway.jz.service.CommonPacker;
import com.iboxpay.settlement.gateway.jz.service.SignatureFacade;

/**
 * 500401-虚拟账户余额查询
 * @author caolipeng
 * @date 2015年8月3日 下午1:54:00
 * @Version 1.0
 */
@Service
public class Balance_500401 extends AbstractBalance {

	private static Logger logger = LoggerFactory.getLogger(Balance_500401.class);
	public static final String BANK_TRANS_CODE = "500401";

	@Override
	public String getBankTransCode() {
		return BANK_TRANS_CODE;
	}

	@Override
	public String getBankTransDesc() {
		return "账户余额查询";
	}

	@Override
	public Map<String, String> getHeaderMap() {
		Map<String, String> headerMap = new HashMap<String, String>();
		//对方渠道，Ajax Form表单http交互时候，窗体数据被编码为名称/值对
		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
		return headerMap;
	}

	@Override
	protected String getUri() {
		JzFrontEndConfig config = (JzFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		return config.getUri().getVal();
	}

	/**
	 * merId=000000000000012&tradeCode=500401&orderId=20位固定长度
	 * &msg=加密后信息&signature=ef1b44da0989f4a5bff631ec13d2f1a5
	 */
	@Override
	public String pack(BalanceEnity[] balanceEntities) throws PackMessageException {
		TransContext transContext = TransContext.getContext();
		JzAccountEntityExt extAccount = (JzAccountEntityExt) transContext.getMainAccount();
		String merId = extAccount.getMerchantId().getVal();//商户编号
		String orderId = Sequence.genNumberSequence(20);//订单号(固定20位)
		//msg:交易时间(yyyyMMddHHmmss)｜账户类型(msg需加密)
		String transTime = DateTimeUtil.format(new Date(), Constants.DATE_FORMAT);

		String msgInfo = transTime + Constants.INTERNAL_SEPARATOR + extAccount.getAcctType().getVal();
		logger.info(("【查询余额】报文:merId=" + merId + ",orderId=" + orderId + ",tradeCode=" + BANK_TRANS_CODE + ",msgInfo=" + msgInfo));
		msgInfo = SignatureFacade.encrypt(msgInfo);//msg加密
		String signature = SignatureFacade.juSignature(merId + orderId + BANK_TRANS_CODE + msgInfo);
		StringBuffer sb = CommonPacker.packHeader(merId, BANK_TRANS_CODE, orderId);
		//组装发送的报文
		sb.append("msg=" + msgInfo).append(Constants.ELEMENT_SEPARATOR).append("signature=" + signature);
		return sb.toString();
	}

	@Override
	public void parse(String respStr, BalanceEnity[] balanceEntities) throws ParseMessageException {
		BalanceEnity balanceEnity = balanceEntities[0];
		TransContext context = TransContext.getContext();
		String charset = context.getCharset();//获取前置机配置的字符集
		try {
			logger.info("【余额查询】返回的JSON字符串为:" + respStr);
			if (SignatureFacade.juValidateSignature(respStr)) {//验签通过
				//{"respCode":"00000","respInfo":"成功","acctType":"01","acctBal":"000000000000295","signature":"9e997e0ea1d48b84f5c5fbe745bb50b6"}
				Map map = (Map) JsonUtil.jsonToObject(respStr, charset, Map.class);
				String respCode = (String) map.get("respCode");
				String respInfo = (String) map.get("respInfo");
				String totalBal = (String) map.get("totalBal");
				String availableBal = (String) map.get("availableBal");
				if (Constants.SUCCESS.equals(respCode)) {//成功
					if (StringUtils.isNotBlank(totalBal) && StringUtils.isNotBlank(availableBal)) {
						BigDecimal total = new BigDecimal(totalBal);
						BigDecimal available = new BigDecimal(availableBal);
						//由于王府井返回来的余额都是以分为单位，因此需要除以100转化成元
						balanceEnity.setBalance(total.divide(Constants.MULT_100));
						balanceEnity.setAvailableBalance(available.divide(Constants.MULT_100));
					} else {
						throw new ParseMessageException("银行没有返回有效余额值.");
					}
				} else {
					throw new ParseMessageException("银行未能正确返回余额信息，返回状态码[" + respCode + "]，状态信息[" + respInfo + "]");
				}
			} else {
				throw new ParseMessageException("银行返回余额信息验签失败");
			}
		} catch (IOException e) {
			logger.error("解析返回JSON报文异常:" + e);
			throw new ParseMessageException("解析返回JSON报文异常:" + e.getMessage());
		}
	}
}
