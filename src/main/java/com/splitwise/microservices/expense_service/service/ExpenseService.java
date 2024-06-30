package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.entity.Expense;
import com.splitwise.microservices.expense_service.entity.PaidUser;
import com.splitwise.microservices.expense_service.mapper.ExpenseMapper;
import com.splitwise.microservices.expense_service.model.ExpenseRequest;
import com.splitwise.microservices.expense_service.repository.ExpenseRepository;
import com.splitwise.microservices.expense_service.repository.PaidUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {

    @Autowired
    ExpenseRepository expenseRepository;
    @Autowired
    PaidUserRepository paidUserRepository;
    @Autowired
    ExpenseMapper expenseMapper;
    BalanceCalculator balanceCalculator;

    public Expense saveExpenseFromRequest(ExpenseRequest expenseRequest) {
        Expense expenseObj = expenseMapper.getExpenseFromRequest(expenseRequest);
        if(expenseObj != null)
        {
            List<PaidUser> paidUsers = expenseRequest.getPaidUsers();
            for(PaidUser paidUser : paidUsers)
            {
                paidUserRepository.save(paidUser);
            }
        }
       return expenseRepository.save(expenseObj);
    }

    public void calculateParticipantsBalance(ExpenseRequest expenseRequest)
    {
        if(expenseRequest != null && expenseRequest.getPaidUsers().size()>1)
        {
            balanceCalculator = new MultiPayerBalanceCalculator();
            balanceCalculator.calculateBalance(expenseRequest);
        }
        else
        {
            balanceCalculator = new SinglePayerBalanceCalculator();
            balanceCalculator.calculateBalance(expenseRequest);
        }
    }


}
