package com.splitwise.microservices.expense_service.controller;

import com.splitwise.microservices.expense_service.entity.Settlement;
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
        List<Settlement> settlements = settlementService.getAllSettlementByGroupId(groupId);
        return new ResponseEntity<>(settlements,HttpStatus.OK);
    }

    @GetMapping("/getSettlementDetails/{settlementId}")
    public ResponseEntity<Settlement> getSettlementDetails(@PathVariable("settlementId") Long settlementId)
    {
        if(settlementId == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Settlement settlement = settlementService.getSettlementDetailsByID(settlementId);
        if(settlement == null)
        {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(settlement,HttpStatus.OK);
    }

    @PutMapping("/updateSettlement")
    public ResponseEntity<String> updateSettlement(@RequestBody Settlement settlement)
    {
        if(settlement == null || settlement.getSettlementId() == null)
        {
            return new ResponseEntity<>("Invalid settlement request", HttpStatus.BAD_REQUEST);
        }
        settlementService.updateSettlement(settlement);
        return new ResponseEntity<>("Expense updated successfully", HttpStatus.OK);
    }
    @DeleteMapping("/deleteSettlement/{settlementId}")
    public ResponseEntity<String> deleteSettlementById(@PathVariable("settlementId") Long settlementId)
    {
        if(settlementId == null)
        {
            return new ResponseEntity<>("Invalid request", HttpStatus.BAD_REQUEST);
        }
        boolean isSettlementDeleted = settlementService.deleteSettlementById(settlementId);
        if(!isSettlementDeleted)
        {
            return new ResponseEntity<>("Error occurred while deleting settlement", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("Expense deleted successfully",HttpStatus.OK);
    }

}
