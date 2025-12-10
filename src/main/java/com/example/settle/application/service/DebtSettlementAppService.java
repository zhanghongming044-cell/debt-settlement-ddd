package com.example.settle.application.service;

import com.example.settle.application.command.RollbackDebtCommand;
import com.example.settle.application.command.SettleDebtCommand;
import com.example.settle.domain.event.DomainEvent;
import com.example.settle.domain.model.settle.DebtContract;
import com.example.settle.domain.model.shared.Money;
import com.example.settle.domain.model.shared.OrderId;
import com.example.settle.domain.repository.DebtContractRepository;
import com.example.settle.domain.repository.DivideRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * 化债应用服务
 *
 * 职责：
 * 1. 编排领域对象完成化债/回滚用例
 * 2. 事务管理
 * 3. 调用防腐层获取外部数据
 * 4. 发布领域事件
 *
 * 原则：
 * - 不包含业务逻辑，业务逻辑在领域层
 * - 薄服务层，只做编排
 */
@Service
public class DebtSettlementAppService {

    private static final Logger log = LoggerFactory.getLogger(DebtSettlementAppService.class);

    @Resource
    private DebtContractRepository debtContractRepository;

    @Resource
    private DivideRecordRepository divideRecordRepository;

    @Resource
    private ApplicationEventPublisher eventPublisher;

    /**
     * 执行化债
     *
     * 业务流程：
     * 1. 查询分账金额（从分账记录）
     * 2. 查询或创建债务合同
     * 3. 调用聚合根执行化债
     * 4. 保存合同
     * 5. 发布领域事件
     *
     * @param command 化债命令
     * @return true 如果化债成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean settleDebt(SettleDebtCommand command) {
        log.info("开始执行化债, orderNumber={}, memberUserId={}",
                command.getOrderNumber(), command.getMemberUserId());

        // 1. 构建订单ID
        OrderId orderId = command.isProductLevel()
                ? OrderId.of(command.getOrderNumber(), command.getOrderDetailId())
                : OrderId.of(command.getOrderNumber());

        // 2. 查询分账金额（供应商分账）
        Money divideAmount = command.isProductLevel()
                ? divideRecordRepository.getSupplierDivideAmountByOrderId(orderId)
                : divideRecordRepository.getSupplierDivideAmount(command.getOrderNumber());

        if (divideAmount == null || divideAmount.isZero()) {
            log.warn("未找到分账记录或分账金额为0, orderNumber={}", command.getOrderNumber());
            return false;
        }

        // 3. 查询债务合同
        Optional<DebtContract> contractOpt = debtContractRepository
                .findByMemberUserId(command.getMemberUserId());

        if (contractOpt.isEmpty()) {
            log.warn("未找到债务合同, memberUserId={}", command.getMemberUserId());
            return false;
        }

        DebtContract contract = contractOpt.get();

        // 4. 执行化债（核心业务逻辑在聚合根内）
        boolean settled = contract.settleDebt(orderId, divideAmount, command.getOrderCreateTime());

        if (!settled) {
            log.info("化债未执行（可能期数不匹配或已还清）, orderNumber={}", command.getOrderNumber());
        }

        // 5. 保存合同
        debtContractRepository.save(contract);

        // 6. 发布领域事件
        publishDomainEvents(contract.getAndClearDomainEvents());

        log.info("化债完成, orderNumber={}, settled={}", command.getOrderNumber(), settled);
        return settled;
    }

    /**
     * 执行化债回滚（退款时调用）
     *
     * 业务流程：
     * 1. 构建订单ID和退款金额
     * 2. 查询债务合同
     * 3. 调用聚合根执行回滚
     * 4. 保存合同
     * 5. 发布领域事件
     *
     * @param command 回滚命令
     * @return 实际回滚金额
     */
    @Transactional(rollbackFor = Exception.class)
    public Money rollbackDebt(RollbackDebtCommand command) {
        log.info("开始执行化债回滚, orderNumber={}, refundAmount={}, refundStatus={}",
                command.getOrderNumber(), command.getRefundAmount(), command.getRefundStatus());

        // 1. 构建订单ID
        OrderId orderId = command.isProductLevel()
                ? OrderId.of(command.getOrderNumber(), command.getOrderDetailId())
                : OrderId.of(command.getOrderNumber());

        // 2. 构建退款金额
        Money refundAmount = Money.ofYuan(command.getRefundAmount());

        // 3. 查询债务合同
        Optional<DebtContract> contractOpt = debtContractRepository
                .findByMemberUserId(command.getMemberUserId());

        if (contractOpt.isEmpty()) {
            log.warn("未找到债务合同, memberUserId={}", command.getMemberUserId());
            return Money.ZERO;
        }

        DebtContract contract = contractOpt.get();

        // 4. 执行回滚（核心业务逻辑在聚合根内）
        Money actualRolledBack = contract.rollbackDebt(orderId, refundAmount);

        if (actualRolledBack.isZero()) {
            log.info("化债回滚未执行（可能未化债）, orderNumber={}", command.getOrderNumber());
        }

        // 5. 保存合同
        debtContractRepository.save(contract);

        // 6. 发布领域事件
        publishDomainEvents(contract.getAndClearDomainEvents());

        log.info("化债回滚完成, orderNumber={}, rolledBack={}", command.getOrderNumber(), actualRolledBack);
        return actualRolledBack;
    }

    /**
     * 发布领域事件
     */
    private void publishDomainEvents(List<DomainEvent> events) {
        for (DomainEvent event : events) {
            log.debug("发布领域事件: {}", event);
            eventPublisher.publishEvent(event);
        }
    }
}
