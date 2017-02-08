package com.iboxpay.settlement.gateway.common.trans.check;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.iboxpay.settlement.gateway.common.domain.PaymentCheckResultEntity;
import com.iboxpay.settlement.gateway.common.trans.PaymentStatus;
import com.iboxpay.settlement.gateway.common.trans.TransCode;

/**
 * 默认的对账策略
 * @author jianbo_chen
 */
public class DefaultPaymentChecker extends AbstractPaymentChecker implements IPaymentChecker {

    protected List<PaymentCheckResultEntity> loopCheck(int timeDiffMinus, long timeDiffMillis, List<CheckerData> paymentList, List<CheckerData> detailList) {
        CheckAssistant paymentCheckAssistant = new CheckAssistant(paymentList);
        CheckAssistant detailCheckAssistant = new CheckAssistant(detailList);
        List<PaymentCheckResultEntity> checkResultList = new LinkedList<PaymentCheckResultEntity>();
        boolean loop = paymentCheckAssistant.next() && detailCheckAssistant.next();
        while (loop) {
            int status = paymentCheckAssistant.getFirst().getCustomerAccNo().compareTo(detailCheckAssistant.getFirst().getCustomerAccNo());
            if (status < 0) {//左边小，下移(未提交银行就直接失败了)
                processNotMatchDetail(paymentCheckAssistant, checkResultList);
                if (paymentCheckAssistant.next())
                    continue;
                else break;
            } else if (status > 0) {//右边小，下移
                if (detailCheckAssistant.next())
                    continue;
                else break;
            }

            //提交多少笔，返回多少笔(大部分都是这种)
            if (paymentCheckAssistant.getCurrentSize() == detailCheckAssistant.getCurrentSize()) {
                if (paymentCheckAssistant.getCurrentSize() == 1) {//都只有一个(这是一般情况)
                    CheckerData checkerData = paymentCheckAssistant.getSingle();
                    CheckerData targetCheckerData = detailCheckAssistant.getSingle();
                    doCheck(checkerData, targetCheckerData, checkResultList, timeDiffMinus, timeDiffMillis);
                    if (paymentCheckAssistant.next() && detailCheckAssistant.next())//正常返回，再找下一个
                        continue;
                    else break;
                } else if (paymentCheckAssistant.getCurrentSize() > 1) {
                    List<CheckerData> checkerDatas = paymentCheckAssistant.getMulti();
                    List<CheckerData> targetCheckerDatas = detailCheckAssistant.getMulti();
                    for (int i = 0; i < checkerDatas.size(); i++) {
                        doCheck(checkerDatas.get(i), targetCheckerDatas.get(i), checkResultList, timeDiffMinus, timeDiffMillis);
                    }
                    if (paymentCheckAssistant.next() && detailCheckAssistant.next())//正常返回，再找下一个
                        continue;
                    else break;
                } else {
                    throw new RuntimeException("当前数量为0");
                }
            } else {//两边数量不一致时，情况比较复杂
                if (paymentCheckAssistant.getCurrentSize() == 1) {
                    if (false == detailCheckAssistant.getCurrentSize() > 1) {
                        throw new RuntimeException("囧，内部错误");
                    }
                    CheckerData checkerData = paymentCheckAssistant.getSingle();
                    checkUnbalance(checkerData, detailCheckAssistant, checkResultList, timeDiffMinus, timeDiffMillis);
                } else if (paymentCheckAssistant.getCurrentSize() > 1) {
                    List<CheckerData> checkerDatas = paymentCheckAssistant.getMulti();
                    for (int i = 0; i < checkerDatas.size(); i++) {
                        CheckerData checkerData = checkerDatas.get(i);
                        if (checkUnbalance(checkerData, detailCheckAssistant, checkResultList, timeDiffMinus, timeDiffMillis) && detailCheckAssistant.getCurrentSize() == 1) break;
                    }
                } else {
                    throw new RuntimeException("囧，内部错误");
                }
                if (paymentCheckAssistant.next() && detailCheckAssistant.next())
                    continue;
                else break;
            }
        }
        return checkResultList;
    }

