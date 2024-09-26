package com.splitwise.microservices.expense_service.service;

import com.google.gson.Gson;
import com.splitwise.microservices.expense_service.clients.UserClient;
import com.splitwise.microservices.expense_service.constants.StringConstants;
import com.splitwise.microservices.expense_service.entity.Balance;
import com.splitwise.microservices.expense_service.entity.Expense;
import com.splitwise.microservices.expense_service.entity.ExpenseParticipant;
import com.splitwise.microservices.expense_service.entity.PaidUser;
import com.splitwise.microservices.expense_service.enums.ActivityType;
import com.splitwise.microservices.expense_service.exception.ExpenseException;
import com.splitwise.microservices.expense_service.external.ActivityRequest;
import com.splitwise.microservices.expense_service.external.ChangeLog;
import com.splitwise.microservices.expense_service.kafka.KafkaProducer;
import com.splitwise.microservices.expense_service.mapper.ExpenseMapper;
import com.splitwise.microservices.expense_service.model.ExpenseResponse;
import com.splitwise.microservices.expense_service.model.ExpenseRequest;
import com.splitwise.microservices.expense_service.repository.ExpenseRepository;
import com.splitwise.microservices.expense_service.repository.PaidUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    @Autowired
    UserClient userClient;
    @Autowired
    KafkaProducer kafkaProducer;
    public static final Logger LOGGER = LoggerFactory.getLogger(ExpenseService.class);

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
                createExpenseActivity(ActivityType.EXPENSE_CREATED,expenseRequest,null);
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

    private void createExpenseActivity(ActivityType activityType, ExpenseRequest newExpenseRequest,
                                       ExpenseRequest oldExpenseRequest) {
        ActivityRequest activityRequest = ActivityRequest.builder()
                .activityType(activityType)
                .createDate(null)
                .expenseId(newExpenseRequest.getExpenseId())
                .groupId(newExpenseRequest.getGroupId())
                .build();
        StringBuilder sb = new StringBuilder();
        if(ActivityType.EXPENSE_CREATED.equals(activityType))
        {
            //Expense Create Activity
            sb.append(StringConstants.USER_ID_PREFIX);
            sb.append(newExpenseRequest.getCreatedBy());
            sb.append(StringConstants.USER_ID_SUFFIX);
            sb.append(StringConstants.EXPENSE_CREATED);
            sb.append(newExpenseRequest.getExpenseDescription());
            activityRequest.setMessage(sb.toString());
        }
        else if(ActivityType.EXPENSE_UPDATED.equals(activityType))
        {
            //Expense Update Activity
            sb.append(StringConstants.USER_ID_PREFIX);
            sb.append(newExpenseRequest.getUpdatedBy());
            sb.append(StringConstants.USER_ID_SUFFIX);
            sb.append(StringConstants.EXPENSE_UPDATED);
            sb.append(newExpenseRequest.getExpenseDescription());
            activityRequest.setMessage(sb.toString());
            if(oldExpenseRequest != null)
            {
                List<ChangeLog> changeLogs = createChangeLogForExpenseModify(newExpenseRequest,oldExpenseRequest);
                if(changeLogs != null && !changeLogs.isEmpty())
                {
                    activityRequest.setChangeLogs(changeLogs);
                }
            }

        }
        else if (ActivityType.EXPENSE_DELETED.equals(activityType))
        {
            //Expense Delete Activity
            sb.append(StringConstants.USER_ID_PREFIX);
            sb.append(newExpenseRequest.getUpdatedBy());
            sb.append(StringConstants.USER_ID_SUFFIX);
            sb.append(StringConstants.EXPENSE_DELETED);
            sb.append(newExpenseRequest.getExpenseDescription());
            activityRequest.setMessage(sb.toString());
        }
        //Send request to the producer
        try
        {
            Gson gson = new Gson();
            String activityJson = gson.toJson(activityRequest);
            kafkaProducer.sendActivityMessage(activityJson);
        }
        catch (Exception ex)
        {
            LOGGER.error("Error occurred while sending message to Kafka Topic "+ex);
        }
    }

    private List<ChangeLog> createChangeLogForExpenseModify(ExpenseRequest newExpenseRequest,
                                                    ExpenseRequest oldExpenseRequest) {
        List<ChangeLog> changeLogs = new ArrayList<>();
        if (newExpenseRequest != null && oldExpenseRequest != null)
        {
            if(!newExpenseRequest.equals(oldExpenseRequest))
            {
                if(!oldExpenseRequest.getExpenseDescription().equals(newExpenseRequest.getExpenseDescription()))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(StringConstants.DESCRIPTION);
                    sb.append(" from ");
                    sb.append(oldExpenseRequest.getExpenseDescription());
                    sb.append(" to ");
                    sb.append(newExpenseRequest.getExpenseDescription());
                    changeLogs.add(new ChangeLog(sb.toString()));
                }
                if(!oldExpenseRequest.getTotalAmount().equals(newExpenseRequest.getTotalAmount()))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(StringConstants.AMOUNT);
                    sb.append(" from ");
                    sb.append(oldExpenseRequest.getTotalAmount());
                    sb.append(" to ");
                    sb.append(newExpenseRequest.getTotalAmount());
                    changeLogs.add(new ChangeLog(sb.toString()));
                }
                if(!oldExpenseRequest.getCategory().equals(newExpenseRequest.getCategory()))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(StringConstants.CATEGORY);
                    sb.append(" from ");
                    sb.append(oldExpenseRequest.getCategory());
                    sb.append(" to ");
                    sb.append(newExpenseRequest.getCategory());
                    changeLogs.add(new ChangeLog(sb.toString()));
                }
               if(!oldExpenseRequest.getSpentOnDate().equals(newExpenseRequest.getSpentOnDate()))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(StringConstants.SPENT_ON_DATE);
                    sb.append(" from ");
                    sb.append(oldExpenseRequest.getSpentOnDate());
                    sb.append(" to ");
                    sb.append(newExpenseRequest.getSpentOnDate());
                    changeLogs.add(new ChangeLog(sb.toString()));
                }
                if(!oldExpenseRequest.getSplitType().equals(newExpenseRequest.getSplitType()))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(StringConstants.SPLIT_TYPE);
                    sb.append(" from ");
                    sb.append(oldExpenseRequest.getSplitType());
                    sb.append(" to ");
                    sb.append(newExpenseRequest.getSplitType());
                    changeLogs.add(new ChangeLog(sb.toString()));
                }
            }
        }
        return changeLogs;
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
        //Record update Activity
        createExpenseActivity(ActivityType.EXPENSE_UPDATED,expenseRequest,oldExpenseRequest);
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
        BalanceCalculator balanceCalculator = null;
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
    public void deleteExpenseDetails(Long expenseId,Long loggedInUser) {
        try
        {
            ExpenseRequest expenseRequest = createExpenseRequestFromExpenseId(expenseId);
            revertPreviousBalanceForExpense(expenseRequest);
            expenseRepository.deleteByExpenseId(expenseId);
            expenseParticipantService.deleteExpenseParticipants(expenseId);
            paidUserService.deleteByExpenseId(expenseId);
            if(expenseRequest != null)
            {
                expenseRequest.setUpdatedBy(loggedInUser);
                createExpenseActivity(ActivityType.EXPENSE_DELETED,expenseRequest,null);
            }
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Error occurred while processing delete request "+ex);
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

    public String getExpenseDescById(Long expenseId)
    {
       return expenseRepository.getExpenseDescById(expenseId);
    }

    public List<ExpenseResponse> getExpensesByGroupId(Long groupId) {
        List<ExpenseResponse> expenseResponseList = new ArrayList<>();
        try {
            List<Long> expenseIds = expenseRepository.getExpensesByGroupId(groupId);
            for(Long expenseId : expenseIds)
            {
                //Get Expense Data
                Expense expense = expenseRepository.findByExpenseId(expenseId).get();
                //Get Paid Users List
                List<PaidUser> paidUsers = paidUserService.findByExpenseId(expenseId);
                //Get Participants List
                List<ExpenseParticipant> expenseParticipants= expenseParticipantService.getParticipantsByExpenseId(expenseId);
                //Get userId and userName Map using feign client
                Map<Long, String> userNameMap = userClient.getUserNameMapByGroupId(groupId);
                //Prepare Expense Response
                ExpenseResponse expenseResponse = expenseMapper.createExpenseResonse(expense,expenseParticipants,paidUsers,userNameMap);
                expenseResponseList.add(expenseResponse);
            }
        }
        catch (Exception ex)
        {
            LOGGER.error("Exception occurred while fetching Expenses", ex.getMessage());
            throw ex;
        }

        return expenseResponseList;
    }
}
