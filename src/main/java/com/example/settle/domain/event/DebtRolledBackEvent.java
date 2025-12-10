package com.example.settle.domain.event;

import com.example.settle.domain.model.shared.Money;
import com.example.settle.domain.model.shared.OrderId;

/**
 * 化债回滚事件
 *
 * 触发时机：退款导致化债回滚后
 * 后续处理：可用于通知、统计、审计等
 */
public class DebtRolledBackEvent extends DomainEvent {

    /**
     * 合同ID
     */
    private final Long contractId;

    /**
     * 订单ID
     */
    private final OrderId orderId;

    /**
     * 回滚金额
     */
    private final Money rolledBackAmount;

    /**
     * 回滚后的累计已还金额
     */
    private final Money totalPaidAmount;

    public DebtRolledBackEvent(Long contractId, OrderId orderId,
                               Money rolledBackAmount, Money totalPaidAmount) {
        super();
        this.contractId = contractId;
        this.orderId = orderId;
        this.rolledBackAmount = rolledBackAmount;
        this.totalPaidAmount = totalPaidAmount;
    }

    public Long getContractId() {
        return contractId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public Money getRolledBackAmount() {
        return rolledBackAmount;
    }

    public Money getTotalPaidAmount() {
        return totalPaidAmount;
    }

    @Override
    public String toString() {
        return String.format("DebtRolledBackEvent{contractId=%d, orderId=%s, rolledBack=%s}",
                contractId, orderId, rolledBackAmount);
    }
}
