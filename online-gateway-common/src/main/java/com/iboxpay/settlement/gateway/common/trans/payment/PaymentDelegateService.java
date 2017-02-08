package com.iboxpay.settlement.gateway.common.trans.payment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.persistence.Column;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.config.SystemConfig;
import com.iboxpay.settlement.gateway.common.dao.PaymentDao;
import com.iboxpay.settlement.gateway.common.dao.PaymentMerchantDao;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.BatchPaymentEntity;
import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.domain.PaymentMerchantEntity;
import com.iboxpay.settlement.gateway.common.inout.CommonRequestModel;
import com.iboxpay.settlement.gateway.common.inout.CommonResultModel;
import com.iboxpay.settlement.gateway.common.inout.payment.PaymentCustomerInfo;
import com.iboxpay.settlement.gateway.common.inout.payment.PaymentOuterStatus;
import com.iboxpay.settlement.gateway.common.inout.payment.PaymentRequestModel;
import com.iboxpay.settlement.gateway.common.inout.payment.PaymentResultModel;
import com.iboxpay.settlement.gateway.common.service.PaymentService;
import com.iboxpay.settlement.gateway.common.task.RunnableTask;
import com.iboxpay.settlement.gateway.common.task.TaskParam;
import com.iboxpay.settlement.gateway.common.task.TaskScheduler;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;
import com.iboxpay.settlement.gateway.common.trans.CompositeTransListener;
import com.iboxpay.settlement.gateway.common.trans.ErrorCode;
import com.iboxpay.settlement.gateway.common.trans.IBankTrans;
import com.iboxpay.settlement.gateway.common.trans.IBankTransInterceptor;
import com.iboxpay.settlement.gateway.common.trans.ITransDelegate;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

/**
 * 支付入口
 * @author jianbo_chen
 */
@Service
public class PaymentDelegateService implements ITransDelegate, RunnableTask {

    private final Logger logger = LoggerFactory.getLogger(PaymentDelegateService.class);
    private final static CompositeTransListener compositeListener = new CompositeTransListener();
    public final static String PARAM_PAYMENT = "payment";
    public final static String PARAM_PAYMENT_IDS = "pids";
    public final static String BATCH_NUM = "batchNum";

    @Resource
    private PaymentService paymentService;

    @Resource
    private PaymentDao paymentDao;
    
    @Resource PaymentMerchantDao paymentMerchantDao;

    @Override
    public TransCode getTransCode() {
        return TransCode.PAY;
    }

    public static CompositeTransListener getCompositelistener() {
        return compositeListener;
    }

    @Override
    public CommonRequestModel parseInput(String input) throws Exception {
        PaymentRequestModel requestModel = (PaymentRequestModel) JsonUtil.jsonToObject(input, "UTF-8", PaymentRequestModel.class);
        if (requestModel.isForTest() && requestModel.getBatchSeqId() == null) {
            requestModel.setBatchSeqId(System.currentTimeMillis() + "" + new Random().nextInt(9999));
        }
        return requestModel;
    }

