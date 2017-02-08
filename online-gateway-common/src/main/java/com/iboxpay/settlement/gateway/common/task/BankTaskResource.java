package com.iboxpay.settlement.gateway.common.task;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iboxpay.settlement.gateway.common.TransContext;
import com.iboxpay.settlement.gateway.common.TransContext.ResultCode;
import com.iboxpay.settlement.gateway.common.config.FrontEndConfig;
import com.iboxpay.settlement.gateway.common.config.SystemConfig;
import com.iboxpay.settlement.gateway.common.exception.FrontEndException;
import com.iboxpay.settlement.gateway.common.msg.Message;
import com.iboxpay.settlement.gateway.common.msg.MessageCenter;
import com.iboxpay.settlement.gateway.common.net.ConnectionAdapter;
import com.iboxpay.settlement.gateway.common.net.IConnectionListener;
import com.iboxpay.settlement.gateway.common.trans.TransCode;

/**
 * 某银行待处理数据及前置机资源.(在调用时加锁，不在本类加锁)<br/>
 * 2015-7-24: 外层使用ReentrantLock，可见性与synchronized是一样的，是不必要使用j.u.c包类的^_____^
 * @author jianbo_chen
 */
class BankTaskResource {

    private static Logger logger = LoggerFactory.getLogger(BankTaskResource.class);
    private final String bankName;
    private PriorityTaskQueue<TaskParam> waitingTaskDataMap = new PriorityTaskQueue<TaskParam>();
    private AtomicInteger syncTaskCounter = new AtomicInteger(0);//同步任务的个数
    //前置机池。如果有40个并发，里面会有40个相同的对象
    private LinkedBlockingQueue<FrontEndConfig> availableFeConfigList = new LinkedBlockingQueue<FrontEndConfig>();//可用前置列表
    private CopyOnWriteArraySet<FrontEndConfig> feConfigSet = new CopyOnWriteArraySet<FrontEndConfig>();//正在使用的前置机配置，不重复。如：127.0.0.1:8080,127.0.0.1:8081那就是两个 
    private CopyOnWriteArraySet<FrontEndConfig> updatingFeConfigSet = new CopyOnWriteArraySet<FrontEndConfig>();//待更新的前置机，会等待相同旧的处理完成后更新
    private CopyOnWriteArrayList<BindingInfo> bindingInfos = new CopyOnWriteArrayList<BindingInfo>();//账号与前置绑定信息
    private ConcurrentHashMap<FrontEndConfig, Integer> feBlackListMap = new ConcurrentHashMap<FrontEndConfig, Integer>();//前置机黑名单.在黑名单中的前置机暂时不能发送支付请求. value=次数

    static {
        ConnectionAdapter.getCompositeconnectionlistener().addListener(new IConnectionListener() {

            public void onConnectionFail(String ip, int port, FrontEndConfig feConfig, FrontEndException e) {
                TransContext.getContext().setResultCode(ResultCode.connectionFail);//打开前置连接失败
            }

            public void onConnectionSuccess(String ip, int port, FrontEndConfig feConfig) {
                TransContext.getContext().setResultCode(ResultCode.connectionSuccess);//打开前置连接失败
            }
        });
    }

    public BankTaskResource(String bankName) {
        this.bankName = bankName;
    }

    public String getBankName() {
        return bankName;
    }

    public void scheduleTask(TaskParam taskParam) {
        if (taskParam != null) {
            boolean permit = true;
            if (taskParam.isAsyn() && taskParam.getDistinct() != null) {//过滤存在的重复的任务
                for (TaskParam existTaskParam : waitingTaskDataMap) {
                    if (taskParam.getDistinct() != null && existTaskParam.getDistinct() != null && taskParam.getDistinct().equals(existTaskParam.getDistinct())) {
                        permit = false;
                        break;
                    }
                }
            }
            if (permit) waitingTaskDataMap.add(taskParam);
            if (!taskParam.isAsyn()) syncTaskCounter.incrementAndGet();//同步任务个数加1
        }
    }

    public List<TaskParam> getReadyTaskParams() {
        List<TaskParam> readyTaskParams = new LinkedList<TaskParam>();
        if (waitingTaskDataMap.size() > 0 && availableFeConfigList.size() > 0) {
            if (bindingInfos.size() == 0) {//不存在账号绑定前置机的情况(大部分情况)
                generateWithNoBinding(readyTaskParams);
            } else {//该银行账号有绑定指定前置的
                generateWithBinding(readyTaskParams);
            }
        }
        if (readyTaskParams.size() > 0)
            return readyTaskParams;
        else return null;
    }

