package com.splitwise.microservices.expense_service.controller;

import com.splitwise.microservices.expense_service.entity.Balance;
import com.splitwise.microservices.expense_service.model.BalanceResponse;
import com.splitwise.microservices.expense_service.model.BalanceSummary;
import com.splitwise.microservices.expense_service.model.GroupBalanceSummary;
import com.splitwise.microservices.expense_service.service.BalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("balance")
public class BalanceController {
    @Autowired
    BalanceService balanceService;

    @GetMapping("/getGroupBalances/{groupId}/{userId}")
    public ResponseEntity<List<BalanceResponse>> getBalancesOfGroup(@PathVariable("groupId") Long groupId, @PathVariable("userId") Long userId)
    {
        if(groupId == null || userId == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<BalanceResponse> balanceList;
        try
        {
            balanceList = balanceService.getAllBalancesByGroupId(groupId, userId);
        }
        catch(Exception ex)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(balanceList,HttpStatus.OK);
    }
    @GetMapping("/getUserAllBalances/{userId}")
    public ResponseEntity<List<BalanceResponse>> getUserAllBalances(@PathVariable("userId") Long userId)
    {
        if(userId == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<BalanceResponse> balanceResponseList;
        try
        {
            balanceResponseList = balanceService.getUsersAllBalances(userId);
        }
        catch(Exception ex)
        {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(balanceResponseList,HttpStatus.OK);
    }
    @GetMapping("getBalanceSummary/{userId}")
    public ResponseEntity<BalanceSummary> getUserBalanceSummary(@PathVariable Long userId){

        if(userId == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        BalanceSummary balanceSummary = balanceService.getUserBalanceSummaryByUserId(userId);

        return new ResponseEntity<>(balanceSummary,HttpStatus.OK);
    }
    @GetMapping("getGroupBalanceSummary/{userId}")
    public ResponseEntity<List<GroupBalanceSummary>> getGroupBalanceSummary(@PathVariable("userId") Long userId){

        if(userId == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<GroupBalanceSummary>  balanceSummaryList = balanceService.getGroupBalanceSummaryByUserId(userId);

        return new ResponseEntity<>(balanceSummaryList,HttpStatus.OK);
    }
    @GetMapping("getGroupBalanceSummary/{groupId}/{userId}")
    public ResponseEntity<List<GroupBalanceSummary>> getGroupBalanceSummary(@PathVariable("userId") Long userId, @PathVariable("groupId") Long groupId){

        if(userId == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        List<GroupBalanceSummary>  balanceSummaryList = balanceService.getGroupBalanceSummaryByUserId(userId, groupId);

        return new ResponseEntity<>(balanceSummaryList,HttpStatus.OK);
    }
}
