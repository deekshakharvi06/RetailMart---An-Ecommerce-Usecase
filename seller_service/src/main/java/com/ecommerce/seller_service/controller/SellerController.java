package com.ecommerce.seller_service.controller;

import com.ecommerce.seller_service.model.SellerModel;
import com.ecommerce.seller_service.service.SellerService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sellers")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173",allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE,RequestMethod.OPTIONS})
public class SellerController {

    private final SellerService sellerService;

    // ------------------ Seller CRUD ------------------

    // Register seller
    @PostMapping("/register")
    public SellerModel registerSeller(@RequestBody SellerModel seller) {
        return sellerService.registerSeller(seller);
    }

    // Login seller (username, password in request body)
    @PostMapping("/login")
    public String login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        return sellerService.login(username, password);
    }

    // Get all sellers
    @GetMapping
    public List<SellerModel> getAllSellers() {
        return sellerService.getAllSellers();
    }

    // Get seller by username
    @GetMapping("/{username}")
    public ResponseEntity<?> getSellerByUsername(@PathVariable String username) {
        try {
            SellerModel seller= sellerService.getSellerByUsername(username);
            return ResponseEntity.ok(seller);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Seller not found");
        }
    }

    // Update seller (PATCH)
    @PatchMapping("/{username}")
    public SellerModel updateSeller(@PathVariable String username, @RequestBody Map<String, Object> updatedSeller) {
        return sellerService.updateSeller(username, updatedSeller);
    }

    // Delete seller by ID
    @DeleteMapping("/{id}")
    public String deleteSeller(@PathVariable int id) {
        sellerService.deleteSeller(id);
        return "Seller deleted successfully.";
    }

    // ------------------ Product APIs (via Product Service) ------------------

    // Get all products of a seller
    @GetMapping("/{username}/products")
    public List<Map<String, Object>> getProductsBySeller(@PathVariable String username) {
        return sellerService.getProductsBySeller(username);
    }

    // Get specific product of a seller
    @GetMapping("/{username}/products/{productId}")
    public Map<String, Object> getProductByIdAndSeller(@PathVariable String username, @PathVariable int productId) {
        return sellerService.getProductByIdAndSeller(username, productId);
    }

    @PostMapping(value = "/{username}/addProduct",
                consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProduct(
            @PathVariable String username,
            @RequestPart("product") String productJson,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {
        try {
            Map<String, Object> products = sellerService.addProduct(username, productJson, imageFile);
            return ResponseEntity.status(HttpStatus.CREATED).body(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping(
        value = "/{username}/products/{productId}",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> updateProduct(
            @PathVariable String username,
            @PathVariable int productId,
            @RequestPart("product") String productJson,
            @RequestPart(value = "imageFile", required = false) MultipartFile imageFile) {

        try {
            Map<String, Object> updatedProduct = sellerService.updateProduct(username, productId, productJson, imageFile);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{username}/products/{productId}")
    public ResponseEntity<?> deleteProduct(
            @PathVariable String username,
            @PathVariable int productId) {

        try {
            String result = sellerService.deleteProductById(username, productId);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    // ------------------ Order APIs (via Order Service) ------------------

    // Get all orders of a seller
    @GetMapping("/{username}/orders")
    public ResponseEntity<?> getOrdersBySeller(@PathVariable String username) {
        try {
            List<Map<String, Object>> orders = sellerService.getOrdersBySellerUsername(username);
            if (orders.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No orders found for seller: " + username);
            }
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Get a specific order of a seller
    @GetMapping("/{username}/orders/{orderId}")
    public Map<String, Object> getOrderById(@PathVariable String username, @PathVariable int orderId) {
        return sellerService.getOrderById(username, orderId);
    }

    @PutMapping("/{username}/orders/{orderId}/updateStatus")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable String username,
            @PathVariable int orderId,
            @RequestBody Map<String, Object> body) {
        try {
            if (!body.containsKey("status")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing 'status' field");
            }

            String newStatus = body.get("status").toString();
            String message = sellerService.updateOrderStatus(username, orderId, newStatus);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
