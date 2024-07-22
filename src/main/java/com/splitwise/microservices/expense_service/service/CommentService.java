package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.clients.UserClient;
import com.splitwise.microservices.expense_service.constants.StringConstants;
import com.splitwise.microservices.expense_service.entity.Comments;
import com.splitwise.microservices.expense_service.enums.ActivityType;
import com.splitwise.microservices.expense_service.external.ActivityRequest;
import com.splitwise.microservices.expense_service.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    UserClient userClient;

    public Comments saveComment(Comments comment) {
        Comments savedComment = commentRepository.save(comment);
        return savedComment;
    }

    public void deleteCommentById(Long commentId) {
        commentRepository.deleteById(commentId);
    }

    public List<Comments> getCommentsByExpenseId(Long expenseId) {

        return commentRepository.findByExpenseId(expenseId);
    }

    public List<Comments> getCommentsBySettlementId(Long settlementId) {
        return commentRepository.findBySettlementId(settlementId);
    }
}
