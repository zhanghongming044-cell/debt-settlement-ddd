package com.example.settle.domain.model.settle;

import com.example.settle.domain.model.shared.Money;
import com.example.settle.domain.model.shared.OrderId;
import com.example.settle.domain.model.shared.Period;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 还款记录实体
 *
 * 记录每次化债或回滚的明细
 * 属于 DebtContract 聚合，由聚合根管理
 */
public class RepaymentRecord {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 订单ID（关联订单）
     */
    private final OrderId orderId;

    /**
     * 期数
     */
    private final Period period;

    /**
     * 类型：INCOME(化债) / EXPENSE(回滚)
     */
    private final RepaymentType type;

    /**
     * 金额
     */
    private final Money amount;

    /**
     * 记录时间
     */
    private final LocalDateTime recordTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建还款记录
     */
    public RepaymentRecord(OrderId orderId, Period period, RepaymentType type, Money amount) {
        if (orderId == null) {
            throw new IllegalArgumentException("订单ID不能为空");
        }
        if (period == null) {
            throw new IllegalArgumentException("期数不能为空");
        }
        if (type == null) {
            throw new IllegalArgumentException("类型不能为空");
        }
        if (amount == null || amount.isZero()) {
            throw new IllegalArgumentException("金额必须大于0");
        }
        this.orderId = orderId;
        this.period = period;
        this.type = type;
        this.amount = amount;
        this.recordTime = LocalDateTime.now();
    }

    /**
     * 从数据库恢复（仓储层使用）
     */
    public RepaymentRecord(Long id, OrderId orderId, Period period, RepaymentType type,
                           Money amount, LocalDateTime recordTime, String remark) {
        this.id = id;
        this.orderId = orderId;
        this.period = period;
        this.type = type;
        this.amount = amount;
        this.recordTime = recordTime;
        this.remark = remark;
    }

    /**
     * 创建化债记录
     */
    public static RepaymentRecord createIncome(OrderId orderId, Period period, Money amount) {
        RepaymentRecord record = new RepaymentRecord(orderId, period, RepaymentType.INCOME, amount);
        record.remark = "化债";
        return record;
    }

    /**
     * 创建回滚记录（退款）
     */
    public static RepaymentRecord createExpense(OrderId orderId, Period period, Money amount) {
        RepaymentRecord record = new RepaymentRecord(orderId, period, RepaymentType.EXPENSE, amount);
        record.remark = "退款回滚";
        return record;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public Period getPeriod() {
        return period;
    }

    public RepaymentType getType() {
        return type;
    }

    public Money getAmount() {
        return amount;
    }

    public LocalDateTime getRecordTime() {
        return recordTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepaymentRecord that = (RepaymentRecord) o;
        if (id != null && that.id != null) {
            return Objects.equals(id, that.id);
        }
        return Objects.equals(orderId, that.orderId) &&
                Objects.equals(period, that.period) &&
                type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id != null ? id : Objects.hash(orderId, period, type));
    }

    @Override
    public String toString() {
        return String.format("RepaymentRecord{orderId=%s, period=%s, type=%s, amount=%s}",
                orderId, period, type, amount);
    }
}