    public CommonResultModel trans(TransContext context, CommonRequestModel _model) {
        int maxPaymentSize = SystemConfig.MAX_PAYMENT_SIZE.getIntVal();//修复批量数动态读取配置值问题
        PaymentRequestModel model = (PaymentRequestModel) _model;
        context.setTransCode(TransCode.PAY);
        PaymentResultModel resultModel = new PaymentResultModel();
        resultModel.setAppCode(model.getAppCode());
        resultModel.setBatchSeqId(model.getBatchSeqId());
        
        IBankTrans[] payImpls = BankTransComponentManager.getBankComponent(context.getMainAccount(), context.getBankProfile().getBankName(), IPayment.class);
        if(payImpls == null || payImpls.length == 0){
            return resultModel.setAppCode(model.getAppCode()).fail(ErrorCode.sys_not_support, "[配置错误]找不到交易实现类");
        }
        if (StringUtils.isBlank(model.getBatchSeqId())) {
            return resultModel.fail(ErrorCode.input_error, "缺少批次流水号.");
        }
        if (model.getData() == null || model.getData().length == 0) {
            return resultModel.fail(ErrorCode.input_error, "支付笔数为0");
        }
        if (model.getData().length > maxPaymentSize) {
            return resultModel.fail(ErrorCode.input_error, "支付笔数过多，最大支持：" + maxPaymentSize + "(笔)");
        }

        //检查流水号重复
        if (paymentDao.getBatchPaymentEntity(model.getBatchSeqId()) != null) {
            return resultModel.fail(ErrorCode.payment_exist, "批次流水号已存在:" + model.getBatchSeqId());
        }

        //检查商户交易商户号(注意：只是在无卡支付时验证) add by liaoxiongjian 20160305
        Map<String,Object> merchantMap=new HashMap<String,Object>();
        String payType =model.getType();
        if(payType!=null&&payType.equals("online")){
            String appCode= model.getAppCode();
            String payMerchantNo = model.getPayMerchantNo();
            if(StringUtils.isEmpty(appCode)){
            	return resultModel.fail(ErrorCode.input_error, "缺少交易通道编码AppCode");
            }else if(StringUtils.isEmpty(payMerchantNo)){
            	return resultModel.fail(ErrorCode.input_error, "缺少交易商户号");
            }
           
            PaymentMerchantEntity paymentMerchantEntity=paymentMerchantDao.findByAppCode(appCode, payMerchantNo);
            if(paymentMerchantEntity==null){
            	return resultModel.fail(ErrorCode.input_error, "交易通道编码AppCode或者交易商户号有误");
            }else{
            	String appId = paymentMerchantEntity.getAppId();//appId 
            	if(!StringUtils.isEmpty(appId)){
            		merchantMap.put("appId", appId);
            	}
            	String subAppId = paymentMerchantEntity.getSubAppId();
            	if(!StringUtils.isEmpty(subAppId)){
            		merchantMap.put("subAppId", subAppId);
            	}
            	String appSecret = paymentMerchantEntity.getAppSecret();//appid密钥
            	if(!StringUtils.isEmpty(appSecret)){
            		merchantMap.put("appSecret", appSecret);
            	}
            	String subAppSecret = paymentMerchantEntity.getSubAppSecret();//subappid密钥
            	if(!StringUtils.isEmpty(subAppSecret)){
            		merchantMap.put("subAppSecret", subAppSecret);
            	}
            	String merchantNo = paymentMerchantEntity.getPayMerchantNo();//交易商户号
            	if(!StringUtils.isEmpty(merchantNo)){
            		merchantMap.put("payMerchantNo",merchantNo);
            	}
            	String payMerchantSubNo = paymentMerchantEntity.getPayMerchantSubNo();//交易子商户号/代理商编号
            	if(!StringUtils.isEmpty(payMerchantSubNo)){
            		merchantMap.put("payMerchantSubNo", payMerchantSubNo);
            	}
            	String payMerchantKey = paymentMerchantEntity.getPayMerchantKey();//交易密钥
            	if(!StringUtils.isEmpty(payMerchantKey)){
            		merchantMap.put("payMerchantKey", payMerchantKey);
            	}
            }
        }

        
        resultModel.setBatchSeqId(model.getBatchSeqId());
        //转换为数据库模型
        BatchPaymentEntity batchPaymentEntity = convertToPayment(context, model,merchantMap);
        //查找实现组件
        List<PaymentEntity> detailPaymentEntities = batchPaymentEntity.getPaymentEntitys();
        Set<String> seqIdSet = new HashSet<String>();
        for (PaymentEntity paymentEntity : detailPaymentEntities) {
            if (seqIdSet != null) {
                if (seqIdSet.contains(paymentEntity.getSeqId()))
                    return resultModel.fail(ErrorCode.input_error, "批次内明细流水重复");
                else seqIdSet.add(paymentEntity.getSeqId());
            }
        }
        seqIdSet = null;
        //用于查看哪些是找不到实现的
        PaymentEntity[] _detailPaymentEntities = Arrays.copyOf(detailPaymentEntities.toArray(new PaymentEntity[0]), detailPaymentEntities.size());
        //查找匹配的支付组件
        List<BankBatchPayment> batchBatchPaymentList = packBatches(payImpls, _detailPaymentEntities);
        for (int i = 0; i < _detailPaymentEntities.length; i++) {//检查是否有找不到银行实现的
            if (_detailPaymentEntities[i] != null) PaymentStatus.setStatus(_detailPaymentEntities[i], PaymentStatus.STATUS_FAIL, "暂不支持的支付业务： 收款账号=" + _detailPaymentEntities[i].getCustomerAccNo());
        }
        _detailPaymentEntities = null;//gc
        IPayment paymentImpl;
        Date now = new Date();
        AtomicInteger batchNum = new AtomicInteger(0);//真正需要提交银行的批次总数
        List<PaymentEntity> realBankBatchPaymentEntitys;
        for (BankBatchPayment bankBatchPayment : batchBatchPaymentList) {
            paymentImpl = bankBatchPayment.getPaymentImpl();
            //民生 光大 这些垃圾银行没有查询接口
            //			if(paymentImpl.getQueryClass() == null){
            //				logger.warn(bankBatchPayment.getPaymentImpl().getClass().getName()+".getQueryClass()没有返回有效的值，将无法查询交易结果.");
            //				return resultModel.fail(ErrorCode.sys_not_support, "缺少状态查询实现.");
            //			}
            List<PaymentEntity> bankDetailList = bankBatchPayment.getBankBatchPaymentEntitys();
            bankBatchPayment.setBankBatchPaymentEntitys(null);//下面会过滤后再设置进来
            PaymentEntity[] bankDetailArray = bankDetailList.toArray(new PaymentEntity[0]);
            //各业务需要的参数检查
            String checkMsg = bankBatchPayment.getPaymentImpl().check(bankDetailArray);//返回总错误不为空，直接全部失败.
            if (checkMsg != null) {
                PaymentStatus.setStatus(bankDetailArray, PaymentStatus.STATUS_FAIL, checkMsg);
                continue;
                //				return resultModel.fail(ErrorCode.input_error, checkMsg);
            }
            realBankBatchPaymentEntitys = new ArrayList<PaymentEntity>();
            for (PaymentEntity paymentEntity : bankDetailArray) {//查检每一批次的输入
                if (paymentEntity.getStatus() == PaymentStatus.STATUS_INIT) {//check的时候，有可能已经失败.
                    paymentEntity.setPayTransCode(paymentImpl.getBankTransCode());//支付交易组件实现
                    paymentEntity.setCreateTime(now);
                    paymentEntity.setUpdateTime(now);
                    paymentEntity.setStatus(PaymentStatus.STATUS_INIT);//初始交易状态
                    paymentEntity.setBatchSeqId(batchPaymentEntity.getBatchSeqId());
                    realBankBatchPaymentEntitys.add(paymentEntity);
                }
            }
            bankDetailArray = realBankBatchPaymentEntitys.toArray(new PaymentEntity[0]);//过滤后银行批次
            bankBatchPayment.setBankBatchPaymentEntitys(realBankBatchPaymentEntitys);
            //生成银行流水
            paymentImpl.genBankBatchSeqId(bankDetailArray);
            paymentImpl.genBankSeqId(bankDetailArray);
            batchNum.incrementAndGet();
        }
        //检查通过，保存支付信息
        paymentService.saveBatchPayment(batchPaymentEntity);
        Map<String, Object> params;
        if (detailPaymentEntities.size() == 1) {//只有一个时，保存在内存
            if (detailPaymentEntities.get(0).getStatus() == PaymentStatus.STATUS_INIT) {
                params = new HashMap<String, Object>();
                batchNum.set(1);
                params.put(PARAM_PAYMENT, detailPaymentEntities.toArray(new PaymentEntity[0]));
                params.put(BATCH_NUM, batchNum);
                return TaskScheduler.scheduleTask(TransCode.PAY, context.getMainAccount(), context.getBankProfile().getBankName(), params, null, model.getPriority(), false);//单笔同步
            } else {
                PaymentOuterStatus.transmitStatusToResultModel(detailPaymentEntities.toArray(new PaymentEntity[0]), resultModel);
                return resultModel;
            }
        } else {
            for (BankBatchPayment bankBatchPayment : batchBatchPaymentList) {
                params = new HashMap<String, Object>();
                List<Long> ids = new LinkedList<Long>();
                //把分批后的ID取出来，放到任务调度中，待执行
                if (bankBatchPayment.getBankBatchPaymentEntitys() != null) //有可能这批拒绝了
                    for (PaymentEntity paymentEntity : bankBatchPayment.getBankBatchPaymentEntitys()) {
                        if (paymentEntity.getStatus() == PaymentStatus.STATUS_INIT) ids.add(paymentEntity.getId());
                    }

                if (ids.size() > 0) {
                    params.put(PARAM_PAYMENT_IDS, ids.toArray(new Long[0]));
                    params.put(BATCH_NUM, batchNum);
                    TaskScheduler.scheduleTask(TransCode.PAY, context.getMainAccount(), context.getBankProfile().getBankName(), params, null, model.getPriority(), true);//批量支付使用异步
                }
            }
            resultModel.setStatus(CommonResultModel.STATUS_SUCCESS);
            resultModel.setErrorMsg("提交成功，等候处理");
            return resultModel;
        }
    }

