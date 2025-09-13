package com.swiftlogistics.esb.controller;

import com.swiftlogistics.esb.model.DeliveryOrder;
import com.swiftlogistics.esb.service.CmsService;
import com.swiftlogistics.esb.service.RosService;
import com.swiftlogistics.esb.service.WmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class EsbController {

    private static final Logger logger = LoggerFactory.getLogger(EsbController.class);

    private final CmsService cmsService;
    private final RosService rosService;
    private final WmsService wmsService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public EsbController(CmsService cmsService, RosService rosService, WmsService wmsService) {
        this.cmsService = cmsService;
        this.rosService = rosService;
        this.wmsService = wmsService;
    }

    @GetMapping("/esb/processOrder")
    public String processOrder(@RequestParam("clientId") String clientId,
            @RequestParam("address") String address) {
        logger.info("Processing order for clientId: {} and address: {}", clientId, address);

        try {
            String cmsResponse = cmsService.fetchClientData(clientId);
            logger.info("CMS Response: {}", cmsResponse);

            String rosResponse = rosService.optimizeRoute(address);
            logger.info("ROS Response: {}", rosResponse);

            String wmsResponse = wmsService.checkWarehouseStatus();
            logger.info("WMS Response: {}", wmsResponse);

            String result = "CMS: " + cmsResponse + " | ROS: " + rosResponse + " | WMS: " + wmsResponse;
            logger.info("Final response: {}", result);

            return result;
        } catch (Exception e) {
            logger.error("Error processing order: ", e);
            return "Error processing order: " + e.getMessage();
        }
    }

    // 2. Create new order (POST method for complete order creation)
    @PostMapping("/orders")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody DeliveryOrder order) {
        logger.info("Creating new order: {}", order);

        try {
            Map<String, Object> response = new HashMap<>();

            // Validate client with CMS
            String clientValidation = cmsService.validateClient(order.getClientId());
            logger.info("Client validation result: {}", clientValidation);

            // Check if client validation failed
            if (clientValidation.contains("Invalid") || clientValidation.contains("Error")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("error", "Client validation failed: " + clientValidation);
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Create order in CMS
            String cmsOrderId = cmsService.createOrder(order);

            // Optimize route with ROS
            String routeId = rosService.createOptimizedRoute(order.getDeliveryAddress(), order.getOrderId());

            // Notify WMS about new package
            String wmsResponse = wmsService.registerPackage(order);

            response.put("success", true);
            response.put("orderId", order.getOrderId());
            response.put("clientValidation", clientValidation);
            response.put("cmsOrderId", cmsOrderId);
            response.put("routeId", routeId);
            response.put("wmsStatus", wmsResponse);

            // 🚀 PUBLISH ORDER CREATED EVENT TO RABBITMQ
            publishOrderCreatedEvent(order, response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error creating order: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // Add Map support for Order Service
    @PostMapping("/orders/map")
    public ResponseEntity<Map<String, Object>> createOrderFromMap(@RequestBody Map<String, Object> orderData) {
        logger.info("ESB received order data from microservice: {}", orderData);

        try {
            // Extract data from the map sent by Order Service
            String orderId = (String) orderData.get("orderId");
            String clientId = (String) orderData.get("clientId");
            String deliveryAddress = (String) orderData.get("deliveryAddress");
            String pickupAddress = (String) orderData.get("pickupAddress");

            logger.info("Processing order - ID: {}, Client: {}, Delivery: {}", orderId, clientId, deliveryAddress);

            // Use your existing service calls
            Map<String, Object> response = new HashMap<>();

            // Call CMS
            String clientValidation = cmsService.fetchClientData(clientId);
            logger.info("Client validation result: {}", clientValidation);

            // Call ROS
            String rosResponse = rosService.optimizeRoute(deliveryAddress);
            logger.info("ROS response: {}", rosResponse);

            // Call WMS
            String wmsResponse = wmsService.checkWarehouseStatus();
            logger.info("WMS response: {}", wmsResponse);

            response.put("success", true);
            response.put("orderId", orderId);
            response.put("clientValidation", clientValidation);
            response.put("routeId", rosResponse);
            response.put("wmsStatus", wmsResponse);
            response.put("processedBy", "ESB");
            response.put("timestamp", System.currentTimeMillis());

            // 🚀 PUBLISH ORDER EVENT TO RABBITMQ
            DeliveryOrder order = new DeliveryOrder();
            order.setOrderId(orderId);
            order.setClientId(clientId);
            order.setDeliveryAddress(deliveryAddress);
            order.setPickupAddress(pickupAddress);

            publishOrderCreatedEvent(order, response);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error creating order from map: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("processedBy", "ESB");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // 🚀 RabbitMQ Event Publisher
    private void publishOrderCreatedEvent(DeliveryOrder order, Map<String, Object> processingResult) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "ORDER_CREATED");
            event.put("orderId", order.getOrderId());
            event.put("clientId", order.getClientId());
            event.put("deliveryAddress", order.getDeliveryAddress());
            event.put("pickupAddress", order.getPickupAddress());
            event.put("processingResult", processingResult);
            event.put("timestamp", System.currentTimeMillis());
            event.put("source", "ESB");

            // Publish to different queues for different services

            // 1. Notification Service Queue
            rabbitTemplate.convertAndSend("order.notifications.exchange", "order.created", event);
            logger.info("📧 Published ORDER_CREATED event to notification service for order: {}", order.getOrderId());

            // 2. Route Service Queue
            rabbitTemplate.convertAndSend("order.routing.exchange", "route.optimize", event);
            logger.info("🛣️ Published ROUTE_OPTIMIZE event to route service for order: {}", order.getOrderId());

            // 3. Warehouse Service Queue
            rabbitTemplate.convertAndSend("order.warehouse.exchange", "package.register", event);
            logger.info("📦 Published PACKAGE_REGISTER event to warehouse service for order: {}", order.getOrderId());

            // 4. General Order Events Queue
            rabbitTemplate.convertAndSend("order.events.exchange", "order.lifecycle", event);
            logger.info("📋 Published ORDER_LIFECYCLE event for order: {}", order.getOrderId());

        } catch (Exception e) {
            logger.error("Failed to publish order created event: ", e);
            // Don't fail the order creation if messaging fails
        }
    }
}