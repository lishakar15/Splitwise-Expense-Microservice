package com.splitwise.microservices.expense_service.repository;

import com.splitwise.microservices.expense_service.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense,Long> {

    Optional<Expense> findByExpenseId(Long expenseId);
}
