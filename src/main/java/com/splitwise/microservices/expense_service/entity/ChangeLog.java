package com.splitwise.microservices.expense_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name ="change_log")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "change_log_id")
    private Long changeLogId;
    @Column(name = "change_type")
    private String changeType;
    @Column(name = "change_from")
    private String changeFrom;
    @Column(name = "change_to")
    private String changeTo;
    @Column(name = "log_date")
    private Date logDate;
    @ManyToOne()
    @JoinColumn(name = "activity_id")
    private Activity activity;

}
