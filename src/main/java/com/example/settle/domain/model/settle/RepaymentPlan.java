package com.example.settle.domain.model.settle;

import com.example.settle.domain.model.shared.Money;
import com.example.settle.domain.model.shared.Period;

import java.util.Objects;

/**
 * 还款计划实体
 *
 * 属于 DebtContract 聚合，由聚合根管理生命周期
 */
public class RepaymentPlan {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 期数
     */
    private final Period period;

    /**
     * 应还金额
     */
    private final Money dueAmount;

    /**
     * 已还金额
     */
    private Money paidAmount;

    /**
     * 是否已完成
     */
    private boolean completed;

    /**
     * 创建还款计划
     *
     * @param period    期数
     * @param dueAmount 应还金额
     */
    public RepaymentPlan(Period period, Money dueAmount) {
        if (period == null) {
            throw new IllegalArgumentException("期数不能为空");
        }
        if (dueAmount == null || dueAmount.isZero()) {
            throw new IllegalArgumentException("应还金额必须大于0");
        }
        this.period = period;
        this.dueAmount = dueAmount;
        this.paidAmount = Money.ZERO;
        this.completed = false;
    }

    /**
     * 从数据库恢复（仓储层使用）
     */
    public RepaymentPlan(Long id, Period period, Money dueAmount, Money paidAmount, boolean completed) {
        this.id = id;
        this.period = period;
        this.dueAmount = dueAmount;
        this.paidAmount = paidAmount;
        this.completed = completed;
    }

    /**
     * 记录还款
     *
     * @param amount 还款金额
     * @return 实际还款金额（不超过剩余应还）
     */
    Money recordPayment(Money amount) {
        Money remaining = getRemainingAmount();
        if (remaining.isZero()) {
            return Money.ZERO;
        }

        // 实际还款金额 = min(还款金额, 剩余应还)
        Money actualPayment = amount.isGreaterThan(remaining) ? remaining : amount;
        this.paidAmount = this.paidAmount.add(actualPayment);

        // 检查是否完成
        if (this.paidAmount.isGreaterThanOrEqual(this.dueAmount)) {
            this.completed = true;
        }

        return actualPayment;
    }

    /**
     * 回滚还款（退款时调用）
     *
     * @param amount 回滚金额
     * @return 实际回滚金额（不超过已还金额）
     */
    Money rollbackPayment(Money amount) {
        if (this.paidAmount.isZero()) {
            return Money.ZERO;
        }

        // 实际回滚金额 = min(回滚金额, 已还金额)
        Money actualRollback = amount.isGreaterThan(this.paidAmount) ? this.paidAmount : amount;
        this.paidAmount = this.paidAmount.subtract(actualRollback);

        // 回滚后标记为未完成
        if (this.completed && this.paidAmount.isLessThan(this.dueAmount)) {
            this.completed = false;
        }

        return actualRollback;
    }

    /**
     * 获取剩余应还金额
     *
     * @return 剩余应还金额
     */
    public Money getRemainingAmount() {
        if (paidAmount.isGreaterThanOrEqual(dueAmount)) {
            return Money.ZERO;
        }
        return dueAmount.subtract(paidAmount);
    }

    // Getters

    public Long getId() {
        return id;
    }

    public Period getPeriod() {
        return period;
    }

    public Money getDueAmount() {
        return dueAmount;
    }

    public Money getPaidAmount() {
        return paidAmount;
    }

    public boolean isCompleted() {
        return completed;
    }

    /**
     * 设置ID（仓储层使用）
     */
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepaymentPlan that = (RepaymentPlan) o;
        // 实体使用ID比较（如果有ID）
        if (id != null && that.id != null) {
            return Objects.equals(id, that.id);
        }
        // 否则使用业务键（期数）
        return Objects.equals(period, that.period);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : period);
    }

    @Override
    public String toString() {
        return String.format("RepaymentPlan{period=%s, due=%s, paid=%s, completed=%s}",
                period, dueAmount, paidAmount, completed);
    }
}
