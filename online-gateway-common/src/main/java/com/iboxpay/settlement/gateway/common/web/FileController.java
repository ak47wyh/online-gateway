package com.iboxpay.settlement.gateway.common.web;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.iboxpay.settlement.gateway.common.config.Property;

@Controller
@RequestMapping("/manage/file")
public class FileController {

    private final static Logger logger = LoggerFactory.getLogger(FileController.class);
    private final static long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @RequestMapping(value = "file-upload.htm", method = RequestMethod.GET)
    public ModelAndView upload(HttpServletRequest request, @RequestParam(value = "fieldName", required = false) String fieldName, @RequestParam(value = "fileName", required = false) String fileName//已上传的旧的文件名
    ) throws UnsupportedEncodingException {
        ModelAndView mv = new ModelAndView();
        if (fileName != null) fileName = URLDecoder.decode(fileName, "UTF-8");//乱码烦，直接用了两层编码
        mv.addObject("queryString", request.getQueryString() == null ? "" : request.getQueryString());
        mv.addObject("fileName", fileName);
        mv.addObject("fieldName", fieldName);
        mv.setViewName("/views/file-upload");
        return mv;
    }

    @RequestMapping(value = "file-upload.htm", method = RequestMethod.POST)
    public ModelAndView doUpload(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "fieldName", required = false) String fieldName) {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("/views/file-upload");
        mv.addObject("queryString", request.getQueryString() == null ? "" : request.getQueryString());
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        String fileName = null;
        String errorMsg = null;
        if (isMultipart) {
            try {
                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                upload.setFileSizeMax(MAX_FILE_SIZE);
                List fileItems = upload.parseRequest(request);
                Iterator iter = fileItems.iterator();

                while (iter.hasNext()) {
                    FileItem item = (FileItem) iter.next();
                    if (!item.isFormField()) {
                        if (item.getSize() > MAX_FILE_SIZE) {
                            errorMsg = "文件超过" + MAX_FILE_SIZE + "字节, 服务器拒绝接收.";
                            break;
                        }
                        File uploadFile = new File(Property.UPLOAD_FILE_DIR, item.getName());
                        if (uploadFile.exists()) {
                            errorMsg = "文件已存在(建议换文件名)";
                            break;
                        } else {
                            fileName = item.getName();
                            item.write(uploadFile);
                        }
                    }
                }
            } catch (Exception e) {
                errorMsg = e.getMessage();
            }
        } else {
            errorMsg = "请求格式不正确";
        }
        mv.addObject("errorMsg", errorMsg);
        mv.addObject("fileName", fileName);
        mv.addObject("fieldName", fieldName);
        mv.addObject("success", fileName != null ? true : false);
        return mv;
    }

    @RequestMapping(value = "file-delete.htm", method = RequestMethod.GET)
    public String delete(HttpServletRequest request, @RequestParam(value = "fieldName", required = false) String fieldName, @RequestParam(value = "fileName", required = false) String fileName)
            throws Exception {
        if (fileName.indexOf("/") != -1) throw new Exception("非法操作");
        fileName = URLDecoder.decode(fileName, "UTF-8");//乱码烦，直接用了两层编码
        File uploadFile = new File(Property.UPLOAD_FILE_DIR, fileName);
        if (uploadFile.exists()) {
            if (!uploadFile.delete()) {
                logger.warn("删除文件'" + fileName + "'失败.");
            }
        }
        return "redirect:file-upload.htm?fieldName=" + fieldName;
    }
}
