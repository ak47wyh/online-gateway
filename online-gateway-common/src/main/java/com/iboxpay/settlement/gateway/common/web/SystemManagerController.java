package com.iboxpay.settlement.gateway.common.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLDecoder;
import java.sql.Connection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.iboxpay.settlement.gateway.common.SystemManager;
import com.iboxpay.settlement.gateway.common.dao.PaymentDao;
import com.iboxpay.settlement.gateway.common.trace.BankTraceInfo;
import com.iboxpay.settlement.gateway.common.trace.Trace;

@Controller
@RequestMapping("/manage/system")
public class SystemManagerController {

    private static Logger logger = LoggerFactory.getLogger(SystemManagerController.class);
    @Resource
    SessionFactory sessionFactory;
    @Resource
    PaymentDao paymentDao;

    @RequestMapping(value = "init.htm", method = RequestMethod.GET)
    public ModelAndView init() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/views/system/init");
        try {
            long count = paymentDao.getCountByHQL("select count(ID) from PaymentEntity", null);
            mv.addObject("message", "系统已初始化，无须重复执行.");
            return mv;
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            StringBuilder sb = new StringBuilder();
            Enumeration<URL> enumURLs = SystemManagerController.class.getClassLoader().getResources("/sql");
            while (enumURLs.hasMoreElements()) {
                URL url = enumURLs.nextElement();
                if ("file".equals(url.getProtocol())) {
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    File dir = new File(filePath);
                    File sqlFiles[] = dir.listFiles();
                    for (File sqlFile : sqlFiles) {
                        sb.append(sqlFile.getName()).append("<br/>");
                        logger.info("执行脚本文件：" + sqlFile.getName());
                        try {
                            excuteSqlFile(sqlFile, sessionFactory);
                            sb.append("执行结果: 成功.<br/>");
                        } catch (Exception e) {
                            logger.error(e.getMessage());
                            sb.append("执行结果: 【错误】" + e.getMessage() + "<br/>");
                        }
                    }
                }
            }
            mv.addObject("message", sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            mv.addObject("message", e.getMessage());
        }
        return mv;
    }

    private void excuteSqlFile(File sqlFile, SessionFactory sessionFactory) throws Exception {
        Session session = sessionFactory.openSession();
        try {
            Connection con = session.connection();
            con.setAutoCommit(false);
            java.sql.Statement stm = con.createStatement();
            InputStreamReader reader = new InputStreamReader(new FileInputStream(sqlFile), "UTF-8");
            int count = 0;
            int totalCount = 0;
            StringBuilder sql = new StringBuilder();
            int c;
            char ch;
            while ((c = reader.read()) != -1) {
                ch = (char) c;
                if (ch == ';') {
                    stm.addBatch(sql.toString());
                    totalCount++;
                    if (count++ > 5000) {
                        stm.executeBatch();
                        count = 0;
                    }
                    sql = new StringBuilder();
                } else {
                    sql.append(ch);
                }
            }
            if (sql.toString().trim().length() > 0) {
                stm.addBatch(sql.toString());
                totalCount++;
            }
            stm.executeBatch();
            con.commit();
            System.out.println("总共执行：" + totalCount);
        } finally {
            try {
                session.close();
            } catch (Exception e) {}
        }
    }

    private static String formatTimeMillsStr(Long t) {
        long day = 0;
        long hour = 0;
        long minute = 0;
        long second = 0;

        day = t / (24 * 60 * 60 * 1000);
        t = t % (24 * 60 * 60 * 1000);
        hour = t / (60 * 60 * 1000);
        t = t % (60 * 60 * 1000);
        minute = t / (60 * 1000);
        t = t % (60 * 1000);
        second = t / 1000;
        return day + "天" + hour + "小时" + minute + "分钟" + second + "秒";
    }

    @RequestMapping(value = "stat.htm", method = RequestMethod.GET)
    public ModelAndView stat() {
        StringBuilder sb = new StringBuilder();
        long startTimeMill = new Date().getTime() - SystemManager.SYSTEM_START_TIME.getTime();
        sb.append("系统已运行：" + formatTimeMillsStr(startTimeMill) + "<br/>");
        sb.append("*****************获取统计信息******************<br/>");
        ConcurrentHashMap<String, BankTraceInfo> bankTraceInfos = Trace.getBankTraceInfos();
        if (bankTraceInfos.size() > 0) {
            Iterator<Entry<String, BankTraceInfo>> itr = bankTraceInfos.entrySet().iterator();
            while (itr.hasNext()) {
                Entry<String, BankTraceInfo> entry = itr.next();
                if (entry.getValue() != null) {
                    sb.append(entry.getValue().toString());
                }
            }
        } else {
            sb.append("【暂时没有相关统计信息】<br/>");
        }
        sb.append("***********************************************<br/>");
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/views/system/stat");
        mv.addObject("statInfo", sb.toString().replace("\r\n", "<br/>"));
        return mv;
    }

}
