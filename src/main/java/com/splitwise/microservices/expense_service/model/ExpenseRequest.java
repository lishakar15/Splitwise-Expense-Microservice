package com.splitwise.microservices.expense_service.model;

import com.splitwise.microservices.expense_service.entity.PaidUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseRequest {
    private Long groupId;
    private Long expenseId;
    private List<PaidUser> paidUsers;
    private Double totalAmount;
    private String expenseDescription;
    private Date spentOnDate;
    private Date createDate;
    private Date lastUpdateDate;
    private String category;
    private List<ParticipantShare> participantShareList;
    private String splitType;
    private Long createdBy;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExpenseRequest that = (ExpenseRequest) o;
        return Objects.equals(groupId, that.groupId) && Objects.equals(expenseId, that.expenseId) && Objects.equals(paidUsers, that.paidUsers) && Objects.equals(totalAmount, that.totalAmount) && Objects.equals(expenseDescription, that.expenseDescription) && Objects.equals(spentOnDate, that.spentOnDate) && Objects.equals(createDate, that.createDate) && Objects.equals(lastUpdateDate, that.lastUpdateDate) && Objects.equals(category, that.category) && Objects.equals(participantShareList, that.participantShareList) && Objects.equals(splitType, that.splitType) && Objects.equals(createdBy, that.createdBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, expenseId, paidUsers, totalAmount, expenseDescription, spentOnDate, createDate, lastUpdateDate, category, participantShareList, splitType, createdBy);
    }
}
