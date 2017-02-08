package com.iboxpay.settlement.gateway.kq.service.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.bill99.schema.ddp.product.MerchantDebitPkiRequest;
import com.bill99.schema.ddp.product.MerchantDebitPkiResponse;
import com.bill99.schema.ddp.product.MerchantDebitQueryRequest;
import com.bill99.schema.ddp.product.MerchantDebitQueryResponse;
import com.bill99.schema.ddp.product.MerchantDebitRequest;
import com.bill99.schema.ddp.product.MerchantDebitRequestItem;
import com.bill99.schema.ddp.product.MerchantDebitResponse;
import com.bill99.schema.ddp.product.MerchantDebitSingleQueryRequest;
import com.bill99.schema.ddp.product.MerchantDebitSingleQueryResponse;
import com.bill99.schema.ddp.product.MerchantSingleDebitRequest;
import com.bill99.schema.ddp.product.MerchantSingleDebitResponse;
import com.iboxpay.settlement.gateway.common.util.StringUtils;
import com.iboxpay.settlement.gateway.kq.entity.DealInfoEntity;
import com.iboxpay.settlement.gateway.kq.entity.OrderInfoEntity;

/**
 * 批量付款工具类
 * */
public class CustomerUtil {
	
	private static final String ENCODING = "utf-8";
	private static Logger logger = LoggerFactory.getLogger(CustomerUtil.class);
	
	
	
	/**
	 * ===============================================================================================
	 * Request to Xml
	 * ===============================================================================================
	 */
	
	/**
	 * MerchantSingleDebitRequest转换为xml格式 
	 * @param request 付款请求（原文）
	 * @return String xml 字符串
	 */
	public static String merchantSingleDebitRequestToXml(MerchantSingleDebitRequest request) {
		String result = "";
		try {
			IBindingFactory bfact = BindingDirectory.getFactory(MerchantSingleDebitRequest.class);
			IMarshallingContext mctx = bfact.createMarshallingContext();
			mctx.setIndent(2);
			StringWriter sw = new StringWriter();
			mctx.setOutput(sw);
			mctx.marshalDocument(request);
			result = sw.toString();
			return result;
		} catch (JiBXException e) {
			logger.error("merchantSingleDebitRequestToXml转换xml异常："+e);
		}
		return null;
	}
	
	
	/**
	 * merchantDebitQueryRequestToXml转换为xml格式 
	 * @param request 付款请求（原文）
	 * @return String xml 字符串
	 */
	public static String merchantDebitQueryRequestToXml(MerchantDebitSingleQueryRequest request) {
		String result = "";
		try {
			IBindingFactory bfact = BindingDirectory.getFactory(MerchantDebitQueryRequest.class);
			IMarshallingContext mctx = bfact.createMarshallingContext();
			mctx.setIndent(2);
			StringWriter sw = new StringWriter();
			mctx.setOutput(sw);
			mctx.marshalDocument(request);
			result = sw.toString();
			return result;
		} catch (JiBXException e) {
			logger.error("merchantDebitQueryRequestToXml转换xml异常："+e);
		}
		return null;
	}
	
	
	/**
	 * merchantDebitRequestToXml转换为xml格式 
	 * @param request 付款请求（原文）
	 * @return String xml 字符串
	 */
	public static String merchantDebitRequestToXml(MerchantDebitRequest request) {
		String result = "";
		try {
			IBindingFactory bfact = BindingDirectory.getFactory(MerchantDebitRequest.class);
			IMarshallingContext mctx = bfact.createMarshallingContext();
			mctx.setIndent(2);
			StringWriter sw = new StringWriter();
			mctx.setOutput(sw);
			mctx.marshalDocument(request);  
			result = sw.toString();
			return result;
		} catch (JiBXException e) {
			logger.error("merchantDebitRequestToXml转换xml异常："+e);
		}
		return null;
	}
	
	/**
	 * merchantDebitQueryRequestToXml转换为xml格式 
	 * @param request 付款请求（原文）
	 * @return String xml 字符串
	 */
	public static String merchantDebitQueryRequestToXml(MerchantDebitQueryRequest request) {
		String result = "";
		try {
			IBindingFactory bfact = BindingDirectory.getFactory(MerchantDebitQueryRequest.class);
			IMarshallingContext mctx = bfact.createMarshallingContext();
			mctx.setIndent(2);
			StringWriter sw = new StringWriter();
			mctx.setOutput(sw);
			mctx.marshalDocument(request);  
			result = sw.toString();
			return result;
		} catch (JiBXException e) {
			logger.error("merchantDebitQueryRequestToXml转换xml异常："+e);
		}
		return null;
	}
	
	

