package com.iboxpay.settlement.gateway.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作日工具类
 * @author jianbo_chen
 */
public class Weekday {

    //TODO 配置文件配置公众假期 
    //2015.holiday=20150101, 20150102, 20150218, 20150219, 20150220, 20150223, 20150224, 20150406...
    //2015.work=20150104, 20150215, 20150228, 20151010...
    private static String holiday = ".holiday";
    private static String work = ".work";
    private Date date;
    //假期
    private static Map<Integer, MonthDay[]> holidayMap = new HashMap<Integer, MonthDay[]>();
    //上班
    private static Map<Integer, MonthDay[]> workMap = new HashMap<Integer, MonthDay[]>();

    private final static class MonthDay {

        public final int month;
        public final int day;

        public MonthDay(int month, int day) {
            this.month = month;
            this.day = day;
        }
    }

    static {
        //2015放假
        holidayMap.put(2015, parseDateStrArray("20150101, 20150102, 20150218, 20150219, 20150220, 20150223, 20150224, 20150406, "
                + "20150501, 20150622, 20151001, 20151002, 20151005, 20151006, 20151007"));
        //2015上班
        workMap.put(2015, parseDateStrArray("20150104, 20150215, 20150228, 20151010"));
    }

    private static MonthDay[] parseDateStrArray(String confDateStr) {
        String[] dateStrArray = confDateStr.split(",");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        List<MonthDay> dates = new ArrayList<MonthDay>();
        for (String dateStr : dateStrArray) {
            try {
                Date date = sdf.parse(dateStr.trim());
                Calendar c = Calendar.getInstance();
                c.setTime(date);
                dates.add(new MonthDay(c.get(Calendar.MONTH), c.get(Calendar.DATE)));
            } catch (ParseException e) {
                throw new RuntimeException("error date format : " + dateStr);
            }
        }
        return dates.toArray(new MonthDay[0]);
    }

    public Weekday() {
        this(new Date());
    }

    public Weekday(Date date) {
        this.date = date;
    }

    /**
     * 是否工作日
     * @param d
     * @return
     */
    public static boolean isWeekday(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return isWeekday(c);
    }

    /**
     * 返回前一个工作日的日期
     * @return
     */
    public Date getPreviousWeekday() {
        return getWeekday(-1);
    }

    /**
     * 返回前n个工作日的日期
     * @param d : 工作日天数
     * @return
     */
    public Date getPreviousWeekday(int n) {
        if (n < 0) throw new IllegalArgumentException();
        return getWeekday(-n);
    }

    /**
     * 返回下一个工作日的日期
     * @param d : 工作日天数
     * @return
     */
    public Date getNextWeekday() {
        return getWeekday(1);
    }

    /**
     * 返回下n个工作日的日期
     * @param d : 工作日天数
     * @return
     */
    public Date getNextWeekday(int n) {
        if (n < 0) throw new IllegalArgumentException();
        return getWeekday(n);
    }

    /**
     * 获取前/后n个工作日
     * @param n
     * @return
     */
    public Date getWeekday(int n) {
        if (n == 0) return this.date;

        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int num = Math.abs(n);
        int op = n > 0 ? 1 : -1;
        while (num > 0) {
            c.add(Calendar.DATE, op);
            if (isWeekday(c)) num--;
        }
        return c.getTime();
    }

    //是否上班日
    private static boolean isWeekday(Calendar c) {
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DATE);
        MonthDay[] works = workMap.get(year);
        if (works != null && works.length > 0) {//可能尚未配置
            for (MonthDay work : works) {
                if (work.month == month && work.day == day) //是工作日，直接返回
                    return true;
            }
        }
        MonthDay[] holidays = holidayMap.get(year);
        if (holidays != null && holidays.length > 0) {//可能尚未配置
            for (MonthDay holiday : holidays) {
                if (holiday.month == month && holiday.day == day) //是假期，直接返回
                    return false;
            }
        }
        int week = c.get(Calendar.DAY_OF_WEEK);
        if (week >= 2 && week <= 6) //周一 ~ 周五
            return true;

        return false;
    }

    public static void main(String[] args) {
        Weekday weekday = new Weekday();
        System.out.println(weekday.getPreviousWeekday(1));
    }
}
