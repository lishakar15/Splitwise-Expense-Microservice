package com.splitwise.microservices.expense_service.repository;

import com.splitwise.microservices.expense_service.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BalanceRepository extends JpaRepository<Balance,Long> {

    @Query(value = "select b.balance_amount from Balance b where user_id =:paidUserId and owes_to_user =:participantId")
    public Optional<Double> getPastBalanceOfParticipant(@Param("paidUserId") Long paidUserId, @Param("participantId")Long participantId);

}