	/**
	 * merchantDebitPkiRequestToXml转换为xml格式 
	 * @param request 付款请求（密文）
	 * @return String xml 字符串
	 */
	public static String merchantDebitPkiRequestToXml(MerchantDebitPkiRequest request) {
		try {
			IBindingFactory bfact = BindingDirectory.getFactory(MerchantDebitPkiRequest.class);
			IMarshallingContext mctx = bfact.createMarshallingContext();
			mctx.setIndent(2);
			StringWriter sw = new StringWriter();
			mctx.setOutput(sw);
			mctx.marshalDocument(request);
			return sw.toString();
		} catch (JiBXException e) {
			logger.error("merchantDebitPkiRequestToXml转换xml异常："+e);
			return null;
		}
	}
	
	/**
	 * ===============================================================================================
	 * xml to Response
	 * ===============================================================================================
	 */
	
	/**
	 * 把xml转换为MerchantDebitPkiResponse 
	 * @param responseXml 返回应答信息xml字符串
	 * @return SettlementPkiApiResponse 返回应答
	 */
	public static MerchantDebitPkiResponse xmlToMerchantDebitPkiResponse(String responseXml) {
		logger.info("返回明文详情："+responseXml);
		InputStream input =null;
		try {
			input = new ByteArrayInputStream(responseXml.getBytes(ENCODING));
			IBindingFactory bfact = BindingDirectory.getFactory(MerchantDebitPkiResponse.class);
			IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
			MerchantDebitPkiResponse response = (MerchantDebitPkiResponse) uctx.unmarshalDocument(input, null);
			return response;
		} catch (JiBXException e) {
			logger.error("xml转换为xmlToMerchantDebitPkiResponse异常："+e);
		} catch (UnsupportedEncodingException e) {
			logger.error("xml转换为xmlToMerchantDebitPkiResponse异常："+e);
		}finally{
			if(input!=null){
				try {
					input.close();
				} catch (IOException e) {
					logger.error("关闭流异常："+e);
				}
			}
		}
		return null;
	}
	
	/**
	 * 把xml转换为MerchantSingleDebitResponse，根据批次号查询
	 * @param responseXml 返回应答信息xml字符串
	 * @return BatchidQueryResponse 返回应答
	 */
	public static MerchantSingleDebitResponse xmlToMerchantSingleDebitResponse(String responseXml) {
		logger.info("返回明文详情："+responseXml);
		InputStream input =null;
		try {
			IBindingFactory bfact = BindingDirectory.getFactory(MerchantSingleDebitResponse.class);
			input = new ByteArrayInputStream(responseXml.getBytes(ENCODING));
			IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
			MerchantSingleDebitResponse response = (MerchantSingleDebitResponse) uctx.unmarshalDocument(input, null);
			return response;
		} catch (Exception e) {
			logger.error("xml转换为xmlToMerchantSingleDebitResponse异常："+e);
			return null;
		}finally{
			if(input!=null){
				try {
					input.close();
				} catch (IOException e) {
					logger.error("关闭流异常："+e);
				}
			}
		}
	}
	
	
	/**
	 * 把xml转换为MerchantDebitSingleQueryResponse，根据单笔流水
	 * @param responseXml 返回应答信息xml字符串
	 * @return BatchidQueryResponse 返回应答
	 */
	public static MerchantDebitSingleQueryResponse xmlToMerchantDebitSingleQueryResponse(String responseXml) {
		logger.info("单笔查询明文："+responseXml);
		InputStream input =null;
		try {
			IBindingFactory bfact = BindingDirectory.getFactory(MerchantSingleDebitResponse.class);
			input = new ByteArrayInputStream(responseXml.getBytes(ENCODING));
			IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
			MerchantDebitSingleQueryResponse response = (MerchantDebitSingleQueryResponse) uctx.unmarshalDocument(input, null);
			return response;
		} catch (Exception e) {
			logger.error("xml转换为xmlToMerchantDebitSingleQueryResponse异常："+e);
			return null;
		}finally{
			if(input!=null){
				try {
					input.close();
				} catch (IOException e) {
					logger.error("关闭流异常："+e);
				}
			}
		}
	}
	
