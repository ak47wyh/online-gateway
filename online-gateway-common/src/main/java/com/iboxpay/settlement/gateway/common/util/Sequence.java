package com.iboxpay.settlement.gateway.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.dao.SequenceDao;
import com.iboxpay.settlement.gateway.common.domain.SequenceRange;

/**
 * 流水号生成类.支持定制不同key和不同位数的生成.
 * @author jianbo_chen
 */
@Service
public class Sequence {

    private final static Logger logger = LoggerFactory.getLogger(Sequence.class);

    public enum Type {
        CHAR, NUMBER
    }

    private final static int RANGE_STEP = 50;//每次取多少个流水号
    //流水号可用的范围
    private static ConcurrentHashMap<String, SequenceRange> seqRangeMap = new ConcurrentHashMap<String, SequenceRange>();
    //锁
    private static ConcurrentHashMap<String, Object> locks = new ConcurrentHashMap<String, Object>();

    //system开头的为系统使用的
    private final static String KEY_SYSTEM_NUMBER = "sys.number";
    //system开头的为系统使用的
    private final static String KEY_SYSTEM_CHAR = "sys.char";

    private static long expiredDayTime;//实现按天来滚动流水
    private static SimpleDateFormat dayNumFormatter = new SimpleDateFormat("yyMMdd");
    private static String dayNumPrefix;//如140505
    private static String dayCharPrefix;//天前缀，从2000年开始的天数，如10年天数为3650对应的36进制为2te
    private static long startDayTime;//起始的计数，以天为单位，从2000年1月1日起开始，过一天加1

    private final static String STUFF_STRING = "00000000000000000000000000000000000000";//不够字符数填充的数字
    private final static int radix = 36;

    private final static long MAX_OF_RADIX_36[] =

    new long[] { 0L,
            Long.parseLong("z", radix),//1位
            Long.parseLong("zz", radix),//2位
            Long.parseLong("zzz", radix),//...
            Long.parseLong("zzzz", radix), Long.parseLong("zzzzz", radix), Long.parseLong("zzzzzz", radix), Long.parseLong("zzzzzzz", radix), Long.parseLong("zzzzzzzz", radix),
            Long.parseLong("zzzzzzzzz", radix), Long.parseLong("zzzzzzzzzz", radix), Long.parseLong("zzzzzzzzzzz", radix), Long.parseLong("zzzzzzzzzzzz", radix) };
    private final static int MAX_OF_RADIX_36_LEN = MAX_OF_RADIX_36.length - 1;
    private final static long MAX_OF_RADIX_10[] =

    new long[] { 0L, 9L, 99L, 999L, 9999L, 99999L, 999999L, 9999999L, 99999999L, 999999999L, 9999999999L, 99999999999L, 999999999999L, 9999999999999L, 99999999999999L, };
    private final static int MAX_OF_RADIX_10_LEN = MAX_OF_RADIX_10.length - 1;

    @Resource
    private SequenceDao _sequenceDao;

    static {
        expiredDayTime = getNextExpiredDay();//实现按天来滚动流水
        try {
            startDayTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2000-01-01 00:00:00").getTime();
        } catch (ParseException e) {
            //不可能的
        }
        long now = System.currentTimeMillis();
        dayCharPrefix = getNextDayCharPrefix(now, startDayTime);
        dayNumPrefix = getNextDayNumPrefix(now);
    }

    private static SequenceDao sequenceDao;

    @PostConstruct
    void init() {
        sequenceDao = _sequenceDao;
    }

    private static long genSequence0(String key) {
        Object lock = locks.get(key);
        if (lock == null) {
            lock = new Object();
            Object existLock = locks.putIfAbsent(key, lock);
            if (existLock != null) {//并发，已存在了
                lock = existLock;
            }
        }

        SequenceRange seqRange = seqRangeMap.get(key);
        long nextSeq;

        if (seqRange == null) {//一开始是空的,只有最开始时才会进来
            synchronized (lock) {
                seqRange = seqRangeMap.get(key);
                if (seqRange == null) {
                    seqRange = sequenceDao.getSequenceRange(key, RANGE_STEP);
                    seqRangeMap.put(key, seqRange);
                }
            }
        }
        while (seqRange != null) {
            if ((nextSeq = seqRange.nextSeq()) != -1L) {
                return nextSeq;
            } else {//需要重新取号
                synchronized (lock) {//取号需要互斥
                    SequenceRange maybeNewSeqRange = seqRangeMap.get(key);
                    if (maybeNewSeqRange != seqRange) {//多线程时，另一线程已经批量拿了一次
                        seqRange = maybeNewSeqRange;
                        continue;
                    }
                    SequenceRange newSeqRange = sequenceDao.getSequenceRange(key, RANGE_STEP);
                    seqRangeMap.replace(key, seqRange, newSeqRange);
                }
            }
        }
        return -1;
    }

