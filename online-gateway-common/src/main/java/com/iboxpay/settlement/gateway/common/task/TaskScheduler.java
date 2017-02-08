package com.iboxpay.settlement.gateway.common.task;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.settlement.gateway.common.Constant;
import com.iboxpay.settlement.gateway.common.IBankProfile;
import com.iboxpay.settlement.gateway.common.SystemManager;
import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.config.SystemConfig;
import com.iboxpay.settlement.gateway.common.dao.CommonDao;
import com.iboxpay.settlement.gateway.common.dao.FrontEndDao;
import com.iboxpay.settlement.gateway.common.dao.impl.CommonDaoImpl;
import com.iboxpay.settlement.gateway.common.domain.AccountEntity;
import com.iboxpay.settlement.gateway.common.domain.AccountFrontEndBindingEntity;
import com.iboxpay.settlement.gateway.common.inout.CommonResultModel;
import com.iboxpay.settlement.gateway.common.trace.Trace;
import com.iboxpay.settlement.gateway.common.trans.BankTransComponentManager;
import com.iboxpay.settlement.gateway.common.trans.ITransDelegate;
import com.iboxpay.settlement.gateway.common.trans.TransCode;
import com.iboxpay.settlement.gateway.common.trans.fetest.FrontEndTestDelegateService;

/**
 * 任务调度器
 * @author jianbo_chen
 */
public class TaskScheduler {

    private static Logger logger = LoggerFactory.getLogger(TaskScheduler.class);

    //key:bank, value:银行待处理的数据
    private final static ConcurrentHashMap<String, BankTaskResource> waitingResourceMap = new ConcurrentHashMap<String, BankTaskResource>();
    //待处理数据的读写锁。调度线程用写锁，添加待处理数据用写锁，处理线程用读锁（可以并发）。
    private static final ReentrantLock taskLock = new ReentrantLock();
    private static final Condition taskSignal = taskLock.newCondition();//如果没有符合条件的任务，则等待
    //	private static final Lock rLock = rwLock.readLock();//多个处理线程使用
    //	private static final Lock wLock = rwLock.writeLock();//调度线程或者外部请求线程使用

    //就绪队列，可以处理的任务，已申请到前置机资源
    private final static PriorityBlockingQueue<TaskParam> readyQueue = new PriorityBlockingQueue<TaskParam>();
    private static ExecutorService threadPool;
    private static AtomicInteger threadPoolPaymentCounter = new AtomicInteger(0);//用于辅助关闭工作线程(支付线程不要随便关闭，其他线程可以直接关闭)
    private final static Condition blackListSignal = taskLock.newCondition();//黑名单的信号.如果产生新的黑名单，则通知探测线程进行测试.

    /**
     * 设置并发数量
     * @param feConfig
     * @param concurrentNum
     */
    public static void addFrontEnd(FrontEndConfig feConfig) {
        taskLock.lock();
        try {
            if (feConfig.getId() <= 0) throw new IllegalArgumentException("无效的前置机配置：缺少ID.");

            if (feConfig.getBankName() == null) throw new IllegalArgumentException("无效的前置机配置：缺少银行信息.");

            BankTaskResource taskResource = waitingResourceMap.get(feConfig.getBankName());
            if (taskResource == null) {
                taskResource = new BankTaskResource(feConfig.getBankName());
                waitingResourceMap.put(feConfig.getBankName(), taskResource);
            }
            taskResource.installFrontEndConfig(feConfig);

            taskSignal.signalAll();//添加了新的前置机，可能可以满足运行条件。
        } finally {
            taskLock.unlock();
        }
    }

    /**
     * 删除前置机
     * @param feConfig
     */
    public static void deleteFrontEnd(FrontEndConfig feConfig) {
        taskLock.lock();
        try {
            if (feConfig.getId() <= 0) throw new IllegalArgumentException("无效的前置机配置：缺少ID.");

            if (feConfig.getBankName() == null) throw new IllegalArgumentException("无效的前置机配置：缺少银行信息.");

            logger.info("禁用/删除前置机：" + feConfig);
            BankTaskResource taskResource = waitingResourceMap.get(feConfig.getBankName());
            if (taskResource != null) {
                taskResource.uninstallFrontEndConfig(feConfig);
            }
        } finally {
            taskLock.unlock();
        }
    }

    /**
     * 账号绑定到指定前置机
     * @param feConfig
     * @param accNo
     */
    public static void bind(String bankName, int frontEndId, String accNo) {
        taskLock.lock();
        try {
            BankTaskResource taskResource = waitingResourceMap.get(bankName);
            if (taskResource != null) {
                taskResource.bind(accNo, frontEndId);
            }
            taskSignal.signalAll();
        } finally {
            taskLock.unlock();
        }
    }

