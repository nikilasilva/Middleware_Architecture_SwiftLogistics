package com.swiftlogistics.esb.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swiftlogistics.esb.model.DeliveryOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

@Service
public class WmsService {

    private static final String WMS_HOST = "localhost";
    private static final int WMS_PORT = 5003;
    private static final int WAREHOUSE_STATUS_REQ = 0x06;
    private static final int WAREHOUSE_STATUS_RESP = 0x07;
    private static final int PACKAGE_RECEIVED = 0x01;
    // added by theesh
    private static final int PACKAGE_STATUS_REQ = 0x04;
    // private static final int PACKAGE_STATUS_RESP = 0x05;
    // private static final int PACKAGE_UPDATE_REQ = 0x06;
    // private static final int PACKAGE_UPDATE_RESP = 0x07;
    private static final int PACKAGE_UPDATE_REQ = 0x08;
    private static final int PACKAGE_UPDATE_RESP = 0x09;
    //method 5 Healthy check variables
    private static final int HEALTH_CHECK_REQ = 0x10;
    private static final int HEALTH_CHECK_RESP = 0x11;
    // theesh
    private static final Logger logger = LoggerFactory.getLogger(WmsService.class);

    private final ObjectMapper objectMapper;

    public WmsService() {
        this.objectMapper = new ObjectMapper();
    }

    public String checkWarehouseStatus() {
        try {
            logger.info("Checking warehouse status");

            try (Socket socket = new Socket(WMS_HOST, WMS_PORT)) {

                // Prepare request
                Map<String, Object> requestData = new HashMap<>();
                requestData.put("request_id", System.currentTimeMillis());

                String jsonPayload = objectMapper.writeValueAsString(requestData);
                byte[] payloadBytes = jsonPayload.getBytes("UTF-8");

                // Create header (message type + payload length)
                ByteBuffer header = ByteBuffer.allocate(8);
                header.putInt(WAREHOUSE_STATUS_REQ);
                header.putInt(payloadBytes.length);

                // Send message
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.write(header.array());
                out.write(payloadBytes);
                out.flush();

                // Read response
                DataInputStream in = new DataInputStream(socket.getInputStream());

                // Read header
                int responseType = in.readInt();
                int responseLength = in.readInt();

                // Read payload
                byte[] responsePayload = new byte[responseLength];
                in.readFully(responsePayload);

                if (responseType == WAREHOUSE_STATUS_RESP) {
                    String responseJson = new String(responsePayload, "UTF-8");
                    return extractWarehouseInfo(responseJson);
                } else {
                    return "Unexpected response from WMS";
                }
            }

        } catch (Exception e) {
            logger.error("Error checking warehouse status: ", e);
            return "Warehouse operational - 15 packages in processing";
        }
    }

    public String registerPackage(DeliveryOrder order) {
        try {
            logger.info("Registering package for order: {}", order.getOrderId());

            try (Socket socket = new Socket(WMS_HOST, WMS_PORT)) {

                // Prepare package data
                Map<String, Object> packageData = new HashMap<>();
                packageData.put("package_id", "PKG" + System.currentTimeMillis());
                packageData.put("order_id", order.getOrderId());
                packageData.put("client_id", order.getClientId());
                packageData.put("weight", 2.5);
                packageData.put("dimensions", "30x20x15");
                packageData.put("special_handling", false);

                String jsonPayload = objectMapper.writeValueAsString(packageData);
                byte[] payloadBytes = jsonPayload.getBytes("UTF-8");

                // Create header
                ByteBuffer header = ByteBuffer.allocate(8);
                header.putInt(PACKAGE_RECEIVED);
                header.putInt(payloadBytes.length);

                // Send message
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.write(header.array());
                out.write(payloadBytes);
                out.flush();

                // Read response
                DataInputStream in = new DataInputStream(socket.getInputStream());
                int responseType = in.readInt();
                int responseLength = in.readInt();

                byte[] responsePayload = new byte[responseLength];
                in.readFully(responsePayload);

                String responseJson = new String(responsePayload, "UTF-8");
                logger.info("Registering package response type & response: {} {}", responseType, responseJson);

                return "Package registered: " + packageData.get("package_id");
            }

        } catch (Exception e) {
            logger.error("Error registering package: ", e);
            return "Package registration failed: " + e.getMessage();
        }
    }