    /**生成任务（没有前置绑定的情况）**/
    private void generateWithNoBinding(List<TaskParam> readyTaskParams) {
        if (feBlackListMap.isEmpty()) {//黑名单不存在(大部分情况)
            while (waitingTaskDataMap.size() > 0 && availableFeConfigList.size() > 0) {
                TaskParam taskParam = waitingTaskDataMap.poll();
                FrontEndConfig feConfig = availableFeConfigList.poll();
                generateReadyTask(readyTaskParams, taskParam, feConfig);
            }
        } else {//检测黑名单
            for (Iterator<FrontEndConfig> feItr = availableFeConfigList.iterator(); feItr.hasNext() && waitingTaskDataMap.size() > 0;) {
                FrontEndConfig feConfig = feItr.next();
                if (syncTaskCounter.get() > 0 || !isForbidden(feConfig)) {//同步任务可以不判断前置可用性 || 异步任务有前置才可以使用
                    TaskParam taskParam = waitingTaskDataMap.poll();
                    generateReadyTask(readyTaskParams, taskParam, feConfig);
                    feItr.remove();
                }
            }
        }
    }

    /**生成任务（存在前置绑定的情况）**/
    private void generateWithBinding(List<TaskParam> readyTaskParams) {
        for (Iterator<FrontEndConfig> feItr = availableFeConfigList.iterator(); feItr.hasNext();) {
            FrontEndConfig feConfig = feItr.next();
            if (!isForbidden(feConfig)) {//前置可用
                for (Iterator<TaskParam> taskItr = waitingTaskDataMap.iterator(); taskItr.hasNext();) {
                    TaskParam taskParam = taskItr.next();
                    if (matchBinding(taskParam.getAccountEntity().getAccNo(), feConfig.getId())) {
                        generateReadyTask(readyTaskParams, taskParam, feConfig);
                        taskItr.remove();//找到，出队
                        feItr.remove();//找到，出队
                        break;
                    }
                }
            } else {//不可用时，看看有没同步任务，黑名单对同步任务无效
                if (syncTaskCounter.get() > 0) {
                    for (Iterator<TaskParam> taskItr = waitingTaskDataMap.iterator(); taskItr.hasNext();) {
                        TaskParam taskParam = taskItr.next();
                        if (!taskParam.isAsyn()) {//同步任务
                            if (matchBinding(taskParam.getAccountEntity().getAccNo(), feConfig.getId())) {//前置匹配，可以生成任务
                                generateReadyTask(readyTaskParams, taskParam, feConfig);
                                taskItr.remove();//找到，出队
                                feItr.remove();//找到，出队
                                break;//该前置已经使用，再看下一个
                            }
                        } else {
                            break;//如果不是同步任务，直接中断（同步任务是排在前面的）
                        }
                    }
                }
            }
        }
    }

    private void generateReadyTask(List<TaskParam> readyTaskParams, TaskParam taskParam, FrontEndConfig feConfig) {
        if (!taskParam.isAsyn()) {//同步任务是排在前面的，同步任务减1
            syncTaskCounter.decrementAndGet();
        }
        feConfig.increaseConcurrentNum();
        taskParam.setFrontEndConfig(feConfig);
        taskParam.setTaskResource(this);
        readyTaskParams.add(taskParam);
    }

    //前置机是否禁用
    private boolean isForbidden(FrontEndConfig feConfig) {
        if (!feBlackListMap.isEmpty()) {
            if (feBlackListMap.get(feConfig) != null)
                return Boolean.valueOf(SystemConfig.payForbdOnConFail.getVal());
            else return false;
        } else return false;
    }

    //查找绑定的前置机
    private boolean matchBinding(String accNo, int frontEndId) {
        List<BindingInfo> currentBindingInfos = bindingInfos;
        if (currentBindingInfos == null || currentBindingInfos.size() == 0) //没有任何绑定，直接返回true
            return true;

        boolean foundAccNo = false, foundFrontEnd = false, fa = false, fe = false;
        for (BindingInfo bindingInfo : currentBindingInfos) {
            if (bindingInfo.accNo.equals(accNo)) {
                foundAccNo = true;
                fa = true;
            }
            if (bindingInfo.frontEndId == frontEndId) {
                foundFrontEnd = true;
                fe = true;
            }
            if (fa && fe) return true;

            fa = false;
            fe = false;
        }
        if (foundAccNo) //账号有绑定，但是没有找到绑定的前置：不匹配
            return false;

        if (foundFrontEnd) //前置有绑定，但不是对应的账号：不匹配
            return false;

        return true;//账号和前置都没有在列表中的，随便发
    }

