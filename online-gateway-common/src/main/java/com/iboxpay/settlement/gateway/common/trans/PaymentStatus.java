package com.iboxpay.settlement.gateway.common.trans;

import java.util.LinkedList;
import java.util.List;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;

public class PaymentStatus {

    /**初始状态*/
    public final static int STATUS_INIT = 0;
    /**准备提交.查询时这种状态的应该忽略，而不能向银行查询(可能是并发，也有可能是保存数据库时失败 交易意外中断)*/
    public final static int STATUS_TO_SUBMIT = 10;
    /**已提交银行，银行已接收，暂时没返回其他信息*/
    public final static int STATUS_SUBMITTED = 20;
    /**支付处理中*/
    public final static int STATUS_WAITTING_PAY = 25;
    /**交易成功*/
    public final static int STATUS_SUCCESS = 30;
    /**交易已撤消(只有{@link PaymentStatus#STATUS_INIT}的状态才能撤消)*/
    public final static int STATUS_CANCEL = 35;
    /**交易失败*/
    public final static int STATUS_FAIL = 40;
    /**交易状态未确定(通常是网络情况或者程序异常等导致)*/
    public final static int STATUS_UNKNOWN = 50;
    /**交易退款中**/
    public final static int STATUS_REFUND = 60;
    /**交易退款失败**/
    public final static int STATUS_REFUND_FAIL = 65;
    /**交易退款成功**/
    public final static int STATUS_REFUND_SUCCESS = 70;
    /**冲正**/
    public final static int STATUS_REVERSE =80;
    /**冲正失败**/
    public final static int STATUS_REVERSE_FAIL =85;
    /**交易关闭*/
    public final static int STATUS_CLOSED_FAIL = 99;
    /**交易关闭*/
    public final static int STATUS_CLOSED = 100;
    

    /**
     * 设置当前状态
     * @param status : 平台定义的状态
     * @param errorCode : 错误码
     * @param statusMsg : 导致该状态的信息
     * @param bankStatus : 银行返回的状态
     * @param bankStatusMsg : 银行返回的状态信息
     * @param payments
     */
    public final static void setStatus(PaymentEntity[] payments, ErrorCode errorCode, int status, String statusMsg, String bankStatus, String bankStatusMsg) {
        boolean isPay = TransContext.getContext().getTransCode() == TransCode.PAY;
        for (PaymentEntity payment : payments) {
            payment.setStatus(status);
            if (errorCode != null) {
                if (isPay) {
                    payment.setPayErrorCode(errorCode.getCode());
                    payment.setErrorCode(errorCode.getCode());
                } else {
                    payment.setErrorCode(errorCode.getCode());
                }
            }
            payment.setStatusMsg(statusMsg);
            payment.setBankStatus(bankStatus);
            payment.setBankStatusMsg(bankStatusMsg);
        }
    }
    
    public final static void setStatus(PaymentEntity[] payments, ErrorCode errorCode, int status, String statusMsg, String bankStatus, String bankStatusMsg, String callbackExtProperties) {
        boolean isPay = TransContext.getContext().getTransCode() == TransCode.PAY;
        for (PaymentEntity payment : payments) {
            payment.setStatus(status);
            if (errorCode != null) {
                if (isPay) {
                    payment.setPayErrorCode(errorCode.getCode());
                    payment.setErrorCode(errorCode.getCode());
                } else {
                    payment.setErrorCode(errorCode.getCode());
                }
            }
            payment.setStatusMsg(statusMsg);
            payment.setBankStatus(bankStatus);
            payment.setBankStatusMsg(bankStatusMsg);
            payment.setCallbackExtProperties(callbackExtProperties);
        }
    }

    /**
     * 设置当前状态
     * @param status : 平台定义的状态
     * @param errorCode : 错误码
     * @param statusMsg : 导致该状态的信息
     * @param bankStatus : 银行返回的状态
     * @param bankStatusMsg : 银行返回的状态信息
     * @param payments
     */
    public final static void setStatus(PaymentEntity payment, ErrorCode errorCode, int status, String statusMsg, String bankStatus, String bankStatusMsg) {
        setStatus(new PaymentEntity[] { payment }, errorCode, status, statusMsg, bankStatus, bankStatusMsg);
    }

    /**
     * 设置当前状态
     * @param status : 平台定义的状态
     * @param statusMsg : 导致该状态的信息
     * @param bankStatus : 银行返回的状态
     * @param bankStatusMsg : 银行返回的状态信息
     * @param payments
     */
    public final static void setStatus(PaymentEntity[] payments, int status, String statusMsg, String bankStatus, String bankStatusMsg) {
        setStatus(payments, null, status, statusMsg, bankStatus, bankStatusMsg);
    }
    
