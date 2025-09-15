package com.swiftlogistics.esb.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchanges
    @Bean
    public TopicExchange orderNotificationsExchange() {
        return new TopicExchange("order.notifications.exchange");
    }

    @Bean
    public TopicExchange orderRoutingExchange() {
        return new TopicExchange("order.routing.exchange");
    }

    @Bean
    public TopicExchange orderWarehouseExchange() {
        return new TopicExchange("order.warehouse.exchange");
    }

    @Bean
    public TopicExchange orderEventsExchange() {
        return new TopicExchange("order.events.exchange");
    }

    // Queues
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable("order.notifications.queue").build();
    }

    @Bean
    public Queue routeQueue() {
        return QueueBuilder.durable("order.routing.queue").build();
    }

    @Bean
    public Queue warehouseQueue() {
        return QueueBuilder.durable("order.warehouse.queue").build();
    }

    @Bean
    public Queue orderLifecycleQueue() {
        return QueueBuilder.durable("order.events.queue").build();
    }

    // Bindings
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(orderNotificationsExchange())
                .with("order.*");
    }

    @Bean
    public Binding routeBinding() {
        return BindingBuilder.bind(routeQueue())
                .to(orderRoutingExchange())
                .with("route.*");
    }

    @Bean
    public Binding warehouseBinding() {
        return BindingBuilder.bind(warehouseQueue())
                .to(orderWarehouseExchange())
                .with("package.*");
    }

    @Bean
    public Binding orderEventsBinding() {
        return BindingBuilder.bind(orderLifecycleQueue())
                .to(orderEventsExchange())
                .with("order.*");
    }

    // Message converter
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

    // NEW: Order Cancellation Exchange
    @Bean
    public TopicExchange orderCancellationExchange() {
        return new TopicExchange("order.cancellation.exchange");
    }

    // NEW: Cancellation Queue
    @Bean
    public Queue cancellationQueue() {
        return QueueBuilder.durable("order.cancellation.queue").build();
    }

    // NEW: Cancellation Binding
    @Bean
    public Binding cancellationBinding() {
        return BindingBuilder.bind(cancellationQueue())
                .to(orderCancellationExchange())
                .with("order.cancelled");
    }
}