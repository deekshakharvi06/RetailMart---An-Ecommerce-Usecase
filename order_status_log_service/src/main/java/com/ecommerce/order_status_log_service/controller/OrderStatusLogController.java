package com.ecommerce.order_status_log_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ecommerce.order_status_log_service.model.OrderStatusLogModel;
import com.ecommerce.order_status_log_service.service.OrderStatusLogService;

@RestController
@RequestMapping("/orderStatusLog")
@CrossOrigin(origins = "http://localhost:5173",allowedHeaders = "*", methods = {RequestMethod.GET,RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE,RequestMethod.OPTIONS})
public class OrderStatusLogController {

    @Autowired
    OrderStatusLogService service;

    //Create a log entry (called from OrderService automatically)
    @PostMapping("/create")
    public ResponseEntity<?> createLog(@RequestBody OrderStatusLogModel model) {
        try {
            OrderStatusLogModel log = service.createLog(model);
            return ResponseEntity.status(HttpStatus.CREATED).body(log);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //Get all logs
    @GetMapping
    public ResponseEntity<List<OrderStatusLogModel>> getAllLogs() {
        return ResponseEntity.ok(service.getAllLogs());
    }

    //Get logs by orderId
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getLogsByOrderId(@PathVariable int orderId) {
        try {
            List<OrderStatusLogModel> logs = service.getLogsByOrderId(orderId);
            if (logs.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No logs found for order ID: " + orderId);
            }
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
