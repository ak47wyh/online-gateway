package com.iboxpay.settlement.gateway.xmcmbc.service;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 加密、解密文件类
 * @author weiyuanhua
 * @date 2016年2月2日 上午9:58:38
 * @Version 1.0
 */
public class Signature {
	
	private static Logger logger = LoggerFactory.getLogger(Signature.class);
	/**
	 * AES加密
	 * @param key
	 *            密钥信息
	 * @param content
	 *            待加密信息
	 */
	public static byte[] encodeAES(byte[] key, byte[] content) throws Exception {
		//不是16的倍数的，补足
		int base = 16;
		if (key.length % base != 0) {
			int groups = key.length / base + (key.length % base != 0 ? 1 : 0);
			byte[] temp = new byte[groups * base];
			Arrays.fill(temp, (byte) 0);
			System.arraycopy(key, 0, temp, 0, key.length);
			key = temp;
		}
		SecretKey secretKey = new SecretKeySpec(key, "AES");
		IvParameterSpec iv = new IvParameterSpec(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
		byte[] tgtBytes = cipher.doFinal(content);
		return tgtBytes;
	}
	
	/**
	 * AES解密
	 * @param key
	 *            密钥信息
	 * @param content
	 *            待加密信息
	 * @return
	 * @throws Exception
	 */
	public static byte[] decodeAES(byte[] key, byte[] content) throws Exception {
		// 不是16的倍数的，补足
		int base = 16;
		if (key.length % base != 0) {
			int groups = key.length / base + (key.length % base != 0 ? 1 : 0);
			byte[] temp = new byte[groups * base];
			Arrays.fill(temp, (byte) 0);
			System.arraycopy(key, 0, temp, 0, key.length);
			key = temp;
		}

		SecretKey secretKey = new SecretKeySpec(key, "AES");
		IvParameterSpec iv = new IvParameterSpec(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
		byte[] tgtBytes = cipher.doFinal(content);
		return tgtBytes;
	}
	
	/**
	 * 加密文件
	 * @param key
	 *            密钥
	 * @param plainFilePath
	 *            明文文件路径路径
	 * @param secretFilePath
	 *            密文文件路径
	 * @throws Exception
	 */
	public static void encodeAESFile(byte[] key, String plainFilePath, String secretFilePath) throws Exception {
		FileInputStream fis = null;
		ByteArrayOutputStream bos = null;
		FileOutputStream fos = null;
		try {

			fis = new FileInputStream(plainFilePath);
			bos = new ByteArrayOutputStream(fis.available());

			byte[] buffer = new byte[1024];
			int count = 0;
			while ((count = fis.read(buffer)) != -1) {
				bos.write(buffer, 0, count);
			}
			bos.flush();

			byte[] bytes = encodeAES(key, bos.toByteArray());

			fos = new FileOutputStream(secretFilePath);
			fos.write(bytes);
			fos.flush();
		} catch (Exception e) {
			throw e;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
			if (bos != null) {
				try {
					bos.close();
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	/**
	 * 解密文件
	 * @param key
	 *            密钥
	 * @param plainFilePath
	 *            明文文件路径
	 * @param secretFilePath
	 *            密文文件路径
	 * @throws Exception
	 */
	public static void decodeAESFile(byte[] key, String plainFilePath, String secretFilePath) throws Exception {
		FileInputStream fis = null;
		ByteArrayOutputStream bos = null;
		FileOutputStream fos = null;
		try {
			fis = new FileInputStream(secretFilePath);
			bos = new ByteArrayOutputStream(fis.available());

			byte[] buffer = new byte[1024];
			int count = 0;
			while ((count = fis.read(buffer)) != -1) {
				bos.write(buffer, 0, count);
			}
			bos.flush();

			byte[] bytes = decodeAES(key, bos.toByteArray());

			fos = new FileOutputStream(plainFilePath);
			fos.write(bytes);
			fos.flush();
		} catch (Exception e) {
			throw e;
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
			if (bos != null) {
				try {
					bos.close();
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			// 密钥
			byte[] key = "1234567887654321".getBytes();
			// 明文文件路径
//			String plainFilePath = "F:/xmcmbc/resouter/req_outer_20160106_152.txt";
//			// 密文文件路径
//			String secretFilePath = "F:/xmcmbc/resouter/res_outer_20160106_152.txt";
//			// 加密
//			encodeAESFile(key, plainFilePath, secretFilePath);
//			logger.info("加密成功");
			// 解密
			String plainFilePath  = "F:/xmcmbc/test.txt";
			String secretFilePath= "F:/xmcmbc/req_outer_20160106_152.txt";
			decodeAESFile(key, plainFilePath, secretFilePath);
			logger.info("解密成功");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