	/**
	 * 把xml转换为MerchantDebitSingleQueryResponse，根据单笔流水
	 * @param responseXml 返回应答信息xml字符串
	 * @return BatchidQueryResponse 返回应答
	 */
	public static MerchantDebitResponse xmlToMerchantDebitResponse(String responseXml) {
		logger.info("批量支付返回"+responseXml);
		InputStream input =null;
		try {
			IBindingFactory bfact = BindingDirectory.getFactory(MerchantDebitResponse.class);
			input = new ByteArrayInputStream(responseXml.getBytes(ENCODING));
			IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
			MerchantDebitResponse response = (MerchantDebitResponse) uctx.unmarshalDocument(input, null);
			return response;
		} catch (Exception e) {
			logger.error("xml转换为xmlToMerchantDebitResponse异常："+e);
			return null;
		}finally{
			if(input!=null){
				try {
					input.close();
				} catch (IOException e) {
					logger.error("关闭流异常："+e);
				}
			}
		}
	}
	/**
	 * 把xml转换为MerchantDebitBatchQueryResponse，根据单笔流水
	 * @param responseXml 返回应答信息xml字符串
	 * @return BatchidQueryResponse 返回应答
	 * com.bill99.schema.ddp.product.MerchantDebitBatchQueryResponse cannot be cast to com.bill99.schema.ddp.product.MerchantDebitQueryResponse
	 */
	public static MerchantDebitQueryResponse xmlToMerchantDebitQueryResponse(String responseXml) {
		logger.info("返回明文详情："+responseXml);
		InputStream input =null;
		try {
			IBindingFactory bfact = BindingDirectory.getFactory(MerchantDebitQueryResponse.class);
			input = new ByteArrayInputStream(responseXml.getBytes(ENCODING));
			IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
			MerchantDebitQueryResponse response = (MerchantDebitQueryResponse) uctx.unmarshalDocument(input, null);
			return response;
		} catch (Exception e) {
			logger.error("xml转换为xmlToMerchantDebitQueryResponse异常："+e);
			return null;
		}finally{
			if(input!=null){
				try {
					input.close();
				} catch (IOException e) {
					logger.error("关闭流异常："+e);
				}
			}
		}
	}
	
	
	/**
	 * ===============================================================================================
	 * 将数据组装成request请求中
	 * ===============================================================================================
	 */

	/**
	 * 免签约支付
	 * 把请求信息数据设置到MerchantSingleDebitRequest类中
	 * @param dealInfo 请求信息数据
	 * @return MerchantSingleDebitRequest 请求付款信息类(原文)
	 */
	public static MerchantSingleDebitRequest getMerchantSingleDebitRequest(DealInfoEntity dealInfo) {
		MerchantSingleDebitRequest request = new MerchantSingleDebitRequest();
		request.setInputCharset(dealInfo.getInputCharset()==null?"":dealInfo.getInputCharset());
		request.setMemberCode(dealInfo.getMemberCode()==null?"":dealInfo.getMemberCode());
		request.setMerchantAcctId(dealInfo.getMerchantAcctId()==null?"":dealInfo.getMerchantAcctId());
		request.setContractId(dealInfo.getContractId()==null?"":dealInfo.getContractId());//合同号
		request.setSeqId(dealInfo.getSeqId()==null?"":dealInfo.getSeqId()); // 商家订单号
		request.setReqSeqno(dealInfo.getReqSeqno()==null?"":dealInfo.getReqSeqno());
		request.setAccType(dealInfo.getAccType()==null?"":dealInfo.getAccType()); 
		request.setBankId(dealInfo.getBankId()==null?"":dealInfo.getBankId()); 
		request.setOpenAccDept(dealInfo.getOpenAccDept()==null?"":dealInfo.getOpenAccDept()); 
		request.setBankAcctName(dealInfo.getBankAcctName()==null?"":dealInfo.getBankAcctName()); 
		request.setBankAcctId(dealInfo.getBankAcctId()==null?"":dealInfo.getBankAcctId()); 
		request.setAmount(dealInfo.getAmount()==null?"":dealInfo.getAmount()); 
		request.setIdType(dealInfo.getIdType()==null?"":dealInfo.getIdType()); 
		request.setIdCode(dealInfo.getIdCode()==null?"":dealInfo.getIdCode()); 
		request.setExpiredDate(dealInfo.getExpiredDate()==null?"":dealInfo.getExpiredDate()); 
		request.setUsage(dealInfo.getUsage()==null?"":dealInfo.getUsage()); 
		request.setCurType(dealInfo.getCurType()==null?"":dealInfo.getCurType()); 
		request.setBgUrl(dealInfo.getBgUrl()==null?"":dealInfo.getBgUrl()); 
		request.setRemark(dealInfo.getRemark()==null?"":dealInfo.getRemark()); 
		request.setExt1(dealInfo.getExt1()==null?"":dealInfo.getExt1()); 
		request.setExt2(dealInfo.getExt2()==null?"":dealInfo.getExt2()); 
		return request;
	}
	
