package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.clients.UserClient;
import com.splitwise.microservices.expense_service.constants.StringConstants;
import com.splitwise.microservices.expense_service.entity.Comments;
import com.splitwise.microservices.expense_service.enums.ActivityType;
import com.splitwise.microservices.expense_service.external.ActivityRequest;
import com.splitwise.microservices.expense_service.kafka.KafkaProducer;
import com.splitwise.microservices.expense_service.repository.CommentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentService {
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    UserClient userClient;
    @Autowired
    KafkaProducer kafkaProducer;
    @Autowired
    ExpenseService expenseService;
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentService.class);

    public Comments saveComment(Comments comment) {
        Comments savedComment = commentRepository.save(comment);
        //Record post comment activity
        createCommentActivity(ActivityType.COMMENT_ADDED,savedComment);
        return savedComment;
    }

    public void deleteCommentById(Long commentId, Long loggedInUser) {
        Comments comment = getCommentById(commentId);
        commentRepository.deleteById(commentId);
        if(comment != null)
        {
            comment.setDeletedBy(loggedInUser);
            createCommentActivity(ActivityType.COMMENT_DELETED,comment);
        }
    }

    public Comments getCommentById(Long commentId)
    {
        Optional<Comments> optional = commentRepository.findByCommentId(commentId);
        return optional.isPresent() ? optional.get() : null;
    }

    public List<Comments> getCommentsByExpenseId(Long expenseId) {

        return commentRepository.findByExpenseId(expenseId);
    }

    public List<Comments> getCommentsBySettlementId(Long settlementId) {
        return commentRepository.findBySettlementId(settlementId);
    }

    public void createCommentActivity(ActivityType activityType, Comments comment)
    {
        ActivityRequest activityRequest = ActivityRequest.builder()
                .groupId(comment.getGroupId())
                .expenseId(comment.getExpenseId())
                .settlementId(comment.getSettlementId())
                .activityType(activityType)
                .createDate(comment.getCreateDate())
                .build();
        StringBuilder sb = new StringBuilder();
        if(ActivityType.COMMENT_ADDED.equals(activityType))
        {
            sb.append(StringConstants.USER_ID_PREFIX);
            sb.append(comment.getCommentedBy());
            sb.append(StringConstants.USER_ID_SUFFIX);
            sb.append(StringConstants.COMMENT_CREATED);
        }
        else if(ActivityType.COMMENT_DELETED.equals(activityType))
        {
            sb.append(StringConstants.USER_ID_PREFIX);
            sb.append(comment.getDeletedBy());
            sb.append(StringConstants.USER_ID_SUFFIX);
            sb.append(StringConstants.COMMENT_DELETED);
        }
        if(comment.getExpenseId() != null)
        {
            sb.append(StringConstants.EXPENSE);
            String expenseDesc = expenseService.getExpenseDescById(comment.getExpenseId());
            sb.append(expenseDesc);
        }
        else if(comment.getSettlementId() != null)
        {
            sb.append(StringConstants.PAYMENT);
        }
        String groupName = userClient.getGroupName(comment.getGroupId());
        sb.append(" in ");
        sb.append(groupName);
        activityRequest.setMessage(sb.toString());
        try
        {
            kafkaProducer.sendActivityMessage(activityRequest);
        }
        catch(Exception ex)
        {
            LOGGER.error("Error occurred while sending message to Kafka Topic "+ex);
        }
    }
}