package com.splitwise.microservices.expense_service.repository;

import com.splitwise.microservices.expense_service.entity.ExpenseParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseParticipantRepository extends JpaRepository<ExpenseParticipant,Long> {

}
