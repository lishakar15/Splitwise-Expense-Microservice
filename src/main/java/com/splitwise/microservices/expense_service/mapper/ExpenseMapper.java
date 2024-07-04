package com.splitwise.microservices.expense_service.mapper;

import com.splitwise.microservices.expense_service.entity.Expense;
import com.splitwise.microservices.expense_service.entity.ExpenseParticipant;
import com.splitwise.microservices.expense_service.entity.PaidUser;
import com.splitwise.microservices.expense_service.enums.SplitType;
import com.splitwise.microservices.expense_service.model.ExpenseRequest;
import com.splitwise.microservices.expense_service.model.ParticipantShare;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class ExpenseMapper {
    @Autowired
    private ExpenseParticipantMapper expenseParticipantMapper;

    public Expense getExpenseFromRequest(ExpenseRequest expenseRequest)
    {
        if(expenseRequest == null )
        {
            //Need to throw exception
            return null;
        }
        Expense expense = Expense.builder().expenseDescription(expenseRequest.getExpenseDescription())
                .groupId(expenseRequest.getGroupId())
                .totalAmount(expenseRequest.getTotalAmount())
                .category(expenseRequest.getCategory())
                .spentOnDate(expenseRequest.getSpentOnDate())
                .createDate(expenseRequest.getCreateDate())
                .lastUpdateDate(expenseRequest.getLastUpdateDate())
                .splitType(SplitType.valueOf(expenseRequest.getSplitType()))
                .build();
        return expense;
    }

    public ExpenseRequest createExpenseRequestFromExpenseId(Expense expense, List<ExpenseParticipant> participantList,
                                                            List<PaidUser> paidUserList)
    {
        List<ParticipantShare> participantShareList = expenseParticipantMapper.getParticipantShareListFromParticipantList(participantList);
        ExpenseRequest expenseRequest = ExpenseRequest.builder()
                .expenseId(expense.getExpenseId())
                .createDate(expense.getCreateDate())
                .expenseDescription(expense.getExpenseDescription())
                .category(expense.getCategory())
                .groupId(expense.getGroupId())
                .paidUsers(paidUserList)
                .participantShareList(participantShareList)
                .spentOnDate(expense.getSpentOnDate())
                .lastUpdateDate(expense.getLastUpdateDate())
                .totalAmount(expense.getTotalAmount())
                .splitType(expense.getSplitType().toString())
                .build();
        return expenseRequest;
    }
}
