package com.splitwise.microservices.expense_service.mapper;

import com.splitwise.microservices.expense_service.entity.Comments;
import com.splitwise.microservices.expense_service.entity.Settlement;
import com.splitwise.microservices.expense_service.model.SettlementRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
}
