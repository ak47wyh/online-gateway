package com.iboxpay.settlement.gateway.common.inout.verify;

import com.iboxpay.settlement.gateway.common.domain.AccountVerifyEntity;
import com.iboxpay.settlement.gateway.common.inout.CommonResultModel;
import com.iboxpay.settlement.gateway.common.inout.payment.PaymentOuterStatus;
import com.iboxpay.settlement.gateway.common.trans.ErrorCode;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

public class VerifyOuterStatus {
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

	public final static void transmitStatusToResultModel(AccountVerifyEntity account,VerifyAccountResultModel resultModel,VerifyAccountRequestModel requestModel) {
		transmitStatusToResultModel(account, resultModel,requestModel, null, null);
	}

	/**
	 * PaymentEntity支付状态转为客户端结果模型ResultModel(支付时才能使用)
	 */
	public final static void transmitStatusToResultModel(AccountVerifyEntity account, VerifyAccountResultModel resultModel,VerifyAccountRequestModel requestModel,ErrorCode unexpectedErrorCode, String errorMsg) {
		resultModel.setStatus(CommonResultModel.STATUS_SUCCESS);//外层算通信成功
		VerifyAccountResult paymentCustomerResults[] = new VerifyAccountResult[1];
        resultModel.setData(paymentCustomerResults);//结果数组
		VerifyAccountResult paymentCustomerResult = new VerifyAccountResult();
		paymentCustomerResult.setSeqId(requestModel.getData()[0].getSeqId());
		paymentCustomerResult.setAccNo(account.getCustomerAccNo());
		paymentCustomerResult.setAccName(account.getCustomerAccName());
		paymentCustomerResult.setCertNo(account.getCertNo());
		paymentCustomerResult.setMobileNo(account.getMobileNo());

		paymentCustomerResults[0] = paymentCustomerResult;
		if (unexpectedErrorCode != null) {//有意外异常，如支付后数据库更新失败.
			paymentCustomerResult.setStatus(PaymentOuterStatus.STATUS_UNKNOW);
			paymentCustomerResult.setStatusMsg(errorMsg);
			paymentCustomerResult.setErrorCode(unexpectedErrorCode.toString());
		} else {
			String statusInfo[] = getStatusInfo(account.getStatus(), account.getStatusMsg(), null);
            paymentCustomerResult.setStatus(statusInfo[0]);
            paymentCustomerResult.setStatusMsg(statusInfo[1]);
			paymentCustomerResult.setErrorCode(account.getErrorCode());
		}
	}

    public static String[] getStatusInfo(int status, String statusMsg, ErrorCode errorCode) {
        String statusInfo[] = new String[2];
        switch (status) {
            case PaymentStatus.STATUS_SUCCESS:
                statusInfo[0] = PaymentOuterStatus.STATUS_SUCCESS;
                statusInfo[1] = ("验证成功." + (StringUtils.isBlank(statusMsg) ? "" : "(" + statusMsg + ")"));
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
                else statusInfo[1] = "等待验证中";//也有可能已经失败，再过来查询吧.
                break;
            case PaymentStatus.STATUS_UNKNOWN:
                statusInfo[0] = PaymentOuterStatus.STATUS_UNKNOW;
                if (statusMsg != null && statusMsg.length() > 0)
                    statusInfo[1] = statusMsg;
                else statusInfo[1] = "交易结果未知，请查询确认";
                break;

            case PaymentStatus.STATUS_FAIL:
                statusInfo[0] = PaymentOuterStatus.STATUS_FAIL;
                if (!StringUtils.isBlank(statusMsg) && !"验证失败".equals(statusMsg))
                    statusInfo[1] = "验证失败(" + statusMsg + ")";
                else if (errorCode != null)
                    statusInfo[1] = "验证失败(" + errorCode.getMessage() + ")";
                else statusInfo[1] = "验证失败";

                break;

            default:
                statusInfo[0] = VerifyOuterStatus.STATUS_UNKNOW;
                statusInfo[1] = "无法识别状态(" + status + ")";
                break;
        }
        return statusInfo;
    }
}
