package com.example.settle.domain.model.settle;

import com.example.settle.domain.event.ContractCompletedEvent;
import com.example.settle.domain.event.DebtRolledBackEvent;
import com.example.settle.domain.event.DebtSettledEvent;
import com.example.settle.domain.event.DomainEvent;
import com.example.settle.domain.event.RepaymentPlanNotMatchedEvent;
import com.example.settle.domain.model.shared.Money;
import com.example.settle.domain.model.shared.OrderId;
import com.example.settle.domain.model.shared.Period;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 债务合同聚合根
 *
 * 职责：
 * 1. 管理还款计划列表
 * 2. 管理还款记录列表
 * 3. 执行化债（分账金额计入还款）
 * 4. 执行化债回滚（退款时恢复债务）
 * 5. 维护聚合不变量（数据一致性）
 * 6. 发布领域事件
 *
 * 聚合边界：
 * - 包含：RepaymentPlan（还款计划）、RepaymentRecord（还款记录）
 * - 所有对子实体的修改必须通过聚合根
 */
public class DebtContract {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 委案ID（关联委案）
     */
    private final Long caseEntrustId;

    /**
     * 会员用户ID
     */
    private final Long memberUserId;

    /**
     * 合同总金额
     */
    private final Money totalAmount;

    /**
     * 已还总金额
     */
    private Money paidTotalAmount;

    /**
     * 状态
     */
    private SettleStatus status;

    /**
     * 创建时间
     */
    private final LocalDateTime createTime;

    /**
     * 还款计划列表（聚合内实体）
     */
    private final List<RepaymentPlan> repaymentPlans;

    /**
     * 还款记录列表（聚合内实体）
     */
    private final List<RepaymentRecord> repaymentRecords;

    /**
     * 领域事件列表（待发布）
     */
    private final List<DomainEvent> domainEvents;

    /**
     * 创建新合同
     *
     * @param caseEntrustId 委案ID
     * @param memberUserId  会员用户ID
     * @param totalAmount   合同总金额
     */
    public DebtContract(Long caseEntrustId, Long memberUserId, Money totalAmount) {
        if (caseEntrustId == null) {
            throw new IllegalArgumentException("委案ID不能为空");
        }
        if (memberUserId == null) {
            throw new IllegalArgumentException("会员用户ID不能为空");
        }
        if (totalAmount == null || totalAmount.isZero()) {
            throw new IllegalArgumentException("合同总金额必须大于0");
        }
        this.caseEntrustId = caseEntrustId;
        this.memberUserId = memberUserId;
        this.totalAmount = totalAmount;
        this.paidTotalAmount = Money.ZERO;
        this.status = SettleStatus.PENDING;
        this.createTime = LocalDateTime.now();
        this.repaymentPlans = new ArrayList<>();
        this.repaymentRecords = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
    }

    /**
     * 从数据库恢复（仓储层使用）
     */
    public DebtContract(Long id, Long caseEntrustId, Long memberUserId, Money totalAmount,
                        Money paidTotalAmount, SettleStatus status, LocalDateTime createTime,
                        List<RepaymentPlan> plans, List<RepaymentRecord> records) {
        this.id = id;
        this.caseEntrustId = caseEntrustId;
        this.memberUserId = memberUserId;
        this.totalAmount = totalAmount;
        this.paidTotalAmount = paidTotalAmount;
        this.status = status;
        this.createTime = createTime;
        this.repaymentPlans = new ArrayList<>(plans != null ? plans : Collections.emptyList());
        this.repaymentRecords = new ArrayList<>(records != null ? records : Collections.emptyList());
        this.domainEvents = new ArrayList<>();
    }

    // ==================== 聚合行为方法 ====================

    /**
     * 添加还款计划
     *
     * @param period    期数
     * @param dueAmount 应还金额
     */
    public void addRepaymentPlan(Period period, Money dueAmount) {
        // 不变量：同一期数不能重复
        boolean exists = repaymentPlans.stream()
                .anyMatch(p -> p.getPeriod().equals(period));
        if (exists) {
            throw new IllegalStateException("期数已存在: " + period);
        }
        repaymentPlans.add(new RepaymentPlan(period, dueAmount));
    }

    /**
     * 执行化债
     *
     * 业务规则：
     * 1. 根据订单创建时间匹配期数
     * 2. 如果未匹配到期数，发布领域事件但不报错
     * 3. 化债金额计入对应期数的已还金额
     * 4. 创建还款记录（type=INCOME）
     * 5. 检查合同是否完结
     *
     * @param orderId         订单ID
     * @param amount          化债金额（分账金额）
     * @param orderCreateTime 订单创建时间（用于匹配期数）
     * @return true 如果化债成功
     */
    public boolean settleDebt(OrderId orderId, Money amount, LocalDate orderCreateTime) {
        if (orderId == null || amount == null || amount.isZero()) {
            throw new IllegalArgumentException("订单ID和金额不能为空");
        }

        // 根据订单创建时间匹配期数
        Period period = Period.fromDate(orderCreateTime);
        Optional<RepaymentPlan> matchedPlan = findPlanByPeriod(period);

        if (matchedPlan.isEmpty()) {
            // 未匹配到期数，发布事件但不报错
            domainEvents.add(new RepaymentPlanNotMatchedEvent(
                    this.id, orderId, period, amount));
            return false;
        }

        // 执行化债
        RepaymentPlan plan = matchedPlan.get();
        Money actualPaid = plan.recordPayment(amount);

        if (actualPaid.isZero()) {
            // 该期已还清
            return false;
        }

        // 更新合同已还总额
        this.paidTotalAmount = this.paidTotalAmount.add(actualPaid);

        // 创建还款记录
        RepaymentRecord record = RepaymentRecord.createIncome(orderId, period, actualPaid);
        repaymentRecords.add(record);

        // 更新状态
        this.status = SettleStatus.SETTLED;

        // 发布化债完成事件
        domainEvents.add(new DebtSettledEvent(
                this.id, orderId, period, actualPaid, this.paidTotalAmount));

        // 检查合同是否完结
        checkContractCompletion();

        return true;
    }

