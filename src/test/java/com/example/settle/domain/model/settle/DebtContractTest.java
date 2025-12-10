package com.example.settle.domain.model.settle;

import com.example.settle.domain.event.ContractCompletedEvent;
import com.example.settle.domain.event.DebtRolledBackEvent;
import com.example.settle.domain.event.DebtSettledEvent;
import com.example.settle.domain.event.DomainEvent;
import com.example.settle.domain.event.RepaymentPlanNotMatchedEvent;
import com.example.settle.domain.model.shared.Money;
import com.example.settle.domain.model.shared.OrderId;
import com.example.settle.domain.model.shared.Period;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DebtContract 聚合根单元测试
 */
@DisplayName("DebtContract 聚合根测试")
class DebtContractTest {

    private DebtContract contract;

    @BeforeEach
    void setUp() {
        // 创建合同：总金额 12000 元（12期 × 1000元）
        contract = new DebtContract(1L, 100L, Money.ofYuan("12000"));
        contract.setId(1L);

        // 添加还款计划：2025-01 到 2025-12，每期 1000 元
        for (int month = 1; month <= 12; month++) {
            contract.addRepaymentPlan(Period.of(2025, month), Money.ofYuan("1000"));
        }
    }

    @Nested
    @DisplayName("化债测试")
    class SettleDebtTests {

        @Test
        @DisplayName("正常化债 - 期数匹配")
        void settleDebt_matchedPeriod_shouldSucceed() {
            OrderId orderId = OrderId.of("ORDER001");
            Money amount = Money.ofYuan("500");
            LocalDate orderDate = LocalDate.of(2025, 3, 15); // 2025-03 期

            boolean result = contract.settleDebt(orderId, amount, orderDate);

            assertTrue(result);
            assertEquals(Money.ofYuan("500"), contract.getPaidTotalAmount());
            assertEquals(SettleStatus.SETTLED, contract.getStatus());

            // 检查领域事件
            List<DomainEvent> events = contract.getAndClearDomainEvents();
            assertEquals(1, events.size());
            assertInstanceOf(DebtSettledEvent.class, events.get(0));
        }

        @Test
        @DisplayName("化债 - 期数不匹配")
        void settleDebt_unmatchedPeriod_shouldPublishEvent() {
            OrderId orderId = OrderId.of("ORDER001");
            Money amount = Money.ofYuan("500");
            LocalDate orderDate = LocalDate.of(2024, 1, 15); // 2024-01 期（不存在）

            boolean result = contract.settleDebt(orderId, amount, orderDate);

            assertFalse(result);
            assertEquals(Money.ZERO, contract.getPaidTotalAmount());

            // 应发布"期数不匹配"事件
            List<DomainEvent> events = contract.getAndClearDomainEvents();
            assertEquals(1, events.size());
            assertInstanceOf(RepaymentPlanNotMatchedEvent.class, events.get(0));
        }

        @Test
        @DisplayName("化债金额超过应还 - 只化债到应还金额")
        void settleDebt_amountExceedsDue_shouldCapAtDueAmount() {
            OrderId orderId = OrderId.of("ORDER001");
            Money amount = Money.ofYuan("1500"); // 超过单期应还 1000
            LocalDate orderDate = LocalDate.of(2025, 1, 15);

            contract.settleDebt(orderId, amount, orderDate);

            // 应只化债 1000 元（该期应还金额）
            assertEquals(Money.ofYuan("1000"), contract.getPaidTotalAmount());
        }

        @Test
        @DisplayName("合同完结 - 所有期数还清")
        void settleDebt_allPeriodsCompleted_shouldComplete() {
            // 每期化债 1000 元，共 12 期
            for (int month = 1; month <= 12; month++) {
                OrderId orderId = OrderId.of("ORDER" + month);
                LocalDate orderDate = LocalDate.of(2025, month, 15);
                contract.settleDebt(orderId, Money.ofYuan("1000"), orderDate);
            }

            assertEquals(SettleStatus.COMPLETED, contract.getStatus());
            assertEquals(Money.ofYuan("12000"), contract.getPaidTotalAmount());

            // 应有 ContractCompletedEvent
            List<DomainEvent> events = contract.getAndClearDomainEvents();
            assertTrue(events.stream().anyMatch(e -> e instanceof ContractCompletedEvent));
        }
    }

