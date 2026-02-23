package com.ecommerce.order_status_log_service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecommerce.order_status_log_service.model.OrderStatusLogModel;
import com.ecommerce.order_status_log_service.repository.OrderStatusLogRepository;

@Service
public class OrderStatusLogService {

    @Autowired
    OrderStatusLogRepository repo;

    //Create new log entry (called automatically from OrderService)
    public OrderStatusLogModel createLog(OrderStatusLogModel model) {
        if (model.getOrderId() == 0) {
            throw new RuntimeException("Order ID cannot be null or zero");
        }

        model.setUpdatedAt(LocalDateTime.now());
        return repo.save(model);
    }

    // Get all logs
    public List<OrderStatusLogModel> getAllLogs() {
        return repo.findAll();
    }

    //Get logs for a specific order
    public List<OrderStatusLogModel> getLogsByOrderId(int orderId) {
        return repo.findByOrderId(orderId);
    }
}
