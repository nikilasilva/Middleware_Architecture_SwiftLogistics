package com.example.order_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "esb-service") // This matches your ESB's spring.application.name
public interface EsbClient {
    
    // Call your existing ESB endpoints
    @GetMapping("/esb/processOrder")
    String processOrderSimple(@RequestParam("clientId") String clientId, 
                            @RequestParam("address") String address);
    
    @PostMapping("/orders")
    ResponseEntity<Map<String, Object>> processOrder(@RequestBody Map<String, Object> order);
}
