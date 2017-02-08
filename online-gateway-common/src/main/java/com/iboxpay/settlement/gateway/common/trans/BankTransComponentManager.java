package com.iboxpay.settlement.gateway.common.trans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.iboxpay.settlement.gateway.common.IBankProfile;
import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.exception.BankComponentException;
import com.iboxpay.settlement.gateway.common.trans.payment.IPayment;
import com.iboxpay.settlement.gateway.common.trans.refund.IRefundPayment;
import com.iboxpay.settlement.gateway.common.trans.reverse.IReversePayment;
import com.iboxpay.settlement.gateway.common.trans.verify.IAccountVerify;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

/**
 * 银行交易组件管理
 * @author jianbo_chen
 */
@Service
public class BankTransComponentManager {

    private final static Logger logger = LoggerFactory.getLogger(BankTransComponentManager.class);

    private static Map<String, IBankTrans[]> bankTransMap;//key: bankName
    private static Map<String, ITransDelegate> transDelegateMap;//key: transCode, value: transImpl
    private static Map<String, IBankProfile> bankProfileMap;//key: bank, value: BankProfile
    private static Map<String, String> bankPackageMap;//
    /**2015-5-29 如果账号开启了接口定制，则使用定制的顺序*/
    private static IAccountTransComponentSelector ACCOUNT_TRANS_COMPONENT_SELECTOR;
    //spring无法注入静态字段,使用变通方式
    @Resource
    private ApplicationContext _context;

    private static ApplicationContext context;

    @PostConstruct
    void postConstruct() {
        context = this._context;
    }
    public static void setAccountTransComponentSelector(IAccountTransComponentSelector accountTransComponentSelector){
        ACCOUNT_TRANS_COMPONENT_SELECTOR = accountTransComponentSelector;
    }
    /**
     * 2015-5-29 如果账号开启了接口定制，则使用定制的顺序。
     * @param accountEntity
     * @param bankName
     * @param bankTransClass
     * @return
     */
    public static IBankTrans[] getBankComponent(AccountEntity accountEntity, String bankName, Class<? extends IBankTrans> bankTransClass) {
           if(accountEntity != null && accountEntity.isTransConfigEnabled()){//启用
               if(ACCOUNT_TRANS_COMPONENT_SELECTOR == null){
                   logger.error("找不到交易接口选择器, 账号=" + accountEntity.getAccNo());
                   return null;
               }
               return ACCOUNT_TRANS_COMPONENT_SELECTOR.select(accountEntity, bankName, bankTransClass);
           }else{
               return getBankComponent(bankName, bankTransClass);
           }
    }
    /**
     * 根据银行名称查找某类组件.
     * (使用包名，把BankProfile与银行组件间建立联系.)
     * @param bankName
     * @param bankTransClass
     * @return
     */
    public static IBankTrans[] getBankComponent(String bankName, Class<? extends IBankTrans> bankTransClass) {
        init();
        IBankTrans[] bankTransArray = bankTransMap.get(bankName);
        List<IBankTrans> foundBankTransList = new ArrayList<IBankTrans>();
        for (IBankTrans bankTrans : bankTransArray) {
            if (bankTransClass.isAssignableFrom(bankTrans.getClass())) foundBankTransList.add(bankTrans);
        }
        if (foundBankTransList.size() > 1 && bankTransClass == IPayment.class) {
            Collections.sort(foundBankTransList, new Comparator<IBankTrans>() {//按顺序排支付组件.

                        @Override
                        public int compare(IBankTrans payTrans1, IBankTrans payTrans2) {
                            int r = ((IPayment) payTrans1).navigate().getPriority() - ((IPayment) payTrans2).navigate().getPriority();
                            if (r < 0)
                                return 1;
                            else if (r > 0)
                                return -1;
                            else return 0;
                        }
                    });
        }
        return foundBankTransList.toArray(new IBankTrans[0]);
    }

    /**
     * 由transCode获取银行业务组件
     * @param bankName
     * @param transCode
     * @return
     */
    public static IPayment getPaymentByTransCode(String bankName, String transCode) {
        init();
        IBankTrans[] bankTransArray = bankTransMap.get(bankName);
        if (bankTransArray != null) {
            for (IBankTrans bankTrans : bankTransArray) {
                if (bankTrans instanceof IPayment && bankTrans.getBankTransCode().equals(transCode)) {
                    return (IPayment) bankTrans;
                }
            }
        }
        return null;
    }
    
    

    /**
     * 由transCode获取银行业务组件
     * @param bankName
     * @param transCode
     * @return
     */
    public static IAccountVerify getAccountVerifyByTransCode(String bankName, String transCode) {
        init();
        IBankTrans[] bankTransArray = bankTransMap.get(bankName);
        if (bankTransArray != null) {
            for (IBankTrans bankTrans : bankTransArray) {
                if (bankTrans instanceof IAccountVerify && bankTrans.getTransCode().getCode().equals(transCode)) {
                    return (IAccountVerify) bankTrans;
                }
            }
        }
        return null;
    }
    

