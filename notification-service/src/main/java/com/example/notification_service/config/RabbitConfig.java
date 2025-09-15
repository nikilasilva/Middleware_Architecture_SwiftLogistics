package com.example.notification_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

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