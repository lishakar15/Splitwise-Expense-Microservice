package com.splitwise.microservices.expense_service.mapper;

import com.splitwise.microservices.expense_service.entity.Comments;
import com.splitwise.microservices.expense_service.model.CommentsResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CommentsMapper {

    public List<CommentsResponse> getCommentsResponse(List<Comments> comments, Map<Long, String> userNameMap) {
        List<CommentsResponse> commentsResponseList = new ArrayList<>();
        if (comments != null && userNameMap != null) {
            for (Comments comment : comments) {
                CommentsResponse commentsResponse = CommentsResponse.builder()
                        .commentId(comment.getCommentId())
                        .expenseId(comment.getExpenseId())
                        .settlementId((comment.getSettlementId()))
                        .groupId(comment.getGroupId())
                        .commentedBy(comment.getCommentedBy())
                        .userName(userNameMap.get(comment.getCommentedBy()))
                        .comment(comment.getComment())
                        .createDate(comment.getCreateDate())
                        .build();
                commentsResponseList.add(commentsResponse);
            }
        }
        return commentsResponseList;
    }
}