    public void bind(String accNo, int frontEndId) {
        logger.info("绑定账号(accNo=" + accNo + ")到指定前置机(id=" + frontEndId + ")");
        if (!containBinding(accNo, frontEndId)) bindingInfos.add(new BindingInfo(accNo, frontEndId));
    }

    public void unbind(String accNo, int frontEndId) {
        logger.info("解除账号(accNo=" + accNo + ")与前置机(id=" + frontEndId + ")的绑定");
        if (containBinding(accNo, frontEndId)) bindingInfos.remove(new BindingInfo(accNo, frontEndId));
    }

    private boolean containBinding(String accNo, int frontEndId) {
        if (this.bindingInfos == null) return false;

        for (BindingInfo bindingInfo : bindingInfos) {
            if (bindingInfo.accNo.equals(accNo) && bindingInfo.frontEndId == frontEndId) return true;
        }
        return false;
    }

    private void addBlackList(FrontEndConfig feConfig) {
        logger.info("检测到前置机无法连接，将前置机加入黑名单：" + feConfig);
        Integer blackTimes = feBlackListMap.get(feConfig);
        MessageCenter.deliver(new Message("fe-" + feConfig.getId() + "-fail", "前置机(id=" + feConfig.getId() + ")连接失败", "ip:" + feConfig.getIp().getVal() + ", 端口：" + feConfig.getPort().getVal()
                + "(需要签名端口的请检查端口是否都已启动)", Message.Color.RED));
        if (blackTimes == null) {
            feBlackListMap.put(feConfig, 1);
        } else {
            feBlackListMap.put(feConfig, blackTimes.intValue() + 1);//次数加1
        }
    }

    void removeBlackList(FrontEndConfig feConfig) {
        if (feBlackListMap.remove(feConfig) != null) {
            MessageCenter.deliver(new Message("fe-" + feConfig.getId() + "-recover", "前置机(id=" + feConfig.getId() + ")连接已恢复",
                    "ip:" + feConfig.getIp().getVal() + ", 端口：" + feConfig.getPort().getVal(), Message.Color.GREEN));
            logger.info("已从黑名单列表中删除前置机（可用）：" + feConfig);
        }
    }

    public Set<FrontEndConfig> getBlackList() {
        return feBlackListMap.keySet();
    }

    /**
     * 任务完成，把前置机放回池中
     * @param taskParam
     */
    public boolean finishTask(TaskParam taskParam) {
        boolean available = true;
        TransContext context = TransContext.getContext();
        if (context.getTransCode() == TransCode.PAY) {//支付连接失败才处理
            if (context.getResultCode() == ResultCode.connectionFail) {
                addBlackList(taskParam.getFrontEndConfig());
                available = false;
            }
        } else if (context.getTransCode() == TransCode.BALANCE) {
            if (context.getResultCode() == ResultCode.connectionSuccess && !feBlackListMap.isEmpty()) {
                removeBlackList(taskParam.getFrontEndConfig());
            }
        }
        FrontEndConfig feConfig = taskParam.getFrontEndConfig();
        feConfig.decreaseConcurrentNum();
        if (feConfig.isDeprecated()) {//旧的过期，被修改了
            doUpdateFrondEnd(feConfig, null);
        } else {//放回来，修改并发数
            availableFeConfigList.add(feConfig);
        }
        return available;
    }

    /**
     * 卸载指定前置机
     * @param feConfig
     */
    public void uninstallFrontEndConfig(FrontEndConfig feConfig) {
        for (FrontEndConfig _feConfig : feConfigSet) {
            if (_feConfig.getId() == feConfig.getId()) {
                _feConfig.setDeprecated(true);//丢弃吧.
            }
        }
        //删除池中的可用前置机
        removeExistOldFrontEndConfig(feConfig);
    }

