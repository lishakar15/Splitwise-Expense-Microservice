package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.model.ExpenseRequest;
import org.springframework.stereotype.Service;

@Service
public class MultiPayerBalanceCalculator implements BalanceCalculator{

    @Override
    public void calculateBalance(ExpenseRequest expenseRequest) {

    }
}
