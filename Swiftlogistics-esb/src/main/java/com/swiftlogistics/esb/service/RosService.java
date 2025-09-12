package com.swiftlogistics.esb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

@Service
public class RosService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String ROS_API_URL = "http://localhost:5002/api/v1";
    private static final Logger logger = LoggerFactory.getLogger(RosService.class);

    public RosService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public String optimizeRoute(String address) {
        try {
            logger.info("Optimizing route for address: {}", address);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("vehicle_id", "VEH001");
            requestBody.put("delivery_addresses", createDeliveryAddresses(address));
            requestBody.put("priority", "normal");
            logger.info("Request Body: {} ", requestBody);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(ROS_API_URL + "/routes/optimize", request, String.class);

            return extractRouteInfo(response);
        } catch (Exception e) {
            logger.error("Error optimizing route: ", e);
            return "Error optimizing route: " + e.getMessage();
        }
    }

    public String createOptimizedRoute(String deliveryAddress, String orderId) {
        try {
            logger.info("Creating optimized route for order: {} to {}", orderId, deliveryAddress);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("vehicle_id", "VEH001");
            requestBody.put("delivery_addresses", createDeliveryAddressesWithOrderId(deliveryAddress, orderId));
            requestBody.put("priority", "normal");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(ROS_API_URL + "/routes/optimize", request, String.class);

            return extractRouteId(response);
        } catch (Exception e) {
            logger.error("Error creating optimized route: ", e);
            return "Error creating route: " + e.getMessage();
        }
    }

    // Private helper methods
    private Object[] createDeliveryAddresses(String address) {
        Map<String, Object> deliveryAddress = new HashMap<>();
        deliveryAddress.put("address", address);
        deliveryAddress.put("lat", 6.9271); // Default Colombo coordinates
        deliveryAddress.put("lng", 79.8612);
        deliveryAddress.put("order_id", "ORD" + System.currentTimeMillis());

        return new Object[] { deliveryAddress };
    }

    private String extractRouteInfo(String jsonResponse) {
        try {
            Map<String, Object> response = objectMapper.readValue(jsonResponse,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                    });
            if (response.containsKey("route_id")) {
                return "Route optimized: " + response.get("route_id");
            }
            return "Route optimization completed";
        } catch (Exception e) {
            return "Route processed";
        }
    }

    private Object[] createDeliveryAddressesWithOrderId(String address, String orderId) {
        Map<String, Object> deliveryAddress = new HashMap<>();
        deliveryAddress.put("address", address);
        deliveryAddress.put("lat", 6.9271);
        deliveryAddress.put("lng", 79.8612);
        deliveryAddress.put("order_id", orderId);

        return new Object[] { deliveryAddress };
    }

    private String extractRouteId(String jsonResponse) {
        try {
            Map<String, Object> response = objectMapper.readValue(jsonResponse,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                    });
            if (response.containsKey("route_id")) {
                return (String) response.get("route_id");
            }
            return "RT" + System.currentTimeMillis();
        } catch (Exception e) {
            return "RT" + System.currentTimeMillis();
        }
    }
}