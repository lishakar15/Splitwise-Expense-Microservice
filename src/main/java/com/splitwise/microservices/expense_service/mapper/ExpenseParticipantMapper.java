package com.splitwise.microservices.expense_service.mapper;

import com.splitwise.microservices.expense_service.entity.ExpenseParticipant;
import com.splitwise.microservices.expense_service.model.ExpenseParticipantVO;
import com.splitwise.microservices.expense_service.model.ExpenseRequest;
import com.splitwise.microservices.expense_service.model.ParticipantShare;
import jakarta.servlet.http.Part;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                    .isPayer(participantShare.getIsPaidUser())
                    .build();
            participantList.add(participant);
        }
        return participantList;
    }

    public List<ParticipantShare> getParticipantShareListFromParticipantList(List<ExpenseParticipant> participantList)
    {
        List<ParticipantShare> participantShareList = new ArrayList<>();
        if(participantList != null )
        {
            for(ExpenseParticipant expenseParticipant : participantList)
            {
                ParticipantShare participantShare = ParticipantShare.builder()
                        .userId(expenseParticipant.getParticipantId())
                        .shareAmount(expenseParticipant.getSettlementAmount())
                        .isPaidUser(expenseParticipant.getIsPayer())
                        .build();
                participantShareList.add(participantShare);
            }
        }

        return participantShareList;
    }
    public List<ExpenseParticipantVO> getParticipantsFromParticipantList(List<ExpenseParticipant> participantList, Map<Long,String> userNameMap)
    {
        List<ExpenseParticipantVO> expenseParticipants = new ArrayList<>();
        if(participantList !=null && userNameMap != null)
        {
            for(ExpenseParticipant expenseParticipant : participantList)
            {
                ExpenseParticipantVO expenseParticipantVO = ExpenseParticipantVO.builder()
                        .userId(expenseParticipant.getParticipantId())
                        .userName(userNameMap.get(expenseParticipant.getParticipantId()))
                        .shareAmount(expenseParticipant.getSettlementAmount())
                        .isPaidUser(expenseParticipant.getIsPayer())
                        .build();
                expenseParticipants.add(expenseParticipantVO);
            }
        }
        return expenseParticipants;
    }
}