    @Override
    public CommonResultModel run(TaskParam taskParam) {
        AccountEntity accountEntity = taskParam.getAccountEntity();
        TransContext context = TransContext.getContext();
        PaymentEntity[] paymentEntitys = (PaymentEntity[]) taskParam.getParams().get(PARAM_PAYMENT);
        if (paymentEntitys == null) {
            Long[] ids = (Long[]) taskParam.getParams().get(PARAM_PAYMENT_IDS);
            paymentEntitys = paymentService.getPaymentEntitys(ids);//再读一次对象，队列中只保存ID
        }
        AtomicInteger batchNum = (AtomicInteger) taskParam.getParams().get(BATCH_NUM);
        PaymentResultModel resultModel = new PaymentResultModel();
        resultModel.setAppCode(accountEntity.getAccNo());
        resultModel.setBatchSeqId(paymentEntitys[0].getBatchSeqId());
        IPayment paymentImpl = BankTransComponentManager.getPaymentByTransCode(context.getBankProfile().getBankName(), paymentEntitys[0].getPayTransCode());

        //执行支付
        String unexpectedException = doPay(paymentEntitys, paymentImpl);
        if (unexpectedException == null) {
            PaymentOuterStatus.transmitStatusToResultModel(paymentEntitys, resultModel);
        } else {//有意外异常
            PaymentOuterStatus.transmitStatusToResultModel(paymentEntitys, resultModel, ErrorCode.sys_internal_err, unexpectedException);
        }
        if (batchNum != null && batchNum.decrementAndGet() <= 0) //FIXME : 如果是在应用重启的情况下，batchNum是空的
            compositeListener.onBatchPaymentSubmitComplete(accountEntity, paymentEntitys[0].getBatchSeqId(), paymentEntitys);
        return resultModel;
    }

