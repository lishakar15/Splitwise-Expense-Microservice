package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.entity.ExpenseParticipant;
import com.splitwise.microservices.expense_service.entity.PaidUser;
import com.splitwise.microservices.expense_service.exception.ExpenseException;
import com.splitwise.microservices.expense_service.mapper.ExpenseParticipantMapper;
import com.splitwise.microservices.expense_service.model.ExpenseRequest;
import com.splitwise.microservices.expense_service.repository.ExpenseParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpenseParticipantService {
    @Autowired
    ExpenseParticipantRepository expenseParticipantRepository;

    @Autowired
    ExpenseParticipantMapper expenseParticipantMapper;

    public boolean saveExpenseParticipantsFromRequest(ExpenseRequest expenseRequest, Long expenseId) {
        boolean isSaved= true;
        if(expenseRequest == null)
        {
            //Need to throw exception
            return false;
        }
        List<ExpenseParticipant> participantsList =
                expenseParticipantMapper.getExpenseParticipantsFromExpenseRequest(expenseRequest,
                        expenseId);
        try
        {
            for(ExpenseParticipant expenseParticipant : participantsList)
            {
                expenseParticipantRepository.save(expenseParticipant);
            }
        }
        catch(Exception ex)
        {
            //Logger.logError("Error occurred while saving participant's expense");
            //Todo throw exception
            isSaved=false;
        }

        return isSaved;
    }

    public List<ExpenseParticipant> getParticipantsByExpenseId(Long expenseId)
    {
        return expenseParticipantRepository.findByExpenseId(expenseId);
    }

    public void updateParticipantsExpense(ExpenseRequest expenseRequest,Long expenseId) throws ExpenseException{
        //Delete existing participants and save the new list
        int deletedRows = deleteExpenseParticipants(expenseId);
        if(deletedRows > 0)
        {
            List<ExpenseParticipant> participantsList =
                    expenseParticipantMapper.getExpenseParticipantsFromExpenseRequest(expenseRequest,
                    expenseId);
            if(participantsList != null && !participantsList.isEmpty())
            {
                for(ExpenseParticipant expenseParticipant : participantsList)
                {
                    expenseParticipantRepository.save(expenseParticipant);
                }
            }
            else
            {
                throw new ExpenseException("Unable to perform update");
            }
        }
        else
        {
            throw new ExpenseException("Unable to perform update");
        }
    }

    public int deleteExpenseParticipants(Long expenseId)
    {
        return expenseParticipantRepository.deleteByExpenseId(expenseId);
    }

}
