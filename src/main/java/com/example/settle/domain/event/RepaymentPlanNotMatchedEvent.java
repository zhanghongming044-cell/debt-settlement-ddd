package com.example.settle.domain.event;

import com.example.settle.domain.model.shared.Money;
import com.example.settle.domain.model.shared.OrderId;
import com.example.settle.domain.model.shared.Period;

/**
 * 还款计划未匹配事件
 *
 * 触发时机：化债时未找到匹配的期数
 * 后续处理：可用于告警、人工处理等
 */
public class RepaymentPlanNotMatchedEvent extends DomainEvent {

    /**
     * 合同ID
     */
    private final Long contractId;

    /**
     * 订单ID
     */
    private final OrderId orderId;

    /**
     * 期数（根据订单创建时间推算）
     */
    private final Period period;

    /**
     * 化债金额（未能入账）
     */
    private final Money amount;

    public RepaymentPlanNotMatchedEvent(Long contractId, OrderId orderId,
                                        Period period, Money amount) {
        super();
        this.contractId = contractId;
        this.orderId = orderId;
        this.period = period;
        this.amount = amount;
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

    public Money getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return String.format("RepaymentPlanNotMatchedEvent{contractId=%d, orderId=%s, period=%s, amount=%s}",
                contractId, orderId, period, amount);
    }
}
