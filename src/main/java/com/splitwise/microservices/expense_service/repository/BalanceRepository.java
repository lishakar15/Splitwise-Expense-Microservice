package com.splitwise.microservices.expense_service.repository;

import com.splitwise.microservices.expense_service.entity.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface BalanceRepository extends JpaRepository<Balance,Long> {

    @Query(value = "select b from Balance b where b.userId =:userId and b.owesTo =:owesTo and b.groupId " +
            "=:groupId")
    public Balance getPastBalanceOfUser(@Param("userId") Long userId,
                                         @Param("owesTo")Long owesToUserId, @Param("groupId") Long groupId);

    @Transactional
    @Modifying
    @Query(value = "delete from Balance b where b.balanceId =:balanceId")
    public void deleteByBalanceId(@Param("balanceId") Long balanceId);
    @Query(value = "select b from Balance b where b.groupId =:groupId")
    List<Balance> getBalancesByGroupId(@Param("groupId") Long groupId);
    @Query(value = "select b from Balance b where b.userId =:userId or b.owesTo =:userId")
    List<Balance> getUserAllBalances(@Param("userId") Long userId);
    @Query(value = "select b from Balance b where b.groupId =:groupId and (b.userId =:userId or b.owesTo =:userId) ")
    List<Balance> getUserBalancesByGroupId( @Param("groupId") Long groupId, @Param("userId") Long userId);
    @Query(value = "select sum(b.balanceAmount) from Balance b where b.userId=:userId")
    Double getPendingBalancesByUserId(@Param("userId") Long userId);
}
