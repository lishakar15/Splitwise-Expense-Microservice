package com.splitwise.microservices.expense_service.controller;

import com.splitwise.microservices.expense_service.exception.ExpenseException;
import com.splitwise.microservices.expense_service.model.ExpenseResponse;
import com.splitwise.microservices.expense_service.model.ExpenseRequest;
import com.splitwise.microservices.expense_service.service.ExpenseParticipantService;
import com.splitwise.microservices.expense_service.service.ExpenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/expense")
@CrossOrigin(origins = "http://localhost:3000")
public class ExpenseController {
    @Autowired
    ExpenseService expenseService;
    @Autowired
    ExpenseParticipantService expenseParticipantService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpenseController.class);

    @GetMapping("/get-expenses/{groupId}")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByGroupId(@PathVariable("groupId") Long groupId)
    {
        if(groupId == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<ExpenseResponse> expenseResponse = new ArrayList<>();
        try
        {
            expenseResponse = expenseService.getExpensesByGroupId(groupId);
        }
        catch (Exception ex)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(expenseResponse, HttpStatus.OK);
    }
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
            expenseService.updateExpenseAndParticipantsFromRequest(expenseRequest);
        }
        catch(ExpenseException ex)
        {
            System.out.println(ex.getMessage());
            return new ResponseEntity<>(ex.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch(Exception ex)
        {
            System.out.println(ex.getMessage());
            return new ResponseEntity<>("Error occurred while updating participant expenses",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Expense updated successfully!",HttpStatus.OK);
    }

    @DeleteMapping("delete-expense/{expenseId}/{loggedInUser}")
    public ResponseEntity<String> deleteExpense(@PathVariable("expenseId") Long expenseId,@PathVariable("loggedInUser") Long loggedInUser)
    {
        try
        {
            expenseService.deleteExpenseDetails(expenseId,loggedInUser);
        }
        catch (Exception ex)
        {
            LOGGER.error("Error occurred while deleting expense "+ex);
            return new ResponseEntity<>(ex.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Expense deleted successfully!!",HttpStatus.OK);
    }


}