    /**
     * 执行化债回滚（退款时调用）
     *
     * 业务规则：
     * 1. 查找该订单的化债记录
     * 2. 回滚对应期数的已还金额
     * 3. 创建还款记录（type=EXPENSE）
     * 4. 更新合同状态
     *
     * @param orderId      订单ID
     * @param refundAmount 退款金额
     * @return 实际回滚金额
     */
    public Money rollbackDebt(OrderId orderId, Money refundAmount) {
        if (orderId == null || refundAmount == null || refundAmount.isZero()) {
            throw new IllegalArgumentException("订单ID和退款金额不能为空");
        }

        // 查找该订单的化债记录（可能有多条，按商品级别）
        List<RepaymentRecord> incomeRecords = repaymentRecords.stream()
                .filter(r -> r.getOrderId().equals(orderId) && r.getType() == RepaymentType.INCOME)
                .toList();

        if (incomeRecords.isEmpty()) {
            // 该订单未化债，无需回滚
            return Money.ZERO;
        }

        // 计算该订单已化债总额
        Money totalSettled = incomeRecords.stream()
                .map(RepaymentRecord::getAmount)
                .reduce(Money.ZERO, Money::add);

        // 实际回滚金额 = min(退款金额, 已化债金额)
        Money actualRollback = refundAmount.isGreaterThan(totalSettled) ? totalSettled : refundAmount;

        if (actualRollback.isZero()) {
            return Money.ZERO;
        }

        // 按期数回滚（先回滚最近的期数）
        Money remainingRollback = actualRollback;
        List<Period> periods = incomeRecords.stream()
                .map(RepaymentRecord::getPeriod)
                .distinct()
                .sorted((p1, p2) -> p2.getYearMonth().compareTo(p1.getYearMonth())) // 倒序
                .toList();

        for (Period period : periods) {
            if (remainingRollback.isZero()) {
                break;
            }

            Optional<RepaymentPlan> plan = findPlanByPeriod(period);
            if (plan.isPresent()) {
                Money rolled = plan.get().rollbackPayment(remainingRollback);
                if (!rolled.isZero()) {
                    // 创建回滚记录
                    RepaymentRecord expenseRecord = RepaymentRecord.createExpense(orderId, period, rolled);
                    repaymentRecords.add(expenseRecord);
                    remainingRollback = remainingRollback.subtract(rolled);
                }
            }
        }

        // 更新合同已还总额
        Money actualRolledBack = actualRollback.subtract(remainingRollback);
        this.paidTotalAmount = this.paidTotalAmount.subtract(actualRolledBack);

        // 更新状态
        if (actualRolledBack.equals(totalSettled)) {
            this.status = SettleStatus.ROLLED_BACK;
        } else {
            this.status = SettleStatus.PARTIAL_BACK;
        }

        // 发布回滚事件
        domainEvents.add(new DebtRolledBackEvent(
                this.id, orderId, actualRolledBack, this.paidTotalAmount));

        return actualRolledBack;
    }

    /**
     * 检查合同是否完结
     */
    private void checkContractCompletion() {
        boolean allCompleted = repaymentPlans.stream().allMatch(RepaymentPlan::isCompleted);
        if (allCompleted && !repaymentPlans.isEmpty()) {
            this.status = SettleStatus.COMPLETED;
            domainEvents.add(new ContractCompletedEvent(this.id, this.paidTotalAmount));
        }
    }

    /**
     * 根据期数查找还款计划
     */
    private Optional<RepaymentPlan> findPlanByPeriod(Period period) {
        return repaymentPlans.stream()
                .filter(p -> p.getPeriod().equals(period))
                .findFirst();
    }

    /**
     * 获取并清空领域事件
     *
     * @return 待发布的领域事件列表
     */
    public List<DomainEvent> getAndClearDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    // ==================== Getters ====================

    public Long getId() {
        return id;
    }

    public Long getCaseEntrustId() {
        return caseEntrustId;
    }

    public Long getMemberUserId() {
        return memberUserId;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public Money getPaidTotalAmount() {
        return paidTotalAmount;
    }

    public SettleStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public List<RepaymentPlan> getRepaymentPlans() {
        return Collections.unmodifiableList(repaymentPlans);
    }

    public List<RepaymentRecord> getRepaymentRecords() {
        return Collections.unmodifiableList(repaymentRecords);
    }

    /**
     * 获取剩余应还金额
     */
    public Money getRemainingAmount() {
        if (paidTotalAmount.isGreaterThanOrEqual(totalAmount)) {
            return Money.ZERO;
        }
        return totalAmount.subtract(paidTotalAmount);
    }

    /**
     * 设置ID（仓储层使用）
     */
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DebtContract that = (DebtContract) o;
        // 聚合根使用ID比较
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("DebtContract{id=%d, caseEntrustId=%d, total=%s, paid=%s, status=%s}",
                id, caseEntrustId, totalAmount, paidTotalAmount, status);
    }
}
