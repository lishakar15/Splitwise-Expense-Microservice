package com.splitwise.microservices.expense_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseParticipantVO {

    private Long userId;
    private String userName;
    private Double shareAmount;
    private boolean isPaidUser;
}
