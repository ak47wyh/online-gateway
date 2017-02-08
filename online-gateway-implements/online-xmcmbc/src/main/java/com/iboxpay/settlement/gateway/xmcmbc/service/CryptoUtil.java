/*
 * Copyright (C) 2011-2015 ShenZhen iBOXPAY Information Technology Co.,Ltd.
 * 
 * All right reserved.
 * 
 * This software is the confidential and proprietary
 * information of iBoxPay Company of China. 
 * ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only
 * in accordance with the terms of the contract agreement 
 * you entered into with iBoxpay inc.
 *
 */
package com.iboxpay.settlement.gateway.xmcmbc.service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;

public class CryptoUtil {

	/**
	 * 数字签名函数入口
	 * 
	 * @param plainBytes
	 *            待签名明文字节数组
	 * @param privateKey
	 *            签名使用私钥
	 * @param signAlgorithm
	 *            签名算法
	 * @return 签名后的字节数组
	 * @throws Exception
	 */
	public static byte[] digitalSign(byte[] plainBytes, PrivateKey privateKey, String signAlgorithm) throws Exception {
		try {
			Signature signature = Signature.getInstance(signAlgorithm);
			signature.initSign(privateKey);
			signature.update(plainBytes);
			byte[] signBytes = signature.sign();

			return signBytes;
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(String.format("数字签名时没有[%s]此类算法", signAlgorithm));
		} catch (InvalidKeyException e) {
			throw new Exception("数字签名时私钥无效");
		} catch (SignatureException e) {
			throw new Exception("数字签名时出现异常");
		}
	}

	/**
	 * 验证数字签名函数入口
	 * 
	 * @param plainBytes
	 *            待验签明文字节数组
	 * @param signBytes
	 *            待验签签名后字节数组
	 * @param publicKey
	 *            验签使用公钥
	 * @param signAlgorithm
	 *            签名算法
	 * @return 验签是否通过
	 * @throws Exception
	 */
	public static boolean verifyDigitalSign(byte[] plainBytes, byte[] signBytes, PublicKey publicKey, String signAlgorithm) throws Exception {
		boolean isValid = false;
		try {
			Signature signature = Signature.getInstance(signAlgorithm);
			signature.initVerify(publicKey);
			signature.update(plainBytes);
			isValid = signature.verify(signBytes);
			return isValid;
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(String.format("验证数字签名时没有[%s]此类算法", signAlgorithm));
		} catch (InvalidKeyException e) {
			throw new Exception("验证数字签名时公钥无效");
		} catch (SignatureException e) {
			throw new Exception("验证数字签名时出现异常");
		}
	}

