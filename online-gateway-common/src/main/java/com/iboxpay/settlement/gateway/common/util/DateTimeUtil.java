package com.iboxpay.settlement.gateway.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;

public class DateTimeUtil {

    public final static Date addDay(Date dDate, int addDayNum) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dDate);
        cal.add(Calendar.DATE, addDayNum);
        Date result = cal.getTime();
        return result;
    }

    public final static String format(Date date, String formatStr) {
        SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
        return sdf.format(date);
    }

    /** 
     * 获取现在时间 
     * 
     * @return 返回时间类型 yyyy-MM-dd HH:mm:ss 
     */
    public final static String getCurrentTime() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    public final static Date truncateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return sdf.parse(sdf.format(date));
        } catch (ParseException e) {
            return date;
        }
    }

    public final static String getToday(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }

    /** 
     * 格式化时间
     * 
     * @return 返回时间类型 type 
     */
    public static String getTimeMillisY(Date date, String type) {
        SimpleDateFormat sdf = new SimpleDateFormat(type);
        String str = sdf.format(date);
        return str;
    }

    public static Date parseDate(String dateStr, String format) throws ParseMessageException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            throw new ParseMessageException(e);
        }
    }

    /**
     * 校验在系统日期之前的一年内
     * 
     * @return Boolean
     */
    public static Boolean withInOneYear(Date date) {
        Date dt = new Date();
        Calendar time = Calendar.getInstance();
        time.setTime(dt);
        time.add(Calendar.YEAR, -1);//日期减1年
        Date dt1 = time.getTime();
        return date.after(dt1) && date.before(dt);
    }
}
