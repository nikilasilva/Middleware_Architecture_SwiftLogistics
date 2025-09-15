package com.example.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "esb-service") // This matches your ESB's spring.application.name
public interface EsbClient {

        // Call your existing ESB endpoints
        @GetMapping("/esb/processOrder")
        String processOrderSimple(@RequestParam("clientId") String clientId,
                        @RequestParam("address") String address);

        // @PostMapping("/orders")
        // ResponseEntity<Map<String, Object>> processOrder(@RequestBody Map<String,
        // Object> order);

        @PostMapping("/orders/map")
        ResponseEntity<Map<String, Object>> processOrder(@RequestBody Map<String, Object> order);

        // NEW: Get order status from ESB
        @GetMapping("/orders/{orderId}/status")
        ResponseEntity<Map<String, Object>> getOrderStatus(@PathVariable("orderId") String orderId);

        // NEW: Update order status in ESB
        @PutMapping("/orders/{orderId}/status")
        ResponseEntity<Map<String, Object>> updateOrderStatus(
                        @PathVariable("orderId") String orderId,
                        @RequestParam("status") String status,
                        @RequestParam(value = "system", required = false) String system);

        // NEW: Check ESB health
        @GetMapping("/health")
        ResponseEntity<Map<String, Object>> checkHealth();

        // method7
        // NEW: Package tracking through ESB
        @GetMapping("/packages/{packageId}/track")
        ResponseEntity<Map<String, Object>> trackPackage(@PathVariable("packageId") String packageId);

}
