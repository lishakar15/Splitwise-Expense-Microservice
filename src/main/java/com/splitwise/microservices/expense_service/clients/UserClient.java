package com.splitwise.microservices.expense_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "http://localhost:8080/")
public interface UserClient {
    @GetMapping("/user/get-user-name/{userId}")
    public String getUserName(@PathVariable("userId") Long userId);
}
