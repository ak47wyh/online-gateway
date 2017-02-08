package com.iboxpay.settlement.gateway.common.task;

import com.iboxpay.settlement.gateway.common.inout.CommonResultModel;

/**
 * 可后台运行的任务.
 * @author jianbo_chen
 */
public interface RunnableTask {

    /**
     * 后台执行
     * @param taskParam : 存放运行需要参数
     * @return : 返回运行结果。如果是同步，请求线程会等待结果；如果异步，不需要(也不会等待)返回结果。
     */
    public CommonResultModel run(TaskParam taskParam);

}
