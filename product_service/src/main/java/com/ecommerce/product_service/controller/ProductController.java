package com.ecommerce.product_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.product_service.model.ProductModel;
import com.ecommerce.product_service.service.ProductService;

import jakarta.annotation.PostConstruct;


@RestController
@RequestMapping("")
@CrossOrigin(origins = "http://localhost:5173",allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class ProductController {
    
    @Autowired
    ProductService service;

    @PostMapping(value="/seller/{sellerUsername}/addProduct", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProduct(@PathVariable String sellerUsername,
        @RequestPart ProductModel product, @RequestPart(value="imageFile", required=false) MultipartFile imageFile) {
        try {
            product.setSellerUsername(sellerUsername);
            ProductModel saved = service.addProduct(sellerUsername, product, imageFile);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/seller/{sellerUsername}")
    public ResponseEntity<?> getProductsBySeller(@PathVariable String sellerUsername) {
        List<ProductModel> products = service.getProductsBySeller(sellerUsername);
        if (products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No products found for " + sellerUsername);
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/seller/{sellerUsername}/{productId}")
    public ResponseEntity<?> getProductBySellerAndId(@PathVariable String sellerUsername,
                                                     @PathVariable int productId) {
        try {
            ProductModel product = service.getProductBySellerAndId(sellerUsername, productId);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductModel>> getAllProducts() {
        List<ProductModel> products = service.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable int productId) {
        return ResponseEntity.ok(service.getProductById(productId));
    }

    // reduce stock (called by consumer order)
    @PutMapping("/products/updateQuantity/{productId}")
    public ResponseEntity<String> updateProductQuantity(
            @PathVariable int productId,
            @RequestParam int orderedQty) {

        try {
            boolean outOfStock = service.reduceStock(productId, orderedQty);
            if (outOfStock) {
                return ResponseEntity.ok("Stock reduced — Product now OUT OF STOCK");
            } else {
                return ResponseEntity.ok("Stock reduced successfully");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Failed to update stock: " + e.getMessage());
        }
    }


    // seller manually updates stock
    @PutMapping("/seller/updateStock/{productId}/{quantity}")
    public ResponseEntity<?> updateStock(@PathVariable int productId, @PathVariable int quantity) {
        try {
            ProductModel updated = service.updateStock(productId, quantity);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping(
        value = "/seller/{sellerUsername}/products/{productId}",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> updateProduct(
            @PathVariable String sellerUsername,
            @PathVariable int productId,
            @RequestPart("product") ProductModel product,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {

        try {
            ProductModel updatedProduct =
                service.updateProduct(sellerUsername, productId, product, imageFile);
            return ResponseEntity.ok(updatedProduct);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @DeleteMapping("/seller/{sellerUsername}/products/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable String sellerUsername,
                                           @PathVariable int productId) {
        try {
            service.deleteProduct(sellerUsername, productId);
            return ResponseEntity.ok("Product deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostConstruct
    public void init() {
        service.initializeRedisSearch();
    }
}
