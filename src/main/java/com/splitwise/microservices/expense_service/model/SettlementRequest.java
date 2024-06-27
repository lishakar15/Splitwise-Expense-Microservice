package com.splitwise.microservices.expense_service.model;

import com.splitwise.microservices.expense_service.entity.Comments;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SettlementRequest {
    private Long groupId;
    private Long paidBy;
    private Long paidTo;
    private Double amountPaid;
    private List<Comments> commentsList;
}
