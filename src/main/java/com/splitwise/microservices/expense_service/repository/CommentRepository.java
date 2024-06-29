package com.splitwise.microservices.expense_service.repository;

import com.splitwise.microservices.expense_service.entity.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comments,Long> {
}
