package com.splitwise.microservices.expense_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name ="comments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Comments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name ="comment_id")
    private Long commentId;
    @Column(name ="settlement_id")
    private Long settlementId;
    @Column(name ="expense_id")
    private Long expenseId;
    @Column(name ="commented_by")
    private Long commentedBy;
    @Column(name ="comment")
    private String comment;
}
