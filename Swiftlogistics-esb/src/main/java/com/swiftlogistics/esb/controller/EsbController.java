package com.swiftlogistics.esb.controller;

import com.swiftlogistics.esb.model.DeliveryOrder;
import com.swiftlogistics.esb.service.CmsService;
import com.swiftlogistics.esb.service.RosService;
import com.swiftlogistics.esb.service.WmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error creating order: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // 10. Emergency order cancellation with smart ID mapping
    @DeleteMapping("/orders/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@RequestParam("orderId") String orderId) {

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

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error cancelling order: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}