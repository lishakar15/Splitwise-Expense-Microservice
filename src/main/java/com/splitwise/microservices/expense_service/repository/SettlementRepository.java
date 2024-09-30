package com.splitwise.microservices.expense_service.repository;

import com.splitwise.microservices.expense_service.entity.Settlement;
import org.hibernate.annotations.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    @Query(value ="select s from Settlement s where s.groupId =:groupId")
    List<Settlement> getAllSettlementByGroupId(@Param("groupId") Long groupId);
    @Query(value="select s from Settlement s where s.paidBy =:userId or s.paidTo =:userId ")
    List<Settlement> getAllSettlementsByUserId(@Param("userId") Long userId);
    @Modifying
    @Transactional
    @Query(value = "delete from Settlement s where s.settlementId =:settlementId")
    void deleteSettlementById(@Param("settlementId")Long settlementId);
}
