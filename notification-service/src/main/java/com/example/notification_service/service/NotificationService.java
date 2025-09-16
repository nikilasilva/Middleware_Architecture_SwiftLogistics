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
        } else if ("ORDER_CANCELLED".equals(eventType)) {
            sendOrderCancellationNotification(orderId, orderEvent);
        }
    }

    // NEW: Handle order status update events
    @RabbitListener(queues = "order.status.queue")
    public void handleOrderStatusUpdate(Map<String, Object> statusEvent) {
        logger.info("📊 Received order status update event: {}", statusEvent);

        String orderId = (String) statusEvent.get("orderId");
        String newStatus = (String) statusEvent.get("newStatus");
        String system = (String) statusEvent.get("system");

        sendOrderStatusUpdateNotification(orderId, newStatus, system, statusEvent);
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

    // NEW: Handle status update notifications
    private void sendOrderStatusUpdateNotification(String orderId, String newStatus, String system,
            Map<String, Object> statusDetails) {
        logger.info("📨 Sending status update notification for order {} - new status: {}", orderId, newStatus);

        try {
            Thread.sleep(1000); // Simulate notification processing

            logger.info("✅ STATUS UPDATE EMAIL: Order {} status changed to {} in {}", orderId, newStatus, system);
            logger.info("📱 STATUS UPDATE SMS: Your order {} is now {}", orderId, newStatus);
            logger.info("🔔 STATUS UPDATE PUSH: Order {} - {}", orderId, newStatus);

        } catch (InterruptedException e) {
            logger.error("❌ Failed to send status update notification for order: {}", orderId, e);
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

    private void sendOrderCancellationNotification(String orderId, Map<String, Object> cancellationDetails) {
        logger.info("📧 Sending order cancellation notification for order {}", orderId);

        try {
            Thread.sleep(1500); // Simulate notification processing time

            // Extract cancellation details
            Object cancellationResultObj = cancellationDetails.get("cancellationResult");
            Map<String, Object> cancellationResult = null;
            if (cancellationResultObj instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, Object> temp = (Map<String, Object>) cancellationResultObj;
                cancellationResult = temp;
            }

            logger.info("✅ CANCELLATION EMAIL SENT: Order {} has been cancelled successfully", orderId);
            logger.info(
                    "📱 CANCELLATION SMS SENT: Your order {} has been cancelled. Refund will be processed within 3-5 business days",
                    orderId);
            logger.info("🔔 CANCELLATION PUSH NOTIFICATION: Order {} cancellation confirmed", orderId);

            // Log system-specific cancellation results
            if (cancellationResult != null) {
                logger.info("📋 CMS Cancellation: {}", cancellationResult.get("cmsResult"));
                logger.info("🛣️ ROS Cancellation: {}", cancellationResult.get("rosResult"));
                logger.info("📦 WMS Cancellation: {}", cancellationResult.get("wmsResult"));
            }

        } catch (InterruptedException e) {
            logger.error("❌ Failed to send cancellation notification for order: {}", orderId, e);
        }
    }
}