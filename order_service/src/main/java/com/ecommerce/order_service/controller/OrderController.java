package com.ecommerce.order_service.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.order_service.model.OrderModel;
import com.ecommerce.order_service.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/orders")
@CrossOrigin(origins = "http://localhost:5173",allowedHeaders = "*", methods = {RequestMethod.GET,RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class OrderController {

    @Autowired
    OrderService service;

    //Place order
    @PostMapping("/placeOrder")
    public ResponseEntity<?> placeOrder(@RequestBody OrderModel model) {
        try {
            OrderModel savedOrder = service.placeOrder(model);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedOrder);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //Get all orders
    @GetMapping
    public ResponseEntity<List<OrderModel>> getAllOrder(OrderModel order) {
        return ResponseEntity.ok(service.getAllOrder(order));
    }

    //Get order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable int orderId) {
        try {
            return ResponseEntity.ok(service.getOrderById(orderId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    //Get orders by consumer username
    @GetMapping("/consumer/{username}")
    public ResponseEntity<?> getOrdersByConsumer(@PathVariable String username) {
        try {
            List<OrderModel> orders = service.getOrdersByConsumer(username);
            if (orders.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No orders found for " + username);
            }
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //Get specific order by username and orderId
    @GetMapping("/consumer/{username}/{orderId}")
    public ResponseEntity<?> getOrderByConsumerAndOrderId(
            @PathVariable String username,
            @PathVariable int orderId) {
        try {
            OrderModel order = service.getOrderByConsumerAndOrderId(username, orderId);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Get all orders for a seller by username
    @GetMapping("/seller/{username}")
    public ResponseEntity<?> getOrdersBySeller(@PathVariable String username) {
        try {
            List<OrderModel> orders = service.getOrdersBySeller(username);
            if (orders.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No orders found for seller: " + username);
            }
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //Update order status (used by seller or consumer)
    @PutMapping("/{orderId}")
    public ResponseEntity<?> updateOrder(
            @PathVariable int orderId,
            @Valid @RequestBody Map<String, Object> updates) {
        try {
            if (!updates.containsKey("status")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Status field is missing");
            }
            String status = updates.get("status").toString();
            return ResponseEntity.ok(service.updateStatus(orderId, status));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    //Delete order
    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable int orderId) {
        try {
            service.deleteOrder(orderId);
            return ResponseEntity.ok("Order deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