    //本地直接失败掉的，在明细是肯定是找不到的，说明一下。
    private void processNotMatchDetail(CheckAssistant paymentCheckAssistant, List<PaymentCheckResultEntity> checkResultList) {
        if (paymentCheckAssistant.getCurrentSize() == 1) {
            CheckerData notMatchCheckData = paymentCheckAssistant.getSingle();
            PaymentCheckResultEntity checkResult = new PaymentCheckResultEntity();
            checkResult.setPaymentId(notMatchCheckData.getTargetId());
            checkResultList.add(checkResult);
            if (notMatchCheckData.getTransDate() == null) {
                checkResult.setCheckStatusMsg("未提交到银行");
            }
        } else if (paymentCheckAssistant.getCurrentSize() > 1) {
            List<CheckerData> notMatchCheckDatas = paymentCheckAssistant.getMulti();
            for (CheckerData notMatchCheckData : notMatchCheckDatas) {
                PaymentCheckResultEntity checkResult = new PaymentCheckResultEntity();
                checkResult.setPaymentId(notMatchCheckData.getTargetId());
                checkResultList.add(checkResult);
                if (notMatchCheckData.getTransDate() == null) {
                    checkResult.setCheckStatusMsg("未提交到银行");
                }
            }
        }
    }

    private boolean checkUnbalance(CheckerData checkerData, CheckAssistant detailCheckAssistant, List<PaymentCheckResultEntity> checkResultList, int timeDiffMinus, long timeDiffMillis) {
        CheckerData closeTargetData = null;//最接近原交易的交易时间的明细
        List<CheckerData> targetCheckerDatas = detailCheckAssistant.getMulti();
        int foundIndex = -1;
        if (targetCheckerDatas != null) {
            long compareTimeDiff = Long.MAX_VALUE;
            for (int i = 0; i < targetCheckerDatas.size(); i++) {
                CheckerData targetCheckerData = targetCheckerDatas.get(i);
                if (targetCheckerData != null && checkerData.getAmount().compareTo(targetCheckerData.getAmount()) == 0) {
                    long _compareTimeDiff = Math.abs(checkerData.getTransDate().getTime() - targetCheckerData.getTransDate().getTime());
                    if (_compareTimeDiff < compareTimeDiff) {
                        closeTargetData = targetCheckerData;
                        compareTimeDiff = _compareTimeDiff;
                        foundIndex = i;
                    }
                }
            }
        } else if (detailCheckAssistant.getCurrentSize() == 1) {
            CheckerData _closeTargetData = detailCheckAssistant.getSingle();
            if (_closeTargetData != null && checkerData.getAmount().compareTo(_closeTargetData.getAmount()) == 0
                    && Math.abs(Math.abs(checkerData.getTransDate().getTime() - _closeTargetData.getTransDate().getTime())) <= timeDiffMillis) {
                closeTargetData = _closeTargetData;
            }
        }
        if (closeTargetData != null) {
            doCheck(checkerData, closeTargetData, checkResultList, timeDiffMinus, timeDiffMillis);
            if (foundIndex != -1) targetCheckerDatas.set(foundIndex, null);//这个已经找到目标的明细，其他的支付就不能再以这个为准了
            return true;
        } else {//一般是失败记录找不到，为了更新到对账表中，就添加个明细号是空的
            PaymentCheckResultEntity checkResult = new PaymentCheckResultEntity();
            checkResult.setPaymentId(checkerData.getTargetId());
            checkResultList.add(checkResult);
        }
        return false;
    }