    /**
     * 支付流程
     * @param paymentEntitysArray
     * @param paymentImpl
     * @return : 支付意外时返回非空 状态信息
     */
    private String doPay(PaymentEntity[] _paymentEntitysArray, IPayment paymentImpl) {
        Arrays.sort(_paymentEntitysArray);//对支付进行ID排序，防止出现并发时prepareSubmitting可能出现的死锁(如果有并发出现,当然可能性不大)
        PaymentEntity[] paymentEntitysArray = paymentDao.prepareSubmitting(_paymentEntitysArray);
        if (paymentEntitysArray.length == 0) {//直接返回，状态按当前的状态
            String message = "准备执行支付(批次号为：" + _paymentEntitysArray[0].getBatchSeqId() + ", 银行批次号为：" + _paymentEntitysArray[0].getBankBatchSeqId() + ")时发现状态异常,可能已经全部撤消交易。";
            logger.warn(message);
            return message;
        }
        boolean hasException = false;
        //1.交易处理前处理.一般用于签名等
        if (paymentImpl instanceof IBankTransInterceptor) {
            try {
                ((IBankTransInterceptor) paymentImpl).beforeTrans(paymentEntitysArray);
            } catch (Exception e) {
                hasException = true;
                logger.error("支付前处理异常", e);
                PaymentStatus.processExceptionBeforePay(e, paymentEntitysArray);
            }
        }

        //2.执行交易处理
        if (!hasException) {
            try {
                paymentImpl.pay(paymentEntitysArray);
            } catch (Throwable e) {
                hasException = true;
                logger.error("执行支付异常", e);
                PaymentStatus.processExceptionWhenPay(e, paymentEntitysArray);
            }
        }
        //3.交易处理后处理.支付后操作，这里最好不要修改状态，出错状态也不会处理
        if (!hasException) {
            if (paymentImpl instanceof IBankTransInterceptor) {
                try {
                    ((IBankTransInterceptor) paymentImpl).afterTrans(paymentEntitysArray);//这里最好不要修改paymentEntity
                } catch (Throwable e) {
                    logger.error("交易后处理异常", e);//异常忽略
                }
            }
        }

        Date now = new Date();
        for (PaymentEntity paymentEntity : paymentEntitysArray) {
            paymentEntity.setSubmitPayTime(now);
            paymentEntity.setUpdateTime(now);
            //设置支付时银行返回状态信息
            paymentEntity.setPayBankStatus(paymentEntity.getBankStatus());
            paymentEntity.setPayBankStatusMsg(paymentEntity.getBankStatusMsg());
        }
        //支付完成更新数据库. 如果这时更新数据库失败，该笔支付就是中间状态“正在提交”.
        try {
            paymentService.updateStatus(paymentEntitysArray, false);
        } catch (Throwable e) {
            String message = "支付后更新数据库失败（需要手工处理状态）";
            logger.error(message, e);
            log(paymentEntitysArray);
            return message;
        }
        return null;
    }

