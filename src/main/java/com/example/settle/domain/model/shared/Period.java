package com.example.settle.domain.model.shared;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

/**
 * 期数值对象
 *
 * 表示还款计划的期数，格式：YYYY-MM
 */
public final class Period {

    /**
     * 年月
     */
    private final YearMonth yearMonth;

    private Period(YearMonth yearMonth) {
        this.yearMonth = yearMonth;
    }

    /**
     * 从年月创建
     *
     * @param year  年
     * @param month 月
     * @return Period
     */
    public static Period of(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("月份必须在1-12之间: " + month);
        }
        return new Period(YearMonth.of(year, month));
    }

    /**
     * 从 YearMonth 创建
     *
     * @param yearMonth YearMonth
     * @return Period
     */
    public static Period of(YearMonth yearMonth) {
        if (yearMonth == null) {
            throw new IllegalArgumentException("年月不能为空");
        }
        return new Period(yearMonth);
    }

    /**
     * 从字符串创建，格式：YYYY-MM
     *
     * @param periodStr 期数字符串
     * @return Period
     */
    public static Period parse(String periodStr) {
        if (periodStr == null || periodStr.isBlank()) {
            throw new IllegalArgumentException("期数字符串不能为空");
        }
        return new Period(YearMonth.parse(periodStr));
    }

    /**
     * 从日期推断期数
     *
     * @param date 日期
     * @return Period
     */
    public static Period fromDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("日期不能为空");
        }
        return new Period(YearMonth.from(date));
    }

    /**
     * 获取当前期数
     *
     * @return 当前月份的期数
     */
    public static Period current() {
        return new Period(YearMonth.now());
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public int getYear() {
        return yearMonth.getYear();
    }

    public int getMonth() {
        return yearMonth.getMonthValue();
    }

    /**
     * 下一期
     *
     * @return 下一个月的期数
     */
    public Period next() {
        return new Period(yearMonth.plusMonths(1));
    }

    /**
     * 上一期
     *
     * @return 上一个月的期数
     */
    public Period previous() {
        return new Period(yearMonth.minusMonths(1));
    }

    /**
     * 是否在指定日期的期数内
     *
     * @param date 日期
     * @return true 如果日期属于此期数
     */
    public boolean contains(LocalDate date) {
        if (date == null) {
            return false;
        }
        return YearMonth.from(date).equals(yearMonth);
    }

    /**
     * 是否在指定期数之前
     *
     * @param other 另一个期数
     * @return true 如果在之前
     */
    public boolean isBefore(Period other) {
        return yearMonth.isBefore(other.yearMonth);
    }

    /**
     * 是否在指定期数之后
     *
     * @param other 另一个期数
     * @return true 如果在之后
     */
    public boolean isAfter(Period other) {
        return yearMonth.isAfter(other.yearMonth);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Period period = (Period) o;
        return Objects.equals(yearMonth, period.yearMonth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(yearMonth);
    }

    @Override
    public String toString() {
        return yearMonth.toString();
    }
}
