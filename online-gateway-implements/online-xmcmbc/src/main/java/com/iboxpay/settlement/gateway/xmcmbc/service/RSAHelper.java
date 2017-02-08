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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;

import sun.misc.BASE64Decoder;

/**
 * RSAHelper -对RSA 签名&验签/分段加密&分段解密的包装签名算法: "SHA1withRSA", 私钥进行签名; 公钥进行验签.
 * 加密算法: "RSA/ECB/PKCS1Padding", 公钥进行加密; 私钥进行解密.
 * 
 * [localPrivKey]是自己的私钥, 自己的公钥给通信对方. [peerPubKey]是对方的公钥, 对方的私钥在对方那边. 为了方便,
 * 这里假定双方的密钥长度一致, 签名和加密的规则也一致.
 * 
 * 以`Base64Str`结尾的参数表示内容是Base64编码的字符串, 其他情况都是raw字符串.
 */
public class RSAHelper {

	public static final String KEY_ALGORITHM = "RSA";
	public static final String SIGNATURE_ALGORITHM = "SHA1withRSA";
	public static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding"; // 加密block需要预留11字节
	public static final int KEYBIT = 2048;
	public static final int RESERVEBYTES = 11;

	private static PrivateKey localPrivKey;
	private static PublicKey peerPubKey;

	public RSAHelper() {
	}

	/**
	 * 初始化自己的私钥,对方的公钥以及密钥长度.
	 * 
	 * @param localPrivKeyBase64Str
	 *            Base64编码的私钥,PKCS#8编码. (去掉pem文件中的头尾标识)
	 * @param peerPubKeyBase64Str
	 *            Base64编码的公钥. (去掉pem文件中的头尾标识)
	 * @param keysize
	 *            密钥长度, 一般2048
	 */
	public static void initKey(String localPrivKeyBase64Str, String peerPubKeyBase64Str, int keysize) throws Exception {
		try {
			localPrivKey = getPrivateKey(localPrivKeyBase64Str);
			peerPubKey = getPublicKey(peerPubKeyBase64Str);
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
	}
	
	public static void initKey(File privKeyFile, File pubKeyFile, int keysize) throws Exception {
		try {
			InputStream privateIn = new FileInputStream(privKeyFile);
			InputStream publicIn = new FileInputStream(pubKeyFile);
			localPrivKey = getPrivateKey(privateIn);
			peerPubKey = getPublicKey(publicIn);
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 从文件中输入流中加载公钥
	 * 
	 * @param in
	 *            公钥输入流
	 * @throws Exception
	 *             加载公钥时产生的异常
	 */
	public static RSAPublicKey getPublicKey(InputStream in) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		try {
			String readLine = null;
			StringBuilder sb = new StringBuilder();
			while ((readLine = br.readLine()) != null) {
				if (readLine.charAt(0) == '-') {
					continue;
				} else {
					sb.append(readLine);
					sb.append('\r');
				}
			}
			return getPublicKey(sb.toString());
		} catch (IOException e) {
			throw new Exception("公钥数据流读取错误");
		} catch (NullPointerException e) {
			throw new Exception("公钥输入流为空");
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (Exception e) {
				throw new Exception("关闭输入缓存流出错");
			}

			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				throw new Exception("关闭输入流出错");
			}
		}
	}

	/**
	 * 从字符串中加载公钥
	 * 
	 * @param publicKeyStr
	 *            公钥数据字符串
	 * @throws Exception
	 *             加载公钥时产生的异常
	 */
	public static RSAPublicKey getPublicKey(String publicKeyStr) throws Exception {
		try {
			BASE64Decoder base64Decoder = new BASE64Decoder();
			byte[] buffer = base64Decoder.decodeBuffer(publicKeyStr);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(buffer);
			RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);
			return publicKey;
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("无此算法");
		} catch (InvalidKeySpecException e) {
			throw new Exception("公钥非法");
		} catch (IOException e) {
			throw new Exception("公钥数据内容读取错误");
		} catch (NullPointerException e) {
			throw new Exception("公钥数据为空");
		}
	}

