package com.iboxpay.settlement.gateway.common.inout.payment;


import java.util.HashMap;
import java.util.Map;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.inout.CommonResultModel;
import com.iboxpay.settlement.gateway.common.trans.ErrorCode;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

/**
 * 对外支付结果状态
 * @author jianbo_chen
 */
public class PaymentOuterStatus {

    //////////////////////返回结果头部（外层）只有成功与失败两种状态///////////////////
    //交易成功
    public final static String STATUS_SUCCESS = "success";
    //交易失败
    public final static String STATUS_FAIL = "fail";
    //交易撤消
    public final static String STATUS_CANCEL = "cancel";

    /////////////////////////////////////////////////////
    //等待处理
    public final static String STATUS_WAITING = "waiting";
    //等待支付中
    public final static String STATUS_USERPAYING = "userpaying";
    //正在处理
    public final static String STATUS_PROCESSING = "processing";
    //交易状态未知
    public final static String STATUS_UNKNOW = "unknow";
    
    ///////////////////////////////////////////////////
    /**交易退款中**/
    public final static String STATUS_REFUND = "refunding";
    /**交易退款成功**/
    public final static String STATUS_REFUND_SUCCESS = "refundsuccess";
    /**交易退款失败**/
    public final static String STATUS_REFUND_FAIL = "refundfail";
    /**冲正**/
    public final static String STATUS_REVERSE ="reverse";
    /**冲正失败**/
    public final static String STATUS_REVERSE_FAIL ="reversefail";
    /**交易关闭*/
    public final static String STATUS_CLOSED = "closed";
    /**交易关闭失败*/
    public final static String STATUS_CLOSED_FAIL = "closedfail";    

    /**
     * PaymentEntity支付状态转为客户端结果模型ResultModel
     */
    public final static void transmitStatusToResultModel(PaymentEntity[] paymentEntitys, PaymentResultModel resultModel) {
        transmitStatusToResultModel(paymentEntitys, resultModel, null, null);
    }

    /**
     * PaymentEntity支付状态转为客户端结果模型ResultModel(支付时才能使用)
     */
    public final static void transmitStatusToResultModel(PaymentEntity[] paymentEntitys, PaymentResultModel resultModel, ErrorCode unexpectedErrorCode, String errorMsg) {
        resultModel.setStatus(CommonResultModel.STATUS_SUCCESS);//外层算通信成功
        PaymentCustomerResult paymentCustomerResults[] = new PaymentCustomerResult[paymentEntitys.length];
        resultModel.setData(paymentCustomerResults);//结果数组
        for (int i = 0; i < paymentEntitys.length; i++) {
            PaymentEntity paymentEntity = paymentEntitys[i];
            PaymentCustomerResult paymentCustomerResult = new PaymentCustomerResult();
            paymentCustomerResult.setSeqId(paymentEntity.getSeqId());
            paymentCustomerResult.setAccNo(paymentEntity.getCustomerAccNo());
            paymentCustomerResult.setAccName(paymentEntity.getCustomerAccName());
            paymentCustomerResult.setBankBatchSeqId(paymentEntity.getBankBatchSeqId());
            paymentCustomerResult.setBankSeqId(paymentEntity.getBankSeqId());
            
            // 扩展属性
            Map<String, Object> extPropertiesMap = initExtPropertesMap(paymentEntity);
            paymentCustomerResult.setExtProperties(extPropertiesMap);
            
            paymentCustomerResults[i] = paymentCustomerResult;
            if (unexpectedErrorCode != null) {//有意外异常，如支付后数据库更新失败.
                paymentCustomerResult.setStatus(PaymentOuterStatus.STATUS_UNKNOW);
                paymentCustomerResult.setErrorCode(unexpectedErrorCode);
                paymentCustomerResult.setStatusMsg(errorMsg);
            } else {

                ErrorCode errorCode = ErrorCode.parseErrorCode(paymentEntity.getErrorCode());
                ErrorCode payErrorCode = ErrorCode.parseErrorCode(paymentEntity.getPayErrorCode());

                paymentCustomerResult.setPayBankStatus(paymentEntity.getPayBankStatus());
                paymentCustomerResult.setPayBankStatusMsg(paymentEntity.getPayBankStatusMsg());
                paymentCustomerResult.setPayErrorCode(payErrorCode);

                paymentCustomerResult.setBankStatus(paymentEntity.getBankStatus());
                paymentCustomerResult.setBankStatusMsg(paymentEntity.getBankStatusMsg());
                paymentCustomerResult.setErrorCode(errorCode);

                String statusInfo[] = getStatusInfo(paymentEntity.getStatus(), paymentEntity.getStatusMsg(), errorCode);
                paymentCustomerResult.setStatus(statusInfo[0]);
                paymentCustomerResult.setStatusMsg(statusInfo[1]);
            }

        }
    }

