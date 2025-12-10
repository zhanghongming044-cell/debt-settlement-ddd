package com.example.settle.application.command;

import java.math.BigDecimal;

/**
 * 化债回滚命令
 *
 * 退款时触发，回滚已化债金额
 */
public class RollbackDebtCommand {

    /**
     * 订单号
     */
    private final String orderNumber;

    /**
     * 订单详情ID（可选，用于商品级退款）
     */
    private final Long orderDetailId;

    /**
     * 退款金额（元）
     */
    private final BigDecimal refundAmount;

    /**
     * 退款状态（2=部分退款, 3=全额退款）
     */
    private final Integer refundStatus;

    /**
     * 会员用户ID
     */
    private final Long memberUserId;

    public RollbackDebtCommand(String orderNumber, Long orderDetailId,
                               BigDecimal refundAmount, Integer refundStatus, Long memberUserId) {
        this.orderNumber = orderNumber;
        this.orderDetailId = orderDetailId;
        this.refundAmount = refundAmount;
        this.refundStatus = refundStatus;
        this.memberUserId = memberUserId;
    }

    /**
     * 创建订单级回滚命令
     */
    public static RollbackDebtCommand ofOrder(String orderNumber, BigDecimal refundAmount,
                                               Integer refundStatus, Long memberUserId) {
        return new RollbackDebtCommand(orderNumber, null, refundAmount, refundStatus, memberUserId);
    }

    /**
     * 创建商品级回滚命令
     */
    public static RollbackDebtCommand ofProduct(String orderNumber, Long orderDetailId,
                                                 BigDecimal refundAmount, Integer refundStatus, Long memberUserId) {
        return new RollbackDebtCommand(orderNumber, orderDetailId, refundAmount, refundStatus, memberUserId);
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Long getOrderDetailId() {
        return orderDetailId;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public Integer getRefundStatus() {
        return refundStatus;
    }

    public Long getMemberUserId() {
        return memberUserId;
    }

    /**
     * 是否为商品级回滚
     */
    public boolean isProductLevel() {
        return orderDetailId != null;
    }

    /**
     * 是否为全额退款
     */
    public boolean isFullRefund() {
        return refundStatus != null && refundStatus == 3;
    }
}
