package com.splitwise.microservices.expense_service.controller;

import com.splitwise.microservices.expense_service.entity.Balance;
import com.splitwise.microservices.expense_service.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("balance")
public class BalanceController {
    @Autowired
    BalanceService balanceService;

    @GetMapping("/getGroupBalances/{groupId}")
    public ResponseEntity<List<Balance>> getBalancesOfGroup(@PathVariable("groupId") Long groupId)
    {
        if(groupId == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<Balance> balanceList = new ArrayList<>();
        try
        {
            balanceList = balanceService.getAllBalancesByGroupId(groupId);
        }
        catch(Exception ex)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(balanceList,HttpStatus.OK);
    }
    @GetMapping("/getUserAllBalances/{userId}")
    public ResponseEntity<List<Balance>> getUserAllBalances(@PathVariable("userId") Long userId)
    {
        if(userId == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<Balance> balanceList = new ArrayList<>();
        try
        {
            balanceList = balanceService.getUsersAllBalances(userId);
        }
        catch(Exception ex)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(balanceList,HttpStatus.OK);
    }
}
