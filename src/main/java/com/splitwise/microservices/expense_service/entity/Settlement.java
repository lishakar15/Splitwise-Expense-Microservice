package com.splitwise.microservices.expense_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name="settlements")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Settlement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementId;
    private Long groupId;
    @Column(name = "paid_by")
    private Long paidBy;
    @Column(name = "paid_to")
    private Long paidTo;
    @Column(name = "added_by")
    private Long addedBy;
    @Column(name = "amount_paid")
    private Double amountPaid;
}
