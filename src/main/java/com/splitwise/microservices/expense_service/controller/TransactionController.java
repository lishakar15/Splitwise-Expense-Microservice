package com.splitwise.microservices.expense_service.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("transaction")
public class TransactionController {
    @PostMapping("/record-cash-transaction")
    public void recordCashTransaction()
    {

    }

    @PostMapping("/record-online-payment")
    public void recordOnlineTransaction()
    {

    }
    @GetMapping("/get-expense-transactions/{expenseId}")
    public void getExpenseTransactions(@PathVariable("expenseId") Long expenseId)
    {
        //Todo Need to check if others transactions should shown to non participants for expense wise
    }
    @GetMapping("/{loggedUserId}/get-user-transaction/{targetUserId}")
    public void getUserTransactions(@PathVariable("loggedUserId") Long loggedUserId,
                                    @PathVariable("targetUserId")  Long targetUserId)
    {

    }


}
