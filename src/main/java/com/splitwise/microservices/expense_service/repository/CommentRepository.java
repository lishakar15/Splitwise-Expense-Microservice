package com.splitwise.microservices.expense_service.repository;

import com.splitwise.microservices.expense_service.entity.Comments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comments,Long> {

    List<Comments> findByExpenseId(Long expenseId);

    List<Comments> findBySettlementId(Long settlementId);

    Optional<Comments> findByCommentId(Long commentId);
}