    /**
     * 初始化返回参数的扩展属性
     * @param paymentEntity
     * @return
     */
	private static Map<String, Object> initExtPropertesMap(PaymentEntity paymentEntity) {
		Map<String,Object> extPropertiesMap=new HashMap<String,Object>();
		//封装解析回调扩展属性对应的参数信息
		initCallbackExtpropeties(paymentEntity, extPropertiesMap);
		
		//封装解析扩展属性对应的参数信息
		initExtPropertys(paymentEntity, extPropertiesMap);
		return extPropertiesMap;
	}

	/**
	 * 封装解析扩展属性对应的参数信息
	 * @param paymentEntity
	 * @param extPropertiesMap
	 */
	public static void initExtPropertys(PaymentEntity paymentEntity, Map<String, Object> extPropertiesMap) {
		// 授权码
		String authCode=(String) paymentEntity.getExtProperty("authCode");
		if(!StringUtils.isEmpty(authCode)){
			extPropertiesMap.put("authCode", authCode);
		}
		// 产品信息
		String productInfo=(String) paymentEntity.getExtProperty("productInfo");
		if(!StringUtils.isEmpty(productInfo)){
			extPropertiesMap.put("productInfo", productInfo);
		}
		// 商品详情
		String productBody=(String) paymentEntity.getExtProperty("productBody");
		if(!StringUtils.isEmpty(productBody)){
			extPropertiesMap.put("productBody", productBody);
		}
		// 证件类型
		String certType=(String) paymentEntity.getExtProperty("certType");
		if(!StringUtils.isEmpty(certType)){
			extPropertiesMap.put("certType", certType);
		}		
		// 证件号
		String certNo=(String) paymentEntity.getExtProperty("certNo");
		if(!StringUtils.isEmpty(certNo)){
			extPropertiesMap.put("certNo", certNo);
		}
		// 手机号
		String mobileNo=(String) paymentEntity.getExtProperty("mobileNo");
		if(!StringUtils.isEmpty(mobileNo)){
			extPropertiesMap.put("mobileNo", mobileNo);
		} 
		// 退款订单号
		String outRefundNo=(String)paymentEntity.getExtProperty("outRefundNo");
		if(!StringUtils.isEmpty(outRefundNo)){
			extPropertiesMap.put("outRefundNo", outRefundNo);
		} 
		// 退款金额
		String outRefundAmount=(String)paymentEntity.getExtProperty("outRefundAmount");
		if(!StringUtils.isEmpty(outRefundAmount)){
			extPropertiesMap.put("outRefundAmount", outRefundAmount);
		} 
		// 商品展示网址
		String showUrl=(String)paymentEntity.getExtProperty("showUrl");
		if(!StringUtils.isEmpty(showUrl)){
			extPropertiesMap.put("showUrl", showUrl);
		} 
		// 订单名称
		String orderTitle=(String)paymentEntity.getExtProperty("orderTitle");
		if(!StringUtils.isEmpty(orderTitle)){
			extPropertiesMap.put("orderTitle", orderTitle);
		} 
		// 商品描述
		String orderBody=(String)paymentEntity.getExtProperty("orderBody");
		if(!StringUtils.isEmpty(orderBody)){
			extPropertiesMap.put("orderBody", orderBody);
		} 
		// 支付请求响应页面
		String htmlContext = paymentEntity.getHtmlContext();
		if(!StringUtils.isEmpty(htmlContext)){
			extPropertiesMap.put("htmlContext", htmlContext);
		}
	}

