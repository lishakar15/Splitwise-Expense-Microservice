package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.model.ExpenseRequest;

import java.util.HashMap;
import java.util.Map;

public class MultiPayerBalanceCalculator implements BalanceCalculator{

    @Override
    public Map<Long, Map<Long, Double>> calculateBalance(ExpenseRequest expenseRequest) {
        Map<Long,Map<Long,Double>> balanceMap = new HashMap<>();

        //Todo: Perform multi payer calculation
        return balanceMap;
    }
}
