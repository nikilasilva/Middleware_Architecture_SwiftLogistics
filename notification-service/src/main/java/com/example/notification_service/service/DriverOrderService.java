package com.example.notification_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DriverOrderService {

    private static final Logger logger = LoggerFactory.getLogger(DriverOrderService.class);

    // In-memory storage for orders (in production, use a database)
    private final Map<String, Map<String, Object>> allOrders = new ConcurrentHashMap<>();
    private final Map<String, String> orderDriverAssignments = new ConcurrentHashMap<>();

    // Available drivers (in production, fetch from database/service)
    private final List<String> availableDrivers = Arrays.asList("DRIVER001", "DRIVER002", "DRIVER003");
    private int currentDriverIndex = 0;

    public void processNewOrder(Map<String, Object> orderData) {
        try {
            String orderId = (String) orderData.get("orderId");
            logger.info("Processing new order for driver assignment: {}", orderId);

            // Auto-assign driver (round-robin)
            String assignedDriver = assignDriverToOrder(orderId);

            // Enhance order data for driver
            Map<String, Object> driverOrder = new HashMap<>(orderData);
            driverOrder.put("assignedDriver", assignedDriver);
            driverOrder.put("driverStatus", "assigned");
            driverOrder.put("assignedAt", System.currentTimeMillis());

            // Store the order
            allOrders.put(orderId, driverOrder);

            logger.info("Order {} assigned to driver: {}", orderId, assignedDriver);

        } catch (Exception e) {
            logger.error("Error processing new order: ", e);
        }
    }

    public void updateOrderFromQueue(Map<String, Object> orderUpdate) {
        try {
            String orderId = (String) orderUpdate.get("orderId");
            String newStatus = (String) orderUpdate.get("status");

            if (allOrders.containsKey(orderId)) {
                Map<String, Object> order = allOrders.get(orderId);
                order.put("status", newStatus);
                order.put("lastUpdated", System.currentTimeMillis());

                // Map backend status to driver-friendly status
                String driverStatus = mapToDriverStatus(newStatus);
                order.put("driverStatus", driverStatus);

                logger.info("Updated order {} status to: {} (driver status: {})", orderId, newStatus, driverStatus);
            }
        } catch (Exception e) {
            logger.error("Error updating order from queue: ", e);
        }
    }

    public List<Map<String, Object>> getOrdersForDriver(String driverId) {
        return allOrders.values().stream()
                .filter(order -> driverId.equals(order.get("assignedDriver")))
                .filter(order -> !isOrderCompleted((String) order.get("driverStatus")))
                .sorted((a, b) -> Long.compare(
                        (Long) b.getOrDefault("assignedAt", 0L),
                        (Long) a.getOrDefault("assignedAt", 0L)))
                .collect(Collectors.toList());
    }

    public Map<String, Object> updateOrderStatus(String orderId, String newStatus, String driverId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Map<String, Object> order = allOrders.get(orderId);
            if (order == null) {
                throw new RuntimeException("Order not found: " + orderId);
            }

            if (!driverId.equals(order.get("assignedDriver"))) {
                throw new RuntimeException("Order not assigned to this driver");
            }

            // Update order status
            order.put("driverStatus", newStatus);
            order.put("lastUpdated", System.currentTimeMillis());
            order.put("lastUpdatedBy", driverId);

            response.put("success", true);
            response.put("orderId", orderId);
            response.put("newStatus", newStatus);
            response.put("driverId", driverId);
            response.put("message", "Order status updated successfully");

            logger.info("Driver {} updated order {} to status: {}", driverId, orderId, newStatus);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            logger.error("Error updating order status: ", e);
        }

        return response;
    }

    private String assignDriverToOrder(String orderId) {
        String driver = availableDrivers.get(currentDriverIndex);
        currentDriverIndex = (currentDriverIndex + 1) % availableDrivers.size();

        orderDriverAssignments.put(orderId, driver);
        return driver;
    }

    private String mapToDriverStatus(String backendStatus) {
        if (backendStatus == null)
            return "assigned";

        switch (backendStatus.toUpperCase()) {
            case "PENDING":
            case "CONFIRMED":
            case "READY_FOR_DISPATCH":
                return "assigned";
            case "IN_TRANSIT":
            case "LOADING":
            case "LOADED":
                return "in_transit";
            case "DELIVERED":
            case "COMPLETED":
                return "delivered";
            case "CANCELLED":
                return "cancelled";
            default:
                return "assigned";
        }
    }

    private boolean isOrderCompleted(String driverStatus) {
        return "delivered".equals(driverStatus) || "cancelled".equals(driverStatus);
    }

    // Get all orders for debugging
    public Map<String, Map<String, Object>> getAllOrders() {
        return new HashMap<>(allOrders);
    }
}