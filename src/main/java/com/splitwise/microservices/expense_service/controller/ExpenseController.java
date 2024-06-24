package com.splitwise.microservices.expense_service.controller;

import com.splitwise.microservices.expense_service.entity.Expense;
import com.splitwise.microservices.expense_service.entity.ExpenseParticipant;
import com.splitwise.microservices.expense_service.mapper.ExpenseMapper;
import com.splitwise.microservices.expense_service.model.ExpenseRequest;
import com.splitwise.microservices.expense_service.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/expense")
public class ExpenseController {
    @Autowired
    ExpenseService expenseService;
    @Autowired
    ExpenseMapper expenseMapper;

    @PostMapping("/add-expense")
    public ResponseEntity<String> addNewExpense(@RequestBody ExpenseRequest expenseRequest)
    {
        if(expenseRequest == null)
        {
           return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Expense expense = expenseMapper.getExpenseFromExpenseRequest(expenseRequest);
        Expense savedExpense = expenseService.saveExpense(expense);
        if(savedExpense != null)
        {
            List<ExpenseParticipant> participantList = expenseMapper.getExpenseParticipantsFromExpenseRequest(expenseRequest,
                    savedExpense.getExpenseId());
            boolean isParticipantsSaved = expenseService.saveExpenseParticipants(participantList);
            if(!isParticipantsSaved)
            {
                return new ResponseEntity<>("Error occurred while saving Expense details",HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>("Expense added successfully!",HttpStatus.OK);
    }

    @PutMapping("/update-expense")
    public void updateExpense(@RequestBody Expense expense)
    {

    }


}
