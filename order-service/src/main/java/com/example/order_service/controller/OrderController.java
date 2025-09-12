package com.example.order_service.controller;

import com.example.order_service.client.EsbClient;
import com.example.order_service.model.CreateOrderRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
