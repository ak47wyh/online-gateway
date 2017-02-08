package com.iboxpay.settlement.gateway.common.trans.verify;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.dao.AccountVerifyDao;
import com.iboxpay.settlement.gateway.common.domain.AccountVerifyEntity;
import com.iboxpay.settlement.gateway.common.inout.CommonRequestModel;
import com.iboxpay.settlement.gateway.common.inout.CommonResultModel;
import com.iboxpay.settlement.gateway.common.inout.verify.VerifyAccountInfo;
import com.iboxpay.settlement.gateway.common.inout.verify.VerifyAccountRequestModel;
import com.iboxpay.settlement.gateway.common.inout.verify.VerifyAccountResultModel;
import com.iboxpay.settlement.gateway.common.inout.verify.VerifyOuterStatus;
import com.iboxpay.settlement.gateway.common.task.RunnableTask;
import com.iboxpay.settlement.gateway.common.task.TaskParam;
import com.iboxpay.settlement.gateway.common.task.TaskScheduler;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;
import com.iboxpay.settlement.gateway.common.trans.ErrorCode;
import com.iboxpay.settlement.gateway.common.trans.ITransDelegate;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.VerifyStatus;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

@Service
public class AccountVerifyDelegateServer implements ITransDelegate, RunnableTask {

	private final Logger logger = LoggerFactory.getLogger(AccountVerifyDelegateServer.class);
	public final static String BATCH_NUM = "batchNum";
	public final static String PARAM_VERIFY = "verifyParam";
	public final static String PARAM_MODEL = "verifyModel";
	public final static String PARAM_ISVERIFY = "isVerify";
	final static String PARAM_QPAYMENT_IDS = "qpids";

	@Resource
	private AccountVerifyDao accountVerifyDao;

	@Override
	public TransCode getTransCode() {
		return TransCode.VERIFY;
	}

	@Override
	public CommonRequestModel parseInput(String input) throws Exception {
		VerifyAccountRequestModel requestModel = (VerifyAccountRequestModel) JsonUtil.jsonToObject(input, "UTF-8", VerifyAccountRequestModel.class);
		return requestModel;
	}

	@Override
	public CommonResultModel trans(TransContext context, CommonRequestModel _model) {
		VerifyAccountRequestModel model = (VerifyAccountRequestModel) _model;
		context.setTransCode(TransCode.VERIFY);
		VerifyAccountResultModel resultModel = new VerifyAccountResultModel();
		resultModel.setAppCode(model.getAppCode());

		IAccountVerify accountVerifyImpl = BankTransComponentManager.getAccountVerifyByTransCode(context.getBankProfile().getBankName(), context.getTransCode().getCode());
		if (accountVerifyImpl == null) {
			return resultModel.setAppCode(model.getAppCode()).fail(ErrorCode.sys_not_support, "[配置错误]找不到交易实现类");
		}
		if (StringUtils.isBlank(model.getBatchSeqId())) {
			return resultModel.fail(ErrorCode.input_error, "缺少批次流水号.");
		}
		if (model.getData() == null || model.getData().length == 0) {
			return resultModel.fail(ErrorCode.input_error, "支付笔数为0");
		}

		if (model.getData().length > 1) {
			return resultModel.fail(ErrorCode.input_error, "验证笔数大于1");
		}

		if (StringUtils.isBlank(model.getData()[0].getAccNo())) {
			return resultModel.fail(ErrorCode.input_error, "银行卡号为空");
		}

		String sysName = model.getAppCode();
		String accNo = model.getData()[0].getAccNo();
		String accName = model.getData()[0].getAccName();
		String certNo = model.getData()[0].getCertNo();
		String mobileNo = model.getData()[0].getMobileNo();

		// 判断账号卡号信息是否存在
		AccountVerifyEntity verifyEntity = accountVerifyDao.getAccountVerifyEntity(sysName, accNo, accName, certNo, mobileNo);
		//		if (accountResut != null && PaymentStatus.STATUS_SUCCESS == accountResut.getStatus()) {
		//			// 判断验证要素参数是否变化，如果变化更新数据
		//			updateAccountVerify(model, accountResut);
		//		} else {
		//			// 保存验证账号信息
		//			accountResut = saveAccountVerfiy(context,model);
		//		}

		if (verifyEntity != null && VerifyStatus.STATUS_SUCCESS == verifyEntity.getStatus()) {
			// 组装返回参数
			resultModel.setAppCode(model.getAppCode());
			resultModel.setBatchSeqId(model.getBatchSeqId());
			// 返回现有的结果，新的在后台查询
			VerifyOuterStatus.transmitStatusToResultModel(verifyEntity, resultModel, model);
			return resultModel;
		}

		if (verifyEntity == null) {
			// 保存验证账号信息
			verifyEntity = saveAccountVerfiy(context, model);
		}

		AtomicInteger batchNum = new AtomicInteger(0);//需要同步的批次总数
		Map<String, Object> params;
		if (verifyEntity != null) {//原来也只有一笔
			params = new HashMap<String, Object>();
			batchNum.set(1);
			params.put(PARAM_VERIFY, verifyEntity);
			params.put(PARAM_MODEL, model);
			params.put(BATCH_NUM, batchNum);
			return TaskScheduler.scheduleTask(TransCode.VERIFY, context.getMainAccount(), context.getBankProfile().getBankName(), params, false);//同步返回
		}

		return resultModel;
	}

