package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.model.ExpenseRequest;

public interface BalanceCalculator {
    public void calculateBalance(ExpenseRequest expenseRequest);
}
