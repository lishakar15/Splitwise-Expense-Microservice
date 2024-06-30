package com.splitwise.microservices.expense_service.repository;

import com.splitwise.microservices.expense_service.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BalanceRepository extends JpaRepository<Balance,Long> {

}