	/**
	 * 从文件中加载私钥
	 * 
	 * @param keyFileName
	 *            私钥文件名
	 * @return是否成功
	 * @throws Exception
	 */
	public static RSAPrivateKey getPrivateKey(InputStream in) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		try {
			String readLine = null;
			StringBuilder sb = new StringBuilder();
			while ((readLine = br.readLine()) != null) {
				if (readLine.charAt(0) == '-') {
					continue;
				} else {
					sb.append(readLine);
					sb.append('\r');
				}
			}
			return getPrivateKey(sb.toString());
		} catch (IOException e) {
			throw new Exception("私钥数据读取错误");
		} catch (NullPointerException e) {
			throw new Exception("私钥输入流为空");
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (Exception e) {
				throw new Exception("关闭输入缓存流出错");
			}

			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				throw new Exception("关闭输入流出错");
			}
		}
	}

	/**
	 * 从字符串中加载私钥
	 * 
	 * @param privateKeyStr
	 *            公钥数据字符串
	 * @throws Exception
	 *             加载私钥时产生的异常
	 */
	public static RSAPrivateKey getPrivateKey(String privateKeyStr) throws Exception {
		try {
			BASE64Decoder base64Decoder = new BASE64Decoder();
			byte[] buffer = base64Decoder.decodeBuffer(privateKeyStr);
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(buffer);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
			return privateKey;
		} catch (NoSuchAlgorithmException e) {
			throw new Exception("无此算法");
		} catch (InvalidKeySpecException e) {
			throw new Exception("私钥非法");
		} catch (IOException e) {
			throw new Exception("私钥数据内容读取错误");
		} catch (NullPointerException e) {
			throw new Exception("私钥数据为空");
		}
	}

	/**
	 * RAS加密
	 * 
	 * @param peerPubKey
	 *            公钥
	 * @param data
	 *            待加密信息
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] encryptRSA(byte[] plainBytes, boolean useBase64Code, String charset) throws Exception {
		String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding"; // 加密block需要预留11字节
		int KEYBIT = 2048;
		int RESERVEBYTES = 11;
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		int decryptBlock = KEYBIT / 8; // 256 bytes
		int encryptBlock = decryptBlock - RESERVEBYTES; // 245 bytes
		// 计算分段加密的block数 (向上取整)
		int nBlock = (plainBytes.length / encryptBlock);
		if ((plainBytes.length % encryptBlock) != 0) { // 余数非0，block数再加1
			nBlock += 1;
		}
		// 输出buffer, 大小为nBlock个decryptBlock
		ByteArrayOutputStream outbuf = new ByteArrayOutputStream(nBlock * decryptBlock);
		cipher.init(Cipher.ENCRYPT_MODE, peerPubKey);
		// cryptedBase64Str =
		// Base64.encodeBase64String(cipher.doFinal(plaintext.getBytes()));
		// 分段加密
		for (int offset = 0; offset < plainBytes.length; offset += encryptBlock) {
			// block大小: encryptBlock 或剩余字节数
			int inputLen = (plainBytes.length - offset);
			if (inputLen > encryptBlock) {
				inputLen = encryptBlock;
			}
			// 得到分段加密结果
			byte[] encryptedBlock = cipher.doFinal(plainBytes, offset, inputLen);
			// 追加结果到输出buffer中
			outbuf.write(encryptedBlock);
		}
		// 如果是Base64编码，则返回Base64编码后的数组
		if (useBase64Code) {
			return Base64.encodeBase64String(outbuf.toByteArray()).getBytes(charset);
		} else {
			return outbuf.toByteArray(); // ciphertext
		}
	}

	/**
	 * RSA解密
	 * 
	 * @param localPrivKey
	 *            私钥
	 * @param cryptedBytes
	 *            待解密信息
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] decryptRSA(byte[] cryptedBytes, boolean useBase64Code, String charset) throws Exception {
		String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding"; // 加密block需要预留11字节
		byte[] data = null;

		// 如果是Base64编码的话，则要Base64解码
		if (useBase64Code) {
			data = Base64.decodeBase64(new String(cryptedBytes, charset));
		} else {
			data = cryptedBytes;
		}

		int KEYBIT = 2048;
		int RESERVEBYTES = 11;
		Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		int decryptBlock = KEYBIT / 8; // 256 bytes
		int encryptBlock = decryptBlock - RESERVEBYTES; // 245 bytes
		// 计算分段解密的block数 (理论上应该能整除)
		int nBlock = (data.length / decryptBlock);
		// 输出buffer, , 大小为nBlock个encryptBlock
		ByteArrayOutputStream outbuf = new ByteArrayOutputStream(nBlock * encryptBlock);
		cipher.init(Cipher.DECRYPT_MODE, localPrivKey);
		// plaintext = new
		// String(cipher.doFinal(Base64.decodeBase64(cryptedBase64Str)));
		// 分段解密
		for (int offset = 0; offset < data.length; offset += decryptBlock) {
			// block大小: decryptBlock 或剩余字节数
			int inputLen = (data.length - offset);
			if (inputLen > decryptBlock) {
				inputLen = decryptBlock;
			}

			// 得到分段解密结果
			byte[] decryptedBlock = cipher.doFinal(data, offset, inputLen);
			// 追加结果到输出buffer中
			outbuf.write(decryptedBlock);
		}
		outbuf.flush();
		outbuf.close();
		return outbuf.toByteArray();
	}

	/**
	 * RSA签名
	 * 
	 * @param localPrivKey
	 *            私钥
	 * @param plaintext
	 *            需要签名的信息
	 * @return byte[]
	 * @throws Exception
	 */
	public static byte[] signRSA(byte[] plainBytes, boolean useBase64Code, String charset) throws Exception {
		String SIGNATURE_ALGORITHM = "SHA1withRSA";
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initSign(localPrivKey);
		signature.update(plainBytes);

		// 如果是Base64编码的话，需要对签名后的数组以Base64编码
		if (useBase64Code) {
			return Base64.encodeBase64String(signature.sign()).getBytes(charset);
		} else {
			return signature.sign();
		}
	}

	/**
	 * 验签操作
	 * 
	 * @param peerPubKey
	 *            公钥
	 * @param plainBytes
	 *            需要验签的信息
	 * @param signBytes
	 *            签名信息
	 * @return boolean
	 */
	public boolean verifyRSA(byte[] plainBytes, byte[] signBytes, boolean useBase64Code, String charset) throws Exception {
		boolean isValid = false;
		String SIGNATURE_ALGORITHM = "SHA1withRSA";
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initVerify(peerPubKey);
		signature.update(plainBytes);

		// 如果是Base64编码的话，需要对验签的数组以Base64解码
		if (useBase64Code) {
			isValid = signature.verify(Base64.decodeBase64(new String(signBytes, charset)));
		} else {
			isValid = signature.verify(signBytes);
		}
		return isValid;
	}

	public static final String privKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDW+J3i8/zTkMvD" + "yCftOsGNNVEmoodxUr23VeYUW9UI6do8U83ExJVtF9vX2O/RUru7ExJTMOxVg2QZ"
			+ "h/IekFFv67iVPSfGBX70DC8vobZKEfhE8V82tWmCtH45zx6U73hVALiV6X8EslIo" + "sdT9xWAMY2NH0HvZ7V/06SvNNO0zKOm2vPyspzbwRDUpA89dmjEAwAlDx12/X+EY"
			+ "AR8VEU9ctLjVwYFXuFjc/BCryColK9HIg21P3vubHZM6GKIXfq6zJawV8lTkJlb5" + "QEdcNk33VjpMhGs7ZRzJXgMG07uaPIBI6k7VQ2UZM7GtKUH8KBfWzViQ+Asz2cpE"
			+ "0t0VUoF9AgMBAAECggEBAKh9zwp+oDCW8g7vB9RZ1DDAlG2KwEwjRQ24pxBX9f75" + "hBL6wHI0fsY2CBsDLtzLUtdLGHbaBrLzu/aC5lPsW9g0UsWuXElKL3pLPoS/5Cfk"
			+ "M8qdwToZMKzAmZrn6xljJNbDLOpbTDI7Lkg1MjMBi8nJ8JvuHdTux+InDCzYCf6o" + "51aZN2XMLmut4e5zEdjHHknV1On2Jjgc5Gu0CbzrLTUpwOlhICRGiWQY/Mnt73HJ"
			+ "vZv7EQ0FjZk7UnWEweEfbgDBok283z6NaClvU5/MUaND2wWVdJfNPaUjHYtf7cE9" + "S3rNrY5mpwVdAHmvAFKfx9PhB4A0hnY2h+i1VmLjBoECgYEA9542ca50sXNsbWwq"
			+ "W3T/78ZWKmQi9nP4MAtZkXzEFJtgEyIXT5CU6J8L1RhZ80J9tokR9FH3cwNUZl9/" + "PiGD8l6jTjd08LfSW9t9z+4E0M6XelHG7ZfRtfV3HgzIHaBWI+nxd7yg4BoY21mg"
			+ "Tmypz+88aPHDvC8Ss8T3jkJZul0CgYEA3j9+5JXt9sYbfw2Yln3XSRAXS9MDWRCN" + "K/NZiQ4RZ22DpMOjNanH4jypIpuV1pnU8BM1iv4e5l49Kx2I5vqnE/GDkouhEzM8"
			+ "vPhBKHKLXloKtziIAR0lJOGGER2FtU3iuqSb7nRjt53G9OSnfJ2IL7H6Br7L0O+r" + "679ofKiSsaECgYB2T0iyHmmxE3Yd/g1q70cN+FTZIkk2Ogi+Y93izpsdQXOxEJvU"
			+ "rz8Gul877MulmAJawbkrZDJ36IJd+4jfVcImfqNGTub30MyYiRHe1FnGrr7fec0z" + "XlObvfGxEOhYh3BA7pkp3Z18FdwEihk2/2JPcH4LomAkPNWRwS2K8hbPHQKBgQCc"
			+ "BYdXgcmkzD7RWwIb5AwWxq0UFfbrt6rjh9r7VFzzdvZL3Ove6GnicSNroD34gdXz" + "FAkqomue3dmjQwCw5pYUciAj6NITYIzrPHzBoGgmvJ95ML6JyaQh2BD+QvNy7FKX"
			+ "JKgzJpI6fREHKt5JpW3NzevwgFElRJw0zBLWMKGLAQKBgCXMuSed+xQk5q8of4SY" + "AoHi+1JFQQw7GLa06PpphmlUJxj8WBijnpPJSfktLrgx9SAQ5Bfqw4cGiRsS7zIu"
			+ "c+FDrSSnQrHCwrRmgsaTm4kvqxglcbJAnbuNescAPPkvkK9ZI2gMsIHeHMi0lLSk" + "kwO17IvdhztJbUzFhOSSiMe6";

	public static final String pubKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA1vid4vP805DLw8gn7TrB" + "jTVRJqKHcVK9t1XmFFvVCOnaPFPNxMSVbRfb19jv0VK7uxMSUzDsVYNkGYfyHpBR"
			+ "b+u4lT0nxgV+9AwvL6G2ShH4RPFfNrVpgrR+Oc8elO94VQC4lel/BLJSKLHU/cVg" + "DGNjR9B72e1f9OkrzTTtMyjptrz8rKc28EQ1KQPPXZoxAMAJQ8ddv1/hGAEfFRFP"
			+ "XLS41cGBV7hY3PwQq8gqJSvRyINtT977mx2TOhiiF36usyWsFfJU5CZW+UBHXDZN" + "91Y6TIRrO2UcyV4DBtO7mjyASOpO1UNlGTOxrSlB/CgX1s1YkPgLM9nKRNLdFVKB" + "fQIDAQAB";

	public static final String plaintext = "你好，测试";

	public static void main(String[] args) throws Exception {
//		System.out.println("=====> init <=====");
//		RSAHelper cipher = new RSAHelper();
//		cipher.initKey(privKey, pubKey, 2048);
//
//		System.out.println("=====> sign & verify <=====");
//
//		// 签名
//		byte[] signBytes = cipher.signRSA(plaintext.getBytes("UTF-8"), false, "UTF-8");
//
//		// 验证签名
//		boolean isValid = cipher.verifyRSA(plaintext.getBytes("UTF-8"), signBytes, false, "UTF-8");
//		System.out.println("isValid: " + isValid);
//
//		// 加密和解密
//		System.out.println("=====> encrypt & decrypt <=====");
//		// 对明文加密
//		byte[] cryptedBytes = cipher.encryptRSA(plaintext.getBytes("UTF-8"), false, "UTF-8");
//
//		// 对密文解密
//		byte[] decryptedBytes = cipher.decryptRSA(cryptedBytes, false, "UTF-8");
//		System.out.println("decrypted: " + new String(decryptedBytes, "UTF-8"));

		
		File privateKeyFile = new File("D:/dk_pkcs8_rsa_private_key_2048.pem");
		File publicKeyFile = new File("D:/xmcmbc_rsa_public_key_2048.pem");
		RSAHelper.initKey(privateKeyFile, publicKeyFile, 2048);
		byte[] bodyBytes = toByteArray("D:/response.bin");
		System.out.println("length:" + bodyBytes.length);
		byte[] bodyBytes2 = ArrayUtils.subarray(bodyBytes, 8 + 8 + 8 + 4 + 256, 796);
		byte[] decryptedBytes = RSAHelper.decryptRSA(bodyBytes2, false, "utf-8");
		String decryptedStr = new String(decryptedBytes, "utf-8");
		System.out.println(decryptedStr);
	}

	public static byte[] toByteArray(String filename) throws IOException {  
        File f = new File(filename);  
        if (!f.exists()) {  
            throw new FileNotFoundException(filename);  
        }  
  
        ByteArrayOutputStream bos = new ByteArrayOutputStream((int) f.length());  
        BufferedInputStream in = null;  
        try {  
            in = new BufferedInputStream(new FileInputStream(f));  
            int buf_size = 1024;  
            byte[] buffer = new byte[buf_size];  
            int len = 0;  
            while (-1 != (len = in.read(buffer, 0, buf_size))) {  
                bos.write(buffer, 0, len);  
            }  
            return bos.toByteArray();  
        } catch (IOException e) {  
            e.printStackTrace();  
            throw e;  
        } finally {  
            try {  
                in.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
            bos.close();  
        }  
    }  
}
