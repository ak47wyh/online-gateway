package com.iboxpay.settlement.gateway.common.trans.payment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.iboxpay.settlement.gateway.common.dao.PaymentDao;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.service.AccountService;
import com.iboxpay.settlement.gateway.common.task.TaskScheduler;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.TransCode;

/**
 * 支付中断交易恢复。用于系统非正常关闭时交易恢复。
 */
public class PaymentRecover {

    private final static Logger logger = LoggerFactory.getLogger(PaymentRecover.class);

    /**
     * 恢复系统关闭前未提交的交易
     * @param context
     */
    public static void recover(ApplicationContext context) {
        AccountService accountService = (AccountService) context.getBean("accountService");
        PaymentDao paymentDao = (PaymentDao) context.getBean("paymentDao");
        List<Object[]> list = paymentDao.findPaymentByStatus(PaymentStatus.STATUS_INIT);
        if (list.size() > 0) {
            logger.info("发现" + list.size() + "待提交付款，将恢复提交.");
        }
        //key:id, value:银行名称
        Map<Long, String> idBankMap = new HashMap<Long, String>();
        //key:id, value:账号
        Map<Long, AccountEntity> idAccountMap = new HashMap<Long, AccountEntity>();
        //key:批次号,value{key: 银行批次号, value:支付记录ID}
        Map<String, Map<String, List<Long>>> recoverIds = new HashMap<String, Map<String, List<Long>>>();
        for (Object[] propertys : list) {
            Map<String, List<Long>> bankBatchIds = recoverIds.get(propertys[1]);
            if (bankBatchIds == null) {
                bankBatchIds = new HashMap<String, List<Long>>();
                recoverIds.put((String) propertys[1], bankBatchIds);
            }
            List<Long> ids = bankBatchIds.get(propertys[2]);
            if (ids == null) {
                ids = new ArrayList<Long>();
                bankBatchIds.put((String) propertys[2], ids);
            }
            ids.add((Long) propertys[0]);
            idBankMap.put((Long) propertys[0], (String) propertys[3]);
            idAccountMap.put((Long) propertys[0], accountService.getAccountEntity((String) propertys[4]));
        }
        Iterator<Map<String, List<Long>>> itr = recoverIds.values().iterator();
        Map<String, Object> params;
        while (itr.hasNext()) {
            Map<String, List<Long>> bankBatchIds = itr.next();
            Iterator<List<Long>> idItr = bankBatchIds.values().iterator();
            while (idItr.hasNext()) {
                params = new HashMap<String, Object>();
                List<Long> ids = idItr.next();
                AccountEntity accountEntity = idAccountMap.get(ids.get(0));
                if (accountEntity == null) {
                    logger.warn("账号'" + ids.get(0) + "'不存在，与该账号相关的付款无法继续.");
                    continue;
                }
                params.put(PaymentDelegateService.PARAM_PAYMENT_IDS, ids.toArray(new Long[0]));
                TaskScheduler.scheduleTask(TransCode.PAY, accountEntity, idBankMap.get(ids.get(0)), params, true);
            }
        }
    }
}
