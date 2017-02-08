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
package com.iboxpay.settlement.gateway.common.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

import com.iboxpay.settlement.gateway.common.exception.FrontEndException;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * sftp文件协议上传和下载辅助类
 * @author caolipeng
 * @date 2015年4月30日 上午10:23:19
 * @Version 1.0
 */
public class SftpHelper {

    /**
     * 连接sftp服务器
     * @param host 主机
     * @param port 端口
     * @param username 用户名
     * @param password 密码
     * @return
     */
    public static ChannelSftp connect(String host, int port, String username, String password) throws FrontEndException {
        ChannelSftp sftp = null;
        try {
            JSch jsch = new JSch();
            jsch.getSession(username, host, port);
            Session sshSession = jsch.getSession(username, host, port);
            sshSession.setPassword(password);
            Properties sshConfig = new Properties();
            sshConfig.put("StrictHostKeyChecking", "no");
            sshSession.setConfig(sshConfig);
            //sshSession.setTimeout(timeout); //设置timeout时间
            sshSession.connect();
            Channel channel = sshSession.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;
        } catch (Exception e) {
            throw new FrontEndException("连接sftp服务器失败", e);
        }
        return sftp;
    }

    /**
     * 上传文件
     * @param host
     * @param port
     * @param username
     * @param password
     * @param directory  上传的目录
     * @param in
     * @param fileName
     * @throws FrontEndException
     */
    public static void upload(String host, int port, String username, String password, String directory, String subDirectory, InputStream in, String fileName) throws FrontEndException {
        ChannelSftp sftp = connect(host, port, username, password);
        try {
            String path = directory + subDirectory;
            sftp.cd(path);
            sftp.put(in, fileName);
        } catch (Exception e) {
            throw new FrontEndException("上传文件失败", e);
        } finally {
        	Session sshSession = null;
        	try {
				sshSession = sftp.getSession();
			} catch (JSchException e) {
				throw new FrontEndException("上传文件时候获取Session发生异常", e);
			}
        	//生产环境上出现open too many files,需要先关闭的是session对象,再关闭ChannelSftp对象
        	if(sshSession != null){
        		sshSession.disconnect();
        	}
            if (sftp != null) {
                sftp.disconnect();
            }
        }
    }
    
	/**
	 * 上传文件(带创建文件目录)
	 * 
	 * @param host
	 *            地址
	 * @param port
	 *            端口
	 * @param username
	 *            用户
	 * @param password
	 *            密码
	 * @param directory
	 *            上传的目录
	 * @param in
	 *            输入流
	 * @param fileName
	 *            文件名
	 * @throws FrontEndException
	 */
    public static void uploadFile(String host, int port, String username, String password, String directory, String subDirectory, InputStream in, String fileName) throws FrontEndException {
        ChannelSftp sftp = connect(host, port, username, password);
        try {
            String path = directory + subDirectory;
            SftpHelper.isNeedMkdir(path, sftp);
            sftp.cd(path);
            sftp.put(in, fileName);
        } catch (Exception e) {
            throw new FrontEndException("上传文件失败", e);
        } finally {
        	Session sshSession = null;
        	try {
				sshSession = sftp.getSession();
			} catch (JSchException e) {
				throw new FrontEndException("上传文件时候获取Session发生异常", e);
			}
        	//生产环境上出现open too many files,需要先关闭的是session对象,再关闭ChannelSftp对象
        	if(sshSession != null){
        		sshSession.disconnect();
        	}
            if (sftp != null) {
                sftp.disconnect();
            }
        }
    }

    /**
     * 下载文件
     * @param host
     * @param port
     * @param username
     * @param password
     * @param directory 下载目录
     * @param fileName
     * @param os 
     * @param sftp
     */
    public static void download(String host, int port, String username, String password, String directory, String subDirectory, String fileName, OutputStream os) throws FrontEndException {
        ChannelSftp sftp = connect(host, port, username, password);
        try {
            String path = directory + subDirectory;
            sftp.cd(path);
            sftp.get(fileName, os);
        } catch (Exception e) {
            throw new FrontEndException("下载文件失败", e);
        } finally {
        	Session sshSession = null;
        	try {
				sshSession = sftp.getSession();
			} catch (JSchException e) {
				throw new FrontEndException("下载文件时候获取Session发生异常", e);
			}
        	//生产环境上出现open too many files,需要先关闭的是session对象,再关闭ChannelSftp对象
        	if(sshSession != null){
        		sshSession.disconnect();
        	}
            if (sftp != null) {
                sftp.disconnect();
            }
        }
    }
    
    
	/**
	 * 下载文件(带创建文件目录)
	 * 
	 * @param host
	 *            地址
	 * @param port
	 *            端口
	 * @param username
	 *            用户
	 * @param password
	 *            密码
	 * @param directory
	 *            下载目录
	 * @param fileName
	 *            文件名称
	 * @param os
	 *            输出流
	 */
    public static void downloadFile(String host, int port, String username, String password, String directory, String subDirectory, String fileName, OutputStream os) throws FrontEndException {
        ChannelSftp sftp = connect(host, port, username, password);
        try {
            String path = directory + subDirectory;
            SftpHelper.isNeedMkdir(path, sftp);
            sftp.cd(path);
            sftp.get(fileName, os);
        } catch (Exception e) {
            throw new FrontEndException("下载文件失败", e);
        } finally {
        	Session sshSession = null;
        	try {
				sshSession = sftp.getSession();
			} catch (JSchException e) {
				throw new FrontEndException("下载文件时候获取Session发生异常", e);
			}
        	//生产环境上出现open too many files,需要先关闭的是session对象,再关闭ChannelSftp对象
        	if(sshSession != null){
        		sshSession.disconnect();
        	}
            if (sftp != null) {
                sftp.disconnect();
            }
        }
    }

