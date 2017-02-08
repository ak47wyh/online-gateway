package com.iboxpay.settlement.gateway.common.dao;
import java.util.Map;

import com.iboxpay.settlement.gateway.common.domain.PaymentMerchantEntity;
import com.iboxpay.settlement.gateway.common.page.PageBean;

public interface PaymentMerchantDao{
	/**
	 * 根据交易主账号查询交易商户信息
	 * 
	 * @param appCode
	 *            交易主账号
	 * @param payMerchantNo
	 *            子交易商户号
	 * @return 交易商户信息
	 */
	public PaymentMerchantEntity findByAppCode(String appCode,String payMerchantSubNo);
	
	/**
	 * 
	 * @param entity
	 */
	public void save(PaymentMerchantEntity entity);
	
	/**
	 * 
	 * @param entity
	 */
	public void update(PaymentMerchantEntity entity);
	
	/**
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @param params
	 * @return
	 */
	public PageBean findPage(int pageNo, int pageSize, Map<String,Object> paramMap);
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public PaymentMerchantEntity load(Long id);
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public Boolean delete(Long id);
}
