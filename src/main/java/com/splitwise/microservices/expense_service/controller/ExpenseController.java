package com.splitwise.microservices.expense_service.controller;

import com.splitwise.microservices.expense_service.entity.Expense;
import com.splitwise.microservices.expense_service.exception.ExpenseException;
import com.splitwise.microservices.expense_service.model.ExpenseRequest;
import com.splitwise.microservices.expense_service.service.ExpenseParticipantService;
import com.splitwise.microservices.expense_service.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/expense")
public class ExpenseController {
    @Autowired
    ExpenseService expenseService;
    @Autowired
    ExpenseParticipantService expenseParticipantService;

    @PostMapping("/add-expense")
    public ResponseEntity<String> addNewExpense(@RequestBody ExpenseRequest expenseRequest)
    {
        //Todo: Add validation for total amount equals to payers sum
        //Create a validation method for request validation
        if(expenseRequest == null)
        {
           return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try
        {
            expenseService.saveExpenseAndParticipantDetails(expenseRequest);
            return new ResponseEntity<>("Expense added successfully!", HttpStatus.OK);
        }
        catch(Exception ex)
        {
            System.out.println("Error occurred while saving Expense details "+ ex.getMessage());
            return new ResponseEntity<>("Error occurred while saving Expense details", HttpStatus.INTERNAL_SERVER_ERROR);

        }
    }

    @PutMapping("/update-expense")
    public ResponseEntity<String> updateExpense(@RequestBody ExpenseRequest expenseRequest)
    {
        if(expenseRequest == null)
        {
            return new ResponseEntity<>("Invalid request",HttpStatus.BAD_REQUEST);
        }
        try
        {
            Expense savedExpense = expenseService.saveExpenseFromRequest(expenseRequest);
            if(savedExpense != null)
            {
                expenseParticipantService.updateParticipantsExpense(expenseRequest,savedExpense.getExpenseId());
            }
        }
        catch(ExpenseException ex)
        {
            return new ResponseEntity<>(ex.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch(Exception ex)
        {
            System.out.println("Error occurred while updating participant expenses "+ ex.getMessage());
            return new ResponseEntity<>("Error occurred while updating participant expenses",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Expense saved successfully!",HttpStatus.OK);
    }

    }



