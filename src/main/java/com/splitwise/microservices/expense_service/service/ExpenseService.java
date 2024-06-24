package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.entity.Expense;
import com.splitwise.microservices.expense_service.entity.ExpenseParticipant;
import com.splitwise.microservices.expense_service.repository.ExpenseParticipantRepository;
import com.splitwise.microservices.expense_service.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseService {

    @Autowired
    ExpenseRepository expenseRepository;

    @Autowired
    ExpenseParticipantRepository expenseParticipantRepository;

    public Expense saveExpense(Expense expense) {
       return expenseRepository.save(expense);
    }

    public boolean saveExpenseParticipants(List<ExpenseParticipant> participantList) {
        boolean isSaved= true;
        if(participantList == null)
        {
            //Need to throw exception
            isSaved = false;
            return isSaved;
        }
        try
        {
            for(ExpenseParticipant expenseParticipant:participantList)
            {
                expenseParticipantRepository.save(expenseParticipant);
            }
        }
        catch(Exception ex)
        {
            //Logger.logError("Error occurred while saving participant's expense");
            isSaved=false;
        }

        isSaved = true;
        return isSaved;
    }
}