    /**
     * 解除账号与前置绑定
     * @param feConfig
     * @param accNo
     */
    public static void unbind(String bankName, int frontEndId, String accNo) {
        taskLock.lock();
        try {
            BankTaskResource taskResource = waitingResourceMap.get(bankName);
            if (taskResource != null) {
                taskResource.unbind(accNo, frontEndId);
            }
            taskSignal.signalAll();
        } finally {
            taskLock.unlock();
        }
    }

    //2015-7-24 by jianbo_chen : 添加priority任务优先级，支持T+0优先
    /**
     * 安排执行任务
     * @param context
     * @param taskParam : 封装任务参数
     * @param asyn : 是否异步
     */
    public static CommonResultModel scheduleTask(TransCode transCode, AccountEntity accountEntity, String bank, Map<String, Object> params, boolean asyn) {
        return scheduleTask(transCode, accountEntity, bank, params, null, 0, asyn);//默认优先为最小0
    }

    //2015-7-24 by jianbo_chen : 添加priority任务优先级，支持T+0优先
    public static CommonResultModel scheduleTask(TransCode transCode, AccountEntity accountEntity, String bank, Map<String, Object> params, String distinct, boolean asyn) {
        return scheduleTask(transCode, accountEntity, bank, params, distinct, 0, asyn);//默认优先为最小0
    }
    /**
     * 安排执行任务
     * @param context
     * @param taskParam : 封装任务参数
     * @param priority : 优先级。默认为最小优先级0， 最大为32
     * @param asyn : 是否异步
     */
    public static CommonResultModel scheduleTask(TransCode transCode, AccountEntity accountEntity, String bank, Map<String, Object> params, String distinct, int priority, boolean asyn) {
        CountDownLatch latch = null;
        TaskParam taskParam = new TaskParam(transCode, accountEntity);

        taskParam.setParams(params);
        taskParam.setAsyn(asyn);
        taskParam.setDistinct(distinct);

        if (!asyn) {//同步
            latch = new CountDownLatch(1);
            taskParam.setLatch(latch);
            taskParam.setPriority(TaskParam.PRIORITY_HIGH);
        }else if(priority > 0){//jianbo_chen 2015-7-24 : 支持优先级设置，优先级越高，越优先处理
            taskParam.setPriority(taskParam.getPriority() >> priority);//移位越多，数值越小，越在前面，最大只能移位63
        }

        taskLock.lock();
        try {
            BankTaskResource taskResource = waitingResourceMap.get(bank);
            if (taskResource == null) {
                taskResource = new BankTaskResource(bank);
                waitingResourceMap.put(bank, taskResource);
            }
            taskResource.scheduleTask(taskParam);
            taskSignal.signalAll();
        } finally {
            taskLock.unlock();
        }
        if (latch != null) {
            try {
                latch.await();//等等返回结果
            } catch (InterruptedException e) {}
        }
        return (CommonResultModel) taskParam.getResult();
    }

    //加载前置机配置。先从数据库中加载，如果找不到对应银行的，则用硬编码的测试环境配置.
    private static List<FrontEndConfig> loadAllFrontEndConfig() {
        List<FrontEndConfig> allFrontEndConfigs = new LinkedList<FrontEndConfig>();
        FrontEndDao frontEndDao = (FrontEndDao) SystemManager.getSpringContext().getBean("frontEndDao");
        List<FrontEndConfig> feList = frontEndDao.loadAllFrontEndConfig();
        Set<String> existBanks = new HashSet<String>();
        if (feList != null && feList.size() > 0) {
            for (FrontEndConfig feConfig : feList) {
                if (!feConfig.isEnable()) continue;
                allFrontEndConfigs.add(feConfig);
                existBanks.add(feConfig.getBankName());
            }
        }
        IBankProfile[] bankProfiles = BankTransComponentManager.getBankProfiles();
        int testId = 1000000000;//测试的前置机ID
        for (IBankProfile bankProfile : bankProfiles) {
            if (existBanks.contains(bankProfile.getBankName())) //配置过的就用正式环境的
                continue;

            FrontEndConfig frontEndConfig = BankTransComponentManager.getFrontEndConfigInstance(bankProfile.getBankName());
            List<FrontEndConfig> testFeList = frontEndConfig.getHardCodeFrontMachineConfigs();
            if (testFeList != null) {
                for (FrontEndConfig testFrontEndConfig : testFeList) {
                    testFrontEndConfig.setId(testId++);//随机生成ID
                    String bank = BankTransComponentManager.getBankNameByPackage(testFrontEndConfig.getClass().getName());
                    testFrontEndConfig.setBankName(bank);
                    if (testFrontEndConfig.getTotalConcurrentNumVal() <= 0) {
                        testFrontEndConfig.setTotalConcurrentNum(1);//默认一个连接
                    }
                    allFrontEndConfigs.add(testFrontEndConfig);
                }
            }
        }
        return allFrontEndConfigs;
    }

