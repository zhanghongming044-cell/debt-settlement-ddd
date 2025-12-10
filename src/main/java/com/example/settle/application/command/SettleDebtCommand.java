package com.example.settle.application.command;

import java.time.LocalDate;

/**
 * 化债命令
 *
 * 确认收货后触发，执行化债
 */
public class SettleDebtCommand {

    /**
     * 订单号
     */
    private final String orderNumber;

    /**
     * 订单详情ID（可选，用于商品级化债）
     */
    private final Long orderDetailId;

    /**
     * 订单创建时间（用于匹配期数）
     */
    private final LocalDate orderCreateTime;

    /**
     * 会员用户ID
     */
    private final Long memberUserId;

    public SettleDebtCommand(String orderNumber, Long orderDetailId,
                             LocalDate orderCreateTime, Long memberUserId) {
        this.orderNumber = orderNumber;
        this.orderDetailId = orderDetailId;
        this.orderCreateTime = orderCreateTime;
        this.memberUserId = memberUserId;
    }

    /**
     * 创建订单级化债命令
     */
    public static SettleDebtCommand ofOrder(String orderNumber, LocalDate orderCreateTime, Long memberUserId) {
        return new SettleDebtCommand(orderNumber, null, orderCreateTime, memberUserId);
    }

    /**
     * 创建商品级化债命令
     */
    public static SettleDebtCommand ofProduct(String orderNumber, Long orderDetailId,
                                               LocalDate orderCreateTime, Long memberUserId) {
        return new SettleDebtCommand(orderNumber, orderDetailId, orderCreateTime, memberUserId);
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Long getOrderDetailId() {
        return orderDetailId;
    }

    public LocalDate getOrderCreateTime() {
        return orderCreateTime;
    }

    public Long getMemberUserId() {
        return memberUserId;
    }

    /**
     * 是否为商品级化债
     */
    public boolean isProductLevel() {
        return orderDetailId != null;
    }
}