    /**
     * 设置当前状态（包含回调扩展属性）
     * @param payments
     * @param status : 平台定义的状态
     * @param statusMsg ：导致该状态的信息
     * @param bankStatus ：银行返回的状态
     * @param bankStatusMsg ：银行返回的状态信息
     * @param callbackExtProperties ：回调扩展属性
     */
    public final static void setStatus(PaymentEntity[] payments, int status, String statusMsg, String bankStatus, String bankStatusMsg, String callbackExtProperties) {
        setStatus(payments, null, status, statusMsg, bankStatus, bankStatusMsg, callbackExtProperties);
    }

    /**
     * 设置当前状态
     * @param status : 平台定义的状态
     * @param statusMsg : 导致该状态的信息
     * @param bankStatus : 银行返回的状态
     * @param bankStatusMsg : 银行返回的状态信息
     * @param payments
     */
    public final static void setStatus(PaymentEntity payment, int status, String statusMsg, String bankStatus, String bankStatusMsg) {
        setStatus(new PaymentEntity[] { payment }, status, statusMsg, bankStatus, bankStatusMsg);
    }

    public final static void setStatus(PaymentEntity payment, int status, String statusMsg, String bankStatus, String bankStatusMsg,String callbackExtProperties) {
        setStatus(new PaymentEntity[] { payment }, status, statusMsg, bankStatus, bankStatusMsg,callbackExtProperties);
    }
    /**
     * 设置当前状态
     * @param status : 平台定义的状态
     * @param errorCode : 错误码
     * @param statusMsg : 导致该状态的信息
     * @param payments
     */
    public final static void setStatus(PaymentEntity[] payments, ErrorCode unexpectedErrorCode, int status, String statusMsg) {
        boolean isPay = TransContext.getContext().getTransCode() == TransCode.PAY;
        for (PaymentEntity payment : payments) {
            payment.setStatus(status);
            if (unexpectedErrorCode != null) {
                if (isPay) {
                    payment.setPayErrorCode(unexpectedErrorCode.getCode());
                    payment.setErrorCode(unexpectedErrorCode.getCode());
                } else {
                    payment.setErrorCode(unexpectedErrorCode.getCode());
                }
            }
            payment.setStatusMsg(statusMsg);
        }
    }

    /**
     * 设置当前状态
     * @param status : 平台定义的状态
     * @param errorCode : 错误码
     * @param statusMsg : 导致该状态的信息
     * @param payments
     */
    public final static void setStatus(PaymentEntity payment, ErrorCode unexpectedErrorCode, int status, String statusMsg) {
        setStatus(new PaymentEntity[] { payment }, unexpectedErrorCode, status, statusMsg);
    }

    /**
     * 设置当前状态
     * @param status : 平台定义的状态
     * @param statusMsg : 导致该状态的信息
     * @param payments
     */
    public final static void setStatus(PaymentEntity[] payments, int status, String statusMsg) {
        setStatus(payments, null, status, statusMsg);
    }

    /**
     * 设置当前状态
     * @param status : 平台定义的状态
     * @param statusMsg : 导致该状态的信息
     * @param payments
     */
    public final static void setStatus(PaymentEntity payment, int status, String statusMsg) {
        setStatus(new PaymentEntity[] { payment }, status, statusMsg);
    }

    public final static void resetErrorCode(PaymentEntity[] payments) {
        for (PaymentEntity payment : payments) {
            payment.setErrorCode(0);
        }
    }

    /**
     * 处理发送前状态.如果在发送前出现异常，直接处理成交易失败.
     */
    public static void processExceptionBeforePay(Throwable e, PaymentEntity[] payments) {
        PaymentStatus.setStatus(payments, ErrorCode.getErrorCodeByException(e), PaymentStatus.STATUS_FAIL, e.getMessage());
    }

    /**
     * 发送时或者解析时出现异常，则认为不确定的状态.
     */
    public static void processExceptionWhenPay(Throwable e, PaymentEntity[] payments) {
        PaymentStatus.setStatus(payments, ErrorCode.getErrorCodeByException(e), PaymentStatus.STATUS_UNKNOWN, e.getMessage());
    }

    /**
     * 过滤掉最终状态的交易(成功 Or 失败 Or 撤消 为最终状态)
     * @param payments
     * @return
     */
    public static List<PaymentEntity> getNotFinalStatus(PaymentEntity[] payments) {
        List<PaymentEntity> notFinalStatusList = new LinkedList<PaymentEntity>();
        for (PaymentEntity payment : payments) {
            if (isFinalStatus(payment)) continue;

            notFinalStatusList.add(payment);
        }
        return notFinalStatusList;
    }

    /**
     * 是否是最终状态(成功 Or 失败 Or 撤消 为最终状态)
     * @param payment
     * @return
     */
    public final static boolean isFinalStatus(PaymentEntity payment) {
        int status = payment.getStatus();
		return status == STATUS_SUCCESS 
			   || status == STATUS_FAIL 
			   || status == STATUS_CANCEL 
			   || status == STATUS_REFUND
			   || status == STATUS_REVERSE
        	   || status == STATUS_REFUND_SUCCESS
        	   || status == STATUS_REFUND_FAIL
		       || status == STATUS_CLOSED;
    }
}
