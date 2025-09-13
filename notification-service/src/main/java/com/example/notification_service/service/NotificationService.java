package com.example.notification_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @RabbitListener(queues = "order.notifications.queue")
    public void handleOrderCreated(Map<String, Object> orderEvent) {
        logger.info("🔔 Received order notification event: {}", orderEvent);

        String eventType = (String) orderEvent.get("eventType");
        String orderId = (String) orderEvent.get("orderId");
        String clientId = (String) orderEvent.get("clientId");
        String deliveryAddress = (String) orderEvent.get("deliveryAddress");

        if ("ORDER_CREATED".equals(eventType)) {
            sendOrderConfirmationNotification(orderId, clientId, deliveryAddress, orderEvent);
        }
    }

    private void sendOrderConfirmationNotification(String orderId, String clientId, String deliveryAddress,
            Map<String, Object> orderDetails) {
        logger.info("📧 Sending order confirmation notification for order {} to client {}", orderId, clientId);

        // Simulate sending email/SMS
        try {
            Thread.sleep(2000); // Simulate notification processing time

            logger.info("✅ EMAIL SENT: Order {} confirmed for client {}", orderId, clientId);
            logger.info("📱 SMS SENT: Your package will be delivered to {}", deliveryAddress);
            logger.info("🔔 PUSH NOTIFICATION: Order {} is being processed", orderId);

        } catch (InterruptedException e) {
            logger.error("❌ Failed to send notification for order: {}", orderId, e);
        }
    }

    @RabbitListener(queues = "order.events.queue")
    public void handleOrderLifecycleEvents(Map<String, Object> orderEvent) {
        logger.info("📋 Received order lifecycle event: {}", orderEvent);

        String orderId = (String) orderEvent.get("orderId");
        String eventType = (String) orderEvent.get("eventType");

        logger.info("📊 Processing lifecycle event {} for order {}", eventType, orderId);

        // You could store these events in a database for tracking
        // You could send different notifications based on event type
        // You could trigger other workflows
    }
}