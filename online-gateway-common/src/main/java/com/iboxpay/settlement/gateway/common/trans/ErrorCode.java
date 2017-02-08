package com.iboxpay.settlement.gateway.common.trans;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.settlement.gateway.common.exception.FrontEndException;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;

/***
 * 错误码
 * @author jianbo_chen
 */
public enum ErrorCode {
    /**输入参数有误**/
    input_error(1, "输入参数有误"),
    /**交易记录不存在**/
    payment_not_exist(2, "交易记录不存在"),
    /**交易记录已存在**/
    payment_exist(3, "交易记录已存在"), //流水号重复
    /**客户账户信息有误**/
    receiver_incorrect(4, "客户账户信息有误"),
    /**主账号业务问题.(与银行对接环境有业务问题，如账户余额不足、协议编号不存在、权限没开通等等)**/
    main_acc_err(5, "主账号(公司账号)业务问题"),
    /**王府井一清通道,一天只能推送一次*/
    payment_once_err(6, "王府井一清通道一天只能推送一次"),
    //新的在这前加
    payment_exist_refund(7, "交易记录已退款"), //流水号重复
    /**未知错误(在接口实现时没设置 或 错误调整后找不到)*/
    unknow(999, "未知错误"),

    //-----------------系统错误相关--------------------

    /**报文封装错误**/
    sys_pack_msg_err(1001, "报文封装错误"),
    /**报文解析错误**/
    sys_parse_msg_err(1002, "报文解析错误"),
    /**未实现操作*/
    sys_not_support(1003, "未实现操作"),
    /**前置机网络错误**/
    sys_network_err(1004, "前置机网络错误"),
    /**系统非运行状态*/
    sys_not_running(1005, "系统非运行状态"),
    //新的在这前加

    /**系统内部错误**/
    sys_internal_err(9999, "系统内部错误"), ;

    private static Logger logger = LoggerFactory.getLogger(ErrorCode.class);

    private int code;
    private String message;
    private static Map<Integer, ErrorCode> allErrorMap;

    static {
        allErrorMap = new HashMap<Integer, ErrorCode>();
        ErrorCode allErrorCodes[] = ErrorCode.values();
        for (ErrorCode errorCode : allErrorCodes) {
            if (allErrorMap.get(errorCode.getCode()) != null) {
                logger.error("", new Exception("错误码重复:" + errorCode.getCode()));
                continue;
            }
            allErrorMap.put(errorCode.getCode(), errorCode);
        }
    }

    private ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ErrorCode parseErrorCode(int code) {
        if (code <= 0) return null;

        ErrorCode errorCode = allErrorMap.get(code);
        if (errorCode == null) {
            logger.error("找不到匹配的错误码:" + code);
            errorCode = unknow;
        }
        return errorCode;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据异常获取错误码
     * @param e
     * @return
     */
    public final static ErrorCode getErrorCodeByException(Throwable e) {
        if (e instanceof PackMessageException) {
            return ErrorCode.sys_pack_msg_err;
        } else if (e instanceof FrontEndException) {
            return ErrorCode.sys_network_err;
        } else if (e instanceof ParseMessageException) {
            return ErrorCode.sys_parse_msg_err;
        } else {
            return ErrorCode.sys_internal_err;
        }
    }
}
