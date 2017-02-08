package com.iboxpay.settlement.gateway.wechat.service.sign.utils;

import java.io.Serializable;
import java.util.Date;

public class JsTicket implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private String ticket;
	private int expiresIn;
	private Date createTime;
	
	public String getTicket() {
		return ticket;
	}
	public void setTicket(String ticket) {
		this.ticket = ticket;
	}
	public int getExpiresIn() {
		return expiresIn;
	}
	public void setExpiresIn(int expiresIn) {
		this.expiresIn = expiresIn;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	
}
