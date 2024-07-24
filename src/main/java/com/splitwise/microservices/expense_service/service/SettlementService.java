package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.clients.UserClient;
import com.splitwise.microservices.expense_service.constants.StringConstants;
import com.splitwise.microservices.expense_service.entity.Settlement;
import com.splitwise.microservices.expense_service.enums.ActivityType;
import com.splitwise.microservices.expense_service.external.ActivityRequest;
import com.splitwise.microservices.expense_service.external.ChangeLog;
import com.splitwise.microservices.expense_service.kafka.KafkaProducer;
import com.splitwise.microservices.expense_service.repository.SettlementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    @Autowired
    KafkaProducer kafkaProducer;

    private static final Logger LOGGER = LoggerFactory.getLogger(SettlementService.class);

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
        Long payerId = newSettlement.getPaidBy();
        Long receiverId = newSettlement.getPaidTo();;
        if(ActivityType.PAYMENT_CREATED.equals(activityType))
        {
            //Payment Create Activity
            sb.append(StringConstants.USER_ID_PREFIX);
            sb.append(newSettlement.getCreatedBy());
            sb.append(StringConstants.USER_ID_SUFFIX);
            sb.append(StringConstants.PAYMENT_CREATED);
            sb.append(" from ");
            sb.append(StringConstants.USER_ID_PREFIX);
            sb.append(payerId);
            sb.append(StringConstants.USER_ID_SUFFIX);
            sb.append(" to ");
            sb.append(StringConstants.USER_ID_PREFIX);
            sb.append(receiverId);
            sb.append(StringConstants.USER_ID_SUFFIX);
            activityRequest.setMessage(sb.toString());
        }
        if(ActivityType.PAYMENT_UPDATED.equals(activityType))
        {
            //Payment Update Activity
            sb.append(StringConstants.USER_ID_PREFIX);
            sb.append(newSettlement.getModifiedBy());
            sb.append(StringConstants.USER_ID_SUFFIX);
            sb.append(StringConstants.PAYMENT_UPDATED);
            sb.append(" from ");
            sb.append(StringConstants.USER_ID_PREFIX);
            sb.append(payerId);
            sb.append(StringConstants.USER_ID_SUFFIX);
            sb.append(" to ");
            sb.append(StringConstants.USER_ID_PREFIX);
            sb.append(receiverId);
            sb.append(StringConstants.USER_ID_SUFFIX);
            activityRequest.setMessage(sb.toString());
            if(oldSettlement != null)
            {
                List<ChangeLog> changeLogs = createChangeLogForSettlementModify(newSettlement,oldSettlement);
                if(changeLogs != null && !changeLogs.isEmpty())
                {
                    activityRequest.setChangeLogs(changeLogs);
                }
            }
        }
        if(ActivityType.PAYMENT_DELETED.equals(activityType))
        {
            //Payment Delete Activity
            sb.append(StringConstants.USER_ID_PREFIX);
            sb.append(newSettlement.getModifiedBy());
            sb.append(StringConstants.USER_ID_SUFFIX);
            sb.append(StringConstants.PAYMENT_DELETED);
            sb.append(" from ");
            sb.append(StringConstants.USER_ID_PREFIX);
            sb.append(payerId);
            sb.append(StringConstants.USER_ID_SUFFIX);
            sb.append(" to ");
            sb.append(StringConstants.USER_ID_PREFIX);
            sb.append(receiverId);
            sb.append(StringConstants.USER_ID_SUFFIX);
            activityRequest.setMessage(sb.toString());
        }
        try
        {
            kafkaProducer.sendActivityMessage(activityRequest);
        }
        catch(Exception ex)
        {
            LOGGER.error("Error occurred while sending message to the topic "+ex);
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
                    sb.append((oldSettlement.getPaymentMethod()));
                    sb.append(" to ");
                    sb.append(newSettlement.getPaymentMethod());
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



    public boolean deleteSettlementById(Long settlementId,Long loggedInUserId)
    {
        boolean isDeleted = false;
        try
        {
            Settlement existingSettlement = getSettlementById(settlementId);
            existingSettlement.setModifiedBy(loggedInUserId);
            balanceService.revertPreviousBalanceForSettlement(existingSettlement);
            settlementRepository.deleteSettlementById(settlementId);
            isDeleted = true;
            createSettlementActivity(ActivityType.PAYMENT_DELETED,existingSettlement,null);
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
            Settlement oldSettlement = new Settlement(existingSettlement);
            balanceService.revertPreviousBalanceForSettlement(existingSettlement);
            //Update new Settlement
            balanceService.calculateBalanceForSettlement(settlement);
            Settlement updatedSettlement =  settlementRepository.save(settlement);
            //Record Update Activity
            createSettlementActivity(ActivityType.PAYMENT_UPDATED,settlement,oldSettlement);

            return new ResponseEntity<>(updatedSettlement, HttpStatus.OK);
        } catch (Exception ex) {
            LOGGER.error("Error occurred while updating expense "+ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
