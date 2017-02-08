package com.iboxpay.settlement.gateway.xmcmbc.service.detail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.DetailEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.net.SftpHelper;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.detail.DetailResult;
import com.iboxpay.settlement.gateway.common.trans.detail.IDetail;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.xmcmbc.Constants;
import com.iboxpay.settlement.gateway.xmcmbc.XmcmbcFrontEndConfig;
/**
 * SFTP 下载T+1日对账文件-当做T日交易明细
 * @author weiyuanhua
 * @date 2016年2月2日 上午9:58:38
 * @Version 1.0
 */
@Service
public class Detail_detail implements IDetail{
	
	private static final String DETAIL_TRANS_CODE = "detail";
	private static Logger logger = LoggerFactory.getLogger(Detail_detail.class);
	@Override
	public TransCode getTransCode() {
		return TransCode.DETAIL;
	}
	@Override
	public String getBankTransCode() {
		return DETAIL_TRANS_CODE;
	}
	@Override
	public String getBankTransDesc() {
		return "实时代付对账文件生成";
	}
	@Override
	public boolean isTodayDetailIndependent() {
		return false;
	}
	@Override
	public int supportQueryHisDaysSpan() {
		return 30;//查询间隔就设为1个月
	}
	
	@Override
	public DetailResult queryTodayDetail(AccountEntity accountEntity,
			int pageIndex, Map<String, Object> pageInfoMap)
			throws BaseTransException {
		return null;
	}
	@Override
	public DetailResult queryHisDetail(AccountEntity accountEntity,
			Date beginDate, Date endDate, int pageIndex,
			Map<String, Object> pageInfoMap) throws BaseTransException {
		TransContext context = TransContext.getContext();
		XmcmbcFrontEndConfig config = (XmcmbcFrontEndConfig)context.getFrontEndConfig();
		ByteArrayOutputStream outputStream = null;
		String charset = config.getCharset().getVal();
		//判断对账结果查询时候，选择的日期是否为同一天
		Date startDate = DateTimeUtil.truncateTime(beginDate);
		Date endD = DateTimeUtil.truncateTime(endDate);
		if (startDate.getTime() != endD.getTime()) {//民生银行对账结果明细查询的时候，开始和结束的日期必须是同一天
			throw new PackMessageException("民生银行厦门分行对账结果明细查询的时候，开始和结束的日期必须是同一天");
        }
		try {
			int sftpPort = Integer.parseInt(config.getSftpPort().getVal());
			outputStream = new ByteArrayOutputStream(1024*8);
			//保存路径：对账文件主路径(前置机配置)/yyyyMMdd(使用了)/文件名
			//接收文件名：合作机构id_DZWJ_YYYYMMDD.txt
			StringBuilder fileName = new StringBuilder();
			//开始和结束同一天，和对账文件目录、对账文件日期名称、代付日期相同(对账就以我们报文返回的银行日期为准进行对账就OK)
			String date = DateTimeUtil.format(beginDate, "yyyyMMdd");
			fileName.append(config.getCompanyId().getVal())//合作机构ID
				.append("_").append("DZWJ").append("_")
				.append(date)
				.append(".txt");
			//结果文件下载
			SftpHelper.download(config.getSftpIp().getVal(), 
								sftpPort, 
								config.getSftpUsername().getVal(), 
								config.getSftpPassword().getVal(), 
								config.getSftpDirectory().getVal(),
								date,
								fileName.toString(),
								outputStream);
			
			//缓存结果文件
			String result = outputStream.toString(charset);
			if(result.length()==0){//结果文件为空
				throw new ParseMessageException("返回的结果文件为空");
			}
			
			return genDetailResult(result,accountEntity);
		} catch (UnsupportedEncodingException e) {
			logger.error("不支出的字符集:" + e.getMessage());
			return null;
		} finally {
			if(outputStream!=null){
				try {
					outputStream.close();
				} catch (IOException e) {
					logger.info("关闭流发生异常");
				}
			}
		}
	}
	/**
	 * 生成交易明细记录
	 * @param result		对账文件明细
	 * @param accountEntity 付款账号
	 * @return				解析生成的交易明细
	 */
	private DetailResult genDetailResult(String detailInfo,AccountEntity accountEntity) {
		logger.info("【民生银行厦门分行】返回的对账结果为:===>\n"+detailInfo);
		String[] result = detailInfo.split("\n");
		String accNo = accountEntity.getAccNo();//付款账户
		List<DetailEntity> detailList = new LinkedList<DetailEntity>();	
		BigDecimal zero = new BigDecimal("0"); 
		for (int i = 0; i < result.length-1; i++) {//最后一行为"########"不需要解析了
			//服务码|第三方流水号|银行流水号|收款帐号|收款户名|金额|代付结果|失败返回码|失败原因|代付日期
			String[] fields = result[i].split("\\|");
			DetailEntity detail = new DetailEntity();
			detail.setBankBatchSeqId(fields[1]);
			detail.setAccNo(accNo);//付款账号
			String customerAccNo = fields[3];//收款账号
			detail.setCustomerAccNo(customerAccNo);
			String customerAccName = fields[4];//收款账户名
			detail.setCustomerAccName(customerAccName);
			BigDecimal amount = new BigDecimal(fields[5]);//交易金额
			detail.setDebitAmount(amount.divide(Constants.MULTI_100));
			detail.setCreditAmount(zero);
			Date transDate = null;
			try {
				//String格式必须为yyyy-MM-dd
				String dateStr = fields[fields.length-1];
				StringBuilder date = new StringBuilder();
				date.append(dateStr.substring(0, 4)).append("-")
					.append(dateStr.substring(4, 6)).append("-")
					.append(dateStr.subSequence(6, 8));
				transDate = DateTimeUtil.parseDate(date.toString(), "yyyy-MM-dd");
			} catch (ParseMessageException e) {
				logger.error("交易日期解析异常:"+e.getMessage());
			}
			detail.setTransDate(transDate);//页面显示交易时间，就只能显示为年月日
			String type = fields[6];//代付结果，E:失败，S:成功
			String code = fields[7];//失败返回码
			String desc = fields[8];//失败原因
			StringBuilder remark = new StringBuilder(type);
			remark.append("【");
			if(Constants.RESPONSE_TYPE_ERROR.equals(type)){//E:错误
				remark.append("失败");
					
			} else if(Constants.RESPONSE_TYPE_SUCCESS.equals(type)){//S:成功
				remark.append("成功");
			}
			remark.append("】").append("--").append(code).append("【").append(desc).append("】");
			detail.setRemark(remark.toString());
			detailList.add(detail);
		}
		//一次性解析完
		DetailResult detailResult = new DetailResult(detailList.toArray(new DetailEntity[0]),false);
		return detailResult;
	}
}