	/**
	 * 封装解析回调扩展属性对应的参数信息
	 * @param paymentEntity
	 * @param extPropertiesMap
	 * @return
	 */
	public static void initCallbackExtpropeties(PaymentEntity paymentEntity, Map<String, Object> extPropertiesMap) {
		// 二维码图片
		String codeImgUrl=(String) paymentEntity.getCallbackExtProperties("codeImgUrl");
		if(!StringUtils.isEmpty(codeImgUrl)){
			extPropertiesMap.put("codeImgUrl", codeImgUrl);
		}
		// 二维码图片[大图]
		String bigImgUrl=(String)paymentEntity.getExtProperty("bigImgUrl");
		if(!StringUtils.isEmpty(bigImgUrl)){
			extPropertiesMap.put("bigImgUrl", bigImgUrl);
		} 
		// 微信二维码
		String codeUrl=(String) paymentEntity.getCallbackExtProperties("codeUrl");
		if(!StringUtils.isEmpty(codeUrl)){
			extPropertiesMap.put("codeUrl", codeUrl);
		}
		// 买家支付宝用户号
		String buyerId=(String) paymentEntity.getCallbackExtProperties("buyerId");
		if(!StringUtils.isEmpty(buyerId)){
			extPropertiesMap.put("buyerId", buyerId);
		}
		
		// 买家登陆账号
		String buyerLoginId=(String) paymentEntity.getCallbackExtProperties("buyerLoginId");
		if(!StringUtils.isEmpty(buyerLoginId)){
			extPropertiesMap.put("buyerLoginId", buyerLoginId);
		}
		// 预支付交易会话标识
		String prepayId=(String) paymentEntity.getCallbackExtProperties("prepayId");
		if(!StringUtils.isEmpty(prepayId)){
			extPropertiesMap.put("prepayId", prepayId);
		}
		// 用户标识
		String openid=(String) paymentEntity.getCallbackExtProperties("openid");
		if(!StringUtils.isEmpty(openid)){
			extPropertiesMap.put("openid", openid);
		}
		// 用户子标识
		String subOpenid=(String) paymentEntity.getCallbackExtProperties("subOpenid");
		if(!StringUtils.isEmpty(subOpenid)){
			extPropertiesMap.put("subOpenid", subOpenid);
		}
		// 是否关注公众账号
		String isSubscribe=(String) paymentEntity.getCallbackExtProperties("isSubscribe");
		if(!StringUtils.isEmpty(isSubscribe)){
			extPropertiesMap.put("isSubscribe", isSubscribe);
		}
		
		// 是否关注子公众账号
		String subIsSubscribe=(String) paymentEntity.getCallbackExtProperties("subIsSubscribe");
		if(!StringUtils.isEmpty(subIsSubscribe)){
			extPropertiesMap.put("subIsSubscribe", subIsSubscribe);
		}
		// 付款银行
		String bankType=(String) paymentEntity.getCallbackExtProperties("bankType");
		if(!StringUtils.isEmpty(bankType)){
			extPropertiesMap.put("bankType", bankType);
		}
		// 货币类型
		String feeType=(String) paymentEntity.getCallbackExtProperties("feeType");
		if(!StringUtils.isEmpty(feeType)){
			extPropertiesMap.put("feeType", feeType);
		}
		// 微信支付订单号
		String transactionId=(String) paymentEntity.getCallbackExtProperties("transactionId");
		if(!StringUtils.isEmpty(transactionId)){
			extPropertiesMap.put("transactionId", transactionId);
		}
		/**************************************************************
		 * 微信公众号支付回调参数返回
		 **************************************************************/
		// 微信支付订单号
		String appId=(String) paymentEntity.getCallbackExtProperties("appId");
		if(!StringUtils.isEmpty(appId)){
			extPropertiesMap.put("appId", appId);
		}
		String timeStamp=(String) paymentEntity.getCallbackExtProperties("timeStamp");
		if(!StringUtils.isEmpty(timeStamp)){
			extPropertiesMap.put("timeStamp", timeStamp);
		}
		String nonceStr=(String) paymentEntity.getCallbackExtProperties("nonceStr");
		if(!StringUtils.isEmpty(nonceStr)){
			extPropertiesMap.put("nonceStr", nonceStr);
		}
		String packageExt=(String) paymentEntity.getCallbackExtProperties("packageExt");
		if(!StringUtils.isEmpty(packageExt)){
			extPropertiesMap.put("packageExt", packageExt);
		}
		String signType=(String) paymentEntity.getCallbackExtProperties("signType");
		if(!StringUtils.isEmpty(signType)){
			extPropertiesMap.put("signType", signType);
		}
		String paySign=(String) paymentEntity.getCallbackExtProperties("paySign");
		if(!StringUtils.isEmpty(paySign)){
			extPropertiesMap.put("paySign", paySign);
		}
	}