    private boolean doCheck(CheckerData checkerData, CheckerData targetCheckerData, List<PaymentCheckResultEntity> checkResultList, int timeDiffMinus, long timeDiffMillis) {
        PaymentCheckResultEntity checkResult = new PaymentCheckResultEntity();
        checkResult.setPaymentId(checkerData.getTargetId());
        checkResult.setDetailId(targetCheckerData.getTargetId());
        if (!"*".equals(targetCheckerData.getCustomerAccName()) //户名不为空
                && !checkerData.getCustomerAccName().equals(targetCheckerData.getCustomerAccName())) {
            checkResult.setCheckStatusMsg("账号同,户名不同");
            checkResultList.add(checkResult);
            return false;
        }
        if (checkerData.getAmount().compareTo(targetCheckerData.getAmount()) != 0) {
            checkResult.setCheckStatusMsg("账号同,金额不同");
            checkResultList.add(checkResult);
            return false;
        }
        long compareTimeDiff = checkerData.getTransDate().getTime() - targetCheckerData.getTransDate().getTime();
        if (Math.abs(compareTimeDiff) > timeDiffMillis) {
            checkResult.setCheckStatusMsg("账号同,交易时间相差过大(超过" + timeDiffMinus + "分钟)");
            checkResultList.add(checkResult);
            return false;
        }
        if (checkerData.getStatus() == PaymentStatus.STATUS_SUCCESS) {
            checkResult.setCheckStatus(PaymentStatus.STATUS_SUCCESS);
            checkResult.setCheckStatusMsg("确认成功");
        } else {
            checkResult.setCheckStatus(PaymentStatus.STATUS_SUCCESS);
            checkResult.setCheckStatusMsg("已成功");
        }
        checkResultList.add(checkResult);
        return true;
    }

    private static class CheckAssistant {

        List<CheckerData> infoList;
        int index;
        private CheckerData current;
        private List<CheckerData> currentList;

        public CheckAssistant(List<CheckerData> infoList) {
            this.infoList = infoList;
        }

        public boolean next() {
            current = null;
            currentList = null;
            boolean hasNext = index < this.infoList.size();
            if (!hasNext)
                return hasNext;
            else {
                int i = index;
                List<CheckerData> seqList = null;//连续多个相同的收款账号
                CheckerData infoEntity = this.infoList.get(i);
                String customerAccNo0 = infoEntity.getCustomerAccNo();
                while (++i < infoList.size()) {
                    CheckerData _infoEntity = this.infoList.get(i);
                    String customerAccNo = (String) _infoEntity.getCustomerAccNo();
                    if (customerAccNo0.equals(customerAccNo)) {
                        if (seqList == null) {
                            seqList = new LinkedList<CheckerData>();
                            seqList.add(infoEntity);
                        }
                        seqList.add(_infoEntity);
                    } else {//按顺序排的，直接可以break了
                        break;
                    }
                }
                if (seqList != null) {
                    sort(seqList);
                    currentList = seqList;
                    index += seqList.size();
                } else {
                    current = infoEntity;
                    index += 1;
                }
                return true;
            }
        }

        //按成功到失败顺序排序，优先对成功的。针对一天同一笔付款提交两次，如果先对失败，成功的就对不上了
        private List<CheckerData> sort(List<CheckerData> seqList) {
            if (seqList.get(0).getStatus() == 0) //明细的状态是0，不用排.
                return seqList;
            Collections.sort(seqList, new Comparator<CheckerData>() {

                @Override
                public int compare(CheckerData o1, CheckerData o2) {
                    if (o1.getStatus() == PaymentStatus.STATUS_SUCCESS) return -1;
                    if (o2.getStatus() == PaymentStatus.STATUS_SUCCESS) return 1;
                    return 0;
                }
            });
            return seqList;
        }

        //返回相同的账号的个数
        public int getCurrentSize() {
            if (current != null)
                return 1;
            else if (currentList != null) return currentList.size();
            return 0;
        }

        public CheckerData getFirst() {
            return getCurrentSize() == 1 ? getSingle() : getMulti().get(0);
        }

        public CheckerData getSingle() {
            return current;
        }

        public List<CheckerData> getMulti() {
            return currentList;
        }
    }

    @Override
    public String getBankTransCode() {
        return null;
    }

    @Override
    public String getBankTransDesc() {
        return null;
    }

    @Override
    public TransCode getTransCode() {
        return TransCode.CHECK;
    }

}
