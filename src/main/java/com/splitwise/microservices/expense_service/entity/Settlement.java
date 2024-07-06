package com.splitwise.microservices.expense_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


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
    @Column(name = "group_id")
    private Long groupId;
    @Column(name = "paid_by")
    private Long paidBy;
    @Column(name = "paid_to")
    private Long paidTo;
    @Column(name = "added_by")
    private Long addedBy;
    @Column(name = "amount_paid")
    private Double amountPaid;
    @Column(name = "date_of_settlement")
    private Date settlementDate;
    @Column(name = "last_update_date")
    private Date lastUpdateDate;
}
