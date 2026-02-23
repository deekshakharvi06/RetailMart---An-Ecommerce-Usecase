package com.ecommerce.order_status_log_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.order_status_log_service.model.OrderStatusLogModel;

@Repository
public interface OrderStatusLogRepository extends JpaRepository<OrderStatusLogModel,Integer>{
    List<OrderStatusLogModel> findByOrderId(int orderId);
}
