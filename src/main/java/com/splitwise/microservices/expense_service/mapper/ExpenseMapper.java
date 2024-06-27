package com.splitwise.microservices.expense_service.mapper;

import com.splitwise.microservices.expense_service.entity.Expense;
import com.splitwise.microservices.expense_service.enums.SplitType;
import com.splitwise.microservices.expense_service.model.ExpenseRequest;
import org.springframework.stereotype.Component;


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
                .splitType(SplitType.valueOf(expenseRequest.getSplitType()))
                .build();
        return expense;
    }
}
