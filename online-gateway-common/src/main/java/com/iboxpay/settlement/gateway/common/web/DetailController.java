package com.iboxpay.settlement.gateway.common.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.iboxpay.settlement.gateway.common.dao.CommonDao;
import com.iboxpay.settlement.gateway.common.dao.impl.CommonDaoImpl;
import com.iboxpay.settlement.gateway.common.domain.DetailQueryRecordEntity;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.page.PageBean;
import com.iboxpay.settlement.gateway.common.service.DetailService;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

@Controller
@RequestMapping("/manage/detail")
public class DetailController {
	private static Logger logger = LoggerFactory.getLogger(DetailController.class);
    private CommonDao detailQueryRecordDao = CommonDaoImpl.getDao(DetailQueryRecordEntity.class);
    //新增DetailDao
    @Resource
    private DetailService detailService;

    @RequestMapping(value = "list.htm", method = RequestMethod.GET)
    public ModelAndView list() {
        ModelAndView mv = new ModelAndView();
        mv.addObject("today", DateTimeUtil.format(new Date(), "yyyy-MM-dd"));
        mv.setViewName("/views/detail/list");
        return mv;
    }

    @RequestMapping(value = "query-records.htm", method = RequestMethod.GET)
    public ModelAndView queryRecords(@RequestParam(value = "page", required = false) String pageStr, @RequestParam(value = "accNo", required = false) String accNo,
            @RequestParam(value = "beginDate", required = false) String beginDateStr, @RequestParam(value = "endDate", required = false) String endDateStr) throws ParseMessageException {
        int page;
        try {
            page = Integer.parseInt(pageStr);
        } catch (Exception e) {
            page = 1;
        }
        StringBuilder sql = new StringBuilder("from DetailQueryRecordEntity where 1=1");
        List<Object> params = new ArrayList<Object>();
        if (!StringUtils.isBlank(accNo)) {
            sql.append(" and accNo=?");
            params.add(accNo);
        }
        if (!StringUtils.isBlank(beginDateStr)) {
            Date date = DateTimeUtil.parseDate(beginDateStr, "yyyy-MM-dd");
            sql.append(" and detailDay<=?");
            params.add(date);
        }
        if (!StringUtils.isBlank(endDateStr)) {
            Date date = DateTimeUtil.parseDate(endDateStr, "yyyy-MM-dd");
            sql.append(" and detailDay>=?");
            params.add(date);
        }
        sql.append(" order by updateTime desc, detailDay desc");
        PageBean pageBean = detailQueryRecordDao.findPage(page, 20, sql.toString(), params.toArray(new Object[0]));
        ModelAndView mv = new ModelAndView();
        mv.addObject("pageBean", pageBean);
        mv.setViewName("/views/detail/query-records");
        return mv;
    }
    /**
     * 导出交易明细到Excel
     * @return
     */
    @RequestMapping(value = "export.htm", method = RequestMethod.GET)
    public void export(HttpServletRequest request,HttpServletResponse response,
    		@RequestParam(value = "accNo", required = false) String accNo,
    		@RequestParam(value = "beginDate", required = false) String beginDateStr,
    		@RequestParam(value = "endDate", required = false) String endDateStr) throws ParseMessageException{
    	
    	String[] titles = new String[]{"ID","公司账号","对方账号","对方账户名","对方银行","借方金额","贷方金额","余额","交易时间","备注","银行批次流水","用途代码","用途描述","查询更新时间"};
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    	String fileName = format.format(new Date().getTime())+".xls";
    	
    	response.setContentType("application/ms-excel;charset=UTF-8");
    	ServletOutputStream outputStream = null;
    	try {
			response.setHeader("Content-Disposition","attachment;filename="
					.concat(String.valueOf(URLEncoder.encode(fileName,"UTF-8"))));
			outputStream = response.getOutputStream();
			Date beginDate = new SimpleDateFormat("yyyy-MM-dd").parse(beginDateStr.replace("\"", ""));
			Date endDate = new SimpleDateFormat("yyyy-MM-dd").parse(endDateStr.replace("\"", ""));
			detailService.exportExcel(accNo,beginDate,endDate,titles,outputStream);
		}catch (UnsupportedEncodingException e1) {
			logger.error("不支持的字符集"+e1.getMessage());
		}catch (IOException e1) {
			logger.error("IO流异常"+e1.getMessage());
		} catch (ParseException e) {
			logger.error("字符串转日期格式异常"+e.getMessage());
		}finally{
    		try {
    			outputStream.close();
			} catch (IOException e) {
				logger.error("IO流关闭异常"+e.getMessage());
			}
    	}
    }
}
