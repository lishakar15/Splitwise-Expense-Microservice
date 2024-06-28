package com.splitwise.microservices.expense_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "balance_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Balance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_id")
    private Long balanceId;
    @Column(name = "group_id")
    private Long groupId;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "owes_to_user")
    private Long owesTo;
    @Column(name = "balance_amount")
    private Double balanceAmount;

}
