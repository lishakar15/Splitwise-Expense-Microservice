package com.splitwise.microservices.expense_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

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
    @Column(name ="group_id")
    private Long groupId;
    @Column(name ="commented_by")
    private Long commentedBy;
    @Transient
    private Long deletedBy;
    @Column(name ="comment")
    private String comment;
    @Column(name ="create_date")
    private Date createDate;
}
