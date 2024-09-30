package com.splitwise.microservices.expense_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettlementResponse {
    private Long settlementId;
    private Long groupId;
    private String groupName;
    private Long paidBy;
    private String paidByUserName;
    private Long paidTo;
    private String paidToUserName;
    private Long createdBy;
    private Long modifiedBy;
    private Double amountPaid;
    private String paymentMethod;
    private Date settlementDate;
    private Date lastUpdateDate;
}
