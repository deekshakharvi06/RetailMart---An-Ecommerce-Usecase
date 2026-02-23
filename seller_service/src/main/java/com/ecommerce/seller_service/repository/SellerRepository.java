package com.ecommerce.seller_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.seller_service.model.SellerModel;

@Repository
public interface SellerRepository extends JpaRepository<SellerModel, Integer> {

    // Find seller by username
    Optional<SellerModel> findByUsername(String username);

}