    private void log(PaymentEntity[] paymentEntities) {
        for (PaymentEntity paymentEntity : paymentEntities)
            try {
                logger.info("更新失败的对象值: " + paymentEntity.toString());
            } catch (Exception e) {

            }
    }

    /**
     * 将批次找到对应实现，并分到批定银行批次
     * @param bankTrans
     * @param paymentEntitys
     * @return
     */
    private List<BankBatchPayment> packBatches(IBankTrans bankTrans[], PaymentEntity[] paymentEntitys) {
        IPayment paymentImpl;
        List<BankBatchPayment> bankBatchList = new LinkedList<BankBatchPayment>();
        BankBatchPayment bankBatchPayment = null;
        for (IBankTrans trans : bankTrans) {
            paymentImpl = ((IPayment) trans);
            PaymentNavigation paymentNavigation = paymentImpl.navigate();
            int batchSize = paymentNavigation.getBatchSize();
            for (int i = 0; i < paymentEntitys.length; i++) {
                PaymentEntity paymentEntity = paymentEntitys[i];
                if (paymentEntitys[i] == null) //已经找到的,或者输入不合法的
                    continue;
                //先检测主要参数,对于非法输入，直接失败
                String checkMsg = checkRequireParam(paymentEntity);
                if (checkMsg != null) {
                    PaymentStatus.setStatus(paymentEntity, PaymentStatus.STATUS_FAIL, checkMsg);
                    paymentEntitys[i] = null;
                } else if (paymentNavigation.match(paymentEntity) && paymentImpl.navigateMatch(paymentEntity)) {
                    if (bankBatchPayment == null) {
                        bankBatchPayment = new BankBatchPayment(paymentImpl);
                    }
                    //分批
                    if (bankBatchPayment.size() >= batchSize) {
                        bankBatchList.add(bankBatchPayment);
                        bankBatchPayment = new BankBatchPayment(paymentImpl);//新的一批
                    }
                    bankBatchPayment.addBatch(paymentEntity);
                    paymentEntitys[i] = null;//找到，清除对应位置
                }
            }

            if (bankBatchPayment != null) bankBatchList.add(bankBatchPayment);

            bankBatchPayment = null;
        }

        return bankBatchList;
    }

    private String checkRequireParam(PaymentEntity paymentEntity) {
    	if (StringUtils.isBlank(paymentEntity.getPayType())) return "交易类型为空";
        if (StringUtils.isBlank(paymentEntity.getSeqId())) return "明细流水号为空";
        if (StringUtils.isBlank(paymentEntity.getAccNo())) return "主账号为空";
        if (paymentEntity.getAmount() == null) return "请填写交易金额";
        if (false == paymentEntity.getAmount().compareTo(ZERO) > 0) return "交易金额必须大于0";
        //if (StringUtils.isBlank(paymentEntity.getCustomerBankName()) && StringUtils.isBlank(paymentEntity.getCustomerBankFullName())) return "客户方银行为空";
        //if (paymentEntity.getCustomerCnaps() != null && !paymentEntity.getCustomerCnaps().matches("\\d{12}")) return "客户方联行号格式非法(要求12位数字)";
        
        if(PaymentNavigation.Type.online.name().equals(paymentEntity.getPayType())) {
        	
        }
        else {
        	if (StringUtils.isBlank(paymentEntity.getCustomerAccNo())) return "客户账号为空";
            if (StringUtils.isBlank(paymentEntity.getCustomerAccName())) return "客户账户名为空";
            if (StringUtils.isBlank(paymentEntity.getCustomerBankFullName())) return "客户方银行为空";
        }
        
        return null;
    }

    private final static BigDecimal ZERO = new BigDecimal("0.0");

