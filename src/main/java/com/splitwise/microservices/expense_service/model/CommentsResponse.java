package com.splitwise.microservices.expense_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentsResponse {

    private Long commentId;
    private Long settlementId;
    private Long expenseId;
    private Long groupId;
    private Long commentedBy;
    private String userName;
    private String comment;
    private Date createDate;
}
