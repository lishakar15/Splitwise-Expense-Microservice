package com.splitwise.microservices.expense_service.service;

import com.google.gson.Gson;
import com.splitwise.microservices.expense_service.clients.UserClient;
import com.splitwise.microservices.expense_service.constants.StringConstants;
import com.splitwise.microservices.expense_service.entity.Comments;
import com.splitwise.microservices.expense_service.enums.ActivityType;
import com.splitwise.microservices.expense_service.external.Activity;
import com.splitwise.microservices.expense_service.mapper.CommentsMapper;
import com.splitwise.microservices.expense_service.model.CommentsResponse;
import com.splitwise.microservices.expense_service.repository.CommentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {
    @Autowired
    CommentRepository commentRepository;
    @Autowired
    UserClient userClient;
    @Autowired
    ExpenseService expenseService;
    @Autowired
    CommentsMapper commentsMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentService.class);

    public Comments saveComment(Comments comment) {
        Comments savedComment = commentRepository.save(comment);
        //Record post comment activity
        //createCommentActivity(ActivityType.COMMENT_ADDED,savedComment);
        return savedComment;
    }

    public void deleteCommentById(Long commentId, Long loggedInUser) {
        Comments comment = getCommentById(commentId);
        commentRepository.deleteById(commentId);
        if(comment != null)
        {
            comment.setDeletedBy(loggedInUser);
            //createCommentActivity(ActivityType.COMMENT_DELETED,comment);
        }
    }

    public Comments getCommentById(Long commentId)
    {
        Optional<Comments> optional = commentRepository.findByCommentId(commentId);
        return optional.isPresent() ? optional.get() : null;
    }

    public List<CommentsResponse> getCommentsByExpenseId(Long expenseId) {

        List<Comments> commentsList = commentRepository.findByExpenseId(expenseId);
        List<Long > userIds = commentsList.stream().map(comment -> comment.getCommentedBy()).distinct().collect(Collectors.toList());
        Map<Long,String> userNameMap = userClient.getUserNameMapByUserIds(userIds);
        List<CommentsResponse> commentsResponseList = commentsMapper.getCommentsResponse(commentsList,userNameMap);
        return commentsResponseList;
    }

    public List<CommentsResponse> getCommentsBySettlementId(Long settlementId) {

        List<Comments> commentsList = commentRepository.findBySettlementId(settlementId);
        List<Long > userIds = commentsList.stream().map(comment -> comment.getCommentedBy()).distinct().collect(Collectors.toList());
        Map<Long,String> userNameMap = userClient.getUserNameMapByUserIds(userIds);
        List<CommentsResponse> commentsResponseList = commentsMapper.getCommentsResponse(commentsList,userNameMap);
        return commentsResponseList;
    }

    public void createCommentActivity(ActivityType activityType, Comments comment)
    {
        Activity activity = Activity.builder()
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
        activity.setMessage(sb.toString());
        try
        {
            Gson gson = new Gson();
            String activityJson = gson.toJson(activity);
        }
        catch(Exception ex)
        {
            LOGGER.error("Error occurred in createCommentActivity()"+ex);
        }
    }
}