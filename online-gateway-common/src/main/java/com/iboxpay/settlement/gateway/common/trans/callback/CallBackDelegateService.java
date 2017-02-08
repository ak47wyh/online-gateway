package com.iboxpay.settlement.gateway.common.trans.callback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.iboxpay.common.utils.OkHttpUtils;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.dao.PaymentDao;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.inout.CommonRequestModel;
import com.iboxpay.settlement.gateway.common.inout.CommonResultModel;
import com.iboxpay.settlement.gateway.common.inout.callback.CallbackPaymentRequestModel;
import com.iboxpay.settlement.gateway.common.inout.payment.PaymentOuterStatus;
import com.iboxpay.settlement.gateway.common.inout.payment.PaymentResultModel;
import com.iboxpay.settlement.gateway.common.service.PaymentService;
import com.iboxpay.settlement.gateway.common.task.RunnableTask;
import com.iboxpay.settlement.gateway.common.task.TaskParam;
import com.iboxpay.settlement.gateway.common.task.TaskScheduler;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;
import com.iboxpay.settlement.gateway.common.trans.ErrorCode;
import com.iboxpay.settlement.gateway.common.trans.ITransDelegate;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.payment.IPayment;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

@Service
public class CallBackDelegateService implements ITransDelegate, RunnableTask{
	private final Logger logger = LoggerFactory.getLogger(CallBackDelegateService.class);
    public final static String BATCH_NUM = "batchNum";
    public final static String PARAM_RPAYMENT = "callbackPayment";
    public final static String PARAM_MODEL="callbackModel";
    final static String PARAM_QPAYMENT_IDS = "qpids";
    @Resource
    private PaymentDao paymentDao;

    @Resource
    private PaymentService paymentService;
	
	@Override
	public TransCode getTransCode() {
		return TransCode.CALLBACK;
	}

	@Override
	public CommonRequestModel parseInput(String input) throws Exception {
		CallbackPaymentRequestModel requestModel = (CallbackPaymentRequestModel) JsonUtil.jsonToObject(input, "UTF-8", CallbackPaymentRequestModel.class);
        return requestModel;
	}

	@Override
	public CommonResultModel trans(TransContext context, CommonRequestModel requestModel) {
	    context.setTransCode(TransCode.CALLBACK);
	    CallbackPaymentRequestModel model = (CallbackPaymentRequestModel) requestModel;
        PaymentResultModel resultModel = new PaymentResultModel();
        resultModel.setAppCode(context.getMainAccount().getAccNo());
        
        String outTradeNo=model.getOutTradeNo();
    	PaymentEntity payment=paymentDao.getPaymentsByBankSeqId(outTradeNo);
        //检查是否存在符合条件的记录
        if (payment == null) {
            return resultModel.fail(ErrorCode.payment_not_exist, "找不到批次流水号");
        }
        if (requestModel==null){
        	return resultModel.fail(ErrorCode.input_error, "输入参数有误");
        }
        	
        // 
        AtomicInteger batchNum = new AtomicInteger(0);//需要同步的批次总数
        Map<String, Object> params;
        params = new HashMap<String, Object>();
        batchNum.set(1);
        params.put(PARAM_RPAYMENT, payment);
        params.put(BATCH_NUM, batchNum);
        params.put(PARAM_MODEL, model);
        return TaskScheduler.scheduleTask(TransCode.CALLBACK, context.getMainAccount(), context.getBankProfile().getBankName(), params, false);//同步返回
	}
	
	@Override
	public CommonResultModel run(TaskParam taskParam) {
        PaymentResultModel resultModel = new PaymentResultModel();
        TransContext context = TransContext.getContext();
        PaymentEntity payment=(PaymentEntity) taskParam.getParams().get(PARAM_RPAYMENT);
        CallbackPaymentRequestModel requestModel=(CallbackPaymentRequestModel) taskParam.getParams().get(PARAM_MODEL);
        AccountEntity accountEntity=taskParam.getAccountEntity();
        doCallback(context, payment,requestModel);
        
        // 组装返回参数
        resultModel.setAppCode(accountEntity.getAccNo());
        resultModel.setBatchSeqId(payment.getBatchSeqId());
        // 返回现有的结果，新的在后台查询
        PaymentEntity[] paymentEntitys=new PaymentEntity[]{payment};
        PaymentOuterStatus.transmitStatusToResultModel(paymentEntitys, resultModel);
        return resultModel;
	}
	
	
	 private void doCallback(TransContext context, PaymentEntity payment,CommonRequestModel requestModel) {
	    String payTransCode = payment.getPayTransCode();
	    IPayment paymentImpl = BankTransComponentManager.getPaymentByTransCode(context.getBankProfile().getBankName(), payTransCode);
	    Class<? extends ICallBackPayment> queryClass = paymentImpl.getCallBackClass();
	    ICallBackPayment callPaymentImpl = (ICallBackPayment) BankTransComponentManager.getBankComponent(queryClass); 
		 
		try {
			callPaymentImpl.doCallback(payment,requestModel);
		} catch (Throwable e) {
			String message = "退款结果后更新数据库失败";
			logger.error(message, e);
		}
		//发送异步通知请求【对接网关的系统】
		String result=CallBackPaymentUtils.doSendNotifyUrl(payment);
		logger.info("CallBackDelegateService.doCallback 异步推送返回："+result);
		
		//更新数据库状态
        try {
        	PaymentEntity[] payments=new PaymentEntity[]{payment};
            paymentService.updateStatus(payments, true);
        } catch (Throwable e) {
            String message = "查询支付结果后更新数据库失败";
            logger.error(message, e);
        }
	 }

	
}
