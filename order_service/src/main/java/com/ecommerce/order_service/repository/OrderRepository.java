package com.ecommerce.order_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.order_service.model.OrderModel;

public interface OrderRepository extends JpaRepository<OrderModel, Integer>{
    List<OrderModel> findByConsumerUsername(String consumerUsername);
}
