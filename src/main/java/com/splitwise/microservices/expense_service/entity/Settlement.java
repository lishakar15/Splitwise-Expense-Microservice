package com.splitwise.microservices.expense_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Objects;


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
    @Column(name = "created_by")
    private Long createdBy;
    @Column(name = "amount_paid")
    private Double amountPaid;
    @Column(name = "payment_method")
    private String paymentMethod;
    @Column(name = "date_of_settlement")
    private Date settlementDate;
    @Column(name = "last_update_date")
    private Date lastUpdateDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Settlement that = (Settlement) o;
        return Objects.equals(settlementId, that.settlementId) && Objects.equals(groupId, that.groupId) && Objects.equals(paidBy, that.paidBy) && Objects.equals(paidTo, that.paidTo) && Objects.equals(createdBy, that.createdBy) && Objects.equals(amountPaid, that.amountPaid) && Objects.equals(paymentMethod, that.paymentMethod) && Objects.equals(settlementDate, that.settlementDate) && Objects.equals(lastUpdateDate, that.lastUpdateDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(settlementId, groupId, paidBy, paidTo, createdBy, amountPaid, paymentMethod, settlementDate, lastUpdateDate);
    }
}
