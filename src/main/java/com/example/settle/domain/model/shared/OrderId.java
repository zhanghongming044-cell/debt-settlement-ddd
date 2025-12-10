package com.example.settle.domain.model.shared;

import java.util.Objects;

/**
 * 订单ID值对象
 *
 * 封装订单相关ID，强类型避免混淆
 */
public final class OrderId {

    /**
     * 订单号
     */
    private final String orderNumber;

    /**
     * 订单详情ID（可选，用于商品级分账）
     */
    private final Long orderDetailId;

    private OrderId(String orderNumber, Long orderDetailId) {
        this.orderNumber = orderNumber;
        this.orderDetailId = orderDetailId;
    }

    /**
     * 创建订单级ID
     *
     * @param orderNumber 订单号
     * @return OrderId
     */
    public static OrderId of(String orderNumber) {
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        return new OrderId(orderNumber, null);
    }

    /**
     * 创建商品级ID（含订单详情ID）
     *
     * @param orderNumber   订单号
     * @param orderDetailId 订单详情ID
     * @return OrderId
     */
    public static OrderId of(String orderNumber, Long orderDetailId) {
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new IllegalArgumentException("订单号不能为空");
        }
        if (orderDetailId == null) {
            throw new IllegalArgumentException("订单详情ID不能为空");
        }
        return new OrderId(orderNumber, orderDetailId);
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public Long getOrderDetailId() {
        return orderDetailId;
    }

    /**
     * 是否为商品级ID
     *
     * @return true 如果包含订单详情ID
     */
    public boolean isProductLevel() {
        return orderDetailId != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderId orderId = (OrderId) o;
        return Objects.equals(orderNumber, orderId.orderNumber) &&
                Objects.equals(orderDetailId, orderId.orderDetailId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderNumber, orderDetailId);
    }

    @Override
    public String toString() {
        if (orderDetailId != null) {
            return orderNumber + "#" + orderDetailId;
        }
        return orderNumber;
    }
}
