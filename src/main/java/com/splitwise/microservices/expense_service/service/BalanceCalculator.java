package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.model.ExpenseRequest;

import java.util.Map;

public interface BalanceCalculator {
    public Map<Long, Map<Long, Double>> calculateBalance(ExpenseRequest expenseRequest);
}
