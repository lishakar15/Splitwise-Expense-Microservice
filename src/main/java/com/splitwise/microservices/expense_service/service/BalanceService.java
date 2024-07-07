package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.entity.Balance;
import com.splitwise.microservices.expense_service.entity.Settlement;
import com.splitwise.microservices.expense_service.repository.BalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BalanceService {
    @Autowired
    BalanceRepository balanceRepository;

    public Balance getPastBalanceOfUser(Long userId, Long owesToUserId, Long groupId)
    {
        return balanceRepository.getPastBalanceOfUser(userId,
                owesToUserId,groupId);
    }

    public void saveBalance(Balance balance)
    {
        balanceRepository.save(balance);
    }

    public void deleteBalanceById(Long balanceId)
    {
        balanceRepository.deleteByBalanceId(balanceId);
    }

    public void revertPreviousBalanceForSettlement(Settlement settlement)
    {
        if(settlement != null)
        {
            //Create reverse settlement object
            Long paidByUser = settlement.getPaidBy();
            Long paidToUser = settlement.getPaidTo();

            settlement.setPaidBy(paidToUser);// Set the payer to be the original payee
            settlement.setPaidTo(paidByUser); // Set the payee to be the original payer
            calculateBalanceForSettlement(settlement);
        }
    }

    public void calculateBalanceForSettlement(Settlement settlement)
    {
        Long payerId = settlement.getPaidBy();
        Long receiverId = settlement.getPaidTo();
        Long groupId = settlement.getGroupId();
        Double amountPaid = settlement.getAmountPaid();
        Balance balance = null;
        try
        {
            //Check if the payer owes any balance to receiver
            balance = getPastBalanceOfUser(payerId,receiverId,groupId);
            if(balance != null)
            {
                Double pastBalanceAmount = balance.getBalanceAmount();
                Double newBalanceAmount = pastBalanceAmount - amountPaid;
                if(newBalanceAmount < 0)
                {
                    balance.setBalanceAmount(newBalanceAmount);
                    Long pastOwesToId = balance.getOwesTo();
                    balance.setOwesTo(balance.getUserId());
                    balance.setUserId(pastOwesToId);
                }

                else if(newBalanceAmount > 0)
                {
                    //Payer balance not fully settled
                    balance.setBalanceAmount(newBalanceAmount);
                }
                else if(newBalanceAmount == 0)
                {
                    deleteBalanceById(balance.getBalanceId());
                    return;
                }
                if(balance != null)
                {
                    saveBalance(balance);
                }

            }
            //Check if the receiver owes any balance to payer
            balance = getPastBalanceOfUser(receiverId,payerId,groupId);
            if(balance != null)
            {
                    Double pastBalanceAmount = balance.getBalanceAmount();
                    Double newBalanceAmount = pastBalanceAmount + amountPaid;
                    balance.setBalanceAmount(newBalanceAmount);
                    balanceRepository.save(balance);
            }
            //If no past balances between payer and receiver then add new balance record
            else
            {
                balance = Balance.builder()
                        .groupId(groupId)
                        .userId(receiverId)
                        .owesTo(payerId)
                        .balanceAmount(amountPaid)
                        .build();
                balanceRepository.save(balance);
            }

        }
        catch (Exception ex)
        {
            // need to throw exception
        }
    }
}
