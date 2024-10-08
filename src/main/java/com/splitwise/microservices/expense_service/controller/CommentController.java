package com.splitwise.microservices.expense_service.controller;

import com.splitwise.microservices.expense_service.clients.UserClient;
import com.splitwise.microservices.expense_service.entity.Comments;
import com.splitwise.microservices.expense_service.model.CommentsResponse;
import com.splitwise.microservices.expense_service.service.CommentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {
    @Autowired
    CommentService commentService;
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentController.class);

    @PostMapping("/post-comment")
    public ResponseEntity<Comments> postComment(@RequestBody Comments comment)
    {
        if(comment == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try
        {
            Comments savedComment = commentService.saveComment(comment);
            return new ResponseEntity<>(savedComment,HttpStatus.OK);
        }
        catch (Exception ex)
        {
            LOGGER.error("Error occurred while saving comments "+ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/get-comments/expense/{expenseId}")
    public ResponseEntity<List<CommentsResponse>> getCommentsByExpenseId(@PathVariable("expenseId") Long expenseId)
    {
        if(expenseId == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<CommentsResponse> commentsResponseList;
        try
        {
            commentsResponseList = commentService.getCommentsByExpenseId(expenseId);
        }
        catch(Exception ex)
        {
            LOGGER.error("Error occurred while retrieving expense comments "+ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(commentsResponseList,HttpStatus.OK);
    }

    @GetMapping("/get-comments/settlement/{settlementId}")
    public ResponseEntity<List<CommentsResponse>> getCommentsBySettlementId(@PathVariable("settlementId") Long settlementId)
    {

        if(settlementId == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<CommentsResponse> commentsResponseList;
        try
        {
            commentsResponseList = commentService.getCommentsBySettlementId(settlementId);
        }
        catch(Exception ex)
        {
            LOGGER.error("Error occurred while retrieving settlement comments "+ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(commentsResponseList,HttpStatus.OK);

    }
    @DeleteMapping("/delete-comment/{commentId}/{loggedInUser}")
    public ResponseEntity<String> deleteComment(@PathVariable("commentId") Long commentId,@PathVariable("loggedInUser") Long loggedInUser)
    {
        if(commentId == null || loggedInUser == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try
        {
            commentService.deleteCommentById(commentId,loggedInUser);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        catch(Exception ex)
        {
            LOGGER.error("Error occurred while deleting the comment "+ex);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