    /**
     * 检查前置机是否已经配置.只是简单检查一下，没必要加锁了.
     * @param bank
     * @return
     */
    public static boolean existFrontEnd(AccountEntity accountEntity) {
        BankTaskResource taskResource = waitingResourceMap.get(accountEntity.getBankName());
        if (taskResource == null) return false;

        return taskResource.existFrontEnd(accountEntity.getAccNo());
    }

    //系统启动时调用
    public static void start() {
        List<FrontEndConfig> allFrontEndConfigs = loadAllFrontEndConfig();
        CommonDao accFrontEndBindingDao = CommonDaoImpl.getDao(AccountFrontEndBindingEntity.class);
        List<AccountFrontEndBindingEntity> bindingList = accFrontEndBindingDao.findAll();
        SystemManager.getSpringContext().getBean("frontEndDao");
        for (FrontEndConfig feConfig : allFrontEndConfigs) {
            addFrontEnd(feConfig);
        }
        if (bindingList != null) {//绑定设置
            for (AccountFrontEndBindingEntity binding : bindingList) {
                AccountFrontEndBindingEntity.Pk pk = binding.getPk();
                bind(pk.getFrontEnd().getBankName(), pk.getFrontEnd().getId(), pk.getAccount().getAccNo());
            }
        }
        if (threadPool == null) {
            TaskSchedulerThread taskSchedulerThread = new TaskSchedulerThread();
            taskSchedulerThread.setName("task-scheduler");
            taskSchedulerThread.setDaemon(true);
            taskSchedulerThread.start();

            FrontEndTestThread frontEndTestThread = new FrontEndTestThread();
            frontEndTestThread.setName("front-end-test");
            frontEndTestThread.setDaemon(true);
            frontEndTestThread.start();

            int threadPoolSize = SystemConfig.threadPoolConfig.getIntVal();
            threadPool = Executors.newFixedThreadPool(threadPoolSize);
            for (int i = 1; i <= threadPoolSize; i++) {
                threadPool.submit(new TaskRunner(i));
            }
        }
        logger.info("任务线程池已启动.");
    }

