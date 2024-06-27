package com.splitwise.microservices.expense_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "transaction_details")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {

    @Id
    private Long transactionId;

    @Column(name = "user_comments")
    private String userComments;
    @Column(name = "transaction_date")
    private Date transactionDate;
    @Column(name ="payment_type")
    private String paymentType;
    @Column(name = "payment_gateway_id")
    private String paymentGatewayId;
}