	/**
	 * 获取RSA公钥对象
	 * 
	 * @param filePath
	 *            RSA公钥路径
	 * @param keyAlgorithm
	 *            密钥算法
	 * @return RSA公钥对象
	 * @throws Exception
	 */
	public static PublicKey getRSAPublicKeyByFileSuffix(File file, String keyAlgorithm) throws Exception {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuilder sb = new StringBuilder();
			String readLine = null;
			while ((readLine = br.readLine()) != null) {
				if (readLine.charAt(0) == '-') {
					continue;
				} else {
					sb.append(readLine);
					sb.append('\r');
				}
			}
			X509EncodedKeySpec pubX509 = new X509EncodedKeySpec(Base64.decodeBase64(sb.toString()));
			KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
			PublicKey pubKey = keyFactory.generatePublic(pubX509);

			return pubKey;
		} catch (FileNotFoundException e) {
			throw new Exception("公钥路径文件不存在");
		} catch (IOException e) {
			throw new Exception("读取公钥异常");
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(String.format("生成密钥工厂时没有[%s]此类算法", keyAlgorithm));
		} catch (InvalidKeySpecException e) {
			throw new Exception("生成公钥对象异常");
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {}
		}
	}

	/**
	 * 获取RSA私钥对象
	 * 
	 * @param filePath
	 *            RSA私钥路径
	 * @param keyAlgorithm
	 *            密钥算法
	 * @return RSA私钥对象
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	public static PrivateKey getRSAPrivateKeyByFileSuffix(File file, String keyAlgorithm) throws Exception {
		InputStream in = null;
		try {
			in = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuilder sb = new StringBuilder();
			String readLine = null;
			while ((readLine = br.readLine()) != null) {
				if (readLine.charAt(0) == '-') {
					continue;
				} else {
					sb.append(readLine);
					sb.append('\r');
				}
			}
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.decodeBase64(sb.toString()));
			KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
			PrivateKey priKey = keyFactory.generatePrivate(priPKCS8);

			return priKey;
		} catch (FileNotFoundException e) {
			throw new Exception("私钥路径文件不存在");
		} catch (IOException e) {
			throw new Exception("读取私钥异常");
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("生成私钥对象异常");
		} catch (InvalidKeySpecException e) {
			throw new Exception("生成私钥对象异常");
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {}
		}
	}

	/**
	 * RSA加密
	 * 
	 * @param plainBytes
	 *            明文字节数组
	 * @param publicKey
	 *            公钥
	 * @param keyLength
	 *            密钥bit长度
	 * @param reserveSize
	 *            padding填充字节数，预留11字节
	 * @param cipherAlgorithm
	 *            加解密算法，一般为RSA/ECB/PKCS1Padding
	 * @return 加密后字节数组，不经base64编码
	 * @throws Exception
	 */
	public static byte[] RSAEncrypt(byte[] plainBytes, PublicKey publicKey, int keyLength, int reserveSize, String cipherAlgorithm) throws Exception {
		int keyByteSize = keyLength / 8; // 密钥字节数
		int encryptBlockSize = keyByteSize - reserveSize; // 加密块大小=密钥字节数-padding填充字节数
		int nBlock = plainBytes.length / encryptBlockSize;// 计算分段加密的block数，向上取整
		if ((plainBytes.length % encryptBlockSize) != 0) { // 余数非0，block数再加1
			nBlock += 1;
		}

		try {
			Cipher cipher = Cipher.getInstance(cipherAlgorithm);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);

			// 输出buffer，大小为nBlock个keyByteSize
			ByteArrayOutputStream outbuf = new ByteArrayOutputStream(nBlock * keyByteSize);
			// 分段加密
			for (int offset = 0; offset < plainBytes.length; offset += encryptBlockSize) {
				int inputLen = plainBytes.length - offset;
				if (inputLen > encryptBlockSize) {
					inputLen = encryptBlockSize;
				}

				// 得到分段加密结果
				byte[] encryptedBlock = cipher.doFinal(plainBytes, offset, inputLen);
				// 追加结果到输出buffer中
				outbuf.write(encryptedBlock);
			}

			outbuf.flush();
			outbuf.close();
			return outbuf.toByteArray();
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(String.format("没有[%s]此类加密算法", cipherAlgorithm));
		} catch (NoSuchPaddingException e) {
			throw new Exception(String.format("没有[%s]此类填充模式", cipherAlgorithm));
		} catch (InvalidKeyException e) {
			throw new Exception("无效密钥");
		} catch (IllegalBlockSizeException e) {
			throw new Exception("加密块大小不合法");
		} catch (BadPaddingException e) {
			throw new Exception("错误填充模式");
		} catch (IOException e) {
			throw new Exception("字节输出流异常");
		}
	}

	/**
	 * RSA解密
	 * 
	 * @param encryptedBytes
	 *            加密后字节数组
	 * @param privateKey
	 *            私钥
	 * @param keyLength
	 *            密钥bit长度
	 * @param reserveSize
	 *            padding填充字节数，预留11字节
	 * @param cipherAlgorithm
	 *            加解密算法，一般为RSA/ECB/PKCS1Padding
	 * @return 解密后字节数组，不经base64编码
	 * @throws Exception
	 */
	public static byte[] RSADecrypt(byte[] encryptedBytes, PrivateKey privateKey, int keyLength, int reserveSize, String cipherAlgorithm) throws Exception {
		int keyByteSize = keyLength / 8; // 密钥字节数
		int decryptBlockSize = keyByteSize - reserveSize; // 解密块大小=密钥字节数-padding填充字节数
		int nBlock = encryptedBytes.length / keyByteSize;// 计算分段解密的block数，理论上能整除

		try {
			Cipher cipher = Cipher.getInstance(cipherAlgorithm);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);

			// 输出buffer，大小为nBlock个decryptBlockSize
			ByteArrayOutputStream outbuf = new ByteArrayOutputStream(nBlock * decryptBlockSize);
			// 分段解密
			for (int offset = 0; offset < encryptedBytes.length; offset += keyByteSize) {
				// block大小: decryptBlock 或 剩余字节数
				int inputLen = encryptedBytes.length - offset;
				if (inputLen > keyByteSize) {
					inputLen = keyByteSize;
				}

				// 得到分段解密结果
				byte[] decryptedBlock = cipher.doFinal(encryptedBytes, offset, inputLen);
				// 追加结果到输出buffer中
				outbuf.write(decryptedBlock);
			}

			outbuf.flush();
			outbuf.close();
			return outbuf.toByteArray();
		} catch (NoSuchAlgorithmException e) {
			throw new Exception(String.format("没有[%s]此类解密算法", cipherAlgorithm));
		} catch (NoSuchPaddingException e) {
			throw new Exception(String.format("没有[%s]此类填充模式", cipherAlgorithm));
		} catch (InvalidKeyException e) {
			throw new Exception("无效密钥");
		} catch (IllegalBlockSizeException e) {
			throw new Exception("解密块大小不合法");
		} catch (BadPaddingException e) {
			throw new Exception("错误填充模式");
		} catch (IOException e) {
			throw new Exception("字节输出流异常");
		}
	}
	
	public static String bytes2HexString(byte[] b) {
		String ret = "";
		for (int i = 0; i < b.length; i++) {
			String hex = Integer.toHexString(b[i] & 0xFF);
			if(hex.length() == 1) {
				hex = '0' + hex;
			}
			
			ret += hex.toLowerCase();
		}
		
		return ret;
	}

	public static void main(String[] args) {
		try {
			File filePubKey = new File("D:/bank_rsa_public_key_2048.pem");
			File filePriKey = new File("D:/bank_pkcs8_rsa_private_key_2048.pem");
			final PublicKey yhPubKey = CryptoUtil.getRSAPublicKeyByFileSuffix(filePubKey, "RSA");
			final PrivateKey hzfPriKey = CryptoUtil.getRSAPrivateKeyByFileSuffix(filePriKey, "RSA");

			String plainXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>...";
			byte[] signData = CryptoUtil.digitalSign(plainXML.getBytes("UTF-8"), hzfPriKey, "SHA1WithRSA");// 签名
			byte[] encrtptData = CryptoUtil.RSAEncrypt(plainXML.getBytes("UTF-8"), yhPubKey, 2048, 11, "RSA/ECB/PKCS1Padding");// 加密
			String signStr = new String(signData);
			String encrtptStr = bytes2HexString(encrtptData);
			System.out.println(signStr);
			
			byte[] decryptData = CryptoUtil.RSADecrypt(encrtptData, hzfPriKey, 2048, 11, "RSA/ECB/PKCS1Padding");// 解密
			boolean verifySign = CryptoUtil.verifyDigitalSign(decryptData, signStr.getBytes("UTF-8"), yhPubKey, "SHA1WithRSA");// 验签

			System.out.println(verifySign);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
