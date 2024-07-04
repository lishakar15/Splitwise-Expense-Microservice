package com.splitwise.microservices.expense_service.repository;

import com.splitwise.microservices.expense_service.entity.PaidUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PaidUserRepository extends JpaRepository<PaidUser,Long> {

    List<PaidUser> findByExpenseId(Long expenseId);
    @Transactional
    @Modifying
    @Query(name = "delete from PaidUser p where p.expenseId =:expenseId")
    boolean deleteByExpenseId(@Param("expenseId") Long expenseId);
}
