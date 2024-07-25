package com.splitwise.microservices.expense_service.kafka;

import com.splitwise.microservices.expense_service.external.ActivityRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Value("${kafka.topic.name}")
    private String topic;

    public void sendActivityMessage(String activityMsg)
    {
        kafkaTemplate.send(topic,activityMsg);
    }

}
