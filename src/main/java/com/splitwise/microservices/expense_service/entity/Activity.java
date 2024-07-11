package com.splitwise.microservices.expense_service.entity;

import com.splitwise.microservices.expense_service.enums.ActivityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "activity_id")
    private Long activityId;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "group_id")
    private Long groupId;
    @Column(name = "settlement_id")
    private Long settlementId;
    @Column(name = "activity_type")
    @Enumerated(EnumType.STRING)
    private ActivityType activityType;
    @Column(name = "activity_message")
    private String message;
    @Column(name = "create_date")
    private Date createDate;
}
