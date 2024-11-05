package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.clients.UserClient;
import com.splitwise.microservices.expense_service.entity.Balance;
import com.splitwise.microservices.expense_service.entity.Settlement;
import com.splitwise.microservices.expense_service.mapper.BalanceMapper;
import com.splitwise.microservices.expense_service.model.BalanceResponse;
import com.splitwise.microservices.expense_service.model.BalanceSummary;
import com.splitwise.microservices.expense_service.model.GroupBalanceSummary;
import com.splitwise.microservices.expense_service.repository.BalanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BalanceService {
    @Autowired
    BalanceRepository balanceRepository;
    @Autowired
    UserClient userClient;
    @Autowired
    BalanceMapper balanceMapper;

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
                newBalanceAmount = Math.round(newBalanceAmount * 100.0) / 100.0; // Rounding newBalanceAmount to two decimal places
                if(newBalanceAmount < 0)
                {
                    balance.setBalanceAmount(Math.abs(newBalanceAmount));
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
                    return;
                }

            }
            //Check if the receiver owes any balance to payer
            balance = getPastBalanceOfUser(receiverId,payerId,groupId);
            if(balance != null)
            {
                    Double pastBalanceAmount = balance.getBalanceAmount();
                    Double newBalanceAmount = pastBalanceAmount + amountPaid;
                    newBalanceAmount = Math.round(newBalanceAmount * 100.0) / 100.0; // Rounding newBalanceAmount to two decimal places
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

    public List<BalanceResponse> getAllBalancesByGroupId(Long groupId, Long userId) {
        List<BalanceResponse> balanceResponseList = new ArrayList<>();
        try
        {
            List<Balance> balanceList =  balanceRepository.getUserBalancesByGroupId(groupId, userId);
            if(balanceList != null && !balanceList.isEmpty()){
                Set<Long> userIds = new HashSet<>();
                for(Balance balance: balanceList){
                    userIds.add(balance.getUserId());
                    userIds.add(balance.getOwesTo());
                }
                Map<Long,String> userNameMap = userClient.getUserNameMapByUserIds(new ArrayList<>(userIds));
                Map<Long,String> groupNameMap = userClient.getGroupNameMapByGroupId(groupId);
                balanceResponseList = balanceMapper.createBalanceResponse(balanceList,userNameMap,groupNameMap,userId);
            }
        }
        catch (Exception ex) {
            //Need to throw Exception
        }
        return balanceResponseList;
    }

    public List<BalanceResponse> getUsersAllBalances(Long userId) {
        List<BalanceResponse> balanceResponseList = new ArrayList<>();
        try
        {
            List<Balance> balanceList  =  balanceRepository.getUserAllBalances(userId);

            if(balanceList != null && !balanceList.isEmpty()){
                Set<Long> userIds = new HashSet<>();
                for(Balance balance: balanceList){
                    userIds.add(balance.getUserId());
                    userIds.add(balance.getOwesTo());
                }
                Map<Long,String> userNameMap = userClient.getUserNameMapByUserIds(new ArrayList<>(userIds));
                Map<Long,String> groupNameMap = userClient.getGroupNameMap(userId);
                balanceResponseList = balanceMapper.createBalanceResponse(balanceList,userNameMap,groupNameMap,userId);
            }
        }
        catch(Exception ex)
        {
            //Need to throw Exception
        }
        return balanceResponseList;
    }

    public BalanceSummary getUserBalanceSummaryByUserId(Long userId) {

        BalanceSummary balanceSummary = new BalanceSummary();
        try{
            List<Balance> balances = balanceRepository.getUserAllBalances(userId);
            if(balances != null && !balances.isEmpty()){
                Double oweAmount = balances.stream().filter(b -> b.getUserId().equals(userId)).mapToDouble(b->b.getBalanceAmount()).sum();
                Double owedAmount = balances.stream().filter(b -> b.getOwesTo().equals(userId)).mapToDouble(b->b.getBalanceAmount()).sum();
                balanceSummary.setUserId(userId);
                balanceSummary.setOweAmount(oweAmount);
                balanceSummary.setOwedAmount(owedAmount);
            }
        }
        catch (Exception ex){
            // Need to throw Exception
        }

            return balanceSummary;
        }
     public Double getUserPendingBalance(Long userId){
        return balanceRepository.getPendingBalancesByUserId(userId);
     }
    public List<GroupBalanceSummary> getGroupBalanceSummaryByUserId(Long userId){

        List<GroupBalanceSummary> balanceSummaryList = new ArrayList<>();
        Map<Long, GroupBalanceSummary> balanceMap = new HashMap<>();
        try{
            List<Balance> balances = balanceRepository.getUserAllBalances(userId);
            if(balances != null && !balances.isEmpty()){
                for(Balance balance : balances){
                    if(balance.getUserId().equals(userId))
                    {
                        balance.setBalanceAmount(-balance.getBalanceAmount());
                    }
                    if(balanceMap.containsKey(balance.getGroupId())){
                        GroupBalanceSummary balanceSummary  = balanceMap.get(balance.getGroupId());
                        Double existingAmount = balanceSummary.getAmount();
                        balanceSummary.setAmount(balance.getBalanceAmount() + existingAmount);
                        balanceSummary.setIsOwed(balance.getBalanceAmount() > 0 ? true : false);
                        balanceMap.put(balance.getGroupId(),balanceSummary);
                    }
                    else {
                        GroupBalanceSummary balanceSummary = GroupBalanceSummary.builder()
                                .groupId(balance.getGroupId())
                                .amount(balance.getBalanceAmount())
                                .isOwed(balance.getBalanceAmount() > 0 ? true : false)
                                .build();
                        balanceMap.put(balance.getGroupId(),balanceSummary);
                    }
                }
                balanceSummaryList = new ArrayList<>(balanceMap.values());
            }
        }
        catch (Exception ex){
            // Need to throw Exception
        }
        return balanceSummaryList;
    }
    public List<GroupBalanceSummary> getGroupBalanceSummaryByUserId(Long userId, Long groupId){

        List<GroupBalanceSummary> balanceSummaryList = new ArrayList<>();
        Map<Long, GroupBalanceSummary> balanceMap = new HashMap<>();
        try{
            List<Balance> balances = balanceRepository.getUserBalancesByGroupId(groupId, userId);
            if(balances != null && !balances.isEmpty()){
                for(Balance balance : balances){
                    if(balance.getUserId().equals(userId))
                    {
                        balance.setBalanceAmount(-balance.getBalanceAmount());
                    }
                    if(balanceMap.containsKey(balance.getGroupId())){
                        GroupBalanceSummary balanceSummary  = balanceMap.get(balance.getGroupId());
                        Double existingAmount = balanceSummary.getAmount();
                        balanceSummary.setAmount(balance.getBalanceAmount() + existingAmount);
                        balanceSummary.setIsOwed(balance.getBalanceAmount() > 0 ? true : false);
                        balanceMap.put(balance.getGroupId(),balanceSummary);
                    }
                    else {
                        GroupBalanceSummary balanceSummary = GroupBalanceSummary.builder()
                                .groupId(balance.getGroupId())
                                .amount(balance.getBalanceAmount())
                                .isOwed(balance.getBalanceAmount() > 0 ? true : false)
                                .build();
                        balanceMap.put(balance.getGroupId(),balanceSummary);
                    }
                }
                balanceSummaryList = new ArrayList<>(balanceMap.values());
            }
        }
        catch (Exception ex){
            // Need to throw Exception
        }
        return balanceSummaryList;
    }

    }
