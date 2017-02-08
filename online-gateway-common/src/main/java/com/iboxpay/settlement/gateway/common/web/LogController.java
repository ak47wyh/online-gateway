package com.iboxpay.settlement.gateway.common.web;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.iboxpay.settlement.gateway.common.SystemManager;
import com.iboxpay.settlement.gateway.common.schedule.AbstractSchedulerJob;
import com.iboxpay.settlement.gateway.common.util.JsonUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

@Controller
@RequestMapping("/manage/log")
public class LogController extends AbstractSchedulerJob {

    private static Logger logger = LoggerFactory.getLogger(LogController.class);

    static boolean isSameDir;

    @RequestMapping(value = "list.htm", method = RequestMethod.GET)
    public ModelAndView list() throws FileNotFoundException, IOException {
        int limit = 300;
        List<FileInfo> logFileList = new LinkedList<FileInfo>();
        File todayFile = SystemManager.getTodayLogDir();
        if (todayFile != null) {
            listLogFiles(todayFile, logFileList);
        }
        File historyDir = SystemManager.getHistoryDir();
        if (historyDir != null && !historyDir.equals(todayFile)) {
            listLogFiles(historyDir, logFileList);
        }
        Collections.sort(logFileList, new Comparator<FileInfo>() {

            @Override
            public int compare(FileInfo o1, FileInfo o2) {
                long r = o1.getLastModified().getTime() - o2.getLastModified().getTime();
                if (r > 0)
                    return -1;
                else if (r < 0)
                    return 1;
                else return 0;
            }
        });
        if (logFileList.size() > 0 && logFileList.size() > limit) logFileList = logFileList.subList(0, limit);
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/views/log/list");
        mv.addObject("logFileList", logFileList);
        return mv;
    }

    private void listLogFiles(File dir, List<FileInfo> logFileList) {
        long MB = 1024 * 1024;
        if (dir.exists()) {
            File[] logFiles = dir.listFiles();
            if (logFiles != null && logFiles.length > 0) for (File logFile : logFiles) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setName(logFile.getName());
                String lengthStr = new BigDecimal(logFile.length()).divide(new BigDecimal(MB), 2, BigDecimal.ROUND_HALF_EVEN).setScale(2).toString();
                fileInfo.setLength(lengthStr + "MB");
                fileInfo.setLastModified(new Date(logFile.lastModified()));
                logFileList.add(fileInfo);
            }
        }
    }

    @RequestMapping(value = "download.htm", method = RequestMethod.GET)
    public void download(HttpServletResponse response, @RequestParam(value = "file", required = true) String file) throws Exception {
        if (file.indexOf("/") != -1) throw new Exception("非法操作");

        File realFile = new File(SystemManager.getTodayLogDir(), file);
        if (!realFile.exists()) realFile = new File(SystemManager.getHistoryDir(), file);

        if (!realFile.exists()) throw new Exception("找不到日志文件");

        FileInputStream fis = new FileInputStream(realFile);
        response.addHeader("Content-Disposition", "attachment;filename=" + new String(file.getBytes()));
        response.addHeader("Content-Length", "" + realFile.length());
        OutputStream out = new BufferedOutputStream(response.getOutputStream());
        response.setContentType("application/octet-stream");
        byte b[] = new byte[2048];
        int len;
        while ((len = fis.read(b)) != -1) {
            out.write(b, 0, len);
        }
        out.flush();
        out.close();
    }

    public final static class FileInfo {

        private String name;
        private String length;
        private Date lastModified;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLength() {
            return length;
        }

        public void setLength(String length) {
            this.length = length;
        }

        public Date getLastModified() {
            return lastModified;
        }

        public void setLastModified(Date lastModified) {
            this.lastModified = lastModified;
        }
    }

    @Override
    public void execute(String params) throws JobExecutionException {
        logger.info("扫描需要删除的过期日志文件...");
        try {
            tryDeleteLogFile(params);
        } catch (Exception e) {
            logger.error("执行删除日志文件异常", e);
        }
    }

    private void tryDeleteLogFile(String params) throws Exception {
        Map map = null;
        Integer daysInt = null;
        if (!StringUtils.isBlank(params)) {
            try {
                map = (Map) JsonUtil.jsonToObject(params, "UTF-8", Map.class);
                daysInt = (Integer) map.get("days");
            } catch (Exception e) {
                //				logger.warn("读取定时参数异常", e);
            }
        }
        Long days = daysInt == null ? 60L : daysInt.longValue();
        long expireTime = days * 24 * 60 * 60 * 1000;
        deleteLogFile(SystemManager.getTodayLogDir(), expireTime);
        deleteLogFile(SystemManager.getHistoryDir(), expireTime);
    }

    private void deleteLogFile(File logDir, long expireTime) throws FileNotFoundException, IOException {
        long now = System.currentTimeMillis();
        if (logDir.exists()) {
            File logFiles[] = logDir.listFiles();
            if (logFiles != null) {
                for (File logFile : logFiles) {
                    long fileTime = Math.abs(now - logFile.lastModified());
                    if (fileTime > expireTime) if (logFile.delete()) logger.info("成功删除日志文件：" + logFile);
                }
            }
        }
    }

    @Override
    public String getConfigDesc() {
        return "每天凌晨定时删除过期日志。“定时运行参数”设置格式为{\"days\":60}，即删除60天前的日志文件。";
    }

    @Override
    public String getJobGroup() {
        return "manage";
    }

    @Override
    public String getJobName() {
        return "LogCleaner";
    }

    @Override
    public String getJobType() {
        return "LogCleaner";
    }

    @Override
    public String getTitle() {
        return "定时删除日志";
    }

    @Override
    public boolean isUniqueConfig() {
        return true;
    }
}
