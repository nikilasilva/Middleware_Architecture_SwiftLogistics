package com.swiftlogistics.esb.model;

import java.util.*;

public class DeliveryOrder {
    private String orderId;
    private String clientId;
    private String pickupAddress;
    private String deliveryAddress;
    private String recipientName;
    private String recipientPhone;
    private List<OrderItem> items;
    private String notes;
    private double totalWeight;
    private int totalItems;
    private Map<String, Object> metadata;

    // Default constructor
    public DeliveryOrder() {
    }

    // Getters and setters
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

    public double getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(double totalWeight) {
        this.totalWeight = totalWeight;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    @Override
    public String toString() {
        return "DeliveryOrder{" +
                "orderId='" + orderId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", recipientName='" + recipientName + '\'' +
                ", items=" + items +
                ", totalWeight=" + totalWeight + "kg" +
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