	/**
	 * 免签约查询（单笔）
	 * 把请求信息数据设置到MerchantSingleDebitRequest类中
	 * @param dealInfo 请求信息数据
	 * @return MerchantSingleDebitRequest 请求付款信息类(原文)
	 */
	public static MerchantDebitSingleQueryRequest getMerchantDebitSingleQueryRequest(DealInfoEntity dealInfo) {
		MerchantDebitSingleQueryRequest request = new MerchantDebitSingleQueryRequest();
		request.setInputCharset(dealInfo.getInputCharset()==null?"":dealInfo.getInputCharset());
		request.setMemberCode(dealInfo.getMemberCode()==null?"":dealInfo.getMemberCode());
		request.setMerchantAcctId(dealInfo.getMerchantAcctId()==null?"":dealInfo.getMerchantAcctId());
		request.setTranscode(dealInfo.getTranscode()==null?"":dealInfo.getTranscode());
		request.setMerchantSeqNo(dealInfo.getMerchantSeqNo()==null?"":dealInfo.getMerchantSeqNo());
		request.setSeqId(dealInfo.getSeqId()==null?"":dealInfo.getSeqId());			
		return request;
	}
	
	
	/**
	 * 批量代扣支付
	 * 把请求信息数据设置到MerchantDebitRequest类中
	 * @param dealInfo 请求信息数据
	 * @return MerchantDebitRequest 请求付款信息类(原文)
	 */
	public static MerchantDebitRequest getMerchantDebitRequest(DealInfoEntity dealInfo) {
		MerchantDebitRequest request = new MerchantDebitRequest();
	
		List<MerchantDebitRequestItem> items = new ArrayList<MerchantDebitRequestItem>();
		List<OrderInfoEntity> orderItems= dealInfo.getItemList();
		for (OrderInfoEntity orderInfoEntity : orderItems) {
			MerchantDebitRequestItem item = new MerchantDebitRequestItem();
			item.setSeqId(StringUtils.isEmpty(orderInfoEntity.getSeqId())?"":orderInfoEntity.getSeqId());
			item.setUsage(StringUtils.isEmpty(orderInfoEntity.getUsage())?"":orderInfoEntity.getUsage());
			item.setBankId(StringUtils.isEmpty(orderInfoEntity.getBankId())?"":orderInfoEntity.getBankId());//交通银行bankid BCOM
			item.setAccType(StringUtils.isEmpty(orderInfoEntity.getAccType())?"":orderInfoEntity.getAccType());
			item.setOpenAccDept(StringUtils.isEmpty(orderInfoEntity.getOpenAccDept())?"":orderInfoEntity.getOpenAccDept());
			item.setBankAcctName(StringUtils.isEmpty(orderInfoEntity.getBankAcctName())?"":orderInfoEntity.getBankAcctName());
			item.setBankAcctId(StringUtils.isEmpty(orderInfoEntity.getBankAcctId())?"":orderInfoEntity.getBankAcctId());
			item.setAmount(StringUtils.isEmpty(orderInfoEntity.getAmount())?"":orderInfoEntity.getAmount());
			item.setExpiredDate(StringUtils.isEmpty(orderInfoEntity.getExpiredDate())?"":orderInfoEntity.getExpiredDate());
			item.setIdType(StringUtils.isEmpty(orderInfoEntity.getIdType())?"":orderInfoEntity.getIdType());
			item.setIdCode(StringUtils.isEmpty(orderInfoEntity.getIdCode())?"":orderInfoEntity.getIdCode());
			item.setCurType(StringUtils.isEmpty(orderInfoEntity.getCurType())?"":orderInfoEntity.getCurType());
			item.setNote(StringUtils.isEmpty(orderInfoEntity.getNote())?"":orderInfoEntity.getNote());
			item.setRemark(StringUtils.isEmpty(orderInfoEntity.getRemark())?"":orderInfoEntity.getRemark());
			items.add(item);
		}
		// 需要加上明细
		request.setItems(items);
		
		request.setInputCharset(StringUtils.isEmpty(dealInfo.getInputCharset())?"":dealInfo.getInputCharset());
		request.setMemberCode(StringUtils.isEmpty(dealInfo.getMemberCode())?"":dealInfo.getMemberCode());
		request.setMerchantAcctId(StringUtils.isEmpty(dealInfo.getMerchantAcctId())?"":dealInfo.getMerchantAcctId());
		request.setContractId(StringUtils.isEmpty(dealInfo.getContractId())?"":dealInfo.getContractId());
		request.setNumTotal(StringUtils.isEmpty(dealInfo.getNumTotal())?"":dealInfo.getNumTotal());
		request.setAmountTotal(StringUtils.isEmpty(dealInfo.getAmountTotal())?"":dealInfo.getAmountTotal());
		request.setRequestId(StringUtils.isEmpty(dealInfo.getRequestId())?"":dealInfo.getRequestId());
		request.setRequestTime(StringUtils.isEmpty(dealInfo.getRequestTime())?"":dealInfo.getRequestTime());
		request.setBgUrl(StringUtils.isEmpty(dealInfo.getBgUrl())?"":dealInfo.getBgUrl());
		request.setExt1(StringUtils.isEmpty(dealInfo.getExt1())?"":dealInfo.getExt1());
		request.setExt2(StringUtils.isEmpty(dealInfo.getExt2())?"":dealInfo.getExt2());		
		return request;
	}
	