    //系统关闭时调用
    public static void stop() {
        boolean waitingPay = true;
        while (threadPoolPaymentCounter.get() > 0) {
            if (waitingPay) {
                logger.info("等待付款任务结束...");
                waitingPay = false;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {}
        }
        threadPool.shutdown();
        logger.info("任务线程池已停止.");
    }

    private static class TaskRunner implements Runnable {

        int index;

        public TaskRunner(int index) {
            this.index = index;
        }

        @Override
        public void run() {
            Thread.currentThread().setName("task-worker-" + index);
            loopRun();
        }

        private void loopRun() {
            boolean isPay = false;
            while (SystemManager.isRunning()) {
                TaskParam taskParam;
                try {
                    taskParam = readyQueue.take();
                } catch (InterruptedException e) {
                    continue;
                }
                //支付业务+1
                isPay = taskParam.getTransCode() == TransCode.PAY;
                if (isPay) threadPoolPaymentCounter.incrementAndGet();

                Trace.beginTrace();//开始跟踪
                try {
                    Object result = doTask(taskParam);
                    taskParam.setResult(result);
                } catch (Exception e) {
                    taskParam.setException(e);
                    logger.error("", e);//这个一般不会出现了
                } finally {
                    if (isPay) threadPoolPaymentCounter.decrementAndGet();
                    ////////////////////////
                    try {//跟踪结束
                        Trace.endTrace();
                    } catch (Throwable e) {
                        logger.warn("", e);
                    }
                    if (taskParam.getLatch() != null) {//同步的，通知结果.
                        taskParam.getLatch().countDown();//通知等待同步线程结果
                    }
                    ////////////////////////
                    try {
                        taskLock.lock();//工作线程归还前置机到池中，并通知调度线程
                        try {
                            if (taskParam.finish())
                                taskSignal.signalAll();
                            else //false为存在前置机黑名单
                            blackListSignal.signalAll();//通知前置机探测线程
                        } finally {
                            //调度器可能在等待.
                            taskLock.unlock();
                        }
                    } catch (Throwable e) {
                        logger.warn("", e);
                    }
                    TransContext.setContext(null);//清除线程缓存
                }
            }
        }

        private Object doTask(TaskParam taskParam) {
            AccountEntity accountEntity = taskParam.getAccountEntity();
            IBankProfile bankProfile = BankTransComponentManager.getBankProfile(taskParam.getAccountEntity().getBankName());
            TransContext context = new TransContext(bankProfile, accountEntity);
            context.setTransCode(taskParam.getTransCode());
            context.setFrontEndConfig(taskParam.getFrontEndConfig());
            context.setIsoCurrency(Constant.CURRENCY_CNY);
            TransContext.setContext(context);
            ITransDelegate transDelegate = BankTransComponentManager.getTransDelegate(taskParam.getTransCode().getCode());
            if (transDelegate == null) {
                logger.warn("调度没有找到对应的业务实现");
                return null;
            }
            if (transDelegate instanceof RunnableTask) {
                return ((RunnableTask) transDelegate).run(taskParam);
            } else {
                logger.warn(transDelegate.getClass().getName() + "没有实现接口" + RunnableTask.class.getName());
            }

            return null;
        }
    }

    private static class TaskSchedulerThread extends Thread {

        private static Logger logger = LoggerFactory.getLogger(TaskSchedulerThread.class);

        public void run() {
            while (SystemManager.isRunning()) {
                taskLock.lock();//写锁，与工作线程互斥
                try {
                    Set<Map.Entry<String, BankTaskResource>> resourceEntrySet = waitingResourceMap.entrySet();
                    Map.Entry<String, BankTaskResource> resourceEntry;
                    BankTaskResource taskResource;
                    List<TaskParam> taskParams = null;
                    for (Iterator<Map.Entry<String, BankTaskResource>> itr = resourceEntrySet.iterator(); itr.hasNext();) {
                        resourceEntry = itr.next();
                        taskResource = resourceEntry.getValue();
                        try {
                            taskParams = taskResource.getReadyTaskParams();
                        } catch (Exception e) {
                            logger.error("【系统发生严重错误】调度时生成任务异常", e);//囧，测试时竟然出现一个nullPointer，不可能的事啊!!!
                        }
                        if (taskParams == null) continue;

                        for (TaskParam taskParam : taskParams)
                            readyQueue.offer(taskParam);
                    }
                    try {
                        taskSignal.await();
                    } catch (InterruptedException e) {
                        //ignore
                    }
                } finally {
                    taskLock.unlock();
                }
            }
            logger.info("调度服务已停止.");
        }
    }

    private static class FrontEndTestThread extends Thread {

        private static Logger logger = LoggerFactory.getLogger(FrontEndTestThread.class);
        private final static Set<BankTaskResource> blackListBankResources = new HashSet<BankTaskResource>();

        public void run() {
            int blackListTestInterval = 2;
            while (SystemManager.isRunning()) {
                try {
                    try {
                        blackListTestInterval = SystemConfig.blackListTestInterval.getIntVal();
                    } catch (Exception e) {}
                    if (blackListTestInterval > 10) //也不要太长时间，不要超过10分钟吧
                        blackListTestInterval = 2;
                    refreshBlackList();
                    testBlackList();
                    if (blackListBankResources.size() > 0) //还有检测未通过的(会影响到新的)
                        Thread.sleep(blackListTestInterval * 60 * 1000);
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        }

        private void testBlackList() {
            for (Iterator<BankTaskResource> itr = blackListBankResources.iterator(); itr.hasNext();) {
                BankTaskResource bankTaskResource = itr.next();
                Set<FrontEndConfig> blackList = bankTaskResource.getBlackList();
                for (Iterator<FrontEndConfig> blItr = blackList.iterator(); blItr.hasNext();) {
                    FrontEndConfig feConfig = blItr.next();
                    if (FrontEndTestDelegateService.testFeConnection(feConfig)) {
                        logger.info("检测到前置机已可用：" + feConfig);
                        taskLock.lock();
                        try {
                            bankTaskResource.removeBlackList(feConfig);//测试通过，可以使用了
                            taskSignal.signalAll();//黑名单去掉，可以通知调度线程
                        } finally {
                            taskLock.unlock();
                        }
                    }
                }
            }
        }

        private void refreshBlackList() {
            while (true) {
                taskLock.lock();
                try {
                    blackListBankResources.clear();
                    for (Iterator<BankTaskResource> itr = waitingResourceMap.values().iterator(); itr.hasNext();) {
                        BankTaskResource bankTaskResource = itr.next();
                        if (bankTaskResource.getBlackList().size() > 0) {
                            blackListBankResources.add(bankTaskResource);
                        }
                    }
                    if (blackListBankResources.size() <= 0) {//没有需要探测的
                        try {
                            blackListSignal.await();
                        } catch (InterruptedException e) {}
                        continue;
                    } else {
                        break;
                    }
                } finally {
                    taskLock.unlock();
                }
            }
        }
    }

}
