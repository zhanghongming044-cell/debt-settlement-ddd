package com.example.settle.domain.model.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 金额值对象
 *
 * 特点：
 * 1. 不可变 - 所有字段 final，无 setter
 * 2. 自封装 - 金额计算逻辑内聚
 * 3. 值相等 - 通过值比较而非引用比较
 *
 * 使用 BigDecimal 避免浮点精度问题，统一使用分为单位存储
 */
public final class Money {

    /**
     * 金额（单位：分）
     */
    private final long cents;

    /**
     * 零元
     */
    public static final Money ZERO = new Money(0L);

    private Money(long cents) {
        this.cents = cents;
    }

    /**
     * 从分创建
     *
     * @param cents 分
     * @return Money
     */
    public static Money ofCents(long cents) {
        if (cents < 0) {
            throw new IllegalArgumentException("金额不能为负数: " + cents);
        }
        return cents == 0 ? ZERO : new Money(cents);
    }

    /**
     * 从元创建
     *
     * @param yuan 元
     * @return Money
     */
    public static Money ofYuan(BigDecimal yuan) {
        if (yuan == null) {
            throw new IllegalArgumentException("金额不能为空");
        }
        if (yuan.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("金额不能为负数: " + yuan);
        }
        // 元转分，四舍五入
        long cents = yuan.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
        return ofCents(cents);
    }

    /**
     * 从元创建（字符串）
     *
     * @param yuan 元（字符串格式）
     * @return Money
     */
    public static Money ofYuan(String yuan) {
        if (yuan == null || yuan.isBlank()) {
            throw new IllegalArgumentException("金额不能为空");
        }
        return ofYuan(new BigDecimal(yuan));
    }

    /**
     * 获取分值
     *
     * @return 分
     */
    public long getCents() {
        return cents;
    }

    /**
     * 获取元值
     *
     * @return 元
     */
    public BigDecimal getYuan() {
        return BigDecimal.valueOf(cents)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 加法
     *
     * @param other 另一个金额
     * @return 新的金额
     */
    public Money add(Money other) {
        if (other == null) {
            return this;
        }
        return ofCents(this.cents + other.cents);
    }

    /**
     * 减法
     *
     * @param other 另一个金额
     * @return 新的金额
     * @throws IllegalArgumentException 如果结果为负数
     */
    public Money subtract(Money other) {
        if (other == null) {
            return this;
        }
        long result = this.cents - other.cents;
        if (result < 0) {
            throw new IllegalArgumentException(
                    String.format("金额不足: %d分 - %d分 = %d分", this.cents, other.cents, result));
        }
        return ofCents(result);
    }

    /**
     * 乘法
     *
     * @param multiplier 乘数
     * @return 新的金额
     */
    public Money multiply(int multiplier) {
        if (multiplier < 0) {
            throw new IllegalArgumentException("乘数不能为负数: " + multiplier);
        }
        return ofCents(this.cents * multiplier);
    }

    /**
     * 是否为零
     *
     * @return true 如果金额为零
     */
    public boolean isZero() {
        return this.cents == 0;
    }

    /**
     * 是否大于
     *
     * @param other 另一个金额
     * @return true 如果大于
     */
    public boolean isGreaterThan(Money other) {
        return this.cents > other.cents;
    }

    /**
     * 是否大于等于
     *
     * @param other 另一个金额
     * @return true 如果大于等于
     */
    public boolean isGreaterThanOrEqual(Money other) {
        return this.cents >= other.cents;
    }

    /**
     * 是否小于
     *
     * @param other 另一个金额
     * @return true 如果小于
     */
    public boolean isLessThan(Money other) {
        return this.cents < other.cents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return cents == money.cents;
    }

    @Override
    public int hashCode() {
        return Objects.hash(cents);
    }

    @Override
    public String toString() {
        return getYuan().toPlainString() + "元";
    }
}
