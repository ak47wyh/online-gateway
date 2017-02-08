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
package com.iboxpay.settlement.gateway.jz.service.query;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.query.AbstractQueryPayment;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.Sequence;
import com.iboxpay.settlement.gateway.jz.Constants;
import com.iboxpay.settlement.gateway.jz.JzAccountEntityExt;
import com.iboxpay.settlement.gateway.jz.JzFrontEndConfig;
import com.iboxpay.settlement.gateway.jz.service.CommonPacker;
import com.iboxpay.settlement.gateway.jz.service.SignatureFacade;
import com.iboxpay.settlement.gateway.jz.service.payment.Payment_500201;

/**
 * 代扣结果查询
 * @author caolipeng
 * @date 2015年8月3日 下午2:19:03
 * @Version 1.0
 */
@Service
public class QueryPayment_500501 extends AbstractQueryPayment {

	private static Logger logger = LoggerFactory.getLogger(QueryPayment_500501.class);
	public static final String BANK_TRANS_CODE = "500501";

	@Override
	public String getBankTransCode() {
		return BANK_TRANS_CODE;
	}

	@Override
	public String getBankTransDesc() {
		return "交易结果查询";
	}

	@Override
	public Map<String, String> getHeaderMap() {
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("Content-Type", "application/x-www-form-urlencoded");
		return headerMap;
	}

	@Override
	protected String getUri() {
		JzFrontEndConfig config = (JzFrontEndConfig) TransContext.getContext().getFrontEndConfig();
		return config.getUri().getVal();
	}

	@Override
	public String pack(PaymentEntity[] payments) throws PackMessageException {
		PaymentEntity payment = payments[0];
		TransContext context = TransContext.getContext();
		JzAccountEntityExt account = (JzAccountEntityExt) context.getMainAccount();
		String merId = account.getMerchantId().getVal();//账号当做商户编号
		String orderId = Sequence.genNumberSequence(20);//订单号(固定20位),查询时候重新再生成一个
		String transTime = DateTimeUtil.format(new Date(), Constants.DATE_FORMAT);
		StringBuffer sb = CommonPacker.packHeader(merId, BANK_TRANS_CODE, orderId);
		//msg	交易时间｜原订单号｜原交易代码(msg需加密)
		String transOrderId = payment.getBankBatchSeqId();
		String msgInfo = transTime + Constants.INTERNAL_SEPARATOR + transOrderId + Constants.INTERNAL_SEPARATOR + Payment_500201.BANK_TRANS_CODE;

		logger.info(("【交易查询】报文:merId=" + merId + ",orderId=" + orderId + ",tradeCode=" + BANK_TRANS_CODE + ",msgInfo=" + msgInfo));

		msgInfo = SignatureFacade.encrypt(msgInfo);//msg加密
		String signature = SignatureFacade.juSignature(merId + orderId + BANK_TRANS_CODE + msgInfo);//签名
		sb.append("msg=" + msgInfo).append(Constants.ELEMENT_SEPARATOR).append("signature=" + signature);
		return sb.toString();
	}

	@Override
	public void parse(String respStr, PaymentEntity[] payments) throws ParseMessageException {
		/**
		 * 返回码,返回信息,交易状态,交易返回信息,卡序列号,交易金额,手续费,签名值
		 * {"respCode":"00000","respInfo":"成功","ordStatus":"00000","ordInfo":"交易成功！",
		 * "cardSeqId":"000000000108","feeAmt":"000000000000050","transAmt":"000000000000200",
		 * "signature":"a0af3d01d0d99abc5fe0e2d240c123a3"}
		 */
		TransContext context = TransContext.getContext();
		String charset = context.getCharset();//获取前置机配置的字符集
		try {
			Map map = (Map) JsonUtil.jsonToObject(respStr, charset, Map.class);
			String respCode = (String) map.get("respCode");//返回码
			String ordStatus = (String) map.get("ordStatus");//交易状态
			String ordInfo = (String) map.get("ordInfo");//交易返回信息
			if (Constants.SUCCESS.equals(respCode)) {//返回码”00000“
				if (Constants.SUCCESS.equals(ordStatus)) {//“00000”成功
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUCCESS, "成功", ordStatus, ordInfo);
				} else if (Constants.ONE_FAIL.equals(ordStatus.substring(0, 1))) {//返回"1"开头的时候，失败
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", ordStatus, ordInfo);
				} else {//其它情况,未确定。查询确定
					PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "", ordStatus, ordInfo);
				}
			} else {//返回非”00000“时候，该次查询不成功
				PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "", ordStatus, ordInfo);
			}
		} catch (IOException e) {
			throw new ParseMessageException("解析返回JSON报文异常:" + e.getMessage());
		}
	}

}
