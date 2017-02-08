package com.iboxpay.settlement.gateway.jd.service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.iboxpay.settlement.gateway.common.util.StringUtils;
import com.iboxpay.settlement.gateway.jd.service.model.JdCancleRespParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdMicropayRespParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdNotifyVerifyRespParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdQrRespParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdQueryStatusRespParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdRefundRespParam;

public class JdPayJsonUtils {

    private static Logger logger = LoggerFactory.getLogger(JdPayJsonUtils.class);

    /**
     * 解析付款码下单接口返回Json
     * @param result
     * @return
     */
    public static JdMicropayRespParam parseMicropayResponse(String result) {
        JdMicropayRespParam resp = new JdMicropayRespParam();
        JSONObject result_ = (JSONObject) JSONObject.parse(result);
        String isSuccess = result_.getString("is_success");
        resp.setIsSuccess(isSuccess);
        if ("Y".equals(isSuccess)) {
            JSONObject data = result_.getJSONObject("data");
            if (data != null) {
                // 订单号
                String orderNo = data.getString("order_no");
                if (!StringUtils.isEmpty(orderNo)) {
                    resp.setOrderNo(orderNo);
                }
                // 交易号
                String tradeNo = data.getString("trade_no");
                if (!StringUtils.isEmpty(tradeNo)) {
                    resp.setTermNo(tradeNo);
                }
                // 支付用户
                String user = data.getString("user");
                if (!StringUtils.isEmpty(user)) {
                    resp.setUser(user);
                }
                // 支付金额
                double amount = data.getDouble("amount");
                resp.setAmount(amount);
                // 营销金额
                double promotionAmount = data.getDouble("promotionAmount");
                resp.setPromotionAmount(promotionAmount);
                // 支付商户号
                String merchantNo = data.getString("merchant_no");
                if (!StringUtils.isEmpty(merchantNo)) {
                    resp.setmerchantNo(merchantNo);
                }
                // 支付时间
                String payTime = data.getString("pay_time");
                if (!StringUtils.isEmpty(payTime)) {
                    resp.setPayTime(payTime);
                }
            }
        } else {
            String errorCode = result_.getString("fail_code");
            if (!StringUtils.isEmpty(errorCode)) {
                resp.setErrorCode(errorCode);
            }
            String errorDes = result_.getString("fail_reason");
            if (!StringUtils.isEmpty(errorDes)) {
                resp.setErrorDes(errorDes);
            }
            logger.info("code:" + errorCode + ",des:" + errorDes);
        }
        return resp;
    }

    /**
     * 解析扫一扫下单接口返回Json
     * @param result
     * @return
     */
    public static JdQrRespParam parsePreCreateResponse(String result) {
        JdQrRespParam respParam = new JdQrRespParam();
        JSONObject result_ = JSONObject.parseObject(result);
        String success = result_.getString("is_success");
        respParam.setIsSuccess(success);
        if ("Y".equals(success)) {
            JSONObject data = result_.getJSONObject("data");
            if (data != null) {
                // 第三方商户可用该qrcode 生成一个二维码图片。 用户使用使用京东钱包扫该二维码即可支付商户订单
                String qrcode = data.getString("qrcode");
                if (!StringUtils.isEmpty(qrcode)) {
                    respParam.setQrcode(qrcode);
                }
                logger.info("生成二维码图片原始字符串:" + qrcode);
            }

        } else {
            String errorCode = result_.getString("fail_code");
            if (!StringUtils.isEmpty(errorCode)) {
                respParam.setErrorCode(errorCode);
            }
            String errorDes = result_.getString("fail_reason");
            if (!StringUtils.isEmpty(errorDes)) {
                respParam.setErrorDes(errorDes);
            }
            logger.info("code:" + errorCode + ",des:" + errorDes);
        }

        return respParam;
    }

    /**
     * 解析异步回调数据返回JSON
     * @param result
     * @return
     */
    public static JdNotifyVerifyRespParam parseNotifyVerifyResponse(String result) {
        JdNotifyVerifyRespParam respParam = new JdNotifyVerifyRespParam();
        JSONObject result_ = JSONObject.parseObject(result);
        String orderNo = result_.getString("order_no");
        if (!StringUtils.isEmpty(orderNo)) {
            respParam.setOrderNo(orderNo);
        }
        String tradeNo = result_.getString("trade_no");
        if (!StringUtils.isEmpty(tradeNo)) {
            respParam.setTradeNo(tradeNo);
        }
        double amount = result_.getDoubleValue("amount");
        respParam.setAmount(amount);

        String payTime = result_.getString("pay_time");
        if (!StringUtils.isEmpty(payTime)) {
            respParam.setPayTime(payTime);
        }
        String refundNo = result_.getString("refund_no");
        if (!StringUtils.isEmpty(refundNo)) {
            respParam.setRefundNo(refundNo);
        }
        String refundTime = result_.getString("refund_time");
        if (!StringUtils.isEmpty(refundTime)) {
            respParam.setRefundTime(refundTime);
        }
        int status = result_.getIntValue("status");
        respParam.setStatus(status);

        return respParam;
    }

