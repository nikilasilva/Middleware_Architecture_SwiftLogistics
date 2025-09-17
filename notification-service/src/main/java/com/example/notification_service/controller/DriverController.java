package com.example.notification_service.controller;

import com.example.notification_service.service.DriverOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/driver")
@CrossOrigin(origins = "*")
public class DriverController {

    private static final Logger logger = LoggerFactory.getLogger(DriverController.class);

    @Autowired
    private DriverOrderService driverOrderService;

    @GetMapping("/{driverId}/orders")
    public ResponseEntity<Map<String, Object>> getDriverOrders(@PathVariable String driverId) {
        logger.info("Getting orders for driver: {}", driverId);

        try {
            List<Map<String, Object>> orders = driverOrderService.getOrdersForDriver(driverId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("driverId", driverId);
            response.put("orders", orders);
            response.put("totalOrders", orders.size());
            response.put("timestamp", System.currentTimeMillis());
            response.put("source", "notification-service");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting driver orders: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable String orderId,
            @RequestParam String status,
            @RequestParam String driverId) {

        logger.info("Driver {} updating order {} to status {}", driverId, orderId, status);

        try {
            Map<String, Object> result = driverOrderService.updateOrderStatus(orderId, status, driverId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error updating order status: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}