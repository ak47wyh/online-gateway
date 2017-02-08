package com.iboxpay.settlement.gateway.jd.service.query;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.query.IQueryPayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.jd.JdpayFrontEndConfig;
import com.iboxpay.settlement.gateway.jd.service.api.JdTradeRemoteService;
import com.iboxpay.settlement.gateway.jd.service.model.JdGatewayParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdQueryStatusReqParam;
import com.iboxpay.settlement.gateway.jd.service.model.JdQueryStatusRespParam;

@Service
public class QueryPayment_JdTrade implements IQueryPayment {

    private static Logger logger = LoggerFactory.getLogger(QueryPayment_JdTrade.class);
    private final static String TRANS_CODE_QUERY = "queryJdTradePay";
    private final static String TRANS_CODE_QUERY_DESC = "京东支付查询";

    @Resource
    private JdTradeRemoteService jdTradeRemoteService;

    @Override
    public TransCode getTransCode() {
        return TransCode.QUERY;
    }

    @Override
    public String getBankTransCode() {
        return TRANS_CODE_QUERY;
    }

    @Override
    public String getBankTransDesc() {

        return TRANS_CODE_QUERY_DESC;
    }

    @Override
    public void query(PaymentEntity[] payments) throws BaseTransException, IOException {
        PaymentEntity paymentEntity = payments[0];
        JdpayFrontEndConfig feConfig = (JdpayFrontEndConfig) TransContext.getContext().getFrontEndConfig();
        try {
            String merchantExt = paymentEntity.getMerchantExtProperties();
            Map<String, Object> merchantMap = JsonUtil.parseJSON2Map(merchantExt);
            // 交易商户号
            String merchantNo = String.valueOf(merchantMap.get("payMerchantNo"));
            // 子商户号
            String merchantSubNo = String.valueOf(merchantMap.get("payMerchantSubNo"));
            // 交易秘钥
            String merchantKey = String.valueOf(merchantMap.get("payMerchantKey"));
            String orderNo = paymentEntity.getSeqId();
            JdQueryStatusReqParam reqParam = new JdQueryStatusReqParam();
            reqParam.setOrderNo(orderNo);
            JdGatewayParam gatewayParam = new JdGatewayParam();
            gatewayParam.setGatewayUrl(feConfig.getQueryUrl().getVal());
            gatewayParam.setMerchantNo(merchantNo);
            gatewayParam.setSignMd5Key(merchantKey);
            JdQueryStatusRespParam resp = jdTradeRemoteService.doQueryStatus(reqParam, gatewayParam);
            /*if (null == resp) {
                return;
            }*/
            String is_success = resp.getIsSuccess();
            if ("Y".equals(is_success)) {
                /**
                 * 0 新建 
                 * 1 处理中 
                 * 2 支付成功  
                 * 3 支付失败 
                 * 4 退款中 
                 * 5 退款成功 
                 * 6 退款失败 
                 * 7 部分退款 
                 * 9 撤单成功
                 */
                String tradeStatus = resp.getStatus();
                //String orderNo = resp.getOrderNo();
                String desc = resp.getDesc();
                if (paymentEntity.getSeqId().equals(resp.getOrderNo())) {
                    if ("2".equals(tradeStatus)) {
                        PaymentStatus.setStatus(payments, PaymentStatus.STATUS_SUCCESS, "", "SUCCESS", desc);
                    } else if ("0".equals(tradeStatus)) {
                        PaymentStatus.setStatus(payments, PaymentStatus.STATUS_INIT, "", "INIT", desc);
                    } else if ("1".equals(tradeStatus)) {
                        PaymentStatus.setStatus(payments, PaymentStatus.STATUS_WAITTING_PAY, "", "WAITTING_PAY", desc);
                    } else if ("3".equals(tradeStatus)) {
                        PaymentStatus.setStatus(payments, PaymentStatus.STATUS_FAIL, "", "FAIL", desc);
                    } else if ("4".equals(tradeStatus)) {
                        PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND, "", "REFUND", desc);
                    } else if ("5".equals(tradeStatus)) {
                        PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND_SUCCESS, "REFUND_SUCCESS", "SUCCESS", desc);
                    } else if ("6".equals(tradeStatus)) {
                        PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND_FAIL, "", "REFUND_FAIL", desc);
                    } else if ("7".equals(tradeStatus)) {
                        PaymentStatus.setStatus(payments, PaymentStatus.STATUS_REFUND_FAIL, "", "REFUND_FAIL", desc);
                    } else if ("9".equals(tradeStatus)) {
                        PaymentStatus.setStatus(payments, PaymentStatus.STATUS_CANCEL, "", "CANCEL", desc);
                    }
                }
            } else if ("N".equals(is_success)) {
                PaymentStatus.setStatus(payments, PaymentStatus.STATUS_UNKNOWN, "", resp.getErrorCode(), resp.getErrorDes());
            }

        } catch (JsonParseException e) {
            logger.error("initCommonQueryData 组装报文异常:" + e);
        } catch (JsonMappingException e) {
            logger.error("initCommonQueryData 组装报文异常:" + e);
        } catch (IOException e) {
            logger.error("initCommonQueryData 组装报文异常:" + e);
        }
    }
}