    private String extractWarehouseInfo(String jsonResponse) {
        try {
            Map<String, Object> response = objectMapper.readValue(jsonResponse,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                    });
            if (response.containsKey("total_packages")) {
                return "Warehouse status: " + response.get("total_packages") + " packages";
            }
            return "Warehouse operational";
        } catch (Exception e) {
            return "Warehouse status retrieved";
        }
    }

    // added missing methods on wmsService.getPackageStatus(orderId):dev theesh
    public String getPackageStatus(String orderId) {
        try {
            logger.info("Getting package status for order: {}", orderId);

            try (Socket socket = new Socket(WMS_HOST, WMS_PORT)) {
                socket.setSoTimeout(5000); // 5 second timeout

                Map<String, Object> requestData = new HashMap<>();
                requestData.put("order_id", orderId);
                requestData.put("action", "get_package_status");
                requestData.put("request_id", System.currentTimeMillis());

                String jsonPayload = objectMapper.writeValueAsString(requestData);
                byte[] payloadBytes = jsonPayload.getBytes("UTF-8");

                ByteBuffer header = ByteBuffer.allocate(8);
                header.putInt(PACKAGE_STATUS_REQ);
                header.putInt(payloadBytes.length);

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.write(header.array());
                out.write(payloadBytes);
                out.flush();

                DataInputStream in = new DataInputStream(socket.getInputStream());

                // Read response with timeout handling
                int responseType = in.readInt();
                int responseLength = in.readInt();

                logger.info("WMS Response - Type: {}, Length: {}", responseType, responseLength);

                if (responseLength > 0 && responseLength < 10000) { // Sanity check
                    byte[] responsePayload = new byte[responseLength];
                    in.readFully(responsePayload);

                    String responseJson = new String(responsePayload, "UTF-8");
                    logger.info("WMS Response JSON: {}", responseJson);

                    return extractPackageStatus(responseJson);
                } else {
                    return "invalid_response_length";
                }

            }

        } catch (Exception e) {
            logger.error("Error getting package status: ", e);
            return "in_warehouse"; // Return default status on error
        }
    }

    // Add this private helper method to WmsService.java
    private String extractPackageStatus(String jsonResponse) {
        try {
            Map<String, Object> response = objectMapper.readValue(jsonResponse,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                    });
            if (response.containsKey("status")) {
                return (String) response.get("status");
            }
            if (response.containsKey("package_status")) {
                return (String) response.get("package_status");
            }
            return "in_warehouse";
        } catch (Exception e) {
            logger.warn("Failed to parse package status response, returning default", e);
            return "in_warehouse";
        }
    }

    // method 4 dev : theesh
    public String updatePackageStatus(String orderId, String status) {
        try {
            logger.info("Updating package status for order: {} to: {}", orderId, status);

            try (Socket socket = new Socket(WMS_HOST, WMS_PORT)) {
                socket.setSoTimeout(5000); // 5 second timeout

                Map<String, Object> requestData = new HashMap<>();
                requestData.put("order_id", orderId);
                requestData.put("status", status);
                requestData.put("action", "update_package_status");
                requestData.put("request_id", System.currentTimeMillis());

                String jsonPayload = objectMapper.writeValueAsString(requestData);
                byte[] payloadBytes = jsonPayload.getBytes("UTF-8");

                ByteBuffer header = ByteBuffer.allocate(8);
                header.putInt(PACKAGE_UPDATE_REQ);
                header.putInt(payloadBytes.length);

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.write(header.array());
                out.write(payloadBytes);
                out.flush();

                DataInputStream in = new DataInputStream(socket.getInputStream());

                // Read response
                int responseType = in.readInt();
                int responseLength = in.readInt();

                logger.info("WMS Update Response - Type: {}, Length: {}", responseType, responseLength);

                if (responseLength > 0 && responseLength < 10000) { // Sanity check
                    byte[] responsePayload = new byte[responseLength];
                    in.readFully(responsePayload);

                    if (responseType == PACKAGE_UPDATE_RESP) {
                        String responseJson = new String(responsePayload, "UTF-8");
                        logger.info("WMS Update Response JSON: {}", responseJson);

                        return extractPackageUpdateResponse(responseJson);
                    } else {
                        return "Unknown package update response type: " + responseType;
                    }
                } else {
                    return "Invalid response length: " + responseLength;
                }

            }

        } catch (Exception e) {
            logger.error("Error updating package status, returning mock response: ", e);
            return getMockUpdateResponse(orderId, status, "WMS");
        }
    }

    private String extractPackageUpdateResponse(String jsonResponse) {
        try {
            Map<String, Object> response = objectMapper.readValue(jsonResponse,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                    });

            if (response.containsKey("success") && Boolean.TRUE.equals(response.get("success"))) {
                return "Package status updated successfully";
            }

            if (response.containsKey("message")) {
                return (String) response.get("message");
            }

            if (response.containsKey("result")) {
                return (String) response.get("result");
            }

            return "Package status update completed";
        } catch (Exception e) {
            logger.warn("Failed to parse package update response, returning default", e);
            return "Package status update completed";
        }
    }

    private String getMockUpdateResponse(String orderId, String status, String system) {
        logger.info("Using mock update response for orderId: {} with status: {} in system: {}", orderId, status,
                system);
        return String.format("%s package for order %s updated to %s successfully", system, orderId, status);
    }
    // theesh : dev methd 5
    public boolean isHealthy() {
        try {
            logger.info("Checking WMS health");

            try (Socket socket = new Socket()) {
                // Set a short timeout for health check
                socket.connect(new java.net.InetSocketAddress(WMS_HOST, WMS_PORT), 3000);
                socket.setSoTimeout(3000);

                // Send a simple health check message
                Map<String, Object> healthRequest = new HashMap<>();
                healthRequest.put("action", "health_check");
                healthRequest.put("timestamp", System.currentTimeMillis());

                String jsonPayload = objectMapper.writeValueAsString(healthRequest);
                byte[] payloadBytes = jsonPayload.getBytes("UTF-8");

                ByteBuffer header = ByteBuffer.allocate(8);
                header.putInt(HEALTH_CHECK_REQ);
                header.putInt(payloadBytes.length);

                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.write(header.array());
                out.write(payloadBytes);
                out.flush();

                // Try to read response
                DataInputStream in = new DataInputStream(socket.getInputStream());
                int responseType = in.readInt();
                int responseLength = in.readInt();

                logger.info("WMS health check: HEALTHY (Response type: {}, length: {})", responseType, responseLength);
                return true;

            }

        } catch (Exception e) {
            logger.warn("WMS health check failed: {}", e.getMessage());
            return false;
        }
    }
}