package com.example.settle.domain.repository;

import com.example.settle.domain.model.divide.DivideRecord;
import com.example.settle.domain.model.shared.Money;
import com.example.settle.domain.model.shared.OrderId;

import java.util.List;
import java.util.Optional;

/**
 * 分账记录仓储接口
 *
 * 领域层定义接口，基础设施层实现
 */
public interface DivideRecordRepository {

    /**
     * 保存分账记录（新增或更新）
     *
     * @param record 分账记录
     * @return 保存后的记录（含ID）
     */
    DivideRecord save(DivideRecord record);

    /**
     * 批量保存
     *
     * @param records 分账记录列表
     * @return 保存后的记录列表
     */
    List<DivideRecord> saveAll(List<DivideRecord> records);

    /**
     * 根据ID查询
     *
     * @param id 记录ID
     * @return 分账记录
     */
    Optional<DivideRecord> findById(Long id);

    /**
     * 根据订单号查询所有分账记录
     *
     * @param orderNumber 订单号
     * @return 分账记录列表
     */
    List<DivideRecord> findByOrderNumber(String orderNumber);

    /**
     * 根据订单ID查询（精确到商品级别）
     *
     * @param orderId 订单ID
     * @return 分账记录
     */
    Optional<DivideRecord> findByOrderId(OrderId orderId);

    /**
     * 查询订单的供应商分账总额（用于化债）
     *
     * @param orderNumber 订单号
     * @return 供应商分账总额
     */
    Money getSupplierDivideAmount(String orderNumber);

    /**
     * 查询订单详情的供应商分账金额（用于部分退款化债回滚）
     *
     * @param orderId 订单ID（含订单详情ID）
     * @return 供应商分账金额
     */
    Money getSupplierDivideAmountByOrderId(OrderId orderId);

    /**
     * 检查是否存在（幂等性检查）
     *
     * @param orderId 订单ID
     * @return true 如果存在
     */
    boolean existsByOrderId(OrderId orderId);
}