	@Override
	public CommonResultModel run(TaskParam taskParam) {
		// 获取传递参数
		VerifyAccountResultModel resultModel = new VerifyAccountResultModel();
		TransContext context = TransContext.getContext();
		AccountVerifyEntity account = (AccountVerifyEntity) taskParam.getParams().get(PARAM_VERIFY);
		VerifyAccountRequestModel requestModel = (VerifyAccountRequestModel) taskParam.getParams().get(PARAM_MODEL);

		// 调用验证组件实现
		doVerify(context, account);

		// 组装返回参数
		resultModel.setAppCode(requestModel.getAppCode());
		resultModel.setBatchSeqId(requestModel.getBatchSeqId());
		// 返回现有的结果，新的在后台查询
		VerifyOuterStatus.transmitStatusToResultModel(account, resultModel, requestModel);
		return resultModel;
	}

	/**
	 * 调用账号验证实现
	 * @param context
	 * @param account
	 */
	public void doVerify(TransContext context, AccountVerifyEntity account) {
		IAccountVerify accountVerifyImpl = BankTransComponentManager.getAccountVerifyByTransCode(context.getBankProfile().getBankName(), context.getTransCode().getCode());
		try {
			accountVerifyImpl.verfiy(account);
		} catch (Throwable e) {
			logger.error("执行验证异常", e);
            VerifyStatus.processExceptionWhenPay(e, account);
		}

		accountVerifyDao.updateStatus(account);// 更新验证状态
	}

	/**
	 * 转换实体，保存
	 * @param model
	 * @return
	 */
	private AccountVerifyEntity saveAccountVerfiy(TransContext context, VerifyAccountRequestModel model) {
		AccountVerifyEntity account = new AccountVerifyEntity();
		VerifyAccountInfo result = model.getData()[0];
		account.setSysName(model.getAppCode());
		account.setCustomerAccNo(result.getAccNo());
		account.setCustomerAccName(result.getAccName());
		account.setCustomerAccType(result.getAccType());
		account.setCustomerCardType(result.getCardType());
		account.setCertNo(result.getCertNo());
		account.setMobileNo(result.getMobileNo());
		account.setCreateTime(new Date());
		account.setBatchSeqId(model.getBatchSeqId());
		account.setSeqId(result.getSeqId());
		account.setPayTransCode(TransCode.VERIFY.getCode());
		account.setStatus(VerifyStatus.STATUS_WAITTING_VERIFY);
		accountVerifyDao.save(account);

		return account;
	}

	/**
	 * 判断验证要素是否存在变化：来源，卡号，持卡人姓名，身份证号码，电话号码
	 * @param model
	 * @param accountResut
	 */
	private void updateAccountVerify(VerifyAccountRequestModel model, AccountVerifyEntity accountResut) {
		// 判断输入卡号信息是否有误
		VerifyAccountInfo[] results = model.getData();
		VerifyAccountInfo verifyResult = results[0];
		String sysName = model.getAppCode().trim();
		String accNo = verifyResult.getAccNo().trim();
		String accName = verifyResult.getAccName().trim();
		String certNo = verifyResult.getCertNo().trim();
		String mobileNo = verifyResult.getMobileNo().trim();

		if (!sysName.equals(accountResut.getSysName()) || !accNo.equals(accountResut.getCustomerAccNo()) || !accName.equals(accountResut.getCustomerAccName())
				|| !certNo.equals(accountResut.getCertNo()) || !mobileNo.equals(accountResut.getMobileNo())) {

			accountResut.setSysName(sysName);
			accountResut.setCustomerAccNo(accNo);
			accountResut.setCustomerAccName(accName);
			accountResut.setCertNo(certNo);
			accountResut.setMobileNo(mobileNo);
			//accountVerifyDao.update(accountResut);
		}
	}

}