    public static String[] getStatusInfo(int status, String statusMsg, ErrorCode errorCode) {
        String statusInfo[] = new String[2];
        switch (status) {
            case PaymentStatus.STATUS_SUCCESS:
                statusInfo[0] = PaymentOuterStatus.STATUS_SUCCESS;
                statusInfo[1] = ("交易成功." + (StringUtils.isBlank(statusMsg) ? "" : "(" + statusMsg + ")"));
                break;

            case PaymentStatus.STATUS_INIT:
                statusInfo[0] = PaymentOuterStatus.STATUS_WAITING;
                statusInfo[1] = "等待处理.";
                break;

            case PaymentStatus.STATUS_TO_SUBMIT:
                statusInfo[0] = PaymentOuterStatus.STATUS_PROCESSING;
                statusInfo[1] = "正在处理(如果时间过长，请联系技术人员)";
                break;

            case PaymentStatus.STATUS_SUBMITTED:
                statusInfo[0] = PaymentOuterStatus.STATUS_PROCESSING;
                if (!StringUtils.isBlank(statusMsg))
                    statusInfo[1] = statusMsg;
                else statusInfo[1] = "银行正在处理";//也有可能已经失败，再过来查询吧.
                break;
            case PaymentStatus.STATUS_WAITTING_PAY:
                statusInfo[0] = PaymentOuterStatus.STATUS_USERPAYING;
                if (!StringUtils.isBlank(statusMsg))
                    statusInfo[1] = statusMsg;
                else statusInfo[1] = "等待支付中";//也有可能已经失败，再过来查询吧.
                break;
            case PaymentStatus.STATUS_UNKNOWN:
                statusInfo[0] = PaymentOuterStatus.STATUS_UNKNOW;
                if (statusMsg != null && statusMsg.length() > 0)
                    statusInfo[1] = statusMsg;
                else statusInfo[1] = "交易结果未知，请查询确认";
                break;

            case PaymentStatus.STATUS_FAIL:
                statusInfo[0] = PaymentOuterStatus.STATUS_FAIL;
                if (!StringUtils.isBlank(statusMsg) && !"交易失败".equals(statusMsg))
                    statusInfo[1] = "交易失败(" + statusMsg + ")";
                else if (errorCode != null)
                    statusInfo[1] = "交易失败(" + errorCode.getMessage() + ")";
                else statusInfo[1] = "交易失败";

                break;

            case PaymentStatus.STATUS_CANCEL:
                statusInfo[0] = PaymentOuterStatus.STATUS_CANCEL;
                statusInfo[1] = "交易已撤消";//也有可能已经失败，再过来查询吧.
                break;

            case PaymentStatus.STATUS_REFUND:
                statusInfo[0] = PaymentOuterStatus.STATUS_REFUND;
                statusInfo[1] = "交易退款中";//
                break;
                
            case PaymentStatus.STATUS_REFUND_SUCCESS:
                statusInfo[0] = PaymentOuterStatus.STATUS_REFUND_SUCCESS;
                statusInfo[1] = "退款成功";//
                break;    
            case PaymentStatus.STATUS_REFUND_FAIL:
                statusInfo[0] = PaymentOuterStatus.STATUS_REFUND_FAIL;
                statusInfo[1] = "退款失败";//
                break;    
            case PaymentStatus.STATUS_REVERSE:
                statusInfo[0] = PaymentOuterStatus.STATUS_REVERSE;
                statusInfo[1] = "冲正成功";//
                break; 
            case PaymentStatus.STATUS_REVERSE_FAIL:
                statusInfo[0] = PaymentOuterStatus.STATUS_REVERSE_FAIL;
                statusInfo[1] = "冲正失败";//
                break;     
            case PaymentStatus.STATUS_CLOSED:
                statusInfo[0] = PaymentOuterStatus.STATUS_CLOSED;
                statusInfo[1] = "交易关闭";//
                break;     
            case PaymentStatus.STATUS_CLOSED_FAIL:
                statusInfo[0] = PaymentOuterStatus.STATUS_CLOSED_FAIL;
                statusInfo[1] = "交易关闭失败";//
                break;     
            default:
                statusInfo[0] = PaymentOuterStatus.STATUS_UNKNOW;
                statusInfo[1] = "无法识别状态(" + status + ")";
                break;
        }
        return statusInfo;
    }
}
