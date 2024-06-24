package com.splitwise.microservices.expense_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name="expense_details")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Expense {

    @Id
    @Column(name="expense_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long expenseId;
    @Column(name="group_id")
    private Long groupId;
    @Column(name ="paid_by_user")
    private Long paidBy;
    @Column(name = "total_amount")
    private Double totalAmount;
    @Column(name = "expense_desc")
    private String expenseDescription;
    @Column(name = "spend_date")
    private Date spentOnDate;
    @Column(name = "create_date")
    private Date createDate;
    @Column(name = "last_updated")
    private Date lastUpdateDate;

    private String category;

    private Enum splitType;

}
