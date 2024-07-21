package com.splitwise.microservices.expense_service.service;

import com.fasterxml.jackson.databind.deser.impl.SetterlessProperty;
import com.splitwise.microservices.expense_service.clients.UserClient;
import com.splitwise.microservices.expense_service.constants.StringConstants;
import com.splitwise.microservices.expense_service.entity.Settlement;
import com.splitwise.microservices.expense_service.enums.ActivityType;
import com.splitwise.microservices.expense_service.external.ActivityRequest;
import com.splitwise.microservices.expense_service.external.ChangeLog;
import com.splitwise.microservices.expense_service.repository.SettlementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SettlementService {
    @Autowired
    SettlementRepository settlementRepository;
    @Autowired
    BalanceService balanceService;
    @Autowired
    UserClient userClient;

    public Settlement saveSettlement(Settlement settlement)
    {
        try
        {
            //Settle Balance Amount
            balanceService.calculateBalanceForSettlement(settlement);

            Settlement savedSettlement =  settlementRepository.save(settlement);
            createSettlementActivity(ActivityType.PAYMENT_CREATED,settlement,null);
            return savedSettlement;
        }
        catch(Exception ex)
        {
            //Need to throw exception
        }
        return settlement;
    }

    private void createSettlementActivity(ActivityType activityType, Settlement newSettlement,Settlement oldSettlement)
    {
        ActivityRequest activityRequest = ActivityRequest.builder()
                .activityType(activityType)
                .createDate(null)
                .groupId(newSettlement.getGroupId())
                .settlementId(newSettlement.getSettlementId())
                .build();
        StringBuilder sb = new StringBuilder();
        String userName = StringConstants.EMPTY_STRING;
        if(ActivityType.PAYMENT_CREATED.equals(activityType))
        {
            //Payment Create Activity
            userName = userClient.getUserName(newSettlement.getCreatedBy());
            sb.append(StringConstants.PAYMENT_CREATED);
            sb.append("from");
            sb.append(newSettlement.getPaidBy());
            sb.append("to");
            sb.append(newSettlement.getPaidTo());
            activityRequest.setMessage(sb.toString());
        }
        if(ActivityType.PAYMENT_UPDATED.equals(activityType))
        {
            //Payment Delete Activity
            userName = userClient.getUserName(newSettlement.getCreatedBy());
            sb.append(StringConstants.PAYMENT_UPDATED);
            sb.append("from");
            sb.append(newSettlement.getPaidBy());
            sb.append("to");
            sb.append(newSettlement.getPaidTo());
            activityRequest.setMessage(sb.toString());
            if(oldSettlement != null)
            {
                List<ChangeLog> changeLogs = createChangeLogForSettlementModify(newSettlement,oldSettlement);
                if(changeLogs != null && changeLogs.isEmpty())
                {
                    activityRequest.setChangeLogs(changeLogs);
                }
            }
        }

        if(ActivityType.PAYMENT_DELETED.equals(activityType))
        {
            //Payment Delete Activity
            userName = userClient.getUserName(newSettlement.getCreatedBy());
            sb.append(StringConstants.PAYMENT_DELETED);
            sb.append("from");
            sb.append(newSettlement.getPaidBy());
            sb.append("to");
            sb.append(newSettlement.getPaidTo());
            activityRequest.setMessage(sb.toString());
        }

    }

    private List<ChangeLog> createChangeLogForSettlementModify(Settlement newSettlement, Settlement oldSettlement)
    {
        List<ChangeLog> changeLogs = new ArrayList<>();
        if(newSettlement != null && oldSettlement != null)
        {
            if(!oldSettlement.equals(newSettlement))
            {
                if(!oldSettlement.getAmountPaid().equals(newSettlement.getAmountPaid()))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(StringConstants.AMOUNT);
                    sb.append(" from ");
                    sb.append((oldSettlement.getAmountPaid()));
                    sb.append(" to ");
                    sb.append(newSettlement.getAmountPaid());
                    changeLogs.add(new ChangeLog(sb.toString()));
                }
                if(!oldSettlement.getPaymentMethod().equals(newSettlement.getPaymentMethod()))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(StringConstants.PAYMENT_METHOD);
                    sb.append(" from ");
                    sb.append((oldSettlement.getAmountPaid()));
                    sb.append(" to ");
                    sb.append(newSettlement.getAmountPaid());
                    changeLogs.add(new ChangeLog(sb.toString()));
                }
            }

        }
        return changeLogs;
    }

    public Settlement getSettlementById(Long settlementId)
    {
        Optional<Settlement> optional = settlementRepository.findById(settlementId);
        return optional.isPresent()? optional.get() : null;
    }
    public List<Settlement> getAllSettlementByGroupId(Long groupId)
    {
        List<Settlement> settlements = new ArrayList<>();
        try
        {
            settlements = settlementRepository.getAllSettlementByGroupId(groupId);
        }
        catch(Exception ex)
        {
            //Need to throw exception
        }
        return settlements;
    }



    public boolean deleteSettlementById(Long settlementId)
    {
        boolean isDeleted = false;
        try
        {
            Settlement existingSettlement = getSettlementById(settlementId);
            balanceService.revertPreviousBalanceForSettlement(existingSettlement);
            settlementRepository.deleteSettlementById(settlementId);
            isDeleted = true;
        }
        catch(Exception ex)
        {
            //Need to throw exception
        }
        return isDeleted;
    }


    public ResponseEntity<Settlement> updateSettlement(Settlement settlement) {
        if(settlement == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try
        {
            Settlement existingSettlement = getSettlementById(settlement.getSettlementId());
            if (existingSettlement == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            balanceService.revertPreviousBalanceForSettlement(existingSettlement);
            //Update new Settlement
            Settlement updatedSettlement = saveSettlement(settlement);

            return new ResponseEntity<>(updatedSettlement, HttpStatus.OK);
        } catch (Exception ex) {
            //Need to throw Exception
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    public Settlement getSettlementDetailsByID(Long settlementId) {
        Settlement settlement = null;
        try
        {
            Optional<Settlement> optional = settlementRepository.findById(settlementId);
            if(optional.isPresent())
            {
                settlement = optional.get();
            }
        }
        catch(Exception ex)
        {
            //Need to throw Exception
        }
        return settlement;
    }
}
