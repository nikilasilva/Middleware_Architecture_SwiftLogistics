package com.swiftlogistics.esb.model;

import java.util.Map;

public class DeliveryOrder {
    private String orderId;
    private String clientId;
    private String pickupAddress;
    private String deliveryAddress;
    private Map<String, Object> metadata;

    // Constructors
    public DeliveryOrder() {
    }

    public DeliveryOrder(String orderId, String clientId, String pickupAddress, String deliveryAddress) {
        this.orderId = orderId;
        this.clientId = clientId;
        this.pickupAddress = pickupAddress;
        this.deliveryAddress = deliveryAddress;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "DeliveryOrder{" +
                "orderId='" + orderId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", pickupAddress='" + pickupAddress + '\'' +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}