    /**
     * 添加前置机
     * @param feConfig
     */
    public void installFrontEndConfig(FrontEndConfig feConfig) {
        if (feConfig.getTotalConcurrentNumVal() > 0) {
            logger.info("添加前置机到调度服务：" + feConfig);
            FrontEndConfig oldFeConfig = null;
            for (FrontEndConfig _feConfig : feConfigSet) {
                if (_feConfig.getId() == feConfig.getId()) {
                    oldFeConfig = _feConfig;
                }
            }
            if (oldFeConfig == null) {
                doUpdateFrondEnd(null, feConfig);
            } else {
                oldFeConfig.setDeprecated(true);//设置旧的为废除
                updatingFeConfigSet.add(feConfig);
                doUpdateFrondEnd(oldFeConfig, feConfig);
            }
        } else {
            if (feConfig.isEnable()) {
                logger.info("禁用前置机(并发数<=0)：" + feConfig);
            } else {
                logger.info("禁用前置机：" + feConfig);
            }
            uninstallFrontEndConfig(feConfig);
        }
    }

    private void doUpdateFrondEnd(FrontEndConfig oldFeConfig, FrontEndConfig newFeConfig) {
        if (oldFeConfig != null) {
            if (oldFeConfig.isDeprecated() && oldFeConfig.getConcurrentNum() <= 0) {//这个是最后一个处理完，可以修改了
                Iterator<FrontEndConfig> itr = updatingFeConfigSet.iterator();
                while (itr.hasNext()) {
                    FrontEndConfig _newFeConfig = itr.next();
                    if (_newFeConfig.getId() == oldFeConfig.getId()) {
                        addConcurrentFrontEndConfig(_newFeConfig);
                        //						itr.remove();//CopyOnWriteArraySet的iterator不能remove
                        updatingFeConfigSet.remove(_newFeConfig);
                    }
                }
            }
        } else {
            addConcurrentFrontEndConfig(newFeConfig);
        }
    }

    private void addConcurrentFrontEndConfig(FrontEndConfig feConfig) {
        removeExistOldFrontEndConfig(feConfig);
        feConfigSet.add(feConfig);
        int totalConcurrentNum = feConfig.getTotalConcurrentNumVal();
        while (totalConcurrentNum-- > 0) {//添加多个
            availableFeConfigList.add(feConfig);
        }
    }

    private void removeExistOldFrontEndConfig(FrontEndConfig feConfig) {
        feConfigSet.remove(feConfig);
        int size = availableFeConfigList.size();
        while (size-- > 0) {
            availableFeConfigList.remove(feConfig);
        }
        removeBlackList(feConfig);//修改了，黑名单去除，可能可以使用了
    }

    public boolean existFrontEnd(String accNo) {
        if (this.bindingInfos != null && this.bindingInfos.size() > 0) {
            List<BindingInfo> currentBindingInfos = this.bindingInfos;
            List<Integer> bindingFeIds = null;
            for (BindingInfo bindingInfo : currentBindingInfos) {
                if (bindingInfo.accNo.equals(accNo)) {
                    if (bindingFeIds == null) bindingFeIds = new LinkedList<Integer>();
                    bindingFeIds.add(bindingInfo.frontEndId);
                }
            }
            if (bindingFeIds != null) {//账号绑定前置
                for (FrontEndConfig frontEndConfig : this.feConfigSet) {//绑定的前置可用
                    if (bindingFeIds.contains(frontEndConfig.getId())) return true;
                }
                return false;
            } else {//该账号未绑定前置
                for (FrontEndConfig frontEndConfig : this.feConfigSet) {//排除所有绑定的前置后，是否还有可用的前置
                    boolean notBinding = true;
                    for (BindingInfo bindingInfo : currentBindingInfos) {
                        if (frontEndConfig.getId() == bindingInfo.frontEndId) {//相同即存在绑定了
                            notBinding = false;
                            break;
                        }
                    }
                    if (notBinding) //存在没有被绑定的前置，可用
                        return true;
                }
                return false;
            }
        } else {
            return feConfigSet.size() > 0;
        }
    }

    private static class BindingInfo {

        public final String accNo;
        public final int frontEndId;

        public BindingInfo(String accNo, int frontEndId) {
            this.accNo = accNo;
            this.frontEndId = frontEndId;
        }

        @Override
        public int hashCode() {
            return (accNo + frontEndId).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            BindingInfo bi = (BindingInfo) obj;
            return this.accNo.equals(bi.accNo) && this.frontEndId == bi.frontEndId;
        }
    }
}
