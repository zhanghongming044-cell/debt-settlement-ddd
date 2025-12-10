package com.example.settle.domain.event;

import com.example.settle.domain.model.shared.Money;

/**
 * 合同完结事件
 *
 * 触发时机：合同所有期数都已还清
 * 后续处理：可用于通知用户、更新委案状态等
 */
public class ContractCompletedEvent extends DomainEvent {

    /**
     * 合同ID
     */
    private final Long contractId;

    /**
     * 最终还款总额
     */
    private final Money finalPaidAmount;

    public ContractCompletedEvent(Long contractId, Money finalPaidAmount) {
        super();
        this.contractId = contractId;
        this.finalPaidAmount = finalPaidAmount;
    }

    public Long getContractId() {
        return contractId;
    }

    public Money getFinalPaidAmount() {
        return finalPaidAmount;
    }

    @Override
    public String toString() {
        return String.format("ContractCompletedEvent{contractId=%d, finalPaid=%s}",
                contractId, finalPaidAmount);
    }
}
