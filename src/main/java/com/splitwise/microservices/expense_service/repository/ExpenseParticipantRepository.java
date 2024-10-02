package com.splitwise.microservices.expense_service.repository;

import com.splitwise.microservices.expense_service.entity.ExpenseParticipant;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Repository
public interface ExpenseParticipantRepository extends JpaRepository<ExpenseParticipant,Long> {

    public List<ExpenseParticipant> findByExpenseId(Long expenseId);

    @Modifying
    @Transactional
    public int deleteByExpenseId(Long expenseId);
    @Query("select ep.expenseId from ExpenseParticipant ep where ep.participantId =:userId")
    public List<Long> getExpenseIdByParticipantId(@PathVariable("userId") Long userId);
}
