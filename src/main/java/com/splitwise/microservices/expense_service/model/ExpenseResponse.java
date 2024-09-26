package com.splitwise.microservices.expense_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseResponse {

    private Long expenseId;
    private Long groupId;
    private List<PaidUsersVO> paidUsers;
    private Double totalAmount;
    private String expenseDescription;
    private Date spentOnDate;
    private Date createDate;
    private Date lastUpdateDate;
    private String category;
    private String splitType;
    private Long createdBy;
    private List<ExpenseParticipantVO> participantShareList;

}
