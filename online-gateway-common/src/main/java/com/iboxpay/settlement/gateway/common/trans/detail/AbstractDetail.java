package com.iboxpay.settlement.gateway.common.trans.detail;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.exception.PackMessageException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;
import com.iboxpay.settlement.gateway.common.net.ConnectionAdapter;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.util.DateTimeUtil;

/**
 * 交易明细抽象实现
 * @author jianbo_chen
 */
public abstract class AbstractDetail extends ConnectionAdapter implements IDetail {

    private static Logger logger = LoggerFactory.getLogger(AbstractDetail.class);

    @Override
    public boolean isTodayDetailIndependent() {
        return false;//一般都是和历史明细一起的
    }

    private String getDateStr(Date beginDate, Date endDate) {
        String dateStr = "beginDate=" + DateTimeUtil.format(beginDate, "yyyy-MM-dd") + ", endDate=" + DateTimeUtil.format(endDate, "yyyy-MM-dd");
        return dateStr;
    }

    protected String doQueryDetail(String rsqt, Date beginDate, Date endDate, int pageIndex) throws BaseTransException {
        String dateStr = getDateStr(beginDate, endDate);
        try {
            logger.info("发送查询【交易明细】报文(" + dateStr + ", pageIndex=" + pageIndex + ")： \n" + rsqt);
            openConnection();
            OutputStream os = getOutputStream();
            // 发送请求
            send(os, rsqt);

            handleAfterWrite(os);
            // 获取输入流
            InputStream is = getInputStream();
            // 接收报文
            String respStr = read(is);
            logger.info("接收查询【交易明细】返回报文：" + dateStr + ", pageIndex=" + pageIndex + ")： \n" + respStr);

            handleAfterRead(is);

            return respStr;

        } finally {
            closeConnection();
        }
    }

    @Override
    public DetailResult queryHisDetail(AccountEntity accountEntity, Date beginDate, Date endDate, int pageIndex, Map<String, Object> pageInfoMap) throws BaseTransException {
        String rsqt = packHisDetail(accountEntity, beginDate, endDate, pageInfoMap);
        String respStr = doQueryDetail(rsqt, beginDate, endDate, pageIndex);
        DetailResult detailResult = parseHisDetail(accountEntity, beginDate, endDate, respStr, pageInfoMap);
        logger.info("查询返回交易明细条数(" + getDateStr(beginDate, endDate) + ", pageIndex=" + pageIndex + ")："
                + (detailResult == null ? 0 : detailResult.getDetailEntitys() == null ? 0 : detailResult.getDetailEntitys().length));
        return detailResult;
    }

    /**
     * 历史明细查询-报文封装
     * @param accountEntity
     * @param beginDate
     * @param endDate
     * @return
     * @throws PackMessageException
     */
    public abstract String packHisDetail(AccountEntity accountEntity, Date beginDate, Date endDate, Map<String, Object> pageInfoMap) throws PackMessageException;

    /**
     * 历史明细查询-报文解析
     * @param accountEntity
     * @param beginDate
     * @param endDate
     * @param respStr
     * @throws ParseMessageException
     */
    public abstract DetailResult parseHisDetail(AccountEntity accountEntity, Date beginDate, Date endDate, String respStr, Map<String, Object> pageInfoMap) throws ParseMessageException;

    ///////////////////////////////////////以下当日明细/////////////////////////////////////
    @Override
    public DetailResult queryTodayDetail(AccountEntity accountEntity, int pageIndex, Map<String, Object> pageInfoMap) throws BaseTransException {
        Date today = new Date();
        String rsqt = packTodyDetail(accountEntity, pageInfoMap);
        String respStr = doQueryDetail(rsqt, today, today, pageIndex);
        DetailResult detailResult = parseTodyDetail(accountEntity, respStr, pageInfoMap);
        logger.info("查询返回交易明细条数(" + getDateStr(today, today) + ", pageIndex=" + pageIndex + ")："
                + (detailResult == null ? 0 : detailResult.getDetailEntitys() == null ? 0 : detailResult.getDetailEntitys().length));
        return detailResult;
    }

    /**
     * 当日明细查询-报文封装.很少银行是分开的，需要覆盖此方法即可.
     * @param accountEntity
     * @param beginDate
     * @param endDate
     * @return
     * @throws PackMessageException
     */
    public String packTodyDetail(AccountEntity accountEntity, Map<String, Object> pageInfoMap) throws PackMessageException {
        return null;
    }

    /**
     * 当日明细查询-报文解析.很少银行是分开的，需要覆盖此方法即可.
     * @param accountEntity
     * @param beginDate
     * @param endDate
     * @param respStr
     * @throws ParseMessageException
     */
    public DetailResult parseTodyDetail(AccountEntity accountEntity, String respStr, Map<String, Object> pageInfoMap) throws ParseMessageException {
        return null;
    }

    @Override
    public TransCode getTransCode() {
        return TransCode.DETAIL;
    }
}
