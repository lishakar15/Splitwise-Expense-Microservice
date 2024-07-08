package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.entity.Settlement;
import com.splitwise.microservices.expense_service.repository.SettlementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            balanceService.calculateBalanceForSettlement(settlement);

            Settlement savedSettlement =  settlementRepository.save(settlement);
            return savedSettlement;
        }
        catch(Exception ex)
        {
            //Need to throw exception
        }
        return settlement;
    }
    public Settlement getSettlementById(Long settlementId)
    {
        Optional<Settlement> optional = settlementRepository.findById(settlementId);
        return optional.isPresent()? optional.get() : null;
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
            Settlement existingSettlement = getSettlementById(settlementId);
            balanceService.revertPreviousBalanceForSettlement(existingSettlement);
            settlementRepository.deleteSettlementById(settlementId);
            isDeleted = true;
        }
        catch(Exception ex)
        {
            //Need to throw exception
        }
        return isDeleted;
    }


    public ResponseEntity<Settlement> updateSettlement(Settlement settlement) {
        if(settlement == null)
        {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try
        {
            Settlement existingSettlement = getSettlementById(settlement.getSettlementId());
            if (existingSettlement == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            balanceService.revertPreviousBalanceForSettlement(existingSettlement);
            //Update new Settlement
            Settlement updatedSettlement = saveSettlement(settlement);

            return new ResponseEntity<>(updatedSettlement, HttpStatus.OK);
        } catch (Exception ex) {
            //Need to throw Exception
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    public Settlement getSettlementDetailsByID(Long settlementId) {
        Settlement settlement = null;
        try
        {
            Optional<Settlement> optional = settlementRepository.findById(settlementId);
            if(optional.isPresent())
            {
                settlement = optional.get();
            }
        }
        catch(Exception ex)
        {
            //Need to throw Exception
        }
        return settlement;
    }
}
