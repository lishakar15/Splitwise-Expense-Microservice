package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.entity.Settlement;
import com.splitwise.microservices.expense_service.repository.SettlementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SettlementService {
    @Autowired
    SettlementRepository settlementRepository;
    @Autowired
    BalanceService balanceService;

    public Settlement saveSettlement(Settlement settlement)
    {
        try
        {
            //Settle Balance Amount

            Settlement savedSettlement =  settlementRepository.save(settlement);
            return savedSettlement;
        }
        catch(Exception ex)
        {
            //Need to throw exception
        }
        return settlement;
    }

    public List<Settlement> getAllSettlementByGroupId(Long groupId)
    {
        List<Settlement> settlements = new ArrayList<>();
        try
        {
            settlements = settlementRepository.getAllSettlementByGroupId(groupId);
        }
        catch(Exception ex)
        {
            //Need to throw exception
        }
        return settlements;
    }



    public boolean deleteSettlementById(Long settlementId)
    {
        boolean isDeleted = false;
        try
        {
            isDeleted = settlementRepository.deleteSettlementById(settlementId);
        }
        catch(Exception ex)
        {
            //Need to throw exception
        }
        return isDeleted;
    }


    public void updateSettlement(Settlement settlement) {

        try{
            settlementRepository.save(settlement);
        }
        catch(Exception ex)
        {
            //Need to throw Exception
        }
    }
}
