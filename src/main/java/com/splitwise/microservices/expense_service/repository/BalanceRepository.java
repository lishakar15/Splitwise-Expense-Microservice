package com.splitwise.microservices.expense_service.repository;

import com.splitwise.microservices.expense_service.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface BalanceRepository extends JpaRepository<Balance,Long> {

    @Query(value = "select b from Balance b where b.userId =:userId and b.owesTo =:owesTo and b.groupId " +
            "=:groupId")
    public Balance getPastBalanceOfUser(@Param("userId") Long userId,
                                         @Param("owesTo")Long owesToUserId, @Param("groupId") Long groupId);

    public void deleteByBalanceId(Long balanceId);

}
