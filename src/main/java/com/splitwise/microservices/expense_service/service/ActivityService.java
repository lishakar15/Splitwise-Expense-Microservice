package com.splitwise.microservices.expense_service.service;

import com.splitwise.microservices.expense_service.entity.Activity;
import com.splitwise.microservices.expense_service.repository.ActivityRepository;
import com.splitwise.microservices.expense_service.repository.ChangeLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ActivityService {
    @Autowired
    ActivityRepository activityRepository;
    @Autowired
    ChangeLogRepository changeLogRepository;


    public List<Activity> getActivityByGroupId(Long groupId) {

        return activityRepository.getActivityByGroupId(groupId);

    }

    public List<Activity> getAllGroupActivitiesOfUser(Long userId) {
        List<Long> groupIds = null; //need to get this from User Microservice
        return activityRepository.findByGroupIdIn(groupIds);

    }
}
