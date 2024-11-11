package com.splitwise.microservices.expense_service.controller;

import com.splitwise.microservices.expense_service.entity.Settlement;
import com.splitwise.microservices.expense_service.model.SettlementInsight;
import com.splitwise.microservices.expense_service.model.SettlementResponse;
import com.splitwise.microservices.expense_service.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/settlement")
public class SettlementController {

    @Autowired
    SettlementService settlementService;

    @PostMapping("/settleAmount")
    public ResponseEntity<?> performSettlement(@RequestBody Settlement settlement)
    {
        if(settlement == null)
        {
            return new ResponseEntity<>("Request cannot be null", HttpStatus.BAD_REQUEST);
        }
        Settlement savedSettlement = settlementService.saveSettlement(settlement);
        return new ResponseEntity<>(savedSettlement,HttpStatus.OK);
    }

    @GetMapping("/getAllSettlements/{groupId}")
    public ResponseEntity<?> getAllSettlementRecords(@PathVariable("groupId") Long groupId)
    {
        if(groupId == null)
        {
            return new ResponseEntity<>("Invalid request", HttpStatus.BAD_REQUEST);
        }
        List<SettlementResponse> settlementResponse = settlementService.getAllSettlementByGroupId(groupId);
        return new ResponseEntity<>(settlementResponse,HttpStatus.OK);
    }
    @GetMapping("/getAllUserSettlements/{userId}")
    public ResponseEntity<?> getAllUserSettlementRecords(@PathVariable("userId") Long userId)
    {
        if(userId == null)
        {
            return new ResponseEntity<>("Invalid request", HttpStatus.BAD_REQUEST);
        }
        List<SettlementResponse> settlementResponse = settlementService.getAllSettlementsByUserId(userId);
        return new ResponseEntity<>(settlementResponse,HttpStatus.OK);
    }

    @GetMapping("/getSettlementDetails/{settlementId}/{userId}")
    public ResponseEntity<SettlementResponse> getSettlementDetails(@PathVariable("settlementId") Long settlementId, @PathVariable("userId") Long userId)
    {
        if(settlementId == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        SettlementResponse settlement = settlementService.getSettlementDetailsByID(settlementId, userId);
        return new ResponseEntity<>(settlement,HttpStatus.OK);
    }
    @GetMapping("/getSettlementInsights/{userId}")
    public ResponseEntity<SettlementInsight> getSettlementInsightsData(@PathVariable("userId") Long userId){
        if(userId == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        SettlementInsight settlementInsight = settlementService.getSettlementInsightsDataByUserId(userId);
        return new ResponseEntity<>(settlementInsight,HttpStatus.OK);
    }

    @PutMapping("/updateSettlement")
    public ResponseEntity<String> updateSettlement(@RequestBody Settlement settlement)
    {
        if(settlement == null || settlement.getSettlementId() == null)
        {
            return new ResponseEntity<>("Invalid settlement request", HttpStatus.BAD_REQUEST);
        }
        settlementService.updateSettlement(settlement);
        return new ResponseEntity<>("Settlement updated successfully", HttpStatus.OK);
    }
    @DeleteMapping("/deleteSettlement/{settlementId}/{loggedInUserId}")
    public ResponseEntity<String> deleteSettlementById(@PathVariable("settlementId") Long settlementId,
                                                       @PathVariable("loggedInUserId") Long loggedInUserId)
    {
        if(settlementId == null)
        {
            return new ResponseEntity<>("Invalid request", HttpStatus.BAD_REQUEST);
        }
        boolean isSettlementDeleted = settlementService.deleteSettlementById(settlementId,loggedInUserId);
        if(!isSettlementDeleted)
        {
            return new ResponseEntity<>("Error occurred while deleting settlement", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>("Settlement deleted successfully",HttpStatus.OK);
    }

}
