package com.splitwise.microservices.expense_service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {
    @GetMapping("/user/get-user-name/{userId}")
    public String getUserName(@PathVariable("userId") Long userId);
    @GetMapping("/group/get-group-name/{groupId}")
    public String getGroupName(@PathVariable("groupId") Long groupId);
    @GetMapping("/user/get-user-name-map/{groupId}")
    public Map<Long,String> getUserNameMapByGroupId(@PathVariable Long groupId);
    @PostMapping("user/get-user-name-map/")
    public Map<Long,String> getUserNameMapByUserIds(List<Long> userIds);
    @GetMapping("/group/get-group-name/{groupId}")
    public String getGroupNameByGroupId(@PathVariable Long groupId);
    @GetMapping("/group/get-all-group-map/{userId}")
    public Map<Long, String> getGroupNameMap(@PathVariable("userId") Long userId);
    @GetMapping("/user/get-friends-name-map/{userId}")
    public Map<Long, String> getFriendsUserNameMapByUserId(@PathVariable("userId") Long userId);
    @GetMapping("/group/get-group-name-map/{groupId}")
    public Map<Long, String> getGroupNameMapByGroupId(@PathVariable("groupId") Long groupId);

}
