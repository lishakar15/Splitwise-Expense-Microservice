package com.splitwise.microservices.expense_service.controller;

import com.splitwise.microservices.expense_service.entity.Comments;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentController {

    @GetMapping("/get-comments/expense/{expenseId}")
    public List<Comments> getCommentsByExpenseId(@PathVariable("expenseId") Long expenseId)
    {
        return null;

    }
    @GetMapping("/get-comments/settlement/{settlementId}")
    public List<Comments> getCommentsBySettlementId(@PathVariable("settlementId") Long settlementId)
    {
        return null;

    }
    @DeleteMapping("/delete-comment/{commentId}")
    public void deleteComment(@PathVariable("commentId") Long commentId)
    {

    }
}
