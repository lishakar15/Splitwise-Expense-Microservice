package com.splitwise.microservices.expense_service.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfiguration {

    @Bean
    public NewTopic getActivityKafkaTopic()
    {
        return TopicBuilder.name("activity").partitions(3).replicas(1).build();
    }

}
