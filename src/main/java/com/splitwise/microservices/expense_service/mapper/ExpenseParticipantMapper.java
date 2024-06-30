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
        for(ParticipantShare participantShare : expenseRequest.getParticipantShareList())
        {
            ExpenseParticipant participant = ExpenseParticipant.builder()
                    .expenseId(expenseId)
                    .participantId(participantShare.getUserId())
                    .settlementAmount(participantShare.getShareAmount())
                    .isPayer(participantShare.getIsPayer())
                    .build();
            participantList.add(participant);
        }
        return participantList;
    }
}
