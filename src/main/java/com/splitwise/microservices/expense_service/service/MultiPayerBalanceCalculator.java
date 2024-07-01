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
        for(ParticipantShare participant : participantShareList)
        {
            if(!participant.getIsPayer())
            {
                Double participantAmount = participant.getShareAmount();
                for(Map.Entry<Long,Double> paidUserOweMapEntry: paidUsersActualOweAmountMap.entrySet())
                {

                    Long payerId = paidUserOweMapEntry.getKey();
                    Double payerAmount = paidUserOweMapEntry.getValue();
                    if(payerAmount > 0 && payerAmount > participantAmount)
                    {
                        balanceMap.putIfAbsent(payerId,new HashMap<>());
                        Map<Long, Double> participantMap = balanceMap.get(payerId);
                        participantMap.put(participant.getUserId(),Math.abs(participantAmount));
                        balanceMap.put(payerId,participantMap);
                        paidUserOweMapEntry.setValue(payerAmount - participantAmount);
                        //return;
                    }
                    else if(payerAmount> 0 && payerAmount <= participantAmount)
                    {
                        balanceMap.putIfAbsent(payerId,new HashMap<>());
                        Map<Long, Double> participantMap = balanceMap.get(payerId);
                        participantMap.put(participant.getUserId(),payerAmount);
                        balanceMap.put(payerId,participantMap);
                        paidUserOweMapEntry.setValue(0.0); // Exit the loop as the amount is settled.
                        //participantAmount = participantAmount +payerAmount;//wrong
                    }
                }
            }
            else if(participant.getIsPayer() && paidUsersActualOweAmountMap.get(participant.getUserId())<0.00)
            {
                for(Map.Entry<Long,Double> payerMapEntry: paidUsersActualOweAmountMap.entrySet()) {
                    Long payerId = payerMapEntry.getKey();
                    Long participantId = participant.getUserId();
                    Double payerAmount = payerMapEntry.getValue();
                    Double payeeAmount = Math.abs(paidUsersActualOweAmountMap.get(participant.getUserId()));
                    if(payerId != participantId)
                    {
                        balanceMap.putIfAbsent(payerId,new HashMap<>());
                        Map<Long, Double> participantMap = balanceMap.get(payerId);
                        if(payerAmount > 0 && payerAmount > payeeAmount)
                        {
                            participantMap.put(participantId,payeeAmount);
                            balanceMap.put(payerId,participantMap);
                            payerMapEntry.setValue(payerAmount - payeeAmount);
                        }
                        else if(payerAmount > 0 && payerAmount <= payeeAmount)
                        {
                            participantMap.put(participantId,payerAmount);
                            balanceMap.put(payerId,participantMap);
                            paidUsersActualOweAmountMap.put(participantId,(paidUsersActualOweAmountMap.get(participantId) + payerAmount)); //Updating remaining amount
                            payerMapEntry.setValue(0.0); // Exit the loop as the amount is settled.
                        }
                    }
                }
            }
        }
        for (Map.Entry<Long,Map<Long,Double>> balanceEntry : balanceMap.entrySet())
        {
            Long getter = balanceEntry.getKey();
            for(Map.Entry<Long,Double> giverEntry : balanceEntry.getValue().entrySet())
            {
                Long giver = giverEntry.getKey();
                Double giverAmount = giverEntry.getValue();
                //Call repo to update existing balance
                System.out.println(giver +" owes "+giverAmount+" to "+getter);
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
            Double oweAmount = participantMap.get(paidUser.getUserId()).getShareAmount();
            paidUsersActualOweAmountMap.put(paidUserId,paidAmount-oweAmount);//Actual amount user owes
        }
        return paidUsersActualOweAmountMap;
    }
}
