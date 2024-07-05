package com.splitwise.microservices.expense_service.controller;

import com.splitwise.microservices.expense_service.entity.Settlement;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/settlement")
public class SettlementController {

    @PostMapping("/settleAmount")
    public void performSettlement(@RequestBody Settlement settlement)
    {


    }

    @GetMapping("/getSettlementDetails")
    public List<Settlement> getAllSettlementRecords(Long groupId)
    {
        return null;
    }
}
