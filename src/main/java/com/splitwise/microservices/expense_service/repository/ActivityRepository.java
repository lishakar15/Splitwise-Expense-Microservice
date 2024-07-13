package com.splitwise.microservices.expense_service.repository;

import com.splitwise.microservices.expense_service.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity,Long> {
    @Query(value = "select a from Activity a where a.groupId =:groupId")
    List<Activity> getActivityByGroupId(Long groupId);

    List<Activity> findByGroupIdIn(List<Long> groupIds);
}
