package com.example.settle.domain.model.shared;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Money 值对象单元测试
 */
@DisplayName("Money 值对象测试")
class MoneyTest {

    @Nested
    @DisplayName("创建测试")
    class CreationTests {

        @Test
        @DisplayName("从分创建")
        void ofCents_shouldCreateMoney() {
            Money money = Money.ofCents(100);

            assertEquals(100, money.getCents());
            assertEquals(new BigDecimal("1.00"), money.getYuan());
        }

        @Test
        @DisplayName("从元创建 - BigDecimal")
        void ofYuan_bigDecimal_shouldCreateMoney() {
            Money money = Money.ofYuan(new BigDecimal("10.50"));

            assertEquals(1050, money.getCents());
            assertEquals(new BigDecimal("10.50"), money.getYuan());
        }

        @Test
        @DisplayName("从元创建 - 字符串")
        void ofYuan_string_shouldCreateMoney() {
            Money money = Money.ofYuan("99.99");

            assertEquals(9999, money.getCents());
        }

        @Test
        @DisplayName("零元")
        void zero_shouldReturnZeroMoney() {
            assertEquals(0, Money.ZERO.getCents());
            assertTrue(Money.ZERO.isZero());
        }

        @Test
        @DisplayName("负数应抛异常")
        void ofCents_negative_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () -> Money.ofCents(-1));
        }

        @Test
        @DisplayName("空值应抛异常")
        void ofYuan_null_shouldThrowException() {
            assertThrows(IllegalArgumentException.class, () -> Money.ofYuan((BigDecimal) null));
            assertThrows(IllegalArgumentException.class, () -> Money.ofYuan((String) null));
        }
    }

    @Nested
    @DisplayName("计算测试")
    class CalculationTests {

        @Test
        @DisplayName("加法")
        void add_shouldReturnSum() {
            Money a = Money.ofCents(100);
            Money b = Money.ofCents(200);

            Money result = a.add(b);

            assertEquals(300, result.getCents());
            // 验证不可变性
            assertEquals(100, a.getCents());
            assertEquals(200, b.getCents());
        }

        @Test
        @DisplayName("减法")
        void subtract_shouldReturnDifference() {
            Money a = Money.ofCents(300);
            Money b = Money.ofCents(100);

            Money result = a.subtract(b);

            assertEquals(200, result.getCents());
        }

        @Test
        @DisplayName("减法结果为负应抛异常")
        void subtract_negativeResult_shouldThrowException() {
            Money a = Money.ofCents(100);
            Money b = Money.ofCents(200);

            assertThrows(IllegalArgumentException.class, () -> a.subtract(b));
        }

        @Test
        @DisplayName("乘法")
        void multiply_shouldReturnProduct() {
            Money money = Money.ofCents(100);

            Money result = money.multiply(3);

            assertEquals(300, result.getCents());
        }
    }

    @Nested
    @DisplayName("比较测试")
    class ComparisonTests {

        @Test
        @DisplayName("大于")
        void isGreaterThan_shouldCompare() {
            Money a = Money.ofCents(200);
            Money b = Money.ofCents(100);

            assertTrue(a.isGreaterThan(b));
            assertFalse(b.isGreaterThan(a));
        }

        @Test
        @DisplayName("大于等于")
        void isGreaterThanOrEqual_shouldCompare() {
            Money a = Money.ofCents(100);
            Money b = Money.ofCents(100);

            assertTrue(a.isGreaterThanOrEqual(b));
        }

        @Test
        @DisplayName("小于")
        void isLessThan_shouldCompare() {
            Money a = Money.ofCents(100);
            Money b = Money.ofCents(200);

            assertTrue(a.isLessThan(b));
        }
    }

    @Nested
    @DisplayName("相等性测试")
    class EqualityTests {

        @Test
        @DisplayName("值相等")
        void equals_sameValue_shouldBeEqual() {
            Money a = Money.ofCents(100);
            Money b = Money.ofCents(100);

            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("值不等")
        void equals_differentValue_shouldNotBeEqual() {
            Money a = Money.ofCents(100);
            Money b = Money.ofCents(200);

            assertNotEquals(a, b);
        }
    }

    @Test
    @DisplayName("toString 输出")
    void toString_shouldFormatCorrectly() {
        Money money = Money.ofYuan("123.45");

        assertEquals("123.45元", money.toString());
    }
}
