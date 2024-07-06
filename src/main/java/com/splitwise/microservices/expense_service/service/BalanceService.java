package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.entity.Balance;
import com.splitwise.microservices.expense_service.repository.BalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BalanceService {
    @Autowired
    BalanceRepository balanceRepository;

    public Balance getPastBalanceOfParticipant(Long paidUserId, Long participantId, Long groupId)
    {
        return balanceRepository.getPastBalanceOfParticipant(paidUserId,
                participantId,groupId);
    }

    public void saveBalance(Balance balance)
    {
        balanceRepository.save(balance);
    }

    public void deleteBalanceById(Long balanceId)
    {
        balanceRepository.deleteByBalanceId(balanceId);
    }
}