    /**
     * 下一天的00:00:00
     * @return
     */
    private static long getNextExpiredDay() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, 1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d;
        try {
            d = sdf.parse(sdf.format(c.getTime()));
            return d.getTime();
        } catch (ParseException e) {
            //忽略 
            return -1;
        }
    }

    private static String getNextDayNumPrefix(long time) {
        return dayNumFormatter.format(new Date(time));
    }

    private static String getNextDayCharPrefix(long time, long startDayTime) {
        long day = (time - startDayTime) / (1000 * 3600 * 24);
        if (day < 0) //除非系统时间调乱了，否则不可能小于2000年
            day = 0;
        String dayPrefix = Long.toString(day, radix);
        if (dayPrefix.length() == 3) {
            return dayPrefix;
        }
        if (dayPrefix.length() < 3) {
            return dayPrefix + STUFF_STRING.substring(0, 3 - dayPrefix.length());
        } else {//大于3位的没可能，3位的可以用100多年
            logger.warn("当前系统时间有误.");
            return dayPrefix.substring(0, 3);
        }
    }

    /**
     * 获取流水号
     * @param key : 指定键，你可以自定义键，或者系统自带的键
     * @param bitCount : 多少个字符的流水
     * @return
     */
    public static String genBitsSequence(String key, Type type, int bitCount) {
        if (key == null) throw new IllegalArgumentException("流水号键值为空.");

        long current = System.currentTimeMillis();
        if (current > expiredDayTime) {
            logger.info("流水号前缀更新.");
            dayNumPrefix = getNextDayNumPrefix(current);
            dayCharPrefix = getNextDayCharPrefix(current, startDayTime);
            expiredDayTime = getNextExpiredDay();
        }
        switch (type) {
            case NUMBER: {
                //			if(bitCount < 15)
                //				throw new IllegalArgumentException("获取数字流水号位数不能小于15位.");

                int remain = bitCount - dayNumPrefix.length();
                int seqBitCount = remain;
                if (seqBitCount > MAX_OF_RADIX_10_LEN) {//long最多支持只有12位36进制
                    seqBitCount = MAX_OF_RADIX_10_LEN;
                }
                long seq = genSequence0(key);
                if (seq > MAX_OF_RADIX_10[seqBitCount]) {//一般都不可能超过了
                    seq = seq % MAX_OF_RADIX_10[seqBitCount];
                }
                String seqStr = Long.toString(seq);
                remain = remain - seqStr.length();
                return dayNumPrefix + STUFF_STRING.substring(0, remain) + seqStr;
            }

            default: {
                if (bitCount < 8) throw new IllegalArgumentException("获取流水号位数不能小于8位.");

                int remain = bitCount - dayCharPrefix.length();
                int seqBitCount = remain;
                if (seqBitCount > MAX_OF_RADIX_36_LEN) {//long最多支持只有12位36进制
                    seqBitCount = MAX_OF_RADIX_36_LEN;
                }
                long seq = genSequence0(key);
                if (seq > MAX_OF_RADIX_36[seqBitCount]) {//一般都不可能超过了
                    seq = seq % MAX_OF_RADIX_36[seqBitCount];
                }
                String seqStr = Long.toString(seq, radix);
                remain = remain - seqStr.length();
                return dayCharPrefix + STUFF_STRING.substring(0, remain) + seqStr;
            }
        }
    }

    /**
     * 默认生成一个的10字符的流水号
     * @return
     */
    public static String genSequence() {
        return genSequence(10);
    }

    /**
     * 生成16字符流水号
     * @return
     */
    public static String gen16CharSequence() {
        return genSequence(16);
    }

    /**
     * 生成8字符流水号
     * @return
     */
    public static String gen8CharSequence() {
        return genSequence(8);
    }

    /**
     * 生成18字符流水号
     * @return
     */
    public static String gen18CharSequence() {
        return genSequence(18);
    }

    /**
     * 生成指定位数的流水号
     * @return
     */
    public static String genSequence(int bitCount) {
        return genBitsSequence(KEY_SYSTEM_CHAR, Type.CHAR, bitCount);
    }

    /**
     * 默认生成15位数字流水号
     * @return
     */
    public static String genNumberSequence() {
        return genNumberSequence(15);
    }

    /**
     * 生成数字流水号
     * @param bitCount
     * @return
     */
    public static String genNumberSequence(int bitCount) {
        return genBitsSequence(KEY_SYSTEM_NUMBER, Type.NUMBER, bitCount);
    }

    /**
     * 生成数字流水号
     * @param key : 键
     * @param bitCount : 生成多少位
     * @return
     */
    public static String genNumberSequence(String key, int bitCount) {
        return genBitsSequence(key, Type.NUMBER, bitCount);
    }

    public static void main(String[] args) throws ParseException {
        System.out.println(Long.toString(3650, 36));
        System.out.println(Long.toString(140410, 36));
        System.out.println(Long.valueOf("zzzzzzzzzzzz", 36));
        System.out.println(Long.valueOf("zzzzzz", 36));
        System.out.println(Long.valueOf("zzzzz", 36));
        System.out.println(Long.valueOf("zzz", 36));

        System.out.println(getNextDayCharPrefix(System.currentTimeMillis(), startDayTime));
    }
}
