package com.iboxpay.settlement.gateway.common.trans.payment;

import java.math.BigDecimal;
import java.util.Arrays;

import com.iboxpay.settlement.gateway.common.domain.PaymentEntity;
import com.iboxpay.settlement.gateway.common.util.StringUtils;

/**
 * 支付接口信息。每个支付业务提供信息，由框架决定调用哪个接口。
 * @author jianbo_chen
 */
public class PaymentNavigation {

    public enum Type {
        pay("普通支付"), collect("扣款"), withdrawal("提现"),online("在线支付");

        private String desc;

        Type(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }

        @Override
        public String toString() {
            return name() + " - " + desc;
        }
    }

    private Type[] types = new Type[] { Type.pay };//业务类别。默认为pay普通支付。
    private boolean sameBank;//同行
    private boolean diffBank;//跨行
    private BigDecimal minAmount;//支持最小资金
    private BigDecimal maxAmount;//支持最大资金
    private boolean toPrivate;//对私
    private boolean toCompany;//对公
    private int batchSize = 1;//支持批量笔数。默认为1
    private int priority;//优先级：最低优先级为0（默认），数值越大优先级越高。

    public PaymentNavigation() {

    }

    /**
     * 创建对象
     * @return
     */
    public static PaymentNavigation create() {
        return new PaymentNavigation();
    }

    /**
     * 支付信息是否匹配，可以走这个接口.
     * @param paymentEntity
     * @return
     */
    public boolean match(PaymentEntity paymentEntity) {
        if (!matchPaymentType(paymentEntity.getPayType())) return false;

//        if (false == (paymentEntity.isToSameBack() == sameBank || !paymentEntity.isToSameBack() == diffBank)) return false;

        if (minAmount != null && paymentEntity.getAmount().compareTo(minAmount) < 0) return false;

        if (maxAmount != null && paymentEntity.getAmount().compareTo(maxAmount) > 0) return false;

        if (paymentEntity.isToPrivate() && paymentEntity.isToPrivate() != toPrivate) return false;

        if (paymentEntity.isToCompay() && paymentEntity.isToCompay() != toCompany) return false;

        return true;
    }

    private boolean matchPaymentType(String typeStr) {
        if (StringUtils.isBlank(typeStr)) typeStr = Type.pay.name();
        if (types == null) {
            return Type.pay.name().equalsIgnoreCase(typeStr);
        } else {
            for (Type type : types) {
                if (type.name().equalsIgnoreCase(typeStr)) return true;
            }
        }
        return false;
    }

    public boolean isSameBank() {
        return sameBank;
    }

    public PaymentNavigation setSameBank(boolean sameBank) {
        this.sameBank = sameBank;
        return this;
    }

    public boolean isDiffBank() {
        return diffBank;
    }

    public PaymentNavigation setDiffBank(boolean diffBank) {
        this.diffBank = diffBank;
        return this;
    }

    public BigDecimal getMinAmount() {
        return minAmount;
    }

    public PaymentNavigation setMinAmount(BigDecimal minAmount) {
        this.minAmount = minAmount;
        return this;
    }

    public boolean isToPrivate() {
        return toPrivate;
    }

    public PaymentNavigation setToPrivate(boolean toPrivate) {
        this.toPrivate = toPrivate;
        return this;
    }

    public boolean isToCompany() {
        return toCompany;
    }

    public PaymentNavigation setToCompany(boolean toCompany) {
        this.toCompany = toCompany;
        return this;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public PaymentNavigation setBatchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public Type[] getType() {
        return types;
    }

    public PaymentNavigation setType(Type type) {
        this.types = new Type[] { type };
        return this;
    }

    public PaymentNavigation setType(Type types[]) {
        this.types = types;
        return this;
    }

    public PaymentNavigation setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
        return this;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public int getPriority() {
        return priority;
    }

    public PaymentNavigation setPriority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{支付类别=").append(Arrays.toString(types)).append(",");
        sb.append("支持同行=").append(sameBank).append(",").append("支持跨行=").append(diffBank).append(",").append("支持对私=").append(toPrivate).append(",").append("支持对公=").append(toCompany).append(",")
                .append("优先级数值=").append(this.priority).append(",").append("支持批量数=").append(batchSize);

        if (minAmount != null) sb.append(",限定最小金额=").append(minAmount.toString());

        if (maxAmount != null) sb.append(",限定最大金额=").append(maxAmount.toString());

        sb.append("}");
        return sb.toString();
    }

}
