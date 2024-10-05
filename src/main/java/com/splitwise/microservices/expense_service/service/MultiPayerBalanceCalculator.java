package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.entity.PaidUser;
import com.splitwise.microservices.expense_service.model.ExpenseRequest;
import com.splitwise.microservices.expense_service.model.ParticipantShare;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiPayerBalanceCalculator implements BalanceCalculator{

    @Override
    public Map<Long, Map<Long, Double>> calculateBalance(ExpenseRequest expenseRequest) {
        if(expenseRequest == null)
        {
            return null; //Throw exception
        }
        Map<Long,Map<Long,Double>> balanceMap = new HashMap<>();
        List<ParticipantShare> participantShareList = expenseRequest.getParticipantShareList();
        Map<Long, Double> paidUsersActualOweAmountMap = getPaidUsersOweAmountMap(expenseRequest);
        for(ParticipantShare participant : participantShareList) {
            if (!participant.getIsPaidUser()) {
                Double participantOweAmount = participant.getShareAmount();
                for (Map.Entry<Long, Double> paidUserOweMapEntry : paidUsersActualOweAmountMap.entrySet()) {

                    Long paidUserId = paidUserOweMapEntry.getKey();
                    Double paidUserOweAmount = paidUserOweMapEntry.getValue();
                    if (paidUserOweAmount > 0 && paidUserOweAmount > participantOweAmount) {
                        balanceMap.putIfAbsent(paidUserId, new HashMap<>());
                        Map<Long, Double> participantMap = balanceMap.get(paidUserId);
                        participantMap.put(participant.getUserId(), Math.abs(participantOweAmount));
                        balanceMap.put(paidUserId, participantMap);
                        paidUserOweMapEntry.setValue(paidUserOweAmount - participantOweAmount);
                        break;
                        //return;
                    }
                    else if (paidUserOweAmount > 0 && paidUserOweAmount < participantOweAmount) {
                        balanceMap.putIfAbsent(paidUserId, new HashMap<>());
                        Map<Long, Double> participantMap = balanceMap.get(paidUserId);
                        participantMap.put(participant.getUserId(), paidUserOweAmount);
                        balanceMap.put(paidUserId, participantMap);
                        paidUserOweMapEntry.setValue(0.0);
                        participantOweAmount = participantOweAmount - paidUserOweAmount;
                    }
                    else if (paidUserOweAmount > 0 && Double.compare(paidUserOweAmount, participantOweAmount) == 0) {
                        balanceMap.putIfAbsent(paidUserId, new HashMap<>());
                        Map<Long, Double> participantMap = balanceMap.get(paidUserId);
                        participantMap.put(participant.getUserId(), paidUserOweAmount);
                        balanceMap.put(paidUserId, participantMap);
                        paidUserOweMapEntry.setValue(0.0);
                        break; //Exit the loop as the amount is settled.
                        //participantAmount = participantAmount +payerAmount;//wrong
                    }
                }
            } else if (participant.getIsPaidUser() && paidUsersActualOweAmountMap.get(participant.getUserId()) < 0.00) {
                for (Map.Entry<Long, Double> payerMapEntry : paidUsersActualOweAmountMap.entrySet()) {
                    Long payerId = payerMapEntry.getKey();
                    Long participantId = participant.getUserId();
                    Double paidUserOweAmount = payerMapEntry.getValue();
                    Double participantOweAmount = Math.abs(paidUsersActualOweAmountMap.get(participant.getUserId()));
                    if (payerId != participantId) {
                        balanceMap.putIfAbsent(payerId, new HashMap<>());
                        Map<Long, Double> participantMap = balanceMap.get(payerId);
                        if (paidUserOweAmount > 0 && paidUserOweAmount > participantOweAmount) {
                            participantMap.put(participantId, participantOweAmount);
                            balanceMap.put(payerId, participantMap);
                            payerMapEntry.setValue(paidUserOweAmount - participantOweAmount);
                        } else if (paidUserOweAmount > 0 && paidUserOweAmount <= participantOweAmount) {
                            participantMap.put(participantId, paidUserOweAmount);
                            balanceMap.put(payerId, participantMap);
                            paidUsersActualOweAmountMap.put(participantId, (paidUsersActualOweAmountMap.get(participantId) + paidUserOweAmount)); //Updating remaining amount
                            payerMapEntry.setValue(0.0); // Exit the loop as the amount is settled.
                        }
                    }
                }
            }
        }
        return balanceMap;
    }

    /**
     * This method calculates the actual amount that paid users owe
     * @param expenseRequest
     * @return
     */
    public Map<Long,Double> getPaidUsersOweAmountMap(ExpenseRequest expenseRequest)
    {
        Map<Long,Double> paidUsersActualOweAmountMap = new HashMap<>();
        Map<Long,ParticipantShare> participantMap = new HashMap<>();
        List<PaidUser> paidUsers = expenseRequest.getPaidUsers();
        List<ParticipantShare> participantSharList = expenseRequest.getParticipantShareList();

        for(ParticipantShare participantShare : participantSharList)
        {
            participantMap.put(participantShare.getUserId(),participantShare);
        }
        for(PaidUser paidUser : paidUsers)
        {
            Long paidUserId = paidUser.getUserId();
            Double paidAmount = paidUser.getPaidAmount();
            ParticipantShare participantShare = participantMap.get(paidUser.getUserId());
            Double oweAmount = participantShare != null ?  participantShare.getShareAmount() : 0;
            paidUsersActualOweAmountMap.put(paidUserId,paidAmount-oweAmount);//Actual amount user owes
        }
        return paidUsersActualOweAmountMap;
    }
}