    private BatchPaymentEntity convertToPayment(TransContext context, PaymentRequestModel model,Map<String,Object> merchantMap) {
        Date now = new Date();
        Date transDate = model.getTransDate();
        if (transDate == null) {
            if (model.getTransDay() >= 0)
                transDate = DateTimeUtil.addDay(now, model.getTransDay());
            else transDate = now;
        }
        PaymentCustomerInfo customerInfos[] = model.getData();
        BatchPaymentEntity batchEntity = new BatchPaymentEntity();
        batchEntity.setBatchSeqId(model.getBatchSeqId());
        batchEntity.setAccNo(model.getAppCode());
        batchEntity.setCreateTime(now);
        batchEntity.setType(model.getType());
        batchEntity.setBatchCount(customerInfos.length);
        batchEntity.setBankName(context.getBankProfile().getBankName());
        batchEntity.setTransDate(transDate);
        batchEntity.setRequestSystem(model.getRequestSystem());
        batchEntity.setPriority(model.getPriority());
        AccountEntity mainAccount = context.getMainAccount();
        BigDecimal batchAmount = new BigDecimal("0.00");

        List<PaymentEntity> detailPaymentList = new ArrayList<PaymentEntity>(customerInfos.length);

        for (PaymentCustomerInfo customerInfo : customerInfos) {
            PaymentEntity entity = new PaymentEntity();
            entity.setBatchSeqId(model.getBatchSeqId());//批次流水号
            //			entity.setSettleId();// 清算号
            entity.setSeqId(customerInfo.getSeqId());//		流水号
            entity.setAccNo(batchEntity.getAccNo());//		交易主账号
            entity.setAccName(mainAccount.getAccName());//交易主账户名
            entity.setBankName(mainAccount.getBankName());//		银行简码
            entity.setPayType(batchEntity.getType());//		业务类别

            entity.setTransDate(transDate);//		预约日期
            entity.setAmount(customerInfo.getAmount());//付款金额
            entity.setCustomerAccNo(StringUtils.trim(customerInfo.getAccNo()));//		客户账号
            entity.setCustomerAccName(StringUtils.trim(customerInfo.getAccName()));//客户账户名
            entity.setCustomerAccType(customerInfo.getAccType());//	int	账户类型 1-表示对公 2-表示对私 3-表示对私存折
            entity.setCustomerCardType(customerInfo.getCardType());//客户卡类型 0-借记卡（默认） 1-存折    2-贷记卡（信用卡）3-公司账号
            entity.setCustomerBankName(customerInfo.getBankName());//		客户银行简称
            entity.setCustomerBankFullName(customerInfo.getBankFullName());// N 客户银行全称
            entity.setCustomerBankBrachName(customerInfo.getBankBranchName());//N 客户银行开户行全称. 如招商银行深圳高新园支行
            //招行提交不了的Bug修改： 联行号非法，不是12位会抛异常.
            String areaCode = null;
            try {
                areaCode = customerInfo.getAreaCode();
            } catch (Exception e) {}
            entity.setCustomerAreaCode(areaCode);//收款方开户地区
            entity.setCustomerCnaps(customerInfo.getCnaps());//	客户账号CNAP号
            entity.setCustomerCnapsBankno(customerInfo.getCnapsBankNo());//网银支付行号
            if (!StringUtils.isBlank(mainAccount.getBankName()) && !StringUtils.isBlank(customerInfo.getBankName())) {
                if (mainAccount.getBankName().equalsIgnoreCase(customerInfo.getBankName()))
                    entity.setSameBank(PaymentEntity.SAME_BANK_YES);
                else entity.setSameBank(PaymentEntity.SAME_BANK_NO);
            }
            entity.setLocalFlag(customerInfo.getLocalFlag());
            entity.setCurrency(model.getCurrency());// Constant.CURRENCY_CNY);//	char(5)	交易币别
            entity.setUseCode(customerInfo.getUseCode());//	N	用途代码
            entity.setUseDesc(customerInfo.getUseDesc());//	N	用途描述
            entity.setRemark(customerInfo.getRemark());//	N	备注
            entity.setAppCode(model.getAppCode());//应用编号
            entity.setAppType(model.getAppType());//应用类型
            entity.setPayMerchantNo(model.getPayMerchantNo());//交易通道号
           
            Map<String,Object> extPropertiesMap=customerInfo.getExtProperties();
            if(extPropertiesMap!=null){
            	String extProperties =JsonUtil.toJson(extPropertiesMap);
                entity.setExtProperties(extProperties);//扩展属性
            }

            batchAmount = batchAmount.add(entity.getAmount());
            if(merchantMap!=null){
            	String merchantExtProperties=JsonUtil.toJson(merchantMap);
            	entity.setMerchantMap(merchantMap);
            	entity.setMerchantExtProperties(merchantExtProperties);
            }
            detailPaymentList.add(entity);
        }
        batchEntity.setBatchAmount(batchAmount);
        batchEntity.setPaymentEntitys(detailPaymentList);
        return batchEntity;
    }

}
