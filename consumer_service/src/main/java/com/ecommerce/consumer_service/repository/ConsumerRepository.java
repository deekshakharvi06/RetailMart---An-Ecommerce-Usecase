package com.ecommerce.consumer_service.repository;

import com.ecommerce.consumer_service.model.ConsumerModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsumerRepository extends JpaRepository<ConsumerModel, Integer> {

    // Find by username for login/validation
    Optional<ConsumerModel> findByUsername(String username);

    // Check if username already exists
    boolean existsByUsername(String username);

}
