package com.splitwise.microservices.expense_service.mapper;

import com.splitwise.microservices.expense_service.entity.ExpenseParticipant;
import com.splitwise.microservices.expense_service.model.ExpenseRequest;
import com.splitwise.microservices.expense_service.model.ParticipantShare;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ExpenseParticipantMapper {

    public List<ExpenseParticipant> getExpenseParticipantsFromExpenseRequest(ExpenseRequest expenseRequest,
                                                                             Long expenseId)
    {
        List<ExpenseParticipant> participantList = new ArrayList<>();
        if(expenseRequest == null )
        {
            //Need to throw exception
            return null;
        }
        for(ParticipantShare expenseSplit : expenseRequest.getParticipantShareList())
        {
            ExpenseParticipant participant = ExpenseParticipant.builder().expenseId(expenseId)
                    .participantId(expenseSplit.getUserId())
                    .settlementAmount(expenseSplit.getShareAmount())
                    .isPayer(expenseSplit.isPayer())
                    .build();
            participantList.add(participant);
        }
        return participantList;
    }
}
