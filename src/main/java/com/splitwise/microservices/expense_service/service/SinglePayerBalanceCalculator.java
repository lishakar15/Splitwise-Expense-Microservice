package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.entity.PaidUser;
import com.splitwise.microservices.expense_service.model.ExpenseRequest;
import com.splitwise.microservices.expense_service.model.ParticipantShare;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SinglePayerBalanceCalculator implements BalanceCalculator{

    @Override
    public Map<Long, Map<Long, Double>> calculateBalance(ExpenseRequest expenseRequest) {
        Map<Long, Map<Long,Double>> balanceMap = new HashMap<>();
        PaidUser paidUser = expenseRequest.getPaidUsers().get(0);
        Long paidUserId = paidUser.getUserId();
        List<ParticipantShare> participantShares = expenseRequest.getParticipantShareList();
        for(ParticipantShare participantShare : participantShares)
        {
            if(!participantShare.isPayer())
            {
                balanceMap.putIfAbsent(paidUserId,new HashMap<>());
                balanceMap.get(paidUserId).put(participantShare.getUserId(),
                        participantShare.getShareAmount());
            }
        }
        return balanceMap;
    }
}
