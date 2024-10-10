package com.splitwise.microservices.expense_service.clients;

import com.splitwise.microservices.expense_service.external.Activity;
import com.splitwise.microservices.expense_service.external.ActivityRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "ACTIVITY-SERVICE")
public interface ActivityClient {

    @PostMapping("/activity/processActivityRequest")
    public void sendActivityRequest(ActivityRequest activityRequest);
}
