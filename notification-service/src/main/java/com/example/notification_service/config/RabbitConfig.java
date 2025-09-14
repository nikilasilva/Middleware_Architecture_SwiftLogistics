package com.example.notification_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // Define all the queues your service listens to
    @Bean
    public Queue orderNotificationsQueue() {
        return QueueBuilder.durable("order.notifications.queue").build();
    }

    @Bean
    public Queue orderStatusQueue() {
        return QueueBuilder.durable("order.status.queue").build();
    }

    @Bean
    public Queue orderEventsQueue() {
        return QueueBuilder.durable("order.events.queue").build();
    }
}