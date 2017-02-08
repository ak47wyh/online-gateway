package com.iboxpay.settlement.gateway.xmcmbc;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.iboxpay.settlement.gateway.common.exception.FrontEndException;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.common.util.DomUtil;
import com.iboxpay.settlement.gateway.common.util.PropertyReader;
import com.iboxpay.settlement.gateway.xmcmbc.service.RSAHelper;

/**
 * 厦门民生代扣跨行长连接Socket封装
 * 
 * @author weiyuanhua
 * @date 2016年2月2日 上午9:58:38
 * @Version 1.0
 */
@Component
public class SocketDiffHelper {

	private static Logger logger = LoggerFactory.getLogger(SocketDiffHelper.class);

	/**
	 * 已经接收的粘包块
	 */
	private byte[] receivedBytes;

	/**
	 * 发送队列
	 */
	public static final LinkedBlockingQueue<String> sendQueue = new LinkedBlockingQueue<String>();

	public static final Map<String, byte[]> sendMap = new ConcurrentHashMap<String, byte[]>();

	public static final Map<String, String> rspMap = new ConcurrentHashMap<String, String>();

	public static final Map<String, Object> keyMap = new ConcurrentHashMap<String, Object>();

	public static final Map<String, CountDownLatch> countDownMap = new ConcurrentHashMap<String, CountDownLatch>();

	private Object object = new Object();// 实例变量,竞争互斥资源(供获取Socket使用)

	private Thread thread;
	
	private Thread sendThread;
	
	private Thread receiveThread;

	private Map<String, Object> configParams = Configuration.configParams;// 加载配置项

	public static volatile XmcmbcFrontEndConfig config;
	
	/**
	 * 允许运行标识
	 */
	public static volatile boolean canRun = false;
	
	/**
	 * @return the receivedBytes
	 */
	public byte[] getReceivedBytes() {
		return receivedBytes;
	}

	/**
	 * @param receivedBytes
	 *            the receivedBytes to set
	 */
	public void setReceivedBytes(byte[] receivedBytes) {
		this.receivedBytes = receivedBytes;
	}

	public Object getObject() {
		return object;
	}

	private Socket socket = null;// 长连接使用的Socket

	// 最后活跃时间
	private Date lastActiveTime = new Date();

	public Date getLastActiveTime() {
		return lastActiveTime;
	}

	public void setLastActiveTime(Date lastActiveTime) {
		this.lastActiveTime = lastActiveTime;
	}

	// 互斥获取Socket对象
	public Socket getSocket() throws FrontEndException {
		canRun = Boolean.parseBoolean((String) configParams.get("DIFF_CAN_RUN"));
		if(!canRun) {
			reset();
			return null;
		}
		
		synchronized (object) {
			if (socket == null || socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
				socket = initSocket();//保证初始化一次
			}
			//lastActiveTime = new Date();
			return socket;
		}
	}

	// 初始化Socket对象
	private Socket initSocket() throws FrontEndException {
		String ip = (String) configParams.get("CONNECT_HOST_DIFF_IP");// "172.30.4.92"
		int port = Integer.parseInt((String) configParams.get("CONNECT_HOST_DIFF_PORT"));// 9006
		try {
			socket = new Socket(ip, port);
			socket.setTcpNoDelay(true);
			socket.setKeepAlive(true);
			socket.setSoTimeout((int) configParams.get("READ_TIME_OUT"));// 读超时时间60s
		} catch (IOException e) {
			logger.error("民生跨行打开网络连接失败" + "(ip:" + ip + ", port:" + port + ")", e);
			reset();
		}
		
		return socket;
	}

