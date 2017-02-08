package com.iboxpay.settlement.gateway.common.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.settlement.gateway.common.exception.FrontEndException;

public class FtpHelper {

    private final static Logger LOGGER = LoggerFactory.getLogger(FtpHelper.class);

    /**
     * 上传文件
     * @param ip
     * @param userName
     * @param password
     * @param sourceStream
     * @param ftpDir
     * @param ftpFileName
     * @param charset
     * @throws FrontEndException
     */
    public final static void upload(String ip, String userName, String password, InputStream sourceStream, String ftpDir, String ftpFileName, String charset) throws FrontEndException {
        upload(ip, -1, userName, password, sourceStream, ftpDir, ftpFileName, charset);
    }

    /**
     * 上传文件
     * @param ip
     * @param port
     * @param userName
     * @param password
     * @param sourceStream
     * @param ftpDir
     * @param ftpFileName
     * @param charset
     * @throws FrontEndException
     */
    public final static void upload(String ip, int port, String userName, String password, InputStream sourceStream, String ftpDir, String ftpFileName, String charset) throws FrontEndException {
        FTPClient ftpClient = new FTPClient();

        try {
            if (port <= 0)
                ftpClient.connect(ip);
            else ftpClient.connect(ip, port);
            ftpClient.login(userName, password);
            //设置上传目录 
            ftpClient.changeWorkingDirectory(ftpDir);
            ftpClient.setBufferSize(1024);
            ftpClient.setControlEncoding(charset);
            //设置文件类型（二进制） 
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.storeFile(ftpFileName, sourceStream);
        } catch (IOException e) {
            throw new FrontEndException("上传文件失败", e);
        } finally {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                LOGGER.error("断开ftp连接异常", e);
            }
        }
    }

    public final static void download(String ip, String userName, String password, OutputStream targetOutputStream, String ftpDir, String ftpFileName, String charset) throws FrontEndException {
        download(ip, -1, userName, password, targetOutputStream, ftpDir, ftpFileName, charset);
    }

    /**
     * 下载文件
     * @param ip
     * @param port
     * @param userName
     * @param password
     * @param sourceStream
     * @param targetDir
     * @param targetFileName
     * @param charset
     * @throws FrontEndException
     */
    public final static void download(String ip, int port, String userName, String password, OutputStream targetOutputStream, String ftpDir, String ftpFileName, String charset)
            throws FrontEndException {
        FTPClient ftpClient = new FTPClient();
        try {
            if (port <= 0)
                ftpClient.connect(ip);
            else ftpClient.connect(ip, port);
            ftpClient.login(userName, password);
            ftpClient.changeWorkingDirectory(ftpDir);
            ftpClient.setBufferSize(1024);
            //设置文件类型（二进制） 
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.retrieveFile(ftpFileName, targetOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("FTP客户端出错！", e);
        } finally {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                LOGGER.error("断开ftp连接异常", e);
            }
        }
    }
}
