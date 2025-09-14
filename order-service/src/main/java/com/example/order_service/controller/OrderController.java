package com.example.order_service.controller;

import com.example.order_service.client.EsbClient;
import com.example.order_service.model.CreateOrderRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private EsbClient esbClient;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody CreateOrderRequest request) {
        logger.info("Order Service received request: {}", request);

        try {
            // Generate unique order ID
            String orderId = "ORD" + System.currentTimeMillis();

            // Prepare request for ESB
            Map<String, Object> esbRequest = new HashMap<>();
            esbRequest.put("orderId", orderId);
            esbRequest.put("clientId", request.getClientId());
            esbRequest.put("deliveryAddress", request.getDeliveryAddress());
            esbRequest.put("pickupAddress", request.getPickupAddress());
            esbRequest.put("packageDetails", request.getPackageDetails());

            // Call ESB Service through Feign Client
            logger.info("Calling ESB Service with order: {}", orderId);
            ResponseEntity<Map<String, Object>> esbResponse = esbClient.processOrder(esbRequest);

            // Get response body
            Map<String, Object> responseBody = esbResponse.getBody();

            // Add Order Service info
            if (responseBody != null) {
                responseBody.put("processedBy", "order-service");
                responseBody.put("timestamp", System.currentTimeMillis());
            }

            logger.info("ESB processing completed for order: {}", orderId);
            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            logger.error("Failed to process order through ESB: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Order processing failed: " + e.getMessage());
            errorResponse.put("processedBy", "order-service");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // NEW: Get order status through ESB
    @GetMapping("/{orderId}/status")
    public ResponseEntity<Map<String, Object>> getOrderStatus(@PathVariable String orderId) {
        logger.info("Order Service getting status for order: {}", orderId);

        try {
            // Call ESB to get order status from all systems
            ResponseEntity<Map<String, Object>> esbResponse = esbClient.getOrderStatus(orderId);
            Map<String, Object> responseBody = esbResponse.getBody();

            // Add Order Service metadata
            if (responseBody != null) {
                responseBody.put("queriedBy", "order-service");
                responseBody.put("queryTimestamp", System.currentTimeMillis());
            }

            logger.info("Successfully retrieved status for order: {}", orderId);
            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            logger.error("Failed to get order status through ESB for order {}: ", orderId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("orderId", orderId);
            errorResponse.put("error", "Failed to retrieve order status: " + e.getMessage());
            errorResponse.put("queriedBy", "order-service");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // NEW: Update order status through ESB
    @PutMapping("/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam String status,
            @RequestParam(required = false) String system) {

        logger.info("Order Service updating status for order {} to {} in system {}", orderId, status, system);

        try {
            // Call ESB to update order status in specified systems
            ResponseEntity<Map<String, Object>> esbResponse = esbClient.updateOrderStatus(orderId, status, system);
            Map<String, Object> responseBody = esbResponse.getBody();

            // Add Order Service metadata
            if (responseBody != null) {
                responseBody.put("updatedBy", "order-service");
                responseBody.put("updateTimestamp", System.currentTimeMillis());
            }

            logger.info("Successfully updated status for order: {}", orderId);
            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            logger.error("Failed to update order status through ESB for order {}: ", orderId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("orderId", orderId);
            errorResponse.put("error", "Failed to update order status: " + e.getMessage());
            errorResponse.put("updatedBy", "order-service");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // NEW: Health check endpoint
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        logger.info("Order Service health check - checking ESB connectivity");

        try {
            // Check ESB health
            ResponseEntity<Map<String, Object>> esbHealthResponse = esbClient.checkHealth();
            Map<String, Object> esbHealth = esbHealthResponse.getBody();

            // Create comprehensive health response
            Map<String, Object> healthResponse = new HashMap<>();
            healthResponse.put("orderService", "UP");
            healthResponse.put("esbConnectivity", "UP");
            healthResponse.put("esbHealth", esbHealth);
            healthResponse.put("timestamp", System.currentTimeMillis());

            // Determine overall health
            boolean esbHealthy = esbHealth != null && "UP".equals(esbHealth.get("overall"));
            healthResponse.put("overall", esbHealthy ? "UP" : "DOWN");

            logger.info("Health check completed - Overall status: {}", healthResponse.get("overall"));
            return ResponseEntity.ok(healthResponse);

        } catch (Exception e) {
            logger.error("Health check failed - ESB connectivity issue: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("orderService", "UP");
            errorResponse.put("esbConnectivity", "DOWN");
            errorResponse.put("overall", "DOWN");
            errorResponse.put("error", "ESB connectivity failed: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.status(503).body(errorResponse); // Service Unavailable
        }
    }

    // Test endpoint
    @GetMapping("/test/{clientId}")
    public ResponseEntity<String> testEsbConnection(@PathVariable String clientId) {
        logger.info("Testing ESB connection for client: {}", clientId);

        try {
            // Test simple ESB call
            String result = esbClient.processOrderSimple(clientId, "Test Address");
            return ResponseEntity.ok("ESB Test Result: " + result);
        } catch (Exception e) {
            logger.error("ESB connection test failed: ", e);
            return ResponseEntity.status(500).body("ESB Connection Failed: " + e.getMessage());
        }
    }
}