    /**
     * 备份划账回盘源文件
     * @param host
     * @param port
     * @param username
     * @param password
     * @param directory
     * @param fileName
     * @param in
     * @throws FrontEndException
     */
    public static void backup(String host, int port, String username, String password, String directory, String fileName, String subDirectory, InputStream in, String backupDirectory)
            throws FrontEndException {
        ChannelSftp sftp = connect(host, port, username, password);
        try {
            //备份划账回盘文件到/upload/hz/Backups/BackupsTransferLedgerReturn/2015-01-01/  下面
            String path = directory + subDirectory + DateTimeUtil.format(new Date(), "yyyy-MM-dd") + "/";

            sftp.mkdir(path);//日期文件夹(2015-01-01)是每日需要我们自己创建

            sftp.cd(path);//cd操作时候 一定要确保该文件夹存在,否在报错
            sftp.put(in, fileName);

            //删除源文件夹下的划账回盘文件(这个目录已经存在)
            String delPath = directory + backupDirectory;
            delete(delPath, fileName, sftp);

        } catch (Exception e) {
            throw new FrontEndException("备份文件失败", e);
        } finally {
        	Session sshSession = null;
        	try {
				sshSession = sftp.getSession();
			} catch (JSchException e) {
				throw new FrontEndException("上传文件时候获取Session发生异常", e);
			}
        	//生产环境上出现open too many files,需要先关闭的是session对象,再关闭ChannelSftp对象
        	if(sshSession != null){
        		sshSession.disconnect();
        	}
            if (sftp != null) {
                sftp.disconnect();
            }
        }
    }

    /**
     * 列出目录下的文件
     * @param directory
     * @param sftp
     * @throws SftpException
     * @return
     */
    public static Vector listFiles(String directory, ChannelSftp sftp) throws SftpException {
        return sftp.ls(directory);
    }
    /**
     * 判断某个目录是否存在，存在则不创建，不存在，则在捕获的异常中创建新的目录
     * @param directory
     * @param sftp
     */
    private static void isNeedMkdir(String directory, ChannelSftp sftp) throws FrontEndException{
    	try {
			sftp.ls(directory);
		} catch (SftpException e) {
			try {
				sftp.mkdir(directory);
			} catch (SftpException e1) {
				throw new FrontEndException("创建目录发生异常", e);
			}
		}
    }
    /**
     * 删除文件
     * 
     * @param directory
     *            要删除文件所在目录
     * @param deleteFile
     *            要删除的文件
     * @param sftp
     */
    public static void delete(String directory, String deleteFile, ChannelSftp sftp) {
        try {
            sftp.cd(directory);
            sftp.rm(deleteFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //		SftpHelper sftp = new SftpHelper();
        //		test1();
        test2();
        //		test3();
    }

    private static void test3() {
        ChannelSftp sftp = null;
        try {
            sftp = connect("202.96.33.145", 3099, "boxpay_test", "boxp@798");
            String directory = "/upload/hz/Backups/BackupsTransferLedgerReturn/2015-05-20/";
            String deleteFile = "transferledgerreturn_20150512.txt";
            delete(directory, deleteFile, sftp);
            test2();
        } catch (FrontEndException e) {
            e.printStackTrace();
        }
    }

    private static void test2() {
        ChannelSftp sftp = null;
        try {
//            sftp = connect("202.96.33.145", 3099, "boxpay_test", "boxp@798");//测试环境
            sftp = connect("202.96.33.145", 3099, "boxpay", "boxp@798");//生产环境
//            			String directory = "/upload/hz/Backups/BackupsTransferLedgerReturn/2015-05-20/";//备份目录下
            String directory = "/upload/hz/Source/SourceTransferLedger/";//存放回盘文件的源目录
            Vector<String> list = listFiles(directory, sftp);
            System.out.println(list);
        } catch (FrontEndException e) {
            e.printStackTrace();
        } catch (SftpException e) {
            e.printStackTrace();
        }
    }

    private static void test1() {
        InputStream in = null;
        String file = genFile();
        String host = "202.96.33.145";
        int port = 3099;
        String username = "boxpay_test";
        String pwd = "boxp@798";
        String path = "/upload/hz/";
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream(1024);
            in = new ByteArrayInputStream(file.getBytes("UTF-8"));
            SftpHelper.upload(host, port, username, pwd, path, "Source/SourceTransferLedgerReturn/", in, "transferledger_20150505.txt");//SourceTransferLedger
            SftpHelper.download(host, port, username, pwd, path, "Source/SourceTransferLedgerReturn/", "transferledger_20150505.txt", outputStream);
            String result = outputStream.toString("UTF-8");
            System.out.println(result);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FrontEndException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String genFile() {
        StringBuilder sb = new StringBuilder();
        sb.append("898110248990097,A公司,100.01,xes00101,hz,0\r\n").append("898110248990097,B公司,20.78,xes00102,hz,0");
        return sb.toString();
    }
}
