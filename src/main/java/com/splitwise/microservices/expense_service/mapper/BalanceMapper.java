package com.splitwise.microservices.expense_service.mapper;

import com.splitwise.microservices.expense_service.entity.Balance;
import com.splitwise.microservices.expense_service.model.BalanceResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class BalanceMapper {

    public List<BalanceResponse> createBalanceResponse(List<Balance> balanceList, Map<Long,String> userNameMap, Map<Long,String> groupNameMap, Long loggedInUserId){
        List<BalanceResponse> balanceResponseList = new ArrayList<>();

        if(userNameMap != null && groupNameMap != null){
            for(Balance balance : balanceList){
                BalanceResponse balanceResponse = BalanceResponse.builder()
                        .groupId(balance.getGroupId())
                        .groupName(groupNameMap.get(balance.getGroupId()))
                        .userId(balance.getUserId())
                        .userName(userNameMap.get(balance.getUserId()))
                        .owesTo(balance.getOwesTo())
                        .owesToUserName(userNameMap.get(balance.getOwesTo()))
                        .isOwed(balance.getOwesTo() == loggedInUserId ? true : false)
                        .balanceAmount(balance.getBalanceAmount())
                        .build();
                balanceResponseList.add(balanceResponse);
            }
        }
        return balanceResponseList;

    }
}
