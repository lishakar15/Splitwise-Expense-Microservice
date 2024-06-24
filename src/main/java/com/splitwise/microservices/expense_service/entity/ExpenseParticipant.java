package com.splitwise.microservices.expense_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name ="expense_participants")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exp_part_id")
    private Long id;
    @Column(name ="expense_id")
    private Long expenseId;
    @Column(name ="participant_id")
    private Long participantId;
    @Column(name = "settle_amount")
    private Double settlementAmount;
    @Column(name = "is_payer")
    private boolean isPayer;
}
