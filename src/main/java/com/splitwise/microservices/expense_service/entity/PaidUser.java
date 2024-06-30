package com.splitwise.microservices.expense_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "paid_user_details")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaidUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long expenseId;
    private Long userId;
    private Double paidAmount;
}
