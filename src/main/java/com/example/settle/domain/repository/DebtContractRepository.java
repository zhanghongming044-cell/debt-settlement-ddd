package com.example.settle.domain.repository;

import com.example.settle.domain.model.settle.DebtContract;

import java.util.Optional;

/**
 * 债务合同仓储接口
 *
 * 领域层定义接口，基础设施层实现
 */
public interface DebtContractRepository {

    /**
     * 保存合同（新增或更新）
     *
     * @param contract 合同
     * @return 保存后的合同（含ID）
     */
    DebtContract save(DebtContract contract);

    /**
     * 根据ID查询
     *
     * @param id 合同ID
     * @return 合同
     */
    Optional<DebtContract> findById(Long id);

    /**
     * 根据委案ID查询
     *
     * @param caseEntrustId 委案ID
     * @return 合同
     */
    Optional<DebtContract> findByCaseEntrustId(Long caseEntrustId);

    /**
     * 根据会员用户ID查询
     *
     * @param memberUserId 会员用户ID
     * @return 合同
     */
    Optional<DebtContract> findByMemberUserId(Long memberUserId);

    /**
     * 根据委案ID和会员用户ID查询
     *
     * @param caseEntrustId 委案ID
     * @param memberUserId  会员用户ID
     * @return 合同
     */
    Optional<DebtContract> findByCaseEntrustIdAndMemberUserId(Long caseEntrustId, Long memberUserId);
}
