package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.entity.PaidUser;
import com.splitwise.microservices.expense_service.repository.PaidUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaidUserService {
    @Autowired
    PaidUserRepository paidUserRepository;

    public void savePaidUser(PaidUser paidUser) {
        paidUserRepository.save(paidUser);
    }

    public List<PaidUser> findByExpenseId(Long expenseId) {
        return paidUserRepository.findByExpenseId(expenseId);
    }

    public void deleteByExpenseId(Long expenseId) {
        paidUserRepository.deleteByExpenseId(expenseId);
    }
}
