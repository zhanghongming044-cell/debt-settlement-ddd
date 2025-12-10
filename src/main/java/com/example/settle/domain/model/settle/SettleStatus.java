package com.example.settle.domain.model.settle;

/**
 * 化债状态枚举
 *
 * 状态流转：
 * PENDING → DIVIDED → SETTLED → (ROLLED_BACK/PARTIAL_BACK/COMPLETED)
 */
public enum SettleStatus {

    /**
     * 待分账 - 支付成功，等待确认收货
     */
    PENDING("待分账"),

    /**
     * 已分账 - 确认收货后，分账完成
     */
    DIVIDED("已分账"),

    /**
     * 已化债 - 分账金额已计入债务减少
     */
    SETTLED("已化债"),

    /**
     * 已回滚 - 全额退款，化债金额已回滚
     */
    ROLLED_BACK("已回滚"),

    /**
     * 部分回滚 - 部分退款，化债金额部分回滚
     */
    PARTIAL_BACK("部分回滚"),

    /**
     * 已完结 - 合同所有期数还清
     */
    COMPLETED("已完结");

    private final String description;

    SettleStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