    @Nested
    @DisplayName("化债回滚测试")
    class RollbackDebtTests {

        @Test
        @DisplayName("正常回滚")
        void rollbackDebt_afterSettle_shouldRollback() {
            // 先化债
            OrderId orderId = OrderId.of("ORDER001");
            LocalDate orderDate = LocalDate.of(2025, 3, 15);
            contract.settleDebt(orderId, Money.ofYuan("500"), orderDate);
            contract.getAndClearDomainEvents(); // 清空事件

            // 回滚
            Money rolledBack = contract.rollbackDebt(orderId, Money.ofYuan("500"));

            assertEquals(Money.ofYuan("500"), rolledBack);
            assertEquals(Money.ZERO, contract.getPaidTotalAmount());
            assertEquals(SettleStatus.ROLLED_BACK, contract.getStatus());

            // 检查领域事件
            List<DomainEvent> events = contract.getAndClearDomainEvents();
            assertEquals(1, events.size());
            assertInstanceOf(DebtRolledBackEvent.class, events.get(0));
        }

        @Test
        @DisplayName("部分回滚")
        void rollbackDebt_partialRefund_shouldPartialRollback() {
            // 先化债 500 元
            OrderId orderId = OrderId.of("ORDER001");
            LocalDate orderDate = LocalDate.of(2025, 3, 15);
            contract.settleDebt(orderId, Money.ofYuan("500"), orderDate);
            contract.getAndClearDomainEvents();

            // 只回滚 200 元
            Money rolledBack = contract.rollbackDebt(orderId, Money.ofYuan("200"));

            assertEquals(Money.ofYuan("200"), rolledBack);
            assertEquals(Money.ofYuan("300"), contract.getPaidTotalAmount());
            assertEquals(SettleStatus.PARTIAL_BACK, contract.getStatus());
        }

        @Test
        @DisplayName("未化债订单回滚 - 返回零")
        void rollbackDebt_noSettlement_shouldReturnZero() {
            OrderId orderId = OrderId.of("ORDER_NOT_SETTLED");

            Money rolledBack = contract.rollbackDebt(orderId, Money.ofYuan("500"));

            assertEquals(Money.ZERO, rolledBack);
        }

        @Test
        @DisplayName("回滚金额超过已化债 - 只回滚已化债金额")
        void rollbackDebt_amountExceedsSettled_shouldCapAtSettledAmount() {
            // 化债 300 元
            OrderId orderId = OrderId.of("ORDER001");
            LocalDate orderDate = LocalDate.of(2025, 3, 15);
            contract.settleDebt(orderId, Money.ofYuan("300"), orderDate);
            contract.getAndClearDomainEvents();

            // 尝试回滚 500 元
            Money rolledBack = contract.rollbackDebt(orderId, Money.ofYuan("500"));

            // 应只回滚 300 元
            assertEquals(Money.ofYuan("300"), rolledBack);
        }
    }

    @Nested
    @DisplayName("聚合不变量测试")
    class InvariantTests {

        @Test
        @DisplayName("同一期数不能重复添加")
        void addRepaymentPlan_duplicatePeriod_shouldThrowException() {
            // setUp 中已添加 2025-01

            assertThrows(IllegalStateException.class, () ->
                    contract.addRepaymentPlan(Period.of(2025, 1), Money.ofYuan("500")));
        }

        @Test
        @DisplayName("还款计划列表不可变")
        void getRepaymentPlans_shouldBeUnmodifiable() {
            List<RepaymentPlan> plans = contract.getRepaymentPlans();

            assertThrows(UnsupportedOperationException.class, () ->
                    plans.add(new RepaymentPlan(Period.of(2026, 1), Money.ofYuan("100"))));
        }
    }
}
