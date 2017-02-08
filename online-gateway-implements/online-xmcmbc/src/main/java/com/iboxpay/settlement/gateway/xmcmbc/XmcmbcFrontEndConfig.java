package com.iboxpay.settlement.gateway.xmcmbc;

import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.config.Property;
import com.iboxpay.settlement.gateway.common.config.Property.Type;

/**
 * 民生银行厦门分行前置机
 * @author weiyuanhua
 * @date 2016年2月2日 上午9:58:38
 * @Version 1.0
 */
public class XmcmbcFrontEndConfig extends FrontEndConfig {

	private static final long serialVersionUID = 1L;
	//本行合作方编号
	private Property tCompanyId;
	//跨行合作方编号
	private Property companyId;
	//商户编号
	private Property mchntCd;
	private Property privateKeyFile;
	private Property publicKeyFile;
	private Property poxy;//是否启动代理
	// 跨行端口
	private Property diffPort;

	//SecretKey-密钥
	private Property secretKey;//MD5签名密钥(32位长度)
	//FTP参数
	private Property sftpIp;//IP
	private Property sftpPort;//port
	private Property sftpUsername;//用户名
	private Property sftpPassword;//密码
	private Property sftpDirectory;//对账文件目录

	private Property uploadSameBank;//同行上传路径
	private Property downloadSameBank;//同行回盘路径
	private Property refundExchange;//退汇路径
	private Property uploadDiffBank;//跨行上传路径
	private Property downloadDiffBank;//跨行回盘路径
	private Property queryTime;//查询时间间隔
	private Property encryptKey;//加解密密钥（16为长度）
	private Property queryInterval;//设置查询时间间隔，单位为分钟

	public XmcmbcFrontEndConfig() {
		setDefVal(protocal, "tcp");
		setDefVal(charset, "UTF-8");
		this.tCompanyId = new Property("tCompanyId", "本行合作方编号");
		this.companyId = new Property("companyId", "跨行合作方编号");
		this.mchntCd = new Property("mchntCd", "商户编号");
		this.privateKeyFile = new Property("privateKeyFile", Type.file, "私钥文件(.pem)");
		this.publicKeyFile = new Property("publicKeyFile", Type.file, "公钥文件(.pem)");
		this.poxy = new Property("poxy", "是否启动代理(true:是,false:否)");
		this.diffPort = new Property("diffPort", "跨行端口");
		this.secretKey = new Property("secretKey", "密钥【MD5签名】");
		this.sftpIp = new Property("sftpIp", "sftpIp服务IP地址");
		this.sftpPort = new Property("sftpPort", "21", "sftp服务端口号");
		this.sftpUsername = new Property("sftpUsername", "sftp连接用户名");
		this.sftpPassword = new Property("sftpPassword", "sftp连接用户密码");
		this.sftpDirectory = new Property("sftpDirectory", "sftp对账文件目录");

		this.uploadSameBank = new Property("uploadSameBank", "上传路径【同行批量】");
		this.downloadSameBank = new Property("downloadSameBank", "回盘路径【同行批量】");
		this.refundExchange = new Property("refundExchange", "退汇路径");
		this.uploadDiffBank = new Property("uploadDiffBank", "上传路径【跨行批量】");
		this.downloadDiffBank = new Property("downloadDiffBank", "回盘路径【跨行批量】");
		this.queryTime = new Property("queryTime", "多少分钟后开始查询【单位分钟】");
		this.encryptKey = new Property("encryptKey", "密钥【AES加解密】");
		this.queryInterval = new Property("queryInterval","1","支付后多久时间开始查询【默认1分钟】");
	}

	public Property getCompanyId() {
		return companyId;
	}
	
	public Property gettCompanyId() {
		return tCompanyId;
	}

	public Property getMchntCd() {
		return mchntCd;
	}

	public Property getPrivateKeyFile() {
		return privateKeyFile;
	}

	public Property getPublicKeyFile() {
		return publicKeyFile;
	}
	
	public Property getPoxy() {
		return poxy;
	}
	
	public Property getDiffPort() {
		return diffPort;
	}

	public Property getSecretKey() {
		return secretKey;
	}

	public Property getSftpIp() {
		return sftpIp;
	}

	public Property getSftpPort() {
		return sftpPort;
	}

	public Property getSftpUsername() {
		return sftpUsername;
	}

	public Property getSftpPassword() {
		return sftpPassword;
	}

	public Property getSftpDirectory() {
		return sftpDirectory;
	}

	public Property getRefundExchange() {
		return refundExchange;
	}

	public Property getUploadSameBank() {
		return uploadSameBank;
	}

	public Property getDownloadSameBank() {
		return downloadSameBank;
	}

	public Property getUploadDiffBank() {
		return uploadDiffBank;
	}

	public Property getDownloadDiffBank() {
		return downloadDiffBank;
	}

	public Property getQueryTime() {
		return queryTime;
	}

	public Property getEncryptKey() {
		return encryptKey;
	}

	public Property getQueryInterval() {
		return queryInterval;
	}
}