    public static IRefundPayment getRefundPaymentByTransCode(String bankName, String transCode){
        init();
        IBankTrans[] bankTransArray = bankTransMap.get(bankName);
        if (bankTransArray != null) {
            for (IBankTrans bankTrans : bankTransArray) {
                if (bankTrans instanceof IRefundPayment && bankTrans.getBankTransCode().equals(transCode)) {
                    return (IRefundPayment) bankTrans;
                }
            }
        }
        return null;
    }
    
    //获取冲正组件实现集合
    public static IReversePayment getReversePaymentByTransCode(String bankName, String transCode){
        init();
        IBankTrans[] bankTransArray = bankTransMap.get(bankName);
        if (bankTransArray != null) {
            for (IBankTrans bankTrans : bankTransArray) {
                if (bankTrans instanceof IReversePayment && bankTrans.getBankTransCode().equals(transCode)) {
                    return (IReversePayment) bankTrans;
                }
            }
        }
        return null;
    }
    
    //缓存所有银行的实现业务组件
    private static void initBankComponents() {
        logger.info("init BankComponents...");
        Map<String, List<IBankTrans>> _bankTransMap = new HashMap<String, List<IBankTrans>>();
        Iterator<Map.Entry<String, IBankProfile>> bankProfileMapItr = bankProfileMap.entrySet().iterator();
        Map.Entry<String, IBankProfile> bankProfileEntry;
        //初始化 银行与包名 对应关系 
        Map<String, String> _bankPackageMap = new HashMap<String, String>();
        while (bankProfileMapItr.hasNext()) {
            bankProfileEntry = bankProfileMapItr.next();
            String bankBasePackage = bankProfileEntry.getValue().getBankBasePackage();
            if (StringUtils.isBlank(bankBasePackage)) {
                throw new BeanInitializationException("【代码错误】" + bankProfileEntry.getValue().getClass().getName() + "的getBankBasePackage()方法未正确实现.");
            }
            if (bankBasePackage.charAt(bankBasePackage.length() - 1) != '.') {
                bankBasePackage = bankBasePackage + ".";//加上.号才严谨
            }
            _bankPackageMap.put(bankProfileEntry.getKey(), bankBasePackage);
            _bankTransMap.put(bankProfileEntry.getKey(), new ArrayList<IBankTrans>());
        }
        //查找银行组件实现
        Map<String, ? extends IBankTrans> bankTransBeansMap = context.getBeansOfType(IBankTrans.class);
        if (bankTransBeansMap != null) {
            Iterator<? extends IBankTrans> itr = bankTransBeansMap.values().iterator();
            while (itr.hasNext()) {
                IBankTrans bankTrans = itr.next();
                String bankName = getBankNameByPackage(_bankPackageMap, bankTrans.getClass().getName());
                if (bankName == null) throw new BankComponentException("获取组件【" + bankTrans.getClass().getName() + "】的对应的银行名称出错，可能是该银行实现BankProfile未设置@Service。");
                _bankTransMap.get(bankName).add(bankTrans);
            }
        }
        Map<String, IBankTrans[]> bankTransMap2 = new HashMap<String, IBankTrans[]>();
        Entry<String, List<IBankTrans>> bankTransEntry;
        for (Iterator<Map.Entry<String, List<IBankTrans>>> itr = _bankTransMap.entrySet().iterator(); itr.hasNext();) {
            bankTransEntry = itr.next();
            bankTransMap2.put(bankTransEntry.getKey(), bankTransEntry.getValue().toArray(new IBankTrans[0]));
            logger.info("bank[" + bankTransEntry.getKey() + "], BankComponents[" + bankTransEntry.getValue().size() + "]");
        }
        bankPackageMap = Collections.unmodifiableMap(_bankPackageMap);
        bankTransMap = Collections.unmodifiableMap(bankTransMap2);
    }

    private static String getBankNameByPackage(Map<String, String> bankPackageMap, String pkg) {
        Entry<String, String> bankPackageEntry;
        Iterator<Entry<String, String>> bankPackageItr = bankPackageMap.entrySet().iterator();
        while (bankPackageItr.hasNext()) {
            bankPackageEntry = bankPackageItr.next();
            if (pkg.startsWith(bankPackageEntry.getValue())) {//使用包名，把BankProfile与银行组件间建立联系.
                return bankPackageEntry.getKey();
            }
        }
        return null;
    }

    /**
     * 由包名获取银行名称
     * @param pkg
     * @return
     */
    public static String getBankNameByPackage(String pkg) {
        init();
        return getBankNameByPackage(bankPackageMap, pkg);
    }