	/**
	 * 检测链接
	 * 
	 * @param heartbeatInterval
	 *            心跳间隔
	 */
	private void checkConnect() {
		if (!canRun) {
			reset();
			return;
		}
		
		try {
			checkSocket();
			int heartInterval = (int) configParams.get("HEART_INTERVAL");// 心跳间隔
			long ss = new Date().getTime() - this.getLastActiveTime().getTime();
			if (ss > heartInterval) {//30s发一次心跳包
				logger.info("民生跨行Socket最后活跃时间:" + DateTimeUtil.format(this.getLastActiveTime(), "yyyy-MM-dd HH:mm:ss"));
				String heartbeatMessage = (String) configParams.get("HEART_BEAT_MESSAGE");// 心跳报文
				String charset = (String) configParams.get("CHARSET");// 字符集
				this.setLastActiveTime(new Date());
				Socket socket = getSocket();
				if (socket != null) {
					OutputStream output = socket.getOutputStream();
					output.write(heartbeatMessage.getBytes(charset));
					output.flush();
					logger.info("民生跨行心跳包" + heartbeatMessage + "发送成功");
				}
			}
		} catch (Exception e) {
			reset();
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	//check互斥获取Socket对象
	private void checkSocket() throws FrontEndException {
		if (!canRun) {
			reset();
			return;
		}
		
		synchronized (object) {
			if (socket == null || socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
				initSocket();//保证初始化一次
			}
		}
	}

	// 发生异常时候关闭Socket
	private void reset() {// 重置，关闭，清空
		logger.info("民生跨行 reset socket....");
		try {
			synchronized (object) {
				if (socket != null) {
					socket.close();// 关闭
					socket = null;// 清空socket
				}
			}
		} catch (IOException e) {
			logger.error("民生跨行关闭socket发生异常:" + e.getMessage());
		} finally {
			socket = null;// 清空socket
		}
	}

	// 一初始化就调用该方法
	@PostConstruct
	public void start() {
		// 启动开关监听
		//startListener();
		//设置启动
		canRun = Boolean.parseBoolean((String) configParams.get("DIFF_CAN_RUN"));
		if(!canRun) {
			return;
		}
		/**
		 * 运行链接检测线程
		 */
		thread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (!Thread.interrupted()) {
					try {
						synchronized (object) {
							checkConnect();
						}
						int heartInterval = (int) configParams.get("HEART_INTERVAL");// 30s
						Thread.currentThread().sleep(heartInterval);
					} catch (InterruptedException e) {
						try {
							reset();
							Thread.currentThread().sleep(5000);// 5s
						} catch (InterruptedException e1) {
							logger.error("民生本行中断异常:" + e1.getMessage());
						}
						logger.error("民生本行获取Socket发生异常:" + e.getMessage());
					}
				}
			}
		});
		thread.start();

