package com.splitwise.microservices.expense_service;


// Online Java Compiler
// Use this editor to write, compile and run your Java code online
import java.util.*;

class MultipayerSplitAlgorithm {
    public static void main(String[] args) {
        Map<Long,Map<Long,Double>> balanceSheet = new HashMap<>();
        MultipayerSplitAlgorithm multipayerSplitAlgorithm = new MultipayerSplitAlgorithm();
        Participant ep1 = multipayerSplitAlgorithm.new Participant(101L, 25.00, true); // Lisha Splits
        Participant ep2 = multipayerSplitAlgorithm.new Participant(102L, 35.00, true); // Sovan Splits
        Participant ep3 = multipayerSplitAlgorithm.new Participant(103L, 40.00, true); // Ankur Splits
        Participant ep4 = multipayerSplitAlgorithm.new Participant(104L, 60.00, true); // Jack Splits
        List<Participant> participantsList = new ArrayList<>();
        participantsList.add(ep1);
        participantsList.add(ep2);
        participantsList.add(ep3);
        participantsList.add(ep4);

        //Calculated the actual payer balances
        Map<Long,Double> payerBalanceMap = new HashMap<>();
        payerBalanceMap.put(101L,30.00);
        payerBalanceMap.put(102L,-50.00);
        payerBalanceMap.put(103L, 30.00);
        payerBalanceMap.put(104L, -10.00);

        for(Participant participant : participantsList)
        {
            if(!participant.isPayer())
            {
                Double participantAmount = participant.getSettlementAmount();
                for(Map.Entry<Long,Double> payerMapEntry: payerBalanceMap.entrySet())
                {

                    Long payerId = payerMapEntry.getKey();
                    Double payerAmount = payerMapEntry.getValue();
                    if(payerAmount > 0 && payerAmount > participantAmount)
                    {
                        balanceSheet.putIfAbsent(payerId,new HashMap<>());
                        Map<Long, Double> participantMap = balanceSheet.get(payerId);
                        participantMap.put(participant.getParticipantId(),Math.abs(participantAmount));
                        balanceSheet.put(payerId,participantMap);
                        payerMapEntry.setValue(payerAmount - participantAmount);
                        return;
                    }
                    else if(payerAmount> 0 && payerAmount <= participantAmount)
                    {
                        balanceSheet.putIfAbsent(payerId,new HashMap<>());
                        Map<Long, Double> participantMap = balanceSheet.get(payerId);
                        participantMap.put(participant.getParticipantId(),payerAmount);
                        balanceSheet.put(payerId,participantMap);
                        payerMapEntry.setValue(0.0); // Exit the loop as the amount is settled.
                        //participantAmount = participantAmount +payerAmount;//wrong
                    }
                }
            }
            else if(participant.isPayer() && payerBalanceMap.get(participant.getParticipantId())<0.00)
            {
                    for(Map.Entry<Long,Double> payerMapEntry: payerBalanceMap.entrySet()) {
                        Long payerId = payerMapEntry.getKey();
                        Long participantId = participant.getParticipantId();
                        Double payerAmount = payerMapEntry.getValue();
                        Double payeeAmount = Math.abs(payerBalanceMap.get(participant.getParticipantId()));
                        if(payerId != participantId)
                        {
                            balanceSheet.putIfAbsent(payerId,new HashMap<>());
                            Map<Long, Double> participantMap = balanceSheet.get(payerId);
                            if(payerAmount > 0 && payerAmount > payeeAmount)
                            {
                                participantMap.put(participantId,payeeAmount);
                                balanceSheet.put(payerId,participantMap);
                                payerMapEntry.setValue(payerAmount - payeeAmount);
                            }
                            else if(payerAmount > 0 && payerAmount <= payeeAmount)
                            {
                                participantMap.put(participantId,payerAmount);
                                balanceSheet.put(payerId,participantMap);
                                payerBalanceMap.put(participantId,(payerBalanceMap.get(participantId) + payerAmount)); //Updating remaining amount
                                payerMapEntry.setValue(0.0); // Exit the loop as the amount is settled.
                            }
                        }
            }
        }
    }
        for (Map.Entry<Long,Map<Long,Double>> balanceEntry : balanceSheet.entrySet())
        {
            Long getter = balanceEntry.getKey();
            for(Map.Entry<Long,Double> giverEntry : balanceEntry.getValue().entrySet())
            {
                Long giver = giverEntry.getKey();
                Double giverAmount = giverEntry.getValue();
                System.out.println(giver +" owes "+giverAmount+" to "+getter);
            }

        }
}

    class Participant {

        private Long participantId;
        private Double settlementAmount;
        private boolean isPayer;

        public Participant(Long participantId, Double settlementAmount, boolean isPayer) {
            this.participantId = participantId;
            this.settlementAmount = settlementAmount;
            this.isPayer = isPayer;
        }

        public Long getParticipantId() {
            return participantId;
        }

        public Double getSettlementAmount() {
            return settlementAmount;
        }

        public boolean isPayer() {
            return isPayer;
        }

        public void setParticipantId(Long participantId) {
            this.participantId = participantId;
        }

        public void setSettlementAmount(Double settlementAmount) {
            this.settlementAmount = settlementAmount;
        }

        public void setPayer(boolean payer) {
            isPayer = payer;
        }

        public Participant() {
        }
    }
}