package com.example.order_service.model;

public class CreateOrderRequest {
    private String clientId;
    private String pickupAddress;
    private String deliveryAddress;
    private String packageDetails;
    private String orderId;
    
    // Default constructor
    public CreateOrderRequest() {}
    
    // Getters and setters
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    
    public String getPackageDetails() { return packageDetails; }
    public void setPackageDetails(String packageDetails) { this.packageDetails = packageDetails; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    @Override
    public String toString() {
        return "CreateOrderRequest{" +
                "clientId='" + clientId + '\'' +
                ", pickupAddress='" + pickupAddress + '\'' +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", packageDetails='" + packageDetails + '\'' +
                '}';
    }
}