package com.ecommerce.order_service.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.ecommerce.order_service.model.OrderModel;
import com.ecommerce.order_service.repository.OrderRepository;

@Service
public class OrderService {

    @Autowired
    OrderRepository repo;

    @Autowired
    RestTemplate restTemplate;

    //External microservice URLs
    private final String CONSUMER_SERVICE_URL = "http://localhost:8090/consumers";
    private final String PRODUCT_SERVICE_URL = "http://localhost:8092/products";
    private final String LOG_SERVICE_URL = "http://localhost:8094/orderStatusLog/create";

    //Place an order
    public OrderModel placeOrder(OrderModel model) {
        try {
            //Validate consumer
            String consumerUrl = CONSUMER_SERVICE_URL + "/" + model.getConsumerUsername();
            restTemplate.getForObject(consumerUrl, Map.class); // Throws 404 if invalid consumer

            // Validate product
            String productUrl = PRODUCT_SERVICE_URL + "/" + model.getProductId();
            Map<String, Object> product = restTemplate.getForObject(productUrl, Map.class);

            if (product == null || product.get("price") == null) {
                throw new RuntimeException("Invalid product");
            }

            //Calculate total price
            BigDecimal price = new BigDecimal(product.get("price").toString());
            BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(model.getQuantity()));

            //Set fields and save
            model.setTotalAmount(totalAmount);
            model.setOrderDate(LocalDateTime.now());
            model.setStatus("Placed");

            OrderModel placedOrder = repo.save(model);

            // Auto log order status (Placed)
            restTemplate.postForObject(
                LOG_SERVICE_URL,
                Map.of(
                    "orderId", placedOrder.getOrderId(),
                    "oldStatus", "Placed",
                    "newStatus", "Placed"
                ),
                Void.class
            );

            return placedOrder;

        } catch (HttpClientErrorException.NotFound e) {
            throw new RuntimeException("Invalid Consumer or Product");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // Get all orders
    public List<OrderModel> getAllOrder(OrderModel order) {
        return repo.findAll();
    }

    //Get order by ID
    public OrderModel getOrderById(int orderId) {
        return repo.findById(orderId)
                   .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
    }

    //Get orders by consumer username
    public List<OrderModel> getOrdersByConsumer(String username) {
        return repo.findByConsumerUsername(username);
    }

    //Get order by consumer + orderId
    public OrderModel getOrderByConsumerAndOrderId(String username, int orderId) {
        OrderModel order = repo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        if (!order.getConsumerUsername().equalsIgnoreCase(username)) {
            throw new RuntimeException("Order does not belong to consumer: " + username);
        }

        return order;
    }

    public List<OrderModel> getOrdersBySeller(String sellerUsername) {
        try {
            List<OrderModel> allOrders = repo.findAll();

            if (allOrders.isEmpty()) {
                throw new RuntimeException("No orders found in the system.");
            }

            List<OrderModel> sellerOrders = allOrders.stream()
                    .filter(order -> {
                        try {
                            // Call Product Service to get seller info
                            String productUrl = "http://localhost:8092/products/" + order.getProductId();
                            Map<String, Object> product = restTemplate.getForObject(productUrl, Map.class);

                            if (product == null || !product.containsKey("sellerUsername")) {
                                return false;
                            }

                            String productSeller = product.get("sellerUsername").toString();
                            return productSeller.equalsIgnoreCase(sellerUsername);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

            if (sellerOrders.isEmpty()) {
                throw new RuntimeException("No orders found for seller: " + sellerUsername);
            }

            return sellerOrders;

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    //Update order status (used by seller or consumer)
    public OrderModel updateStatus(int orderId, String newStatus) {
        OrderModel existing = repo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        String oldStatus = existing.getStatus();
        existing.setStatus(newStatus);

        OrderModel updatedOrder = repo.save(existing);

        // Log the status change
        restTemplate.postForObject(
            LOG_SERVICE_URL,
            Map.of(
                "orderId", orderId,
                "oldStatus", oldStatus,
                "newStatus", newStatus
            ),
            Void.class
        );

        return updatedOrder;
    }

    //Delete order
    public void deleteOrder(int orderId) {
        if (!repo.existsById(orderId)) {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }
        repo.deleteById(orderId);
    }

    
}
 