		/**
		 * 运行报文发送线程
		 */
		sendThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						if (canRun && getSocket() != null) {
							sendMessage();
						} else {
							int interval = NumberUtils.toInt((String) configParams.get("RUN_CHECK_INTERVAL"), 30) * 1000;// 运行检测间隔，单位：分
							Thread.sleep(interval);
						}
					} catch (Exception e) {
						logger.error(e.getLocalizedMessage(), e);
					}
				}
			}
		});
		sendThread.start();
		/**
		 * 运行报文接收线程
		 */
		receiveThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						if (canRun && getSocket() != null) {
							receiveMessage();
						} else {
							int interval = NumberUtils.toInt((String) configParams.get("RUN_CHECK_INTERVAL"), 30) * 1000;// 运行检测间隔，单位：分
							Thread.sleep(interval);
						}
					} catch (Exception e) {
						logger.error(e.getLocalizedMessage(), e);
					}
				}
			}
		});
		receiveThread.start();
	}

	/**
	 * 发送报文
	 * 
	 * @param SocketDiffHelper
	 */
	private void sendMessage() throws Exception {
		byte[] bytes = null;
		String tranId = null;
		try {
			tranId = sendQueue.take();
			bytes = sendMap.get(tranId);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return;
		}
		if (bytes == null || bytes.length < 1) {
			return;
		}
		try {
			Socket socket = getSocket();
			if (socket != null) {
				logger.info("民生跨行开始发送报文");
				OutputStream os = socket.getOutputStream();
				os.write(bytes);
				os.flush();
				this.setLastActiveTime(new Date());
				logger.info("民生跨行结束发送报文");
			} else {
				logger.info("民生跨行getSocket is null");
				throw new NullPointerException("socket == null");
			}
		} catch (Exception e) {
			logger.error("民生跨行发送报文出现异常\n:{}", e);
			//			this.close();
			try {
				if (tranId != null) {
					synchronized (keyMap.get(tranId)) {
						sendQueue.put(tranId);
					}
				}
			} catch (Exception e1) {
				logger.error(e1.getLocalizedMessage(), e1);
			}
		}
	}

	/**
	 * 接收报文
	 * 
	 * @param SocketDiffHelper
	 */
	private void receiveMessage() throws Exception {
		String charset = (String) configParams.get("CHARSET");// 字符集
		int headLength = NumberUtils.toInt((String) configParams.get("HEAD_LENGTH"), 8);// 报文头长度位数
		int maxSingleLength = NumberUtils.toInt((String) configParams.get("MAX_SINGLE_LENGTH"), 200 * 1024);// 单个报文最大长度，单位：字节
		try {
			Socket socket = getSocket();
			if (socket != null) {
				InputStream in = socket.getInputStream();
				if (in.available() > 0) {
					byte[] bytes = getReceivedBytes();
					if (bytes == null) {
						bytes = new byte[0];
					}
					
					/**
					 * 1、读取报文头
					 */
					if (bytes.length < headLength) {
						byte[] headBytes = new byte[headLength - bytes.length];
						int couter = in.read(headBytes);
						if (couter < 0) {
							reset();
							return;
						}
						bytes = ArrayUtils.addAll(bytes, ArrayUtils.subarray(headBytes, 0, couter));
						if (couter < headBytes.length) {// 未满足长度位数，可能是粘包造成，保存读取到的
							setReceivedBytes(bytes);
							return;
						}
					}
					
					String headMsg = new String(ArrayUtils.subarray(bytes, 0, headLength), charset);
					int bodyLength = NumberUtils.toInt(headMsg);
					if (bodyLength <= 0 || bodyLength > maxSingleLength * 1024) {
						this.reset();
						return;
					}
					
					/**
					 * 2、读取报文体
					 */
					if (bytes.length < headLength + bodyLength) {
						byte[] bodyBytes = new byte[headLength + bodyLength - bytes.length];
						int couter = in.read(bodyBytes);
						if (couter < 0) {
							reset();
							return;
						}
						bytes = ArrayUtils.addAll(bytes, ArrayUtils.subarray(bodyBytes, 0, couter));
						if (couter < bodyBytes.length) {// 未满足长度位数，可能是粘包造成，保存读取到的
							setReceivedBytes(bytes);
							return;
						}
					}
					
					byte[] bodyBytes = ArrayUtils.subarray(bytes, headLength + 276, headLength + bodyLength);
					logger.info("民生跨行接收【支付】返回报文bodyBytes: \n{}", bodyBytes);
					logger.info("民生跨行接收【支付】返回报文bodyBytes: \n" + new String(bodyBytes, charset));
					// 对密文解密
					File privateKeyFile = config.getPrivateKeyFile().getFileVal();
					File publicKeyFile = config.getPublicKeyFile().getFileVal();
					RSAHelper.initKey(privateKeyFile, publicKeyFile, 2048);
					byte[] decryptedBytes = RSAHelper.decryptRSA(bodyBytes, false, charset);
					String decryptedStr = new String(decryptedBytes, charset);
					//logger.info("民生跨行接收【支付】返回报文: \n" + decryptedStr);
					Element root = DomUtil.parseXml(decryptedStr);
					String tranId = DomUtil.getTextTrim(root, "ReqSerialNo");//原交易流水
					if (StringUtils.isNotBlank(tranId)) {
						synchronized (keyMap.get(tranId)) {
							SocketDiffHelper.rspMap.put(tranId, decryptedStr);
							SocketDiffHelper.countDownMap.get(tranId).countDown();
						}
					}
				}
				else {
					Thread.sleep(200);
				}
			}
		} catch (Exception e) {
			logger.error("民生跨行接收报文出现异常\n:{}", e);
			//			this.close();
		}
	}

	public static void main(String[] arg) {
		Object object = null;
		synchronized (object) {
			System.out.println("---------");
		}
	}

	/**
	 * 关闭
	 */
	private void close() {
		if (socket != null) {
			try {
				socket.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		socket = null;
		setLastActiveTime(null);
	}

	public static void put(String tranId, byte[] bytes) throws Exception {
		countDownMap.put(tranId, new CountDownLatch(1));
		keyMap.put(tranId, new Object());
		sendQueue.put(tranId);
		sendMap.put(tranId, bytes);
	}

	public static void clear(String tranId) {
		try {
			synchronized (keyMap.get(tranId)) {
				rspMap.remove(tranId);
				countDownMap.remove(tranId);
				keyMap.remove(tranId);
				sendQueue.remove(tranId);
				sendMap.remove(tranId);
			}
		} catch (Exception e) {

		}
	}

	/**
	 * 停止
	 * @throws FrontEndException 
	 */
	public void stop() throws FrontEndException {
		canRun = false;
		setReceivedBytes(null);// 清除已经保存的粘包块
		try {
			Socket socket = getSocket();
			if (socket != null) {
				socket.close();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		setLastActiveTime(null);
	}

	// 销毁检测线程
	@PreDestroy
	public void destory() {
		logger.info("民生跨行心跳包检测线程关闭....");
		thread.interrupt();// 停止服务之前，强行停止检测线程
		sendThread.interrupt();// 停止服务之前，强行停止发送线程
		receiveThread.interrupt();// 停止服务之前，强行停止接收线程
	}
	
    //telnet
    final static class SystemManagerListener extends Thread {

        private static Logger logger = LoggerFactory.getLogger(SystemManagerListener.class);
        private static String charset = "GBK";
        private static String lineSeparator = "\r\n";

        @Override
        public void run() {
            try {
                runManager();
            } catch (IOException e) {
                logger.error("", e);
            }
        }

        private static void runManager() throws IOException {
        	PropertyReader reader = PropertyReader.getInstance();
        	int port = Integer.parseInt(reader.getPropertyValue("/xmcmbc.properties", "DIFF_RUN_PORT"));
            ServerSocket ss = new ServerSocket(port);
            while (true) {
                try {
                    Socket socket = ss.accept();
                    InputStream is = socket.getInputStream();
                    OutputStream os = socket.getOutputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is, charset));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        if ("disconnect".equalsIgnoreCase(line) || "close".equalsIgnoreCase(line)) {
                            socket.close();
                        } else {
                            try {
                                processOrder(os, line);
                            } catch (Exception e) {
                                e.printStackTrace();
                                println(os, "民生跨行处理出错：" + e.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {

                }
            }
        }

        private static void processOrder(OutputStream os, String line) {
            if ("shutdown".equals(line)) {//FIXME 安全
                logger.warn("民生跨行收到关闭指令");
                println(os, "民生跨行系统正在关闭...");
                canRun = false;
            } else if ("starting".equals(line)) {// 启动
            	logger.warn("民生跨行收到启动指令");
                println(os, "民生跨行系统正在启动...");
            	canRun = true;
            } else {
                println(os, "民生跨行无法识别的命令: " + line);
            }
        }

        private static void println(OutputStream os, String s) {
            try {
                os.write((lineSeparator + s + lineSeparator).getBytes(charset));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void startListener() {
        Thread t = new SystemManagerListener();
        t.setName("banks-system-manager");
        t.setDaemon(true);
        t.start();
    }
}
