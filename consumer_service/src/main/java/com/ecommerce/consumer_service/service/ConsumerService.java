package com.ecommerce.consumer_service.service;

import com.ecommerce.consumer_service.model.ConsumerModel;
import com.ecommerce.consumer_service.repository.ConsumerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ConsumerService {

    @Autowired
    private ConsumerRepository repo;

    @Autowired
    private RestTemplate restTemplate;

    // Microservice URLs
    private String product_service_url = "http://localhost:8092/";
    private String order_service_url = "http://localhost:8093/orders";

    // Register
    public ConsumerModel registerConsumer(ConsumerModel consumer) {
        Optional<ConsumerModel> existing = repo.findByUsername(consumer.getUsername());
        if (existing.isPresent()) {
            throw new RuntimeException("Username already exists.");
        }
        consumer.setRole("Consumer");
        return repo.save(consumer);
    }

    // Login
    public String login(String username, String password) {
        Optional<ConsumerModel> existing = repo.findByUsername(username);
        if (existing.isPresent()) {
            ConsumerModel consumer = existing.get();
            
            if(consumer.getPassword().equals(password)){
            return "Login successful";
            }
            else return "Invalid Credentials";
        }
        return "Consumer doesnot exist";
    }

    // Get all consumers
    public List<ConsumerModel> getAllConsumers() {
        return repo.findAll();
    }

    // Get consumer by username
    public ConsumerModel getConsumerByUsername(String username) {
        return repo.findByUsername(username)
                   .orElseThrow(() -> new RuntimeException("Consumer does not exist."));
    }

    // Update consumer details (PATCH)
    public ConsumerModel updateConsumer(String username, Map<String, Object> updatedConsumer) {
        ConsumerModel existing = repo.findByUsername(username)
                                     .orElseThrow(() -> new RuntimeException("Consumer not found"));
        for (Map.Entry<String, Object> entry : updatedConsumer.entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();
            switch (field) {
                case "username":
                    existing.setUsername((String) value);
                    break;
                case "password":
                    existing.setPassword((String) value);
                    break;
                case "location":
                    existing.setLocation((String) value);
                    break;
                case "role":
                    throw new RuntimeException("Role cannot be changed.");
                default:
                    throw new RuntimeException("Invalid Field: " + field);
            }
        }
        return repo.save(existing);
    }

    // Delete consumer
    public void deleteConsumer(int id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Consumer does not exist.");
        }
        repo.deleteById(id);
    }

    // Get products for consumer (all products they can see/buy)
    public List<Map<String, Object>> getAllProducts() {
        List<Map<String, Object>> products = restTemplate.getForObject(product_service_url + "products", List.class);
        if (products == null || products.isEmpty()) {
            throw new RuntimeException("No products available.");
        }
        return products;
    }

    // Get a specific product by ID
    public Map<String, Object> getProductById(int productId) {
        return restTemplate.getForObject(product_service_url + "products/" + productId, Map.class);
    }

    // Place an order and update stock in Product Service
public Map<String, Object> placeOrder(String username, Map<String, Object> orderDetails) {
    // Add consumer username
    orderDetails.put("consumerUsername", username);

    // Call Order Service to create a new order
    Map<String, Object> createdOrder = restTemplate.postForObject(
            order_service_url + "/placeOrder",  // existing order API
            orderDetails,
            Map.class
    );

    if (createdOrder == null || createdOrder.isEmpty()) {
        throw new RuntimeException("Failed to place order.");
    }

    // Extract productId and ordered quantity
    Integer productId = (Integer) orderDetails.get("productId");
    Integer orderedQty = (Integer) orderDetails.get("quantity");

    // Call Product Service to reduce stock
    try {
        restTemplate.put(
            product_service_url + "products/updateQuantity/" + productId + "?orderedQty=" + orderedQty,
            null
        );
        System.out.println("Stock updated successfully in Product Service.");
    } catch (Exception e) {
        System.err.println("Failed to update stock in Product Service: " + e.getMessage());
    }

    return createdOrder;
}


    // Get orders placed by the consumer
    public List<Map<String, Object>> getOrdersByConsumer(String username) {
        List<Map<String, Object>> orders = restTemplate.getForObject(
            order_service_url + "/consumer/" + username, List.class
        );
        if (orders == null || orders.isEmpty()) {
            throw new RuntimeException("No orders found for consumer: " + username);
        }
        return orders;
    }

    // Get a specific order by ID
    public Map<String, Object> getOrderById(String username, int orderId) {
    Map<String, Object> order = restTemplate.getForObject(
        order_service_url + "/" + orderId, Map.class
    );
    if (order == null || order.isEmpty()) {
        throw new RuntimeException("Order not found with ID: " + orderId);
    }
    return order;
}
}
