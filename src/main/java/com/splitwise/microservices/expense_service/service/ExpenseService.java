package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.clients.ActivityClient;
import com.splitwise.microservices.expense_service.clients.UserClient;
import com.splitwise.microservices.expense_service.constants.StringConstants;
import com.splitwise.microservices.expense_service.entity.Balance;
import com.splitwise.microservices.expense_service.entity.Expense;
import com.splitwise.microservices.expense_service.entity.ExpenseParticipant;
import com.splitwise.microservices.expense_service.entity.PaidUser;
import com.splitwise.microservices.expense_service.enums.ActivityType;
import com.splitwise.microservices.expense_service.exception.ExpenseException;
import com.splitwise.microservices.expense_service.external.Activity;
import com.splitwise.microservices.expense_service.external.ActivityRequest;
import com.splitwise.microservices.expense_service.external.ChangeLog;
import com.splitwise.microservices.expense_service.mapper.ExpenseMapper;
import com.splitwise.microservices.expense_service.model.ExpenseResponse;
import com.splitwise.microservices.expense_service.model.ExpenseRequest;
import com.splitwise.microservices.expense_service.model.ParticipantShare;
import com.splitwise.microservices.expense_service.repository.ExpenseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    ActivityClient activityClient;
    public static final Logger LOGGER = LoggerFactory.getLogger(ExpenseService.class);

    public Expense saveExpenseFromRequest(ExpenseRequest expenseRequest) {
        Expense expenseObj = expenseMapper.getExpenseFromRequest(expenseRequest);
        Expense savedExpense = null;
        if (expenseObj != null) {
            savedExpense = expenseRepository.save(expenseObj);
            savePaidUsers(expenseRequest.getPaidUsers(), savedExpense.getExpenseId());
        }
        return savedExpense;
    }

    /**
     * This method handles saving Expense and related details
     *
     * @param expenseRequest
     */
    @Transactional
    public void saveExpenseAndParticipantDetails(ExpenseRequest expenseRequest) {
        Expense savedExpense = saveExpenseFromRequest(expenseRequest);
        if (savedExpense != null) {
            boolean isParticipantsSaved = expenseParticipantService.saveExpenseParticipantsFromRequest(expenseRequest,
                    savedExpense.getExpenseId());

            if (isParticipantsSaved) {
                //Todo: Add validation for total amount equals to payers` sum (On request)
                //Calculate and save individual balances users owe
                saveParticipantsBalance(expenseRequest);
                expenseRequest.setExpenseId(savedExpense.getExpenseId());//Setting saved expenseId to request
                createExpenseActivity(ActivityType.EXPENSE_CREATED, expenseRequest, null);
            } else {
                throw new RuntimeException("Error occurred while saving Expense");
            }
        } else {
            throw new RuntimeException("Error occurred while saving Expense");
        }
    }

    private void createExpenseActivity(ActivityType activityType, ExpenseRequest newExpenseRequest,
                                       ExpenseRequest oldExpenseRequest) {
        try {
            Activity activity = Activity.builder()
                    .activityType(activityType)
                    .createDate(newExpenseRequest.getCreateDate())
                    .expenseId(newExpenseRequest.getExpenseId())
                    .groupId(newExpenseRequest.getGroupId())
                    .build();

            StringBuilder sb = new StringBuilder();
            if (ActivityType.EXPENSE_CREATED.equals(activityType)) {
                //Expense Create Activity
                String createdUserName = userClient.getUserName(newExpenseRequest.getCreatedBy());
                sb.append(createdUserName);
                sb.append(StringConstants.EXPENSE_CREATED);
                sb.append(newExpenseRequest.getExpenseDescription());
            } else if (ActivityType.EXPENSE_UPDATED.equals(activityType)) {
                //Expense Update Activity
                String updatedUserName = userClient.getUserName(newExpenseRequest.getUpdatedBy());
                sb.append(updatedUserName);
                sb.append(StringConstants.EXPENSE_UPDATED);
                sb.append(newExpenseRequest.getExpenseDescription());
                if (oldExpenseRequest != null) {
                    List<ChangeLog> changeLogs = createChangeLogForExpenseModify(newExpenseRequest, oldExpenseRequest);
                    if (changeLogs != null && !changeLogs.isEmpty()) {
                        activity.setChangeLogs(changeLogs);
                    }
                }

            } else if (ActivityType.EXPENSE_DELETED.equals(activityType)) {
                //Expense Delete Activity
                String deletedUserName = userClient.getUserName(newExpenseRequest.getUpdatedBy());
                sb.append(deletedUserName);
                sb.append(StringConstants.EXPENSE_DELETED);
                sb.append(newExpenseRequest.getExpenseDescription());

            }
            String groupName = userClient.getGroupName(newExpenseRequest.getGroupId());
            sb.append(" in " + groupName);
            activity.setMessage(sb.toString());
            List<Long> relatedUsersIds = getRelatedUsersIdFromExpense(newExpenseRequest, oldExpenseRequest);
            ActivityRequest activityRequest = ActivityRequest.builder()
                    .activity(activity)
                    .userIdList(relatedUsersIds)
                    .build();
            //Send ActivityRequest to Orchestrate
            activityClient.sendActivityRequest(activityRequest);

        } catch (Exception ex) {
            LOGGER.error("Error occurred while creating Expense Activity" + ex);
        }
    }

    public List<Long> getRelatedUsersIdFromExpense(ExpenseRequest newExpenseReq, ExpenseRequest oldExpenseReq) {
        Set<Long> userIdSet = new HashSet<>();
        if (newExpenseReq != null) {
            List<PaidUser> paidUsers = newExpenseReq.getPaidUsers();
            List<ParticipantShare> participants = newExpenseReq.getParticipantShareList();
            for (PaidUser paidUser : paidUsers) {
                userIdSet.add(paidUser.getUserId());
            }
            for (ParticipantShare participant : participants) {
                userIdSet.add(participant.getUserId());
            }
        }
        if (oldExpenseReq != null) {
            List<PaidUser> paidUsers = oldExpenseReq.getPaidUsers();
            List<ParticipantShare> participants = oldExpenseReq.getParticipantShareList();
            for (PaidUser paidUser : paidUsers) {
                userIdSet.add(paidUser.getUserId());
            }
            for (ParticipantShare participant : participants) {
                userIdSet.add(participant.getUserId());
            }
        }
        return new ArrayList<>(userIdSet);
    }

    private List<ChangeLog> createChangeLogForExpenseModify(ExpenseRequest newExpenseRequest,
                                                            ExpenseRequest oldExpenseRequest) {
        List<ChangeLog> changeLogs = new ArrayList<>();
        if (newExpenseRequest != null && oldExpenseRequest != null) {
            if (!newExpenseRequest.equals(oldExpenseRequest)) {
                if (!oldExpenseRequest.getExpenseDescription().equals(newExpenseRequest.getExpenseDescription())) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(StringConstants.DESCRIPTION);
                    sb.append(" from ");
                    sb.append(oldExpenseRequest.getExpenseDescription());
                    sb.append(" to ");
                    sb.append(newExpenseRequest.getExpenseDescription());
                    changeLogs.add(new ChangeLog(sb.toString()));
                }
                if (!oldExpenseRequest.getTotalAmount().equals(newExpenseRequest.getTotalAmount())) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(StringConstants.AMOUNT);
                    sb.append(" from ");
                    sb.append("₹" + oldExpenseRequest.getTotalAmount());
                    sb.append(" to ");
                    sb.append("₹" + newExpenseRequest.getTotalAmount());
                    changeLogs.add(new ChangeLog(sb.toString()));
                }
                if (!oldExpenseRequest.getCategory().equals(newExpenseRequest.getCategory())) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(StringConstants.CATEGORY);
                    sb.append(" from ");
                    sb.append(oldExpenseRequest.getCategory());
                    sb.append(" to ");
                    sb.append(newExpenseRequest.getCategory());
                    changeLogs.add(new ChangeLog(sb.toString()));
                }
                LocalDate oldSpentOnDate = oldExpenseRequest.getSpentOnDate()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                LocalDate newSpentOnDate = newExpenseRequest.getSpentOnDate()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                if (!oldSpentOnDate.equals(newSpentOnDate)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(StringConstants.SPENT_ON_DATE);
                    sb.append(" from ");
                    sb.append(oldSpentOnDate);
                    sb.append(" to ");
                    sb.append(newSpentOnDate);
                    changeLogs.add(new ChangeLog(sb.toString()));
                }
                if (!oldExpenseRequest.getSplitType().equals(newExpenseRequest.getSplitType())) {
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


    public void savePaidUsers(List<PaidUser> paidUsers, Long expenseId) {
        try {
            for (PaidUser paidUser : paidUsers) {
                paidUser.setExpenseId(expenseId);
                paidUserService.savePaidUser(paidUser);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error occurred while saving paid User details", ex);
        }
    }

    @Transactional
    public void updateExpenseAndParticipantsFromRequest(ExpenseRequest expenseRequest) throws ExpenseException {
        if (expenseRequest == null) {
            throw new RuntimeException("Request cannot be null");
        }
        Long expenseId = expenseRequest.getExpenseId();
        try {

            //Undo previous balance calculation
            ExpenseRequest oldExpenseRequest = createExpenseRequestFromExpenseId(expenseId);
            revertPreviousBalanceForExpense(oldExpenseRequest);

            Expense updatedExpense = expenseMapper.getExpenseFromRequest(expenseRequest);
            updatedExpense.setExpenseId(expenseId);
            List<PaidUser> paidUsers = expenseRequest.getPaidUsers();
            //Save Expense and Paid Users
            expenseRepository.save(updatedExpense);
            paidUserService.updatePaidUsers(paidUsers, expenseId);
            //Save Participants
            expenseParticipantService.updateParticipantsExpense(expenseRequest, expenseId);
            //Calculate balance for updated expense
            saveParticipantsBalance(expenseRequest);
            //Record update Activity
            createExpenseActivity(ActivityType.EXPENSE_UPDATED, expenseRequest, oldExpenseRequest);
        } catch (Exception ex) {
            System.out.println(ex);
            throw new RuntimeException("Error occurred while updating Expense details");
        }
    }

    public void saveParticipantsBalance(ExpenseRequest expenseRequest) {
        if (expenseRequest == null) {
            throw new RuntimeException("Request cannot be null");
        }
        Map<Long, Map<Long, Double>> balanceMap = calculateParticipantsBalance(expenseRequest);
        saveParticipantsBalance(balanceMap, expenseRequest.getGroupId());
    }

    /**
     * This method undo the balance calculation of an expense before update/delete request
     *
     * @param expenseRequest
     */
    public void revertPreviousBalanceForExpense(ExpenseRequest expenseRequest) {
        if (expenseRequest == null) {
            throw new RuntimeException("Request cannot be null");
        }
        Long groupId = expenseRequest.getGroupId();
        Map<Long, Map<Long, Double>> balanceMap = calculateParticipantsBalance(expenseRequest);
        Map<Long, Map<Long, Double>> reverseBalanceMap = new HashMap<>();
        if (balanceMap != null) {
            for (Map.Entry<Long, Map<Long, Double>> mapEntry : balanceMap.entrySet()) {
                Long paidUser = mapEntry.getKey();
                for (Map.Entry<Long, Double> participantEntry : mapEntry.getValue().entrySet()) {
                    Long participantId = participantEntry.getKey();
                    Double oweAmount = participantEntry.getValue();
                    reverseBalanceMap.putIfAbsent(participantId, new HashMap<>());
                    Map<Long, Double> paidUserMap = reverseBalanceMap.get(participantId);
                    paidUserMap.put(paidUser, oweAmount);
                    reverseBalanceMap.put(participantId, paidUserMap);
                }
            }
        }
        saveParticipantsBalance(reverseBalanceMap, groupId);
    }

    public void saveParticipantsBalance(Map<Long, Map<Long, Double>> balanceMap, Long groupId) {
        if (balanceMap != null) {
            for (Map.Entry<Long, Map<Long, Double>> mapEntry : balanceMap.entrySet()) {
                Long paidUserId = mapEntry.getKey();
                Map<Long, Double> participantsMap = mapEntry.getValue();

                if (participantsMap != null) {
                    for (Map.Entry<Long, Double> participantEntry : participantsMap.entrySet()) {
                        Long participantId = participantEntry.getKey();
                        Double amountOwes = participantEntry.getValue();

                        //Check if participant owes any amount to paid user in the past
                        Balance existingBalance = balanceService.getPastBalanceOfUser(participantId, paidUserId, groupId);
                        if (existingBalance != null) {
                            //Update existing balance
                            amountOwes = amountOwes + existingBalance.getBalanceAmount();
                            amountOwes = Math.round(amountOwes * 100.0) / 100.0; // rounding
                            existingBalance.setBalanceAmount(amountOwes);
                            balanceService.saveBalance(existingBalance);
                        } else {
                            //Check if paid user owes any amount to participant in the past
                            existingBalance = balanceService.getPastBalanceOfUser(paidUserId, participantId, groupId);

                            if (existingBalance != null) {
                                // Reduce balances
                                Double updatedAmount = existingBalance.getBalanceAmount() - amountOwes;
                                updatedAmount = Math.round(updatedAmount * 100.0) / 100.0; // rounding

                                if (updatedAmount > 0) {
                                    existingBalance.setBalanceAmount(updatedAmount);
                                    balanceService.saveBalance(existingBalance);
                                } else if (updatedAmount < 0) {
                                    // If balance goes negative, we switch the direction
                                    balanceService.deleteBalanceById(existingBalance.getBalanceId());

                                    Balance newBalance = Balance.builder()
                                            .groupId(groupId)
                                            .userId(participantId)
                                            .owesTo(paidUserId)
                                            .balanceAmount(Math.abs(updatedAmount))
                                            .build();
                                    balanceService.saveBalance(newBalance);
                                } else {
                                    // Balance settled
                                    balanceService.deleteBalanceById(existingBalance.getBalanceId());
                                }
                            } else {
                                // Create a new balance if no existing balance found
                                Balance newBalance = Balance.builder()
                                        .groupId(groupId)
                                        .userId(participantId)
                                        .owesTo(paidUserId)
                                        .balanceAmount(amountOwes)
                                        .build();
                                balanceService.saveBalance(newBalance);
                            }
                        }
                    }
                }
            }
        }
    }


    public Map<Long, Map<Long, Double>> calculateParticipantsBalance(ExpenseRequest expenseRequest) {
        Map<Long, Map<Long, Double>> balanceMap = null;
        Long groupId = expenseRequest.getGroupId();
        BalanceCalculator balanceCalculator = null;
        if (expenseRequest != null && expenseRequest.getPaidUsers().size() > 1) {
            balanceCalculator = new MultiPayerBalanceCalculator();
        } else {
            balanceCalculator = new SinglePayerBalanceCalculator();
        }
        return balanceCalculator.calculateBalance(expenseRequest);
    }

    @Transactional
    public void deleteExpenseDetails(Long expenseId, Long loggedInUser) {
        try {
            ExpenseRequest expenseRequest = createExpenseRequestFromExpenseId(expenseId);
            revertPreviousBalanceForExpense(expenseRequest);
            expenseRepository.deleteByExpenseId(expenseId);
            expenseParticipantService.deleteExpenseParticipants(expenseId);
            paidUserService.deleteByExpenseId(expenseId);
            if (expenseRequest != null) {
                expenseRequest.setUpdatedBy(loggedInUser);
                createExpenseActivity(ActivityType.EXPENSE_DELETED, expenseRequest, null);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error occurred while processing delete request " + ex);
        }
    }

    private ExpenseRequest createExpenseRequestFromExpenseId(Long expenseId) {
        List<PaidUser> paidUsers = paidUserService.findByExpenseId(expenseId);
        List<ExpenseParticipant> participantList = expenseParticipantService.getParticipantsByExpenseId(expenseId);
        Optional<Expense> optionalExpense = expenseRepository.findByExpenseId(expenseId);
        if (!optionalExpense.isPresent()) {
            throw new RuntimeException("Expense you are trying to delete doesn't exists");
        }
        Expense expense = optionalExpense.get();
        ExpenseRequest expenseRequest = expenseMapper.createExpenseRequestFromExpenseId(expense, participantList,
                paidUsers);
        return expenseRequest;
    }

    public String getExpenseDescById(Long expenseId) {
        return expenseRepository.getExpenseDescById(expenseId);
    }

    public List<ExpenseResponse> getExpensesByGroupId(Long groupId) {
        List<ExpenseResponse> expenseResponseList = new ArrayList<>();
        try {
            List<Long> expenseIds = expenseRepository.getExpensesByGroupId(groupId);
            //Get userId and userName Map using feign client
            Map<Long, String> userNameMap = userClient.getUserNameMapByGroupId(groupId);
            for (Long expenseId : expenseIds) {
                //Get Expense Data
                Expense expense = expenseRepository.findByExpenseId(expenseId).get();
                //Get Paid Users List
                List<PaidUser> paidUsers = paidUserService.findByExpenseId(expenseId);
                //Get Participants List
                List<ExpenseParticipant> expenseParticipants = expenseParticipantService.getParticipantsByExpenseId(expenseId);
                //Get groupNameMap
                Map<Long, String> groupNameMap = userClient.getGroupNameMapByGroupId(groupId);
                //Prepare Expense Response
                ExpenseResponse expenseResponse = expenseMapper.createExpenseResponse(expense, expenseParticipants, paidUsers, userNameMap, groupNameMap);
                expenseResponseList.add(expenseResponse);
            }
        } catch (Exception ex) {
            LOGGER.error("Exception occurred while fetching Expenses" + ex);
            throw ex;
        }

        return expenseResponseList;
    }

    public List<ExpenseResponse> getExpensesByExpenseIds(List<Long> expenseIds, Map<Long, String> userNameMap, Map<Long, String> groupNameMap) {
        List<ExpenseResponse> expenseResponseList = new ArrayList<>();
        try {
            for (Long expenseId : expenseIds) {
                //Get Expense Data
                Expense expense = expenseRepository.findByExpenseId(expenseId).get();
                //Get Paid Users List
                List<PaidUser> paidUsers = paidUserService.findByExpenseId(expenseId);
                //Get Participants List
                List<ExpenseParticipant> expenseParticipants = expenseParticipantService.getParticipantsByExpenseId(expenseId);
                //Prepare Expense Response
                ExpenseResponse expenseResponse = expenseMapper.createExpenseResponse(expense, expenseParticipants, paidUsers, userNameMap, groupNameMap);
                expenseResponseList.add(expenseResponse);
            }
        } catch (Exception ex) {
            LOGGER.error("Error occurred while retrieving Expenses at getExpensesByExpenseIds() " + ex);
            throw ex;
        }
        return expenseResponseList;
    }

    public List<ExpenseResponse> getExpensesByUserId(Long userId) {
        List<ExpenseResponse> expenseResponseList = new ArrayList<>();
        try {
            //Get expense id from paidUser by userId
            List<Long> paidUserExpenseIds = paidUserService.getPaidUsersExpenseIdByUserId(userId);
            //Get expense if from participants by userId
            List<Long> participantsExpenseIds = expenseParticipantService.getParticipantsExpenseIdByUserId(userId);

            if (paidUserExpenseIds != null && !paidUserExpenseIds.isEmpty() || participantsExpenseIds != null && !participantsExpenseIds.isEmpty()) {
                //Extract unique expenseId
                List<Long> uniqueExpenseId = Stream.concat(paidUserExpenseIds.stream(), participantsExpenseIds.stream()).distinct().collect(Collectors.toList());
                //Get userNameMap of Friends by userId
                Map<Long, String> userNameMap = userClient.getFriendsUserNameMapByUserId(userId);
                //Get groupNameMap using userId
                Map<Long, String> groupNameMap = userClient.getGroupNameMap(userId);
                //Get ExpenseResponse List with ExpenseIds
                expenseResponseList = getExpensesByExpenseIds(uniqueExpenseId, userNameMap, groupNameMap);
            }
        } catch (Exception ex) {
            LOGGER.error("Error occurred while retrieving Expenses at getExpensesByUserId() " + ex);
            throw ex;
        }
        return expenseResponseList;
    }

    public ExpenseResponse getExpensesByExpenseId(Long expenseId, Long userId) {
        ExpenseResponse expenseResponse = null;
        try {
            Optional<Expense> optional = expenseRepository.findByExpenseId(expenseId);
            if (optional.isPresent()) {
                Expense expense = optional.get();
                List<PaidUser> paidUserList = paidUserService.findByExpenseId(expenseId);
                List<ExpenseParticipant> participantList = expenseParticipantService.getParticipantsByExpenseId(expenseId);
                //Get userNameMap of Friends by userId
                Map<Long, String> userNameMap = userClient.getFriendsUserNameMapByUserId(userId);
                //Get groupNameMap using userId
                Map<Long, String> groupNameMap = userClient.getGroupNameMap(userId);
                //Prepare Expense Response
                expenseResponse =  expenseMapper.createExpenseResponse(expense, participantList, paidUserList, userNameMap, groupNameMap);
            }
        } catch (Exception ex) {
            LOGGER.error("Error occurred while retrieving Expenses at getExpensesByExpenseId() " + ex);
            throw ex;
        }
        return expenseResponse;
    }
}