	/**
	 * 批量代扣明细查询结果
	 * 把请求信息数据设置到MerchantDebitBatchQueryRequest类中
	 * @param dealInfo 请求信息数据
	 * @return MerchantDebitRequest 请求付款信息类(原文)
	 */
	public static MerchantDebitQueryRequest getMerchantDebitQueryRequest(DealInfoEntity dealInfo){
		MerchantDebitQueryRequest request = new MerchantDebitQueryRequest();		
		request.setBatchId(StringUtils.isEmpty(dealInfo.getBatchId())?"":dealInfo.getBatchId());
		request.setInputCharset(StringUtils.isEmpty(dealInfo.getInputCharset())?"":dealInfo.getInputCharset());
		request.setMemberCode(StringUtils.isEmpty(dealInfo.getMemberCode())?"":dealInfo.getMemberCode());
		request.setMerchantAcctId(StringUtils.isEmpty(dealInfo.getMerchantAcctId())?"":dealInfo.getMerchantAcctId());
		request.setRequestTime(StringUtils.isEmpty(dealInfo.getRequestTime())?"":dealInfo.getRequestTime());
		request.setSeqId(StringUtils.isEmpty(dealInfo.getSeqId())?"":dealInfo.getSeqId());
		request.setReqSeqno(StringUtils.isEmpty(dealInfo.getReqSeqno())?"":dealInfo.getReqSeqno());
		request.setStarttime(StringUtils.isEmpty(dealInfo.getStartTime())?"":dealInfo.getStartTime());
		request.setEndtime(StringUtils.isEmpty(dealInfo.getEndTime())?"":dealInfo.getEndTime());
		request.setPageNo(StringUtils.isEmpty(dealInfo.getPageNo())?"":dealInfo.getPageNo());
		request.setPageSize(StringUtils.isEmpty(dealInfo.getPageSize())?"":dealInfo.getPageSize());
		request.setExt1(StringUtils.isEmpty(dealInfo.getExt1())?"":dealInfo.getExt1());
		request.setExt2(StringUtils.isEmpty(dealInfo.getExt2())?"":dealInfo.getExt2());		
		return request;
	}
	
	
}
