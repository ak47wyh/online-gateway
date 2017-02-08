package com.iboxpay.settlement.gateway.common.inout.verify;

import javax.annotation.Resource;

import com.iboxpay.settlement.gateway.common.dao.AccountVerifyDao;
import com.iboxpay.settlement.gateway.common.domain.AccountVerifyEntity;

public class VerifyAccountOuterStatus {
	
	@Resource
	private AccountVerifyDao accountVerifyDao;

	public static void updateStatus(AccountVerifyEntity account) {
		
		
	}
	
	public static void updateStatus(){
		
	}

}
