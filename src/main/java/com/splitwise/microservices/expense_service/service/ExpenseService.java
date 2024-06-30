package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.entity.Balance;
import com.splitwise.microservices.expense_service.entity.Expense;
import com.splitwise.microservices.expense_service.entity.PaidUser;
import com.splitwise.microservices.expense_service.mapper.ExpenseMapper;
import com.splitwise.microservices.expense_service.model.ExpenseRequest;
import com.splitwise.microservices.expense_service.repository.BalanceRepository;
import com.splitwise.microservices.expense_service.repository.ExpenseRepository;
import com.splitwise.microservices.expense_service.repository.PaidUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ExpenseService {

    @Autowired
    ExpenseRepository expenseRepository;
    @Autowired
    PaidUserRepository paidUserRepository;
    @Autowired
    ExpenseMapper expenseMapper;
    @Autowired
    BalanceRepository balanceRepository;

    BalanceCalculator balanceCalculator;

    public Expense saveExpenseFromRequest(ExpenseRequest expenseRequest) {
        Expense expenseObj = expenseMapper.getExpenseFromRequest(expenseRequest);
        Expense savedExpense = null;
        if(expenseObj != null)
        {
            savedExpense = expenseRepository.save(expenseObj);
            if(savedExpense != null)
            {
                List<PaidUser> paidUsers = expenseRequest.getPaidUsers();
                for(PaidUser paidUser : paidUsers)
                {
                    paidUser.setExpenseId(savedExpense.getExpenseId());
                    paidUserRepository.save(paidUser);
                }
            }
        }
       return savedExpense;
    }

    public void saveParticipantsBalance(ExpenseRequest expenseRequest)
    {
        Long groupId = expenseRequest.getGroupId();
        Map<Long, Map<Long, Double>> balanceMap = calculateParticipantsBalance(expenseRequest);
        if(balanceMap != null)
        {
            for(Map.Entry<Long,Map<Long,Double>> mapEntry : balanceMap.entrySet())
            {
                Long paidUserId = mapEntry.getKey();
                Map<Long,Double> participantsMap = mapEntry.getValue();
                if(participantsMap != null)
                {
                    for(Map.Entry<Long,Double> participantEntry : participantsMap.entrySet())
                    {
                        Long participantId = participantEntry.getKey();
                        Double amountOwes = participantEntry.getValue();
                        //Check if there is any past pending balance
                        Optional<Double> optional = balanceRepository.getPastBalanceOfParticipant(paidUserId,
                            participantId);
                        if(optional.isPresent())
                        {
                            //Add current balance with past balance
                            amountOwes = amountOwes +optional.get();
                        }
                        Balance balance = Balance.builder()
                                .groupId(groupId)
                                .userId(participantId)
                                .owesTo(paidUserId)
                                .balanceAmount(amountOwes)
                                .build();
                        balanceRepository.save(balance);
                    }
                }
            }
        }
    }

    public Map<Long, Map<Long, Double>> calculateParticipantsBalance(ExpenseRequest expenseRequest)
    {
        Map<Long, Map<Long, Double>> balanceMap = null;
        Long groupId = expenseRequest.getGroupId();
        if(expenseRequest != null && expenseRequest.getPaidUsers().size()>1)
        {
            balanceCalculator = new MultiPayerBalanceCalculator();
            balanceCalculator.calculateBalance(expenseRequest);
        }
        else
        {
            balanceCalculator = new SinglePayerBalanceCalculator();
            balanceMap = balanceCalculator.calculateBalance(expenseRequest);
        }
        return balanceMap;
    }


}
