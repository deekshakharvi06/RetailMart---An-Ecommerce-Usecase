package com.ecommerce.consumer_service.controller;

import com.ecommerce.consumer_service.model.ConsumerModel;
import com.ecommerce.consumer_service.service.ConsumerService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/consumers")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173",allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class ConsumerController {

    private final ConsumerService consumerService;

    // ------------------ Consumer CRUD ------------------

    // Register consumer
    @PostMapping("/register")
    public ConsumerModel registerConsumer(@RequestBody ConsumerModel consumer) {
        return consumerService.registerConsumer(consumer);
    }

    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        if (username == null || password == null) {
            throw new RuntimeException("Username and password are required");
        }

        return consumerService.login(username, password);
    }

    // Get all consumers
    @GetMapping
    public List<ConsumerModel> getAllConsumers() {
        return consumerService.getAllConsumers();
    }

    // Get consumer by username
    @GetMapping("/{username}")
    public ResponseEntity<?> getConsumerByUsername(@PathVariable String username) {
        try {
            ConsumerModel consumer= consumerService.getConsumerByUsername(username);
            return ResponseEntity.ok(consumer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Consumer not found");
        }
    }

    // Update consumer (PATCH)
    @PatchMapping("/{username}")
    public ConsumerModel updateConsumer(@PathVariable String username,
                                        @RequestBody Map<String, Object> updatedConsumer) {
        return consumerService.updateConsumer(username, updatedConsumer);
    }

    // Delete consumer
    @DeleteMapping("/{id}")
    public String deleteConsumer(@PathVariable int id) {
        consumerService.deleteConsumer(id);
        return "Consumer deleted successfully.";
    }

    // ------------------ Product Endpoints ------------------

    // Get all products
    @GetMapping("/products")
    public List<Map<String, Object>> getAllProducts() {
        return consumerService.getAllProducts();
    }

    // Get product by ID
    @GetMapping("/products/{productId}")
    public Map<String, Object> getProductById(@PathVariable int productId) {
        return consumerService.getProductById(productId);
    }

    // ------------------ Order Endpoints ------------------

    // Place a new order
    @PostMapping("/{username}/orders/placeOrder")
    public Map<String, Object> placeOrder(@PathVariable String username,
                                          @RequestBody Map<String, Object> orderDetails) {
        return consumerService.placeOrder(username, orderDetails);
    }
    
    // Get all orders of a consumer
    @GetMapping("/{username}/orders")
    public List<Map<String, Object>> getOrdersByConsumer(@PathVariable String username) {
        return consumerService.getOrdersByConsumer(username);
    }

    // Get specific order of a consumer
    @GetMapping("/{username}/orders/{orderId}")
    public Map<String, Object> getOrderById(@PathVariable String username,
                                            @PathVariable int orderId) {
        return consumerService.getOrderById(username, orderId);
    }

}
