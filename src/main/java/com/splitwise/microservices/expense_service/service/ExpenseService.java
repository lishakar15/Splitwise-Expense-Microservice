package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.entity.Balance;
import com.splitwise.microservices.expense_service.entity.Expense;
import com.splitwise.microservices.expense_service.entity.ExpenseParticipant;
import com.splitwise.microservices.expense_service.entity.PaidUser;
import com.splitwise.microservices.expense_service.exception.ExpenseException;
import com.splitwise.microservices.expense_service.mapper.ExpenseMapper;
import com.splitwise.microservices.expense_service.model.ExpenseRequest;
import com.splitwise.microservices.expense_service.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ExpenseService {

    @Autowired
    ExpenseParticipantService expenseParticipantService;
    @Autowired
    ExpenseRepository expenseRepository;
    @Autowired
    ExpenseMapper expenseMapper;
    @Autowired
    BalanceService balanceService;
    @Autowired
    PaidUserService paidUserService;

    BalanceCalculator balanceCalculator;

    public Expense saveExpenseFromRequest(ExpenseRequest expenseRequest) {
        Expense expenseObj = expenseMapper.getExpenseFromRequest(expenseRequest);
        Expense savedExpense = null;
        if(expenseObj != null)
        {
            savedExpense = expenseRepository.save(expenseObj);
            savePaidUsers(expenseRequest.getPaidUsers(),savedExpense.getExpenseId());
        }
       return savedExpense;
    }
    /**
     * This method handles saving Expense and related details
     * @param expenseRequest
     */
    @Transactional
    public void saveExpenseAndParticipantDetails(ExpenseRequest expenseRequest) {
        Expense savedExpense = saveExpenseFromRequest(expenseRequest);
        if(savedExpense != null)
        {
            boolean isParticipantsSaved = expenseParticipantService.saveExpenseParticipantsFromRequest(expenseRequest,
                    savedExpense.getExpenseId());

            if(isParticipantsSaved)
            {
                //Todo: Add validation for total amount equals to payers` sum (On request)
                //Calculate and save individual balances users owe
                saveParticipantsBalance(expenseRequest);
            }
            else
            {
                throw new RuntimeException("Error occurred while saving Expense");
            }
        }
        else
        {
            throw new RuntimeException("Error occurred while saving Expense");
        }
    }
    public void savePaidUsers(List<PaidUser> paidUsers,Long expenseId)
    {
        try{
            for(PaidUser paidUser : paidUsers)
            {
                paidUser.setExpenseId(expenseId);
                paidUserService.savePaidUser(paidUser);
            }
        }
        catch(Exception ex)
        {
            throw new RuntimeException("Error occurred while saving paid User details",ex);
        }
    }
    @Transactional
    public void updateExpenseAndParticipantsFromRequest(ExpenseRequest expenseRequest) throws ExpenseException {
        if(expenseRequest == null)
        {
            throw new RuntimeException("Request cannot be null");
        }
        Long expenseId = expenseRequest.getExpenseId();
        try{

        //Undo previous balance calculation
        ExpenseRequest oldExpenseRequest = createExpenseRequestFromExpenseId(expenseId);
            revertPreviousBalanceForExpense(oldExpenseRequest);

        Expense updatedExpense = expenseMapper.getExpenseFromRequest(expenseRequest);
            updatedExpense.setExpenseId(expenseId);
        List<PaidUser> paidUsers = expenseRequest.getPaidUsers();
        //Save Expense and Paid Users
        expenseRepository.save(updatedExpense);
        savePaidUsers(paidUsers,expenseId);
        //Save Participants
        expenseParticipantService.updateParticipantsExpense(expenseRequest,expenseId);
        //Calculate balance for updated expense
        calculateParticipantsBalance(expenseRequest);

        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
            throw new RuntimeException("Error occurred while updating Expense details");
        }
    }
    public void saveParticipantsBalance(ExpenseRequest expenseRequest)
    {
        if(expenseRequest == null)
        {
            throw new RuntimeException("Request cannot be null");
        }
        Map<Long, Map<Long, Double>> balanceMap = calculateParticipantsBalance(expenseRequest);
        saveParticipantsBalance(balanceMap,expenseRequest.getGroupId());
    }
    /**
     * This method undo the balance calculation of an expense before update/delete request
     * @param expenseRequest
     */
    public void revertPreviousBalanceForExpense(ExpenseRequest expenseRequest)
    {
        if(expenseRequest == null)
        {
            throw new RuntimeException("Request cannot be null");
        }
        Long groupId = expenseRequest.getGroupId();
        Map<Long, Map<Long, Double>> balanceMap = calculateParticipantsBalance(expenseRequest);
        Map<Long,Map<Long,Double>> reverseBalanceMap = new HashMap<>();
        if(balanceMap != null)
        {
            for(Map.Entry<Long,Map<Long,Double>> mapEntry : balanceMap.entrySet())
            {
                Long paidUser = mapEntry.getKey();
                for(Map.Entry<Long,Double> participantEntry : mapEntry.getValue().entrySet())
                {
                    Long participantId = participantEntry.getKey();
                    Double oweAmount = participantEntry.getValue();
                    reverseBalanceMap.putIfAbsent(participantId,new HashMap<>());
                    Map<Long,Double> paidUserMap = reverseBalanceMap.get(participantId);
                    paidUserMap.put(paidUser,oweAmount);
                    reverseBalanceMap.put(participantId,paidUserMap);
                }
            }
        }
        saveParticipantsBalance(reverseBalanceMap,groupId);
    }

    public void saveParticipantsBalance(Map<Long, Map<Long, Double>> balanceMap,Long groupId)
    {
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
                        Balance existingBalance = balanceService.getPastBalanceOfUser(paidUserId,participantId
                                ,groupId);
                        if(existingBalance != null)
                        {
                            //Update existing balance
                            amountOwes = amountOwes + existingBalance.getBalanceAmount();
                            amountOwes = Math.round(amountOwes * 100.0) / 100.0;
                            existingBalance.setBalanceAmount(amountOwes);
                            balanceService.saveBalance(existingBalance);
                        }
                        else
                        {
                            //Check if paid user owes any amount to participant in the past
                            existingBalance = balanceService.getPastBalanceOfUser(participantId,
                                    paidUserId,groupId);

                            if(existingBalance != null)
                            {
                                //reduce balances
                                 Double updatedAmount = existingBalance.getBalanceAmount() - amountOwes;
                                 updatedAmount = Math.round(updatedAmount * 100.0) / 100.0;
                                 if(updatedAmount > 0)
                                 {
                                     existingBalance.setBalanceAmount(updatedAmount);
                                     balanceService.saveBalance(existingBalance);
                                 }
                                 else if(updatedAmount<0){
                                     balanceService.deleteBalanceById(existingBalance.getBalanceId());
                                     Balance balance = Balance.builder()
                                             .groupId(groupId)
                                             .userId(participantId)
                                             .owesTo(paidUserId)
                                             .balanceAmount(Math.abs(updatedAmount))
                                             .build();
                                     balanceService.saveBalance(balance);
                                 }
                                 else if(updatedAmount == 0 )
                                 {
                                     //balance settled
                                     balanceService.deleteBalanceById(existingBalance.getBalanceId());
                                 }
                            }
                            else
                            {
                                Balance balance = Balance.builder()
                                        .groupId(groupId)
                                        .userId(participantId)
                                        .owesTo(paidUserId)
                                        .balanceAmount(amountOwes)
                                        .build();
                                balanceService.saveBalance(balance);                            }
                        }
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
        }
        else
        {
            balanceCalculator = new SinglePayerBalanceCalculator();
        }
        return balanceCalculator.calculateBalance(expenseRequest);
    }

    @Transactional
    public void deleteExpenseDetails(Long expenseId) {
        try
        {
            ExpenseRequest expenseRequest = createExpenseRequestFromExpenseId(expenseId);
            revertPreviousBalanceForExpense(expenseRequest);
            expenseRepository.deleteByExpenseId(expenseId);
            expenseParticipantService.deleteExpenseParticipants(expenseId);
            paidUserService.deleteByExpenseId(expenseId);
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Error occurred while processing delete request ");
        }
    }

    private ExpenseRequest createExpenseRequestFromExpenseId(Long expenseId) {
        List<PaidUser> paidUsers = paidUserService.findByExpenseId(expenseId);
        List<ExpenseParticipant> participantList = expenseParticipantService.getParticipantsByExpenseId(expenseId);
        Optional<Expense> optionalExpense = expenseRepository.findByExpenseId(expenseId);
        if(!optionalExpense.isPresent())
        {
            throw new RuntimeException("Expense you are trying to delete doesn't exists");
        }
        Expense expense = optionalExpense.get();
        ExpenseRequest expenseRequest = expenseMapper.createExpenseRequestFromExpenseId(expense,participantList,
                paidUsers);
        return expenseRequest;
    }
}
