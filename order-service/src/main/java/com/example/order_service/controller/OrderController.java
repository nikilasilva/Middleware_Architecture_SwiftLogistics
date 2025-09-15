package com.example.order_service.controller;

import com.example.order_service.client.EsbClient;
import com.example.order_service.model.CreateOrderRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//
@RestController
//@CrossOrigin(origins = "http://192.168.56.1:3000")
@RequestMapping("/api/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private EsbClient esbClient;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody CreateOrderRequest request) {
        logger.info("Order Service received request: {}", request);

        try {
            // Generate unique order ID
            String orderId = request.getOrderId();

            // Prepare request for ESB
            Map<String, Object> esbRequest = new HashMap<>();
            esbRequest.put("orderId", orderId);
            esbRequest.put("clientId", request.getClientId());
            esbRequest.put("deliveryAddress", request.getDeliveryAddress());
            esbRequest.put("pickupAddress", request.getPickupAddress());
            esbRequest.put("recipientName", request.getRecipientName());
            esbRequest.put("recipientPhone", request.getRecipientPhone());
            esbRequest.put("items", request.getItems());
            esbRequest.put("notes", request.getNotes());
            esbRequest.put("totalWeight", request.getTotalWeight());
            esbRequest.put("totalItems", request.getTotalItems());

            // Generate package details from items
            String packageDetails = generatePackageDetails(request.getItems());
            esbRequest.put("packageDetails", packageDetails);

            // Call ESB Service through Feign Client
            logger.info("Calling ESB Service with order: {} containing {} items ({}kg total)",
                    orderId, request.getTotalItems(), request.getTotalWeight());

            ResponseEntity<Map<String, Object>> esbResponse = esbClient.processOrder(esbRequest);

            // Get response body
            Map<String, Object> responseBody = esbResponse.getBody();

            // Add Order Service info
            if (responseBody != null) {
                responseBody.put("processedBy", "order-service");
                responseBody.put("timestamp", System.currentTimeMillis());
                responseBody.put("itemsSummary",
                        request.getTotalItems() + " items, " + request.getTotalWeight() + "kg total");
            }

            logger.info("ESB processing completed for order: {} with {} items", orderId, request.getTotalItems());
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

    //method 6 : route optimization
    // Add this method to your OrderController class
    @PostMapping("/routes/optimize")
    public ResponseEntity<Map<String, Object>> optimizeRoute(@RequestBody Map<String, Object> routeRequest) {
        logger.info("Order Service received route optimization request: {}", routeRequest);

        try {
            // Call ESB route optimization through Feign Client
            ResponseEntity<Map<String, Object>> esbResponse = esbClient.optimizeRoute(routeRequest);
            Map<String, Object> responseBody = esbResponse.getBody();

            // Add Order Service metadata
            if (responseBody != null) {
                responseBody.put("routedBy", "order-service");
                responseBody.put("routeRequestTime", System.currentTimeMillis());
            }

            logger.info("Successfully optimized route via ESB");
            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            logger.error("Failed to optimize route through ESB: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Route optimization failed: " + e.getMessage());
            errorResponse.put("routedBy", "order-service");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // method 7
    // NEW: Package tracking endpoint - Customer facing
    @GetMapping("/packages/{packageId}/track")
    public ResponseEntity<Map<String, Object>> trackPackage(@PathVariable String packageId) {
        logger.info("Order Service received package tracking request for package: {}", packageId);

        try {
            // Call ESB to get comprehensive package tracking information
            ResponseEntity<Map<String, Object>> esbResponse = esbClient.trackPackage(packageId);
            Map<String, Object> trackingData = esbResponse.getBody();

            // Enhance response with Order Service metadata
            if (trackingData != null) {
                trackingData.put("servicedBy", "order-service");
                trackingData.put("trackingRequestTime", System.currentTimeMillis());

                // Add customer-friendly status interpretation
                String customerStatus = interpretTrackingStatus(trackingData);
                trackingData.put("customerFriendlyStatus", customerStatus);

                // Add estimated delivery if available
                String estimatedDelivery = extractEstimatedDelivery(trackingData);
                if (estimatedDelivery != null) {
                    trackingData.put("estimatedDelivery", estimatedDelivery);
                }
            }

            logger.info("Successfully retrieved tracking information for package: {}", packageId);
            return ResponseEntity.ok(trackingData);

        } catch (Exception e) {
            logger.error("Failed to track package {} through ESB: ", packageId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("packageId", packageId);
            errorResponse.put("error", "Package tracking failed: " + e.getMessage());
            errorResponse.put("servicedBy", "order-service");
            errorResponse.put("trackingRequestTime", System.currentTimeMillis());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    //method 6 route optimization


    // NEW: Track by Order ID (convenience method for customers)
    @GetMapping("/{orderId}/track")
    public ResponseEntity<Map<String, Object>> trackByOrderId(@PathVariable String orderId) {
        logger.info("Order Service received tracking request for order: {}", orderId);

        try {
            // Convert order ID to package ID (simple mapping)
            String packageId = convertOrderIdToPackageId(orderId);

            // Call the package tracking method
            return trackPackage(packageId);

        } catch (Exception e) {
            logger.error("Failed to track order {}: ", orderId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("orderId", orderId);
            errorResponse.put("error", "Order tracking failed: " + e.getMessage());
            errorResponse.put("servicedBy", "order-service");
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

    // method7
    // Private helper methods for package tracking
    private String interpretTrackingStatus(Map<String, Object> trackingData) {
        try {
            String warehouseInfo = (String) trackingData.get("warehouseInfo");
            String routeInfo = (String) trackingData.get("routeInfo");

            if (warehouseInfo != null && warehouseInfo.contains("Delivered")) {
                return "📦 Delivered - Your package has been successfully delivered!";
            } else if (routeInfo != null && routeInfo.contains("in_progress")) {
                return "🚚 Out for Delivery - Your package is on its way!";
            } else if (warehouseInfo != null && warehouseInfo.contains("Ready for Pickup")) {
                return "📍 Ready for Delivery - Your package is prepared for dispatch";
            } else if (warehouseInfo != null && warehouseInfo.contains("Processing")) {
                return "⚙️ Processing - Your package is being prepared";
            } else if (warehouseInfo != null && warehouseInfo.contains("In Transit")) {
                return "🔄 In Transit - Your package is moving through our network";
            } else {
                return "📋 Order Received - We're processing your order";
            }
        } catch (Exception e) {
            logger.debug("Error interpreting tracking status: ", e);
            return "📋 Tracking information available";
        }
    }

    private String extractEstimatedDelivery(Map<String, Object> trackingData) {
        try {
            String routeInfo = (String) trackingData.get("routeInfo");
            if (routeInfo != null && routeInfo.contains("ETA:")) {
                // Extract ETA from route info
                int etaIndex = routeInfo.indexOf("ETA:");
                if (etaIndex != -1) {
                    String eta = routeInfo.substring(etaIndex + 4).trim();
                    // Remove any trailing text after the date
                    if (eta.contains(",")) {
                        eta = eta.substring(0, eta.indexOf(","));
                    }
                    return eta;
                }
            }
        } catch (Exception e) {
            logger.debug("Error extracting estimated delivery: ", e);
        }
        return null;
    }

    private String convertOrderIdToPackageId(String orderId) {
        // Simple conversion logic - in real implementation this would query a database
        if (orderId.startsWith("ORD")) {
            return orderId.replace("ORD", "PKG");
        }
        return "PKG" + orderId;
    }

    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable String orderId) {
        logger.info("Order Service received cancellation request for order: {}", orderId);

        try {
            // Call ESB to cancel order across all systems
            ResponseEntity<Map<String, Object>> esbResponse = esbClient.cancelOrder(orderId);
            Map<String, Object> responseBody = esbResponse.getBody();

            // Add Order Service metadata
            if (responseBody != null) {
                responseBody.put("cancelledBy", "order-service");
                responseBody.put("cancellationTimestamp", System.currentTimeMillis());

                // Add customer-friendly message
                if (Boolean.TRUE.equals(responseBody.get("success"))) {
                    responseBody.put("customerMessage", "Your order has been successfully cancelled");
                }
            }

            logger.info("Successfully cancelled order: {}", orderId);
            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            logger.error("Failed to cancel order {} through ESB: ", orderId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("orderId", orderId);
            errorResponse.put("error", "Order cancellation failed: " + e.getMessage());
            errorResponse.put("cancelledBy", "order-service");
            errorResponse.put("cancellationTimestamp", System.currentTimeMillis());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // Helper method to generate package details from items
    private String generatePackageDetails(List<CreateOrderRequest.OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return "Package contents not specified";
        }

        StringBuilder details = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            CreateOrderRequest.OrderItem item = items.get(i);
            details.append(String.format("#%d: %s (ID: %s, Qty: %d, Weight: %.1fkg)",
                    i + 1, item.getDescription(), item.getItemId(), item.getQuantity(), item.getWeightKg()));
            if (i < items.size() - 1) {
                details.append("; ");
            }
        }
        return details.toString();
    }
}
