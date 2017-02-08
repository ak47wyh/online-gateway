package com.iboxpay.settlement.gateway.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.SystemPropertyUtils;

import com.iboxpay.settlement.gateway.common.config.ConfPropertyManager;
import com.iboxpay.settlement.gateway.common.schedule.ScheduleCenter;
import com.iboxpay.settlement.gateway.common.task.TaskScheduler;
import com.iboxpay.settlement.gateway.common.trace.BankTraceInfo;
import com.iboxpay.settlement.gateway.common.trace.Trace;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;
import com.iboxpay.settlement.gateway.common.trans.IAccountTransComponentSelector;
import com.iboxpay.settlement.gateway.common.trans.payment.PaymentRecover;
import com.iboxpay.settlement.gateway.common.util.PropertyReader;
import com.iboxpay.settlement.gateway.common.util.StringUtils;
import com.iboxpay.settlement.gateway.common.web.BankTransController;

/**
 * 系统开关
 * @author jianbo_chen
 */
@Service
public class SystemManager implements ApplicationListener {

    private static Logger logger = LoggerFactory.getLogger(SystemManager.class);

    static ApplicationContext context;
    /**系统启动时间**/
    public final static Date SYSTEM_START_TIME = new Date();

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            if (context == null) {//不支持重新加载
                context = ((ContextRefreshedEvent) event).getApplicationContext();
                start();
            }
        } else if (event instanceof ContextClosedEvent) {
            stop(false);
        }
    }

    public static ApplicationContext getSpringContext() {
        return context;
    }

    private static synchronized void start() {
        starting();
        //		try {
        //			registerTransactionTimeoutListener();
        //		} catch (ProxoolException e) {
        //			logger.warn("注册proxool事务超时处理监听器失败.", e);
        //		}
        System.setProperty("org.apache.el.parser.SKIP_IDENTIFIER_CHECK", "true");//tomcat 7的EL表达式会检查java保留关键字，导致表达式{xxx.class.name}失败
        logger.info("正在初始化银行组件...");
        BankTransComponentManager.init();
        //2015-5-29
        BankTransComponentManager.setAccountTransComponentSelector((IAccountTransComponentSelector)context.getBean("accountTransComponentSelector"));
        ConfPropertyManager.init();
        logger.info("所有银行组件初始化完毕.");
        startListener();
        logger.info("管理服务已启动.");
        try {
            PaymentRecover.recover(context);
        } catch (Exception e) {
            logger.warn("尝试恢复中断交易时异常：" + e.getMessage(), e);
        }
        running();
        try {
            TaskScheduler.start();//里面要依赖running状态
        } catch (Exception e) {
            logger.warn("启动调度服务时异常：" + e.getMessage(), e);
        }
        logger.info("调度服务已启动.");
        ScheduleCenter.start();
        logger.info("定时任务已启动.");
        logger.info("系统已启动.");
    }

    /*
     * <<<在生产环境中出现的问题>>>：应用变得特别慢，支付插入很慢，只插入部分就超时，proxool的HouseKeeper会自动清除连接，即关闭数据库物理连接。
     * 而关闭物理连接时，orcle驱动又会自动提交已执行的语句，会造成数据的不一致。
     * <<<处理方法>>>：超时的连接先被执行回滚，即已经执行过的语句不会被提交，最后才由proxool关闭连接。
     */
    //	private static void registerTransactionTimeoutListener()
    //			throws ProxoolException {
    //		ProxoolFacade.addConnectionListener("banksgateway", new ConnectionListenerIF() {
    //			public void onFail(String command, Exception exception) {}
    //			public void onExecute(String command, long elapsedTime) {}
    //			public void onBirth(Connection connection) throws SQLException {}
    //			public void onDeath(Connection connection, int reasonCode)throws SQLException {
    //				logger.info("回收数据库连接资源，reasonCode="+reasonCode);
    //				connection.rollback(); 
    //			}
    //		});
    //	}

    private static synchronized void stop(boolean existVm) {
        stopping();
        //停止服务入口
        ((BankTransController) context.getBean("bankTransController")).stop();
        TaskScheduler.stop();
        ScheduleCenter.stop();
        stopped();
        if (existVm) System.exit(0);
    }

    public enum AppStatus {
        starting("正在启动"), running("正在运行"), stopping("正在停止"), stopped("已停止");

        final String message;

        private AppStatus(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    private static volatile AppStatus appStatus = AppStatus.stopped;

    public static void starting() {
        appStatus = AppStatus.starting;
    }

    public static void running() {
        appStatus = AppStatus.running;
    }

    public static void stopping() {
        appStatus = AppStatus.stopping;
    }

    public static void stopped() {
        appStatus = AppStatus.stopped;
    }

    public static boolean isStarting() {
        return appStatus == AppStatus.starting;
    }

    public static boolean isRunning() {
        return appStatus == AppStatus.running;
    }

    public static boolean isStopping() {
        return appStatus == AppStatus.stopping;
    }

    public static boolean isStopped() {
        return appStatus == AppStatus.stopped;
    }

    public static AppStatus getCurrentStatus() {
        return appStatus;
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
        	int port = Integer.parseInt(reader.getPropertyValue("/socket.properties", "RUN_MANAGER_PORT"));
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
                                println(os, "处理出错：" + e.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {

                }
            }
        }

        private static void processOrder(OutputStream os, String line) {
            if ("stat".equals(line) || "netstat".equals(line)) {
                println(os, "*****************获取统计信息******************" + lineSeparator);
                ConcurrentHashMap<String, BankTraceInfo> bankTraceInfos = Trace.getBankTraceInfos();
                if (bankTraceInfos.size() > 0) {
                    Iterator<Entry<String, BankTraceInfo>> itr = bankTraceInfos.entrySet().iterator();
                    while (itr.hasNext()) {
                        Entry<String, BankTraceInfo> entry = itr.next();
                        if (entry.getValue() != null) {
                            println(os, entry.getValue().toString());
                        }
                    }
                } else {
                    println(os, "【暂时没有相关统计信息】");
                }
                println(os, "***********************************************");
            } else if ("shutdown".equals(line)) {//FIXME 安全
                logger.warn("收到关闭指令");
                println(os, "系统正在关闭...");
                SystemManager.stop(true);
            } else {
                println(os, "无法识别的命令: " + line);
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

    /**
     * 执行关闭
     */
    public static void shutdown() {
        stopping();
        //
        stopped();
    }

    private static File todayDir;
    private static File historyDir;
    private static boolean hasInitLogFile;

    private static void initLogFile() throws FileNotFoundException, IOException {
        String todayDirStr = null, historyDirStr = null;
        String resolvedLocation = SystemPropertyUtils.resolvePlaceholders("classpath:logBack.xml");
        URL url = ResourceUtils.getURL(resolvedLocation);
        InputStream fis = url.openStream();
        StringBuilder sb = new StringBuilder();
        int ch;
        while ((ch = fis.read()) != -1) {
            sb.append((char) ch);
        }
        String logConfStr = sb.toString();
        Pattern p = Pattern.compile("(?im)<File>([^<>]+)</File>");
        Matcher m = p.matcher(logConfStr);
        if (m.find()) {
            todayDirStr = m.group(1);
        }
        if (todayDirStr != null) {
            todayDirStr = todayDirStr.trim();
            logger.info("当日日志路径：" + todayDirStr);
            if (!StringUtils.isBlank(todayDirStr)) todayDirStr = new File(todayDirStr).getParent();
        }

        p = Pattern.compile("(?im)<FileNamePattern>([^<>]+)</FileNamePattern>");
        m = p.matcher(logConfStr);
        if (m.find()) {
            historyDirStr = m.group(1);
            logger.info("历史日志路径：" + historyDirStr);
        }
        if (historyDirStr != null) {
            historyDirStr = historyDirStr.trim();
            if (!StringUtils.isBlank(historyDirStr)) historyDirStr = new File(historyDirStr).getParent();
        }
        if (todayDirStr != null) todayDir = new File(todayDirStr);
        if (historyDirStr != null) historyDir = new File(historyDirStr);

        hasInitLogFile = true;
    }

    /**
     * 获取当日日志文件夹
     * @return
     */
    public static File getTodayLogDir() {
        if (!hasInitLogFile) {
            try {
                initLogFile();
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return todayDir;
    }

    /**
     * 获取历史日志文件夹
     * @return
     */
    public static File getHistoryDir() {
        if (!hasInitLogFile) {
            try {
                initLogFile();
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return historyDir;
    }
}