    //扫描并缓存BankProfile
    private static void initBankProfile() {
        logger.info("init BankProfile...");
        Map<String, IBankProfile> _bankProfileMap = new HashMap<String, IBankProfile>();
        Map<String, IBankProfile> bankProfiles = context.getBeansOfType(IBankProfile.class);
        if (bankProfiles != null) {
            IBankProfile bankProfile;
            for (Iterator<IBankProfile> itr = bankProfiles.values().iterator(); itr.hasNext();) {
                bankProfile = itr.next();
                _bankProfileMap.put(bankProfile.getBankName(), bankProfile);
                logger.info("found bank: bankName[" + bankProfile.getBankName() + "], bankFullName[" + bankProfile.getBankFullName() + "]");
            }
        }
        logger.info("total [" + bankProfiles.size() + "] banks found.");
        bankProfileMap = Collections.unmodifiableMap(_bankProfileMap);
    }

    private static void initTransMap() {
        logger.info("init TransMap...");
        Map<String, ITransDelegate> beans = context.getBeansOfType(ITransDelegate.class);
        Map<String, ITransDelegate> _transDelegateMap = new HashMap<String, ITransDelegate>();
        if (beans != null && beans.values() != null) {
            Iterator<ITransDelegate> itr = beans.values().iterator();
            ITransDelegate trans;
            while (itr.hasNext()) {
                trans = itr.next();
                _transDelegateMap.put(trans.getTransCode().getCode(), trans);
                logger.info(trans.getTransCode() + " : transDelegate[" + trans.getClass().getName() + "]");
            }
        }
        transDelegateMap = Collections.unmodifiableMap(_transDelegateMap);
    }

    public static void init() {
        if (bankProfileMap == null) {
            synchronized (BankTransComponentManager.class) {
                if (bankProfileMap == null) {//double check here
                    initBankProfile();
                    initBankComponents();
                    initTransMap();
                    logger.info("BankTransComponent init finish.");
                }
            }
        }
    }

    /***
     * 获取银行业务组件实例.
     * @param componentImplClass : 必须为实现类
     * @return
     */
    @SuppressWarnings("unchecked")
    public static IBankTrans getBankComponent(Class<? extends IBankTrans> componentImplClass) {
        return context.getBean(componentImplClass);
    }

    /**
     * 获取
     * @param bankName
     * @return
     */
    public static IBankProfile getBankProfile(String bankName) {
        init();
        IBankProfile bankProfile = bankProfileMap.get(bankName);
        if (bankProfile != null) {
            return bankProfile;
        }
        return null;
    }

    /**
     * 查找业务入口
     * @param transCode
     * @return
     */
    public static ITransDelegate getTransDelegate(String transCode) {
        init();
        return transDelegateMap.get(transCode);
    }

    /**
     * 获取所有银行的实现
     * @return
     */
    public static IBankProfile[] getBankProfiles() {
        Map<String, IBankProfile> bpMap = context.getBeansOfType(IBankProfile.class);
        if (bpMap != null) {
            return bpMap.values().toArray(new IBankProfile[0]);
        } else {
            throw new NoSuchBeanDefinitionException("找不到银行实现.");
        }
    }

    /**
     * 获取某银行的扩展账号对象
     * @param bankName
     * @return
     */
    public static AccountEntity getAccountEntityInstance(String bankName) {
        IBankProfile bankProfile = getBankProfile(bankName);
        if (bankProfile != null) {
            AccountEntity account;
            Class<? extends AccountEntity> extAccountEntityClass = bankProfile.getExtAccountEntityClass();
            if (extAccountEntityClass != null) {
                try {
                    account = extAccountEntityClass.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("create new instance for '" + extAccountEntityClass.getName() + "' error", e);
                }
            } else {
                account = new AccountEntity();
            }
            account.setBankName(bankProfile.getBankName());
            account.setBankFullName(bankProfile.getBankFullName());
            return account;
        } else {
            throw new RuntimeException("bank not found : " + bankName);
        }
    }

    /**
     * 获取某银行的前置机配置实例
     * @param bankName
     * @return
     */
    public static FrontEndConfig getFrontEndConfigInstance(String bankName) {
        IBankProfile bankProfile = getBankProfile(bankName);
        if (bankProfile != null) {
            Class<? extends FrontEndConfig> frontEndConfigClass = bankProfile.getFrontEndConfigClass();
            if (frontEndConfigClass != null) {
                try {
                    FrontEndConfig instance = frontEndConfigClass.newInstance();
                    instance.setBankName(bankProfile.getBankName());
                    instance.setBankFullName(bankProfile.getBankFullName());
                    return instance;
                } catch (Exception e) {
                    throw new RuntimeException("create new instance for '" + frontEndConfigClass.getName() + "' error", e);
                }
            }
            return null;
        } else {
            throw new RuntimeException("bank not found : " + bankName);
        }
    }
}
