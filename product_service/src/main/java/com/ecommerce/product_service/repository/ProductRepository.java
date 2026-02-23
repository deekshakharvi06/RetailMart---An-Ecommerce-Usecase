package com.ecommerce.product_service.repository;

import java.util.List;
// import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.product_service.model.ProductModel;

public interface ProductRepository extends JpaRepository<ProductModel,Integer>{
    // Find all products by seller username
    List<ProductModel> findBySellerUsername(String sellerUsername);

}
