package com.splitwise.microservices.expense_service.mapper;

import com.splitwise.microservices.expense_service.entity.Expense;
import com.splitwise.microservices.expense_service.entity.ExpenseParticipant;
import com.splitwise.microservices.expense_service.model.ExpenseRequest;
import com.splitwise.microservices.expense_service.model.UserExpenseSplit;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ExpenseMapper {

    public Expense getExpenseFromExpenseRequest(ExpenseRequest expenseRequest)
    {
        if(expenseRequest == null )
        {
            //Need to throw exception
            return null;
        }
        Expense expense = Expense.builder().expenseDescription(expenseRequest.getExpenseDescription())
                .groupId(expenseRequest.getGroupId())
                .paidBy(expenseRequest.getPaidBy())
                .totalAmount(expenseRequest.getTotalAmount())
                .category(expenseRequest.getCategory())
                .spentOnDate(expenseRequest.getSpentOnDate())
                .createDate(expenseRequest.getCreateDate())
                .lastUpdateDate(expenseRequest.getLastUpdateDate())
                //.splitType(expenseRequest.getSplitType())
                .build();
        return expense;
    }
}
