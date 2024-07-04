package com.splitwise.microservices.expense_service.repository;

import com.splitwise.microservices.expense_service.entity.PaidUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaidUserRepository extends JpaRepository<PaidUser,Long> {

    List<PaidUser> findByExpenseId(Long expenseId);
}
