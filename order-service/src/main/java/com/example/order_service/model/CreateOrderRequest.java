package com.example.order_service.model;

import java.util.List;

public class CreateOrderRequest {
    private String clientId;
    private String pickupAddress;
    private String deliveryAddress;
    private String packageDetails;
    private String orderId;
    private String recipientName;
    private String recipientPhone;
    private List<OrderItem> items;
    private String notes;

    // Default constructor
    public CreateOrderRequest() {
    }

    // Getters and setters
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

    public String getPackageDetails() {
        return packageDetails;
    }

    public void setPackageDetails(String packageDetails) {
        this.packageDetails = packageDetails;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Helper method to calculate total weight
    public double getTotalWeight() {
        return items != null ? items.stream().mapToDouble(item -> item.getWeightKg() * item.getQuantity()).sum() : 0.0;
    }

    // Helper method to get item count
    public int getTotalItems() {
        return items != null ? items.stream().mapToInt(OrderItem::getQuantity).sum() : 0;
    }

    @Override
    public String toString() {
        return "CreateOrderRequest{" +
                "orderId='" + orderId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", recipientName='" + recipientName + '\'' +
                ", pickupAddress='" + pickupAddress + '\'' +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", items=" + items +
                ", totalWeight=" + getTotalWeight() + "kg" +
                ", notes='" + notes + '\'' +
                '}';
    }

    // Inner class for order items
    public static class OrderItem {
        private String itemId;
        private String description;
        private int quantity;
        private double weightKg;

        // Default constructor
        public OrderItem() {
        }

        // Constructor
        public OrderItem(String itemId, String description, int quantity, double weightKg) {
            this.itemId = itemId;
            this.description = description;
            this.quantity = quantity;
            this.weightKg = weightKg;
        }

        // Getters and setters
        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getWeightKg() {
            return weightKg;
        }

        public void setWeightKg(double weightKg) {
            this.weightKg = weightKg;
        }

        @Override
        public String toString() {
            return "OrderItem{" +
                    "itemId='" + itemId + '\'' +
                    ", description='" + description + '\'' +
                    ", quantity=" + quantity +
                    ", weightKg=" + weightKg +
                    '}';
        }
    }
}