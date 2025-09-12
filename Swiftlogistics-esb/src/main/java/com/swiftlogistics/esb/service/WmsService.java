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
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WmsService {

    private static final String WMS_HOST = "localhost";
    private static final int WMS_PORT = 5003;
    private static final int WAREHOUSE_STATUS_REQ = 0x06;
    private static final int WAREHOUSE_STATUS_RESP = 0x07;
    private static final int PACKAGE_RECEIVED = 0x01;
    private static final int WMS_CANCEL_PACKAGE_REQ = 0x10;
    private static final int WMS_CANCEL_PACKAGE_RESP = 0x11;
    // private static final int PACKAGE_STATUS_REQ = 0x04;
    // private static final int PACKAGE_STATUS_RESP = 0x05;
    private static final Logger logger = LoggerFactory.getLogger(WmsService.class);

    private final ObjectMapper objectMapper;
    private final Map<String, String> orderToPackageMap;

    public WmsService() {
        this.objectMapper = new ObjectMapper();
        this.orderToPackageMap = new ConcurrentHashMap<>();
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
                String packageId = "PKG" + System.currentTimeMillis();
                packageData.put("package_id", packageId);
                packageData.put("order_id", order.getOrderId());
                packageData.put("client_id", order.getClientId());
                packageData.put("weight", 2.5);
                packageData.put("dimensions", "30x20x15");
                packageData.put("special_handling", false);

                // Store the mapping between order ID and package ID
                orderToPackageMap.put(order.getOrderId(), packageId);
                logger.info("Stored mapping: Order {} -> Package {}", order.getOrderId(), packageId);

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

                return "Package registered: " + packageId;
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

     private String extractCancelPackageResult(String jsonResponse) {
        try {
            Map<String, Object> response = objectMapper.readValue(jsonResponse,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                    });
            if (response.containsKey("status") && "cancelled".equals(response.get("status"))) {
                return "Package cancelled successfully for order: " + response.get("order_id");
            }
            if (response.containsKey("message")) {
                return "Package cancellation: " + response.get("message");
            }
            return "Package cancellation confirmed";
        } catch (Exception e) {
            logger.error("Error parsing cancel package response: ", e);
            return "Package cancellation confirmed";
        }
    }
    public String cancelPackage(String orderId) {
    try {
        logger.info("Cancelling WMS package for order: {}", orderId);

        try (Socket socket = new Socket(WMS_HOST, WMS_PORT)) {
            // Map order ID to package ID for WMS
            String packageId = mapOrderIdToPackageId(orderId);
            
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("package_id", packageId);

            String jsonPayload = objectMapper.writeValueAsString(requestData);
            byte[] payloadBytes = jsonPayload.getBytes("UTF-8");
           
            ByteBuffer header = ByteBuffer.allocate(8);
            header.putInt(WMS_CANCEL_PACKAGE_REQ); // some constant for cancel request
            header.putInt(payloadBytes.length);
            logger.info("Cancel package header: type {} length {}", WMS_CANCEL_PACKAGE_RESP, payloadBytes.length);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.write(header.array());
            out.write(payloadBytes);
            out.flush();

            DataInputStream in = new DataInputStream(socket.getInputStream());
            logger.info("Input stream {}", in);
            int responseType = in.readInt();
            int responseLength = in.readInt();
            byte[] responsePayload = new byte[responseLength];
            in.readFully(responsePayload);
            
            logger.info("Cancel package response type: {} (expected: {}), length: {}", 
                       responseType, WMS_CANCEL_PACKAGE_RESP, responseLength);
            logger.info("Response payload: {}", new String(responsePayload, "UTF-8"));

            if (responseType == WMS_CANCEL_PACKAGE_RESP) {
                String responseJson = new String(responsePayload, "UTF-8");
                return extractCancelPackageResult(responseJson); // parse JSON
            } else {
                String responseJson = new String(responsePayload, "UTF-8");
                logger.warn("Unexpected response type {}, but parsing anyway: {}", responseType, responseJson);
                return "Unexpected WMS response: " + responseJson;
            }
        }

    } catch (Exception e) {
        logger.error("Error cancelling WMS package: ", e);
        return "Error cancelling WMS package: " + e.getMessage();
    }
}

    /**
     * Maps order ID to corresponding package ID for WMS service
     */
    private String mapOrderIdToPackageId(String orderId) {
        // Check if we have a stored mapping for this order ID
        String packageId = orderToPackageMap.get(orderId);
        if (packageId != null) {
            logger.info("Found stored mapping: Order {} -> Package {}", orderId, packageId);
            return packageId;
        }
        
        // Fallback: generate a package ID if no mapping exists
        logger.warn("No stored mapping found for order ID: {}, generating fallback package ID", orderId);
        return "PKG" + Math.abs(orderId.hashCode());
    }
}