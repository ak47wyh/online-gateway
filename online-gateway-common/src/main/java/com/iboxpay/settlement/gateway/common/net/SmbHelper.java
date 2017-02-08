package com.iboxpay.settlement.gateway.common.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;

import com.iboxpay.settlement.gateway.common.util.StringUtils;



public class SmbHelper {
	
	private static Logger logger = LoggerFactory.getLogger(SmbHelper.class);
	
	//  获取上传的输出流   
	public static OutputStream getSmbOutPutStream(String remoteUrl,String domain, String userName, String password, String fileName) throws Exception {
        SmbFile remoteFile = null;
        if (StringUtils.isBlank(domain) || StringUtils.isBlank(userName) || StringUtils.isBlank(password)) {
            remoteFile = new SmbFile(remoteUrl+"/"+fileName);
        } else {
            NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, userName, password);
            remoteFile = new SmbFile(remoteUrl+"/"+fileName, auth);
        }
        OutputStream out = new SmbFileOutputStream(remoteFile);
        return out;
	}
	
	
	// 从共享目录下载文件  
    public static void smbGet(String remoteUrl, String domain, String userName, String password ,String remoteFileName, File localFile) throws Exception {  
        InputStream in = null;  
        OutputStream out = null;  
        try {  
			SmbFile remoteFile = null;
			
            if (StringUtils.isBlank(domain) || StringUtils.isBlank(userName) || StringUtils.isBlank(password)) {
                remoteFile = new SmbFile(remoteUrl+ "/" +remoteFileName);
            } else {
                NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, userName, password);
                remoteFile = new SmbFile(remoteUrl+"/"+remoteFileName, auth);
            }
            if(!remoteFile.exists()){
            	throw new Exception("共享文件不存在");
            }
            
            in = new BufferedInputStream(new SmbFileInputStream(remoteFile));
            out = new BufferedOutputStream(new FileOutputStream(localFile));
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1) {
                out.write(buffer);
                buffer = new byte[1024];
            }
        } catch (Exception e) {
        	throw e;
        } finally {  
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				logger.info("关闭流发生异常");
			}
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				logger.info("关闭流发生异常");
			}
        }
    }
}
