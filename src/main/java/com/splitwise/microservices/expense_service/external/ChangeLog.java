package com.splitwise.microservices.expense_service.external;

import jakarta.persistence.*;

import java.util.Date;

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
}
