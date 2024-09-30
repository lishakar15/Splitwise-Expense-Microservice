package com.splitwise.microservices.expense_service.mapper;

import com.splitwise.microservices.expense_service.entity.Comments;
import com.splitwise.microservices.expense_service.entity.Settlement;
import com.splitwise.microservices.expense_service.model.SettlementRequest;
import com.splitwise.microservices.expense_service.model.SettlementResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class SettlementMapper {

    public Settlement getSettlementFromSettlementReq(SettlementRequest settlementRequest)
    {
        Settlement settlement = null;
        if(settlementRequest == null)
        {
            //Todo Throw exception
            return null;
        }
        else
        {
            settlement = Settlement.builder().groupId(settlementRequest.getGroupId())
                    .paidTo(settlementRequest.getPaidTo())
                    .paidBy(settlementRequest.getPaidBy())
                    .amountPaid(settlementRequest.getAmountPaid())
                    .build();
        }
        return settlement;
    }

    public List<Comments> getCommentListFromSettlementReq(SettlementRequest settlementRequest)
    {
        List<Comments> commentsList = null;
        if(settlementRequest != null && !settlementRequest.getCommentsList().isEmpty())
        {
            commentsList = new ArrayList<>(settlementRequest.getCommentsList());
        }
        return commentsList;
    }
    public List<SettlementResponse> createSettlementResponse(List<Settlement> settlements, Map<Long,String> userNameMap,Map<Long,String> groupNameMap){
        List<SettlementResponse> settlementResponseList = new ArrayList<>();
        if(settlements != null && userNameMap != null)
        {
            for(Settlement settlement : settlements){
                SettlementResponse settlementResponse = SettlementResponse.builder()
                        .settlementId(settlement.getSettlementId())
                        .groupId(settlement.getGroupId())
                        .groupName(groupNameMap.get(settlement.getGroupId()))
                        .paidBy(settlement.getPaidBy())
                        .paidByUserName(userNameMap.get(settlement.getPaidBy()))
                        .paidTo(settlement.getPaidTo())
                        .paidToUserName(userNameMap.get(settlement.getPaidTo()))
                        .amountPaid(settlement.getAmountPaid())
                        .createdBy(settlement.getCreatedBy())
                        .modifiedBy(settlement.getModifiedBy())
                        .paymentMethod(settlement.getPaymentMethod())
                        .settlementDate(settlement.getSettlementDate())
                        .lastUpdateDate(settlement.getLastUpdateDate())
                        .build();
                settlementResponseList.add(settlementResponse);
            }
        }
        return settlementResponseList;
    }
}
