package com.splitwise.microservices.expense_service.controller;

import com.splitwise.microservices.expense_service.entity.Expense;
import com.splitwise.microservices.expense_service.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/expense")
public class ExpenseController {
    @Autowired
    ExpenseService expenseService;

    @PostMapping("/add-expense")
    public ResponseEntity<String> addNewExpense(@RequestBody Expense expense)
    {
        if(expense == null)
        {
           return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        expenseService.saveExpense(expense);
        return null;
    }


}
