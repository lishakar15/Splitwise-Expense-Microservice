package com.splitwise.microservices.expense_service.repository;

import com.splitwise.microservices.expense_service.entity.PaidUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Repository
public interface PaidUserRepository extends JpaRepository<PaidUser,Long> {

    List<PaidUser> findByExpenseId(Long expenseId);

    @Modifying
    @Transactional
    @Query(value = "delete from PaidUser p where p.expenseId =:expenseId")
    int deleteByExpenseId(@Param("expenseId") Long expenseId);
    @Query("select p.expenseId from PaidUser p where p.userId =:userId")
    List<Long> getExpenseIdByUserId(@PathVariable("userId") Long userId);
}
