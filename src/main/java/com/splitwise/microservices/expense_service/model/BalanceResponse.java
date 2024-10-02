package com.splitwise.microservices.expense_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BalanceResponse {
    private Long groupId;
    private String groupName;
    private Long userId;
    private String userName;
    private Long owesTo;
    private Boolean isOwed;
    private String owesToUserName;
    private Double balanceAmount;
}
