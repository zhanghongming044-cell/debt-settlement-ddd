package com.example.settle.domain.model.settle;

/**
 * 还款记录类型枚举
 */
public enum RepaymentType {

    /**
     * 收入 - 化债（债务减少）
     */
    INCOME("收入"),

    /**
     * 支出 - 退款回滚（债务恢复）
     */
    EXPENSE("支出");

    private final String description;

    RepaymentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
