package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.clients.ActivityClient;
import com.splitwise.microservices.expense_service.clients.UserClient;
import com.splitwise.microservices.expense_service.constants.StringConstants;
import com.splitwise.microservices.expense_service.entity.Settlement;
import com.splitwise.microservices.expense_service.enums.ActivityType;
import com.splitwise.microservices.expense_service.external.Activity;
import com.splitwise.microservices.expense_service.external.ActivityRequest;
import com.splitwise.microservices.expense_service.external.ChangeLog;
import com.splitwise.microservices.expense_service.mapper.SettlementMapper;
import com.splitwise.microservices.expense_service.model.SettlementInsight;
import com.splitwise.microservices.expense_service.model.SettlementResponse;
import com.splitwise.microservices.expense_service.repository.SettlementRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class SettlementService {
    @Autowired
    SettlementRepository settlementRepository;
    @Autowired
    BalanceService balanceService;
    @Autowired
    UserClient userClient;
    @Autowired
    ActivityClient activityClient;
    @Autowired
    SettlementMapper settlementMapper;

    private static final Logger LOGGER = LoggerFactory.getLogger(SettlementService.class);

    public Settlement saveSettlement(Settlement settlement) {
        try {
            //Settle Balance Amount
            balanceService.calculateBalanceForSettlement(settlement);

            Settlement savedSettlement = settlementRepository.save(settlement);
            createSettlementActivity(ActivityType.PAYMENT_CREATED,settlement,null);
            return savedSettlement;
        } catch (Exception ex) {
            //Need to throw exception
        }
        return settlement;
    }

    private void createSettlementActivity(ActivityType activityType, Settlement newSettlement, Settlement oldSettlement) {
        try {
            Activity activity = Activity.builder()
                    .activityType(activityType)
                    .createDate(newSettlement.getLastUpdateDate())
                    .groupId(newSettlement.getGroupId())
                    .settlementId(newSettlement.getSettlementId())
                    .build();
            StringBuilder sb = new StringBuilder();
            Long payerId = newSettlement.getPaidBy();
            Long receiverId = newSettlement.getPaidTo();
            Map<Long, String> userNameMap;
            if (ActivityType.PAYMENT_CREATED.equals(activityType)) {
                //Payment Create Activity
                userNameMap = userClient.getUserNameMapByUserIds(Arrays.asList(payerId, receiverId, newSettlement.getCreatedBy()));
                sb.append(userNameMap.get(newSettlement.getCreatedBy()));
                sb.append(StringConstants.PAYMENT_CREATED);
                sb.append(" from ");
                sb.append(userNameMap.get(payerId));
                sb.append(" to ");
                sb.append(userNameMap.get(receiverId));
            }
            if (ActivityType.PAYMENT_UPDATED.equals(activityType)) {
                //Payment Update Activity
                userNameMap = userClient.getUserNameMapByUserIds(Arrays.asList(payerId, receiverId, newSettlement.getModifiedBy()));
                sb.append(userNameMap.get(newSettlement.getModifiedBy()));
                sb.append(StringConstants.PAYMENT_UPDATED);
                sb.append(" from ");
                sb.append(userNameMap.get(payerId));
                sb.append(" to ");
                sb.append(userNameMap.get(receiverId));
                if (oldSettlement != null) {
                    List<ChangeLog> changeLogs = createChangeLogForSettlementModify(newSettlement, oldSettlement);
                    if (changeLogs != null && !changeLogs.isEmpty()) {
                        activity.setChangeLogs(changeLogs);
                    }
                }
            }
            if (ActivityType.PAYMENT_DELETED.equals(activityType)) {
                //Payment Delete Activity
                userNameMap = userClient.getUserNameMapByUserIds(Arrays.asList(payerId, receiverId, newSettlement.getModifiedBy()));
                sb.append(userNameMap.get(newSettlement.getModifiedBy()));
                sb.append(StringConstants.PAYMENT_DELETED);
                sb.append(" from ");
                sb.append(userNameMap.get(payerId));
                sb.append(" to ");
                sb.append(userNameMap.get(receiverId));
            }
            String groupName = userClient.getGroupName(newSettlement.getGroupId());
            sb.append(" in "+groupName);
            activity.setMessage(sb.toString());
            List<Long> relatedUserIds = Arrays.asList(payerId,receiverId);
            ActivityRequest activityRequest = ActivityRequest.builder()
                    .activity(activity)
                    .userIdList(relatedUserIds)
                    .build();
            //Sent Activity request to Orchestrate
            activityClient.sendActivityRequest(activityRequest);

        } catch (Exception ex) {
            LOGGER.error("Error occurred while creating Settlement Activity " + ex);
        }

    }

    private List<ChangeLog> createChangeLogForSettlementModify(Settlement newSettlement, Settlement oldSettlement) {
        List<ChangeLog> changeLogs = new ArrayList<>();
        if (newSettlement != null && oldSettlement != null) {
            if (!oldSettlement.equals(newSettlement)) {
                if (!oldSettlement.getAmountPaid().equals(newSettlement.getAmountPaid())) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(StringConstants.AMOUNT);
                    sb.append(" from ");
                    sb.append((oldSettlement.getAmountPaid()));
                    sb.append(" to ");
                    sb.append(newSettlement.getAmountPaid());
                    changeLogs.add(new ChangeLog(sb.toString()));
                }
                if (!oldSettlement.getPaymentMethod().equals(newSettlement.getPaymentMethod())) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(StringConstants.PAYMENT_METHOD);
                    sb.append(" from ");
                    sb.append((oldSettlement.getPaymentMethod()));
                    sb.append(" to ");
                    sb.append(newSettlement.getPaymentMethod());
                    changeLogs.add(new ChangeLog(sb.toString()));
                }
                LocalDate oldSettlementDate = oldSettlement.getSettlementDate()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                LocalDate newSettlementDate = newSettlement.getSettlementDate()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                if(!oldSettlementDate.equals(newSettlementDate))
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append(StringConstants.PAYMENT_DATE);
                    sb.append(" from ");
                    sb.append((oldSettlementDate));
                    sb.append(" to ");
                    sb.append(newSettlementDate);
                    changeLogs.add(new ChangeLog(sb.toString()));
                }
            }

        }
        return changeLogs;
    }

    public Settlement getSettlementById(Long settlementId) {
        Optional<Settlement> optional = settlementRepository.findById(settlementId);
        return optional.isPresent() ? optional.get() : null;
    }

    public List<SettlementResponse> getAllSettlementByGroupId(Long groupId) {
        List<Settlement> settlements = new ArrayList<>();
        try {
            settlements = settlementRepository.getAllSettlementByGroupId(groupId);
            //Get the userId and userName Map
            Map<Long, String> userNameMap = userClient.getUserNameMapByGroupId(groupId);
            //Get Group Name from user Microservice
            String groupName = userClient.getGroupNameByGroupId(groupId);
            Map<Long, String> groupNameMap = new HashMap<>();
            groupNameMap.put(groupId, groupName);
            //Create Settlement Response
            return settlementMapper.createSettlementResponseList(settlements, userNameMap, groupNameMap);
        } catch (Exception ex) {
            LOGGER.error("Error occurred while fetching data in getAllSettlementByGroupId() {}", ex);
        }
        return List.of();
    }

    public List<SettlementResponse> getAllSettlementsByUserId(Long userId) {
        List<SettlementResponse> settlementResponseList = new ArrayList<>();
        Set<Long> uniqueUserIds = new HashSet<>();
        try {
            List<Settlement> settlements = settlementRepository.getAllSettlementsByUserId(userId);
            if (settlements != null) {
                for (Settlement settlement : settlements) {
                    uniqueUserIds.add(settlement.getPaidBy());
                    uniqueUserIds.add(settlement.getPaidTo());
                }
                List<Long> allUserIds = new ArrayList<>(uniqueUserIds);
                //Get userId and userName map
                Map<Long, String> userNameMap = userClient.getUserNameMapByUserIds(allUserIds);
                //Get the groupId and groupName Map using user id
                Map<Long, String> groupNameMap = userClient.getGroupNameMap(userId);
                settlementResponseList = settlementMapper.createSettlementResponseList(settlements, userNameMap, groupNameMap);
            }
        } catch (Exception ex) {
            LOGGER.error("Error occurred while fetching data in getAllSettlementsByUserId() {}", ex);
        }
        return settlementResponseList;
    }

    public boolean deleteSettlementById(Long settlementId, Long loggedInUserId) {
        boolean isDeleted = false;
        try {
            Settlement existingSettlement = getSettlementById(settlementId);
            Settlement oldSettlement = new Settlement(existingSettlement);
            oldSettlement.setModifiedBy(loggedInUserId);
            balanceService.revertPreviousBalanceForSettlement(existingSettlement);
            settlementRepository.deleteSettlementById(settlementId);
            isDeleted = true;
            createSettlementActivity(ActivityType.PAYMENT_DELETED,oldSettlement,null);
        } catch (Exception ex) {
            //Need to throw exception
        }
        return isDeleted;
    }


    public ResponseEntity<Settlement> updateSettlement(Settlement settlement) {
        if (settlement == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            Settlement existingSettlement = getSettlementById(settlement.getSettlementId());
            if (existingSettlement == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            Settlement oldSettlement = new Settlement(existingSettlement);
            balanceService.revertPreviousBalanceForSettlement(existingSettlement);
            //Update new Settlement
            balanceService.calculateBalanceForSettlement(settlement);
            Settlement updatedSettlement = settlementRepository.save(settlement);
            //Record Update Activity
            createSettlementActivity(ActivityType.PAYMENT_UPDATED,settlement,oldSettlement);

            return new ResponseEntity<>(updatedSettlement, HttpStatus.OK);
        } catch (Exception ex) {
            LOGGER.error("Error occurred while updating expense " + ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public SettlementResponse getSettlementDetailsByID(Long settlementId, Long userId) {
        SettlementResponse settlementResponse = null;
        try {
            Optional<Settlement> optional = settlementRepository.findById(settlementId);
            if (optional.isPresent()) {
                Settlement settlement = optional.get();
                List<Long> allUserIds = Arrays.asList(settlement.getPaidBy(),settlement.getPaidTo());
                //Get userId and userName map
                Map<Long, String> userNameMap = userClient.getUserNameMapByUserIds(allUserIds);
                //Get the groupId and groupName Map using user id
                Map<Long, String> groupNameMap = userClient.getGroupNameMap(userId);
                settlementResponse = settlementMapper.createSettlementResponse(settlement, userNameMap, groupNameMap);
            }
        } catch (Exception ex) {
            LOGGER.error("Error occurred while getting settlement getSettlementDetailsByID " + ex);
        }
        return settlementResponse;
    }

    public SettlementInsight getSettlementInsightsDataByUserId(Long userId) {
        //Get the total Settlements made by the user
        Double totalSettlements = settlementRepository.getTotalSettlementsByUserId(userId);
        Double pendingSettlements = balanceService.getUserPendingBalance(userId);
        //Get the pending total Balances
        SettlementInsight settlementInsight = SettlementInsight.builder()
                .totalSettlements(totalSettlements)
                .pendingSettlements(pendingSettlements)
                .build();
        return settlementInsight;
    }
}