    /**
     * 解析查询支付状态数据返回JSON
     * @param result
     * @return
     */
    public static JdQueryStatusRespParam parseQueryStatusResponse(String result) {
        JdQueryStatusRespParam respParam = new JdQueryStatusRespParam();
        JSONObject result_ = (JSONObject) JSONObject.parse(result);
        String success = result_.getString("is_success");
        respParam.setIsSuccess(success);
        if ("Y".equals(success)) {
            JSONObject data = result_.getJSONObject("data");//(JSONObject) JSON.parse("data")
            if (data != null) {
                // 订单号
                String order_no = data.getString("order_no");
                // 交易号
                String trade_no = data.getString("trade_no");
                // 支付用户
                String user = data.getString("user");
                // 支付金额
                double amount = data.getDouble("amount");
                // 营销金额
                double promotionAmount = data.getDouble("promotionAmount");
                // 支付商户号
                String merchant_no = data.getString("merchant_no");
                // 支付时间
                String pay_time = data.getString("pay_time");

                String status = data.getString("status");

                respParam.setOrderNo(order_no);
                respParam.setTradeNo(trade_no);
                respParam.setUser(user);
                respParam.setAmount(amount);
                respParam.setPromotionAmount(promotionAmount);
                respParam.setMerchantNo(merchant_no);
                respParam.setPayTime(pay_time);
                respParam.setStatus(status);
            }
        } else {
            String errorCode = result_.getString("fail_code");
            if (!StringUtils.isEmpty(errorCode)) {
                respParam.setErrorCode(errorCode);
            }
            String errorDes = result_.getString("fail_reason");
            if (!StringUtils.isEmpty(errorDes)) {
                respParam.setErrorDes(errorDes);
            }
            logger.info("code:" + errorCode + ",des:" + errorDes);
        }

        return respParam;
    }

    /**
     * 解析取消订单状态数据返回JSON
     * @param result
     * @return
     */
    public static JdCancleRespParam parseCancleTradeResponse(String result) {
        JdCancleRespParam respParam = new JdCancleRespParam();
        JSONObject result_ = JSONObject.parseObject(result);
        String success = result_.getString("is_success");
        respParam.setIsSuccess(success);
        if ("Y".equals(success)) {
            JSONObject data = result_.getJSONObject("data");
            if (data != null) {
                String status = data.getString("status");
                String orderNo = data.getString("order_no");
                String cancleNo = data.getString("cancle_no");
                String tradeNo = data.getString("trade_no");
                String merchantNo = data.getString("merchant_no");
                double amount = data.getDouble("amount");
                String cancleTime = data.getString("cancle_time");
                String note = data.getString("note");
                respParam.setStatus(status);
                respParam.setOrderNo(orderNo);
                respParam.setCancleNo(cancleNo);
                respParam.setTradeNo(tradeNo);
                respParam.setMerchantNo(merchantNo);
                respParam.setAmount(amount);
                respParam.setCancleTime(cancleTime);
                respParam.setNote(note);
            }
        } else {
            String errorCode = result_.getString("fail_code");
            String errorDes = result_.getString("fail_reason");
            respParam.setErrorCode(errorCode);
            respParam.setErrorDes(errorDes);
            logger.info("code:" + errorCode + ",des:" + errorDes);
        }

        return respParam;
    }

    /**
     * 解析退款返回JSON
     * @param result
     * @return
     */
    public static JdRefundRespParam parseRefundTradeResponse(String result) {
        JdRefundRespParam respParam = new JdRefundRespParam();
        JSONObject result_ = JSONObject.parseObject(result);
        respParam = new JdRefundRespParam();
        String success = result_.getString("is_success");
        respParam.setIsSuccess(success);;
        if ("Y".equals(success)) {
            JSONObject data = result_.getJSONObject("data");
            if (data != null) {
                // 订单号
                String order_no = data.getString("order_no");
                // 交易号
                String trade_no = data.getString("trade_no");
                // 退款订单号
                String refund_no = data.getString("refund_no");
                // 支付金额
                double amount_ = data.getDouble("amount");
                // 支付时间
                String refund_time = data.getString("refund_time");
                String status = data.getString("status");

                respParam.setOrderNo(order_no);
                respParam.setTradeNo(trade_no);
                respParam.setRefundNo(refund_no);
                respParam.setRefundAmount(amount_);
                respParam.setRefundTime(refund_time);
                respParam.setStatus(status);
            }
        } else {
            String errorCode = result_.getString("fail_code");
            String errorDes = result_.getString("fail_reason");
            logger.info("code:" + errorCode + ",des:" + errorDes);
            respParam.setErrorCode(errorCode);
            respParam.setErrorDes(errorDes);;
        }
        return respParam;
    }

    /**
     * 解析反馈查询退款报文
     * @param result
     * @return
     */
    public static JdRefundRespParam parseQueryRefundResponse(String result) {
        JdRefundRespParam respParam = new JdRefundRespParam();
        JSONObject result_ = JSONObject.parseObject(result);
        respParam = new JdRefundRespParam();
        String success = result_.getString("is_success");
        respParam.setIsSuccess(success);
        if ("Y".equals(success)) {
            JSONObject data = result_.getJSONObject("data");
            if (data != null) {
                // 订单号
                String order_no = data.getString("order_no");
                // 退款号
                String refund_no = data.getString("refund_no");
                // 京东支付正单交易流水号
                String original_trade_no = data.getString("original_trade_no");
                // 退款状态
                String status = data.getString("status");
                String merchantNo = data.getString("merchant_no");
                double refundAmount = data.getDouble("refund_amount");
                String refund_time = data.getString("refund_time");
                respParam.setMerchantNo(merchantNo);
                respParam.setOrderNo(order_no);
                respParam.setRefundNo(refund_no);
                respParam.setTradeNo(original_trade_no);
                respParam.setRefundAmount(refundAmount);
                respParam.setRefundTime(refund_time);
                respParam.setStatus(status);
            }
        } else {
            String errorCode = result_.getString("fail_code");
            String errorDes = result_.getString("fail_reason");
            logger.info("code:" + errorCode + ",des:" + errorDes);
            respParam.setErrorCode(errorCode);
            respParam.setErrorDes(errorDes);
        }
        return respParam;
    }

}
