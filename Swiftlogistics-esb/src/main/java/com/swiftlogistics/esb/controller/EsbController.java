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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

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

    // 3. Get order status from all systems
    @GetMapping("/orders/{orderId}/status")
    public ResponseEntity<Map<String, Object>> getOrderStatus(@PathVariable("orderId") String orderId) {
        logger.info("Getting status for order: {}", orderId);

        try {
            Map<String, Object> status = new HashMap<>();

            // Get status from CMS
            String cmsStatus = cmsService.getOrderStatus(orderId);

            // Get route status from ROS
            String routeStatus = rosService.getRouteStatus(orderId);

            // Get package status from WMS
            String packageStatus = wmsService.getPackageStatus(orderId);

            status.put("orderId", orderId);
            status.put("cmsStatus", cmsStatus);
            status.put("routeStatus", routeStatus);
            status.put("packageStatus", packageStatus);
            status.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            logger.error("Error getting order status: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // 4. Update order status
    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable("orderId") String orderId,
            @RequestParam("status") String status,
            @RequestParam(value = "system", required = false) String system) {

        logger.info("Updating order {} status to {} from system {}", orderId, status,
                system);

        try {
            Map<String, Object> response = new HashMap<>();

            if ("CMS".equalsIgnoreCase(system) || system == null) {
                String cmsResponse = cmsService.updateOrderStatus(orderId, status);
                response.put("cmsUpdate", cmsResponse);
            }

            if ("ROS".equalsIgnoreCase(system) || system == null) {
                String rosResponse = rosService.updateRouteStatus(orderId, status);
                response.put("rosUpdate", rosResponse);
            }

            if ("WMS".equalsIgnoreCase(system) || system == null) {
                String wmsResponse = wmsService.updatePackageStatus(orderId, status);
                response.put("wmsUpdate", wmsResponse);
            }

            response.put("success", true);
            response.put("orderId", orderId);
            response.put("newStatus", status);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error updating order status: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // Add Map support for Order Service
    @PostMapping("/orders/map")
    public ResponseEntity<Map<String, Object>> createOrderFromMap(@RequestBody Map<String, Object> orderData) {
        logger.info("ESB received detailed order data: {}", orderData);

        try {
            // Extract basic order data
            String orderId = (String) orderData.get("orderId");
            String clientId = (String) orderData.get("clientId");
            String deliveryAddress = (String) orderData.get("deliveryAddress");
            String pickupAddress = (String) orderData.get("pickupAddress");
            String recipientName = (String) orderData.get("recipientName");
            String recipientPhone = (String) orderData.get("recipientPhone");
            String notes = (String) orderData.get("notes");
            Double totalWeight = orderData.get("totalWeight") != null ? ((Number) orderData.get("totalWeight")).doubleValue() : 0.0;
            Integer totalItems = orderData.get("totalItems") != null ? (Integer) orderData.get("totalItems") : 0;
            List<?> items = (List<?>) orderData.get("items");

            logger.info("Processing order {} with {} items ({}kg total) for recipient: {}",
                    orderId, totalItems, totalWeight, recipientName);

            // Create DeliveryOrder object
            DeliveryOrder order = new DeliveryOrder();
            order.setOrderId(orderId);
            order.setClientId(clientId);
            order.setDeliveryAddress(deliveryAddress);
            order.setPickupAddress(pickupAddress);
            order.setRecipientName(recipientName);
            order.setRecipientPhone(recipientPhone);
            order.setNotes(notes);
            order.setTotalWeight(totalWeight);
            order.setTotalItems(totalItems);

            // Map items from request
            List<DeliveryOrder.OrderItem> orderItems = null;
            if (items != null && !items.isEmpty()) {
                orderItems = new ArrayList<>();
                for (Object obj : items) {
                    Map<String, Object> itemMap = (Map<String, Object>) obj;
                    DeliveryOrder.OrderItem item = new DeliveryOrder.OrderItem(
                            (String) itemMap.get("itemId"),
                            (String) itemMap.get("description"),
                            itemMap.get("quantity") != null ? ((Number) itemMap.get("quantity")).intValue() : 0,
                            itemMap.get("weightKg") != null ? ((Number) itemMap.get("weightKg")).doubleValue() : 0.0
                    );
                    orderItems.add(item);
                }
                order.setItems(orderItems);
            }

            Map<String, Object> response = new HashMap<>();
            Map<String, Boolean> registrationResults = new HashMap<>();

            // 1. Validate client and create order in CMS
            String clientValidation = cmsService.fetchClientData(clientId);
            logger.info("Client validation result: {}", clientValidation);

            if (!clientValidation.contains("Invalid") && !clientValidation.contains("Error")) {
                try {
                    String cmsOrderResult = cmsService.createOrder(order);
                    logger.info("CMS order creation result: {}", cmsOrderResult);
                    registrationResults.put("CMS", true);
                } catch (Exception e) {
                    logger.warn("Failed to create order in CMS: {}", e.getMessage());
                    registrationResults.put("CMS", false);
                }
            } else {
                registrationResults.put("CMS", false);
            }

            // 2. Create optimized route with ROS
            try {
                String routeResult = rosService.createOptimizedRoute(deliveryAddress, orderId, totalWeight);
                logger.info("ROS route creation result: {}", routeResult);
                response.put("routeId", routeResult);
                registrationResults.put("ROS", true);
            } catch (Exception e) {
                logger.warn("Failed to create route in ROS: {}", e.getMessage());
                response.put("routeId", "Route creation failed: " + e.getMessage());
                registrationResults.put("ROS", false);
            }

            // 3. Register package with WMS
            try {
                String wmsResult = wmsService.registerPackage(order);
                logger.info("WMS package registration result: {}", wmsResult);
                response.put("wmsStatus", wmsResult);
                registrationResults.put("WMS", true);
            } catch (Exception e) {
                logger.warn("Failed to register package in WMS: {}", e.getMessage());
                response.put("wmsStatus", "Package registration failed: " + e.getMessage());
                registrationResults.put("WMS", false);
            }

            // Determine overall success
            long successfulRegistrations = registrationResults.values().stream().filter(b -> b).count();
            boolean overallSuccess = successfulRegistrations >= 2;

            response.put("success", overallSuccess);
            response.put("orderId", orderId);
            response.put("clientValidation", clientValidation);
            response.put("registrationResults", registrationResults);
            response.put("itemsSummary", totalItems + " items, " + totalWeight + "kg total");
            response.put("itemList", orderItems != null ? orderItems : new ArrayList<>());
            response.put("recipient", recipientName);
            response.put("processedBy", "ESB");
            response.put("timestamp", System.currentTimeMillis());

            if (overallSuccess) {
                publishOrderCreatedEvent(order, response);
                logger.info("Order {} successfully created with {} items ({}kg) for {}",
                        orderId, totalItems, totalWeight, recipientName);
            }

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

    // 5. Health check for all systems : theesh dev
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        logger.info("Performing health check on all systems");

        Map<String, Object> health = new HashMap<>();

        try {
            // Check CMS health
            boolean cmsHealthy = cmsService.isHealthy();
            health.put("cms", Map.of("status", cmsHealthy ? "UP" : "DOWN"));

            // Check ROS health
            boolean rosHealthy = rosService.isHealthy();
            health.put("ros", Map.of("status", rosHealthy ? "UP" : "DOWN"));

            // Check WMS health
            boolean wmsHealthy = wmsService.isHealthy();
            health.put("wms", Map.of("status", wmsHealthy ? "UP" : "DOWN"));

            boolean allHealthy = cmsHealthy && rosHealthy && wmsHealthy;
            health.put("overall", allHealthy ? "UP" : "DOWN");
            health.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            logger.error("Error during health check: ", e);
            health.put("overall", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(health);
        }
    }

    // // 6. Route optimization endpoint
    @PostMapping("/routes/optimize")
    public ResponseEntity<Map<String, Object>> optimizeRoute(@RequestBody Map<String, Object> routeRequest) {
        logger.info("Optimizing route: {}", routeRequest);

        try {
            String vehicleId = (String) routeRequest.get("vehicleId");
            String address = (String) routeRequest.get("address");

            String routeResponse = rosService.optimizeRoute(address);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("routeOptimization", routeResponse);
            response.put("vehicleId", vehicleId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error optimizing route: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // // 7. Package tracking endpoint
    @GetMapping("/packages/{packageId}/track")
    public ResponseEntity<Map<String, Object>> trackPackage(@PathVariable("packageId") String packageId) {
        logger.info("Tracking package: {}", packageId);

        try {
            Map<String, Object> tracking = new HashMap<>();

            // Get package info from WMS
            String wmsInfo = wmsService.getPackageInfo(packageId);

            // Get related order info from CMS
            String cmsInfo = cmsService.getPackageOrderInfo(packageId);

            // Get route info from ROS
            String routeInfo = rosService.getPackageRouteInfo(packageId);

            tracking.put("packageId", packageId);
            tracking.put("warehouseInfo", wmsInfo);
            tracking.put("orderInfo", cmsInfo);
            tracking.put("routeInfo", routeInfo);
            tracking.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(tracking);

        } catch (Exception e) {
            logger.error("Error tracking package: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // // 8. Client information endpoint
    // @GetMapping("/clients/{clientId}")
    // public ResponseEntity<Map<String, Object>> getClientInfo(@PathVariable String
    // clientId) {
    // logger.info("Getting client info for: {}", clientId);

    // try {
    // String clientInfo = cmsService.fetchClientData(clientId);

    // Map<String, Object> response = new HashMap<>();
    // response.put("clientId", clientId);
    // response.put("clientInfo", clientInfo);
    // response.put("timestamp", System.currentTimeMillis());

    // return ResponseEntity.ok(response);

    // } catch (Exception e) {
    // logger.error("Error getting client info: ", e);
    // Map<String, Object> errorResponse = new HashMap<>();
    // errorResponse.put("error", e.getMessage());
    // return ResponseEntity.internalServerError().body(errorResponse);
    // }
    // }

    // // 9. System statistics endpoint
    // @GetMapping("/stats")
    // public ResponseEntity<Map<String, Object>> getSystemStats() {
    // logger.info("Getting system statistics");

    // try {
    // Map<String, Object> stats = new HashMap<>();

    // // Get stats from each system
    // Map<String, Object> cmsStats = cmsService.getSystemStats();
    // Map<String, Object> rosStats = rosService.getSystemStats();
    // Map<String, Object> wmsStats = wmsService.getSystemStats();

    // stats.put("cms", cmsStats);
    // stats.put("ros", rosStats);
    // stats.put("wms", wmsStats);
    // stats.put("timestamp", System.currentTimeMillis());

    // return ResponseEntity.ok(stats);

    // } catch (Exception e) {
    // logger.error("Error getting system stats: ", e);
    // Map<String, Object> errorResponse = new HashMap<>();
    // errorResponse.put("error", e.getMessage());
    // return ResponseEntity.internalServerError().body(errorResponse);
    // }
    // }

    // 10. Emergency order cancellation with smart ID mapping
    @DeleteMapping("/orders/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@RequestParam("orderId") String orderId) {
        logger.info("ESB received cancellation request for order: {}", orderId);

        try {
            Map<String, Object> response = new HashMap<>();

            // Cancel in all systems
            String cmsResult = cmsService.cancelOrder(orderId);
            String rosResult = rosService.cancelRoute(orderId);
            String wmsResult = wmsService.cancelPackage(orderId);

            response.put("success", true);
            response.put("orderId", orderId);
            response.put("cmsResult", cmsResult);
            response.put("rosResult", rosResult);
            response.put("wmsResult", wmsResult);

            // 🚀 PUBLISH ORDER CANCELLATION EVENT TO RABBITMQ
            publishOrderCancellationEvent(orderId, response);
            logger.info("Order {} cancelled successfully across all systems", orderId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error cancelling order: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
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

    private void publishOrderCancellationEvent(String orderId, Map<String, Object> cancellationResult) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("eventType", "ORDER_CANCELLED");
            event.put("orderId", orderId);
            event.put("cancellationResult", cancellationResult);
            event.put("timestamp", System.currentTimeMillis());
            event.put("source", "ESB");

            // Publish to different queues for different services

            // 1. Notification Service Queue - Send cancellation confirmation
            rabbitTemplate.convertAndSend("order.notifications.exchange", "order.cancelled", event);
            logger.info("📧 Published ORDER_CANCELLED event to notification service for order: {}", orderId);

            // 2. Route Service Queue - Stop route optimization
            rabbitTemplate.convertAndSend("order.routing.exchange", "route.cancelled", event);
            logger.info("🛣️ Published ROUTE_CANCELLED event to route service for order: {}", orderId);

            // 3. Warehouse Service Queue - Release package resources
            rabbitTemplate.convertAndSend("order.warehouse.exchange", "package.cancelled", event);
            logger.info("📦 Published PACKAGE_CANCELLED event to warehouse service for order: {}", orderId);

            // 4. General Order Events Queue - Track cancellation
            rabbitTemplate.convertAndSend("order.events.exchange", "order.lifecycle", event);
            logger.info("📋 Published ORDER_CANCELLATION_LIFECYCLE event for order: {}", orderId);

        } catch (Exception e) {
            logger.error("Failed to publish order cancellation event: ", e);
            // Don't fail the cancellation if messaging fails
        }
    }

    // Get all orders for a specific client
    @GetMapping("/clients/{clientId}/orders")
    public ResponseEntity<Map<String, Object>> getOrdersByClient(@PathVariable("clientId") String clientId) {
        logger.info("ESB received request to get all orders for client: {}", clientId);

        try {
            Map<String, Object> response = new HashMap<>();

            // Get orders from CMS (primary source)
            List<Map<String, Object>> cmsOrders = cmsService.getOrdersByClient(clientId);

            // Enhance with data from ROS and WMS
            List<Map<String, Object>> enrichedOrders = enrichOrdersWithSystemData(cmsOrders);

            response.put("success", true);
            response.put("clientId", clientId);
            response.put("orders", enrichedOrders);
            response.put("totalOrders", enrichedOrders.size());
            response.put("dataSource", "CMS with ROS/WMS enrichment");
            response.put("timestamp", System.currentTimeMillis());

            logger.info("Successfully retrieved {} orders for client: {}", enrichedOrders.size(), clientId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting orders for client {}: ", clientId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("clientId", clientId);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // Helper method to enrich orders with ROS and WMS data
    private List<Map<String, Object>> enrichOrdersWithSystemData(List<Map<String, Object>> orders) {
        List<Map<String, Object>> enrichedOrders = new ArrayList<>();

        for (Map<String, Object> order : orders) {
            Map<String, Object> enrichedOrder = new HashMap<>(order);
            String orderId = (String) order.get("orderId");

            try {
                // Get route information from ROS
                String routeStatus = rosService.getRouteStatus(orderId);
                enrichedOrder.put("routeStatus", routeStatus);

                // Get package information from WMS
                String packageStatus = wmsService.getPackageStatus(orderId);
                enrichedOrder.put("packageStatus", packageStatus);

                // Add comprehensive status
                enrichedOrder.put("overallStatus", determineOverallStatus(
                        (String) order.get("status"), routeStatus, packageStatus));

            } catch (Exception e) {
                logger.debug("Failed to enrich order {} with system data: {}", orderId, e.getMessage());
                // Add default values if enrichment fails
                enrichedOrder.put("routeStatus", "unknown");
                enrichedOrder.put("packageStatus", "unknown");
                enrichedOrder.put("overallStatus", order.get("status"));
            }

            enrichedOrders.add(enrichedOrder);
        }

        return enrichedOrders;
    }

    // Helper method to determine overall order status
    private String determineOverallStatus(String cmsStatus, String routeStatus, String packageStatus) {
        // Logic to combine statuses from different systems
        if ("delivered".equalsIgnoreCase(packageStatus) || "completed".equalsIgnoreCase(routeStatus)) {
            return "DELIVERED";
        } else if ("in_progress".equalsIgnoreCase(routeStatus) || "LOADED".equalsIgnoreCase(packageStatus)) {
            return "IN_TRANSIT";
        } else if ("confirmed".equalsIgnoreCase(cmsStatus) && "READY_FOR_LOADING".equalsIgnoreCase(packageStatus)) {
            return "READY_FOR_DISPATCH";
        } else if ("processing".equalsIgnoreCase(cmsStatus) || "PROCESSING".equalsIgnoreCase(packageStatus)) {
            return "PROCESSING";
        } else if ("pending".equalsIgnoreCase(cmsStatus)) {
            return "PENDING";
        } else if ("CANCELLED".equalsIgnoreCase(cmsStatus) || "cancelled".equalsIgnoreCase(routeStatus)) {
            return "CANCELLED";
        } else {
            return "UNKNOWN";
        }
    }

}