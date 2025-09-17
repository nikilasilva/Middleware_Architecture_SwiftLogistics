package com.example.notification_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    private DriverOrderService driverOrderService;

    @RabbitListener(queues = "order.notifications.queue")
    public void handleOrderCreated(Map<String, Object> orderEvent) {
        logger.info("🔔 Received order notification event: {}", orderEvent);

        String eventType = (String) orderEvent.get("eventType");
        String orderId = (String) orderEvent.get("orderId");
        String clientId = (String) orderEvent.get("clientId");
        String deliveryAddress = (String) orderEvent.get("deliveryAddress");

        if ("ORDER_CREATED".equals(eventType)) {
            // Send notification to customer
            sendOrderConfirmationNotification(orderId, clientId, deliveryAddress, orderEvent);

            // Process order for driver assignment
            logger.info("🚚 Processing order {} for driver assignment", orderId);
            driverOrderService.processNewOrder(orderEvent);

        } else if ("ORDER_CANCELLED".equals(eventType)) {
            sendOrderCancellationNotification(orderId, orderEvent);
        }
    }

    // Handle order status update events
    @RabbitListener(queues = "order.status.queue")
    public void handleOrderStatusUpdate(Map<String, Object> statusEvent) {
        logger.info("📊 Received order status update event: {}", statusEvent);

        String orderId = (String) statusEvent.get("orderId");
        String newStatus = (String) statusEvent.get("newStatus");
        String system = (String) statusEvent.get("system");

        // Send notification to customer
        sendOrderStatusUpdateNotification(orderId, newStatus, system, statusEvent);

        // NEW: Update driver order status
        logger.info("🚚 Updating driver order status for order {}", orderId);
        driverOrderService.updateOrderFromQueue(statusEvent);
    }

    @RabbitListener(queues = "order.events.queue")
    public void handleOrderLifecycleEvents(Map<String, Object> orderEvent) {
        logger.info("📋 Received order lifecycle event: {}", orderEvent);

        String orderId = (String) orderEvent.get("orderId");
        String eventType = (String) orderEvent.get("eventType");

        logger.info("📊 Processing lifecycle event {} for order {}", eventType, orderId);

        // Process lifecycle events for driver service
        if ("ORDER_CREATED".equals(eventType)) {
            driverOrderService.processNewOrder(orderEvent);
        }
    }

    private void sendOrderConfirmationNotification(String orderId, String clientId, String deliveryAddress,
            Map<String, Object> orderDetails) {
        logger.info("📧 Sending order confirmation notification for order {} to client {}", orderId, clientId);

        try {
            Thread.sleep(2000); // Simulate notification processing time

            logger.info("✅ EMAIL SENT: Order {} confirmed for client {}", orderId, clientId);
            logger.info("📱 SMS SENT: Your package will be delivered to {}", deliveryAddress);
            logger.info("🔔 PUSH NOTIFICATION: Order {} is being processed", orderId);

        } catch (InterruptedException e) {
            logger.error("❌ Failed to send notification for order: {}", orderId, e);
        }
    }

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

    private void sendOrderCancellationNotification(String orderId, Map<String, Object> cancellationDetails) {
        logger.info("📧 Sending order cancellation notification for order {}", orderId);

        try {
            Thread.sleep(1500); // Simulate notification processing time

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