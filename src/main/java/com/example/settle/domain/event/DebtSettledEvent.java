package com.example.settle.domain.event;

import com.example.settle.domain.model.shared.Money;
import com.example.settle.domain.model.shared.OrderId;
import com.example.settle.domain.model.shared.Period;

/**
 * 化债完成事件
 *
 * 触发时机：化债成功后
 * 后续处理：可用于通知、统计等
 */
public class DebtSettledEvent extends DomainEvent {

    /**
     * 合同ID
     */
    private final Long contractId;

    /**
     * 订单ID
     */
    private final OrderId orderId;

    /**
     * 期数
     */
    private final Period period;

    /**
     * 本次化债金额
     */
    private final Money settledAmount;

    /**
     * 累计已还金额
     */
    private final Money totalPaidAmount;

    public DebtSettledEvent(Long contractId, OrderId orderId, Period period,
                            Money settledAmount, Money totalPaidAmount) {
        super();
        this.contractId = contractId;
        this.orderId = orderId;
        this.period = period;
        this.settledAmount = settledAmount;
        this.totalPaidAmount = totalPaidAmount;
    }

    public Long getContractId() {
        return contractId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public Period getPeriod() {
        return period;
    }

    public Money getSettledAmount() {
        return settledAmount;
    }

    public Money getTotalPaidAmount() {
        return totalPaidAmount;
    }

    @Override
    public String toString() {
        return String.format("DebtSettledEvent{contractId=%d, orderId=%s, period=%s, amount=%s}",
                contractId, orderId, period, settledAmount);
    }
}
