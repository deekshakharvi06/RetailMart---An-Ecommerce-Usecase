package com.ecommerce.seller_service.service;

import com.ecommerce.seller_service.model.SellerModel;
import com.ecommerce.seller_service.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SellerService {

    @Autowired
    private SellerRepository repo;

    @Autowired
    private RestTemplate restTemplate;

    // Microservice URLs
    private String product_service_url = "http://localhost:8092/"; // Product service
    private String order_service_url = "http://localhost:8093/orders"; // Order service

    // ------------------ Seller CRUD ------------------

    // Register seller
    public SellerModel registerSeller(SellerModel seller) {
        Optional<SellerModel> existing = repo.findByUsername(seller.getUsername());
        if (existing.isPresent()) {
            throw new RuntimeException("Username already exists.");
        }

        // Force role to SELLER
        seller.setRole("SELLER");

        return repo.save(seller);
    }

    // Login seller
    public String login(String username, String password) {
        Optional<SellerModel> existing = repo.findByUsername(username);
        if (existing.isPresent()) {
            SellerModel seller = existing.get();
            if(seller.getPassword().equals(password)){
            return "Login successful";
            }
            else return "Invalid Credentials";
        }
        return "Consumer doesnot exist";
    }

    // Get all sellers
    public List<SellerModel> getAllSellers() {
        return repo.findAll();
    }

    // Get seller by username
    public SellerModel getSellerByUsername(String username) {
        return repo.findByUsername(username)
                   .orElseThrow(() -> new RuntimeException("Seller does not exist."));
    }

    // Update seller details (PATCH)
    public SellerModel updateSeller(String username, Map<String, Object> updatedSeller) {
        SellerModel existing = repo.findByUsername(username)
                                   .orElseThrow(() -> new RuntimeException("Seller not found"));

        for (Map.Entry<String, Object> entry : updatedSeller.entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();

            switch (field) {
                case "username":
                    existing.setUsername((String) value);
                    break;
                case "password":
                    existing.setPassword((String) value);
                    break;
                case "location":
                    existing.setLocation((String) value);
                    break;
                case "role":
                    throw new RuntimeException("Role cannot be changed.");
                default:
                    throw new RuntimeException("Invalid field: " + field);
            }
        }

        return repo.save(existing);
    }

    // Delete seller
    public void deleteSeller(int id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Seller does not exist.");
        }
        repo.deleteById(id);
    }

    // ------------------ Product / Order APIs ------------------

    // Get all products by seller
    public List<Map<String, Object>> getProductsBySeller(String username) {
        String url = product_service_url + "seller/" + username;
        List<Map<String, Object>> products = restTemplate.getForObject(url, List.class);

        if (products == null || products.isEmpty()) {
            throw new RuntimeException("No products found for seller: " + username);
        }

        return products;
    }

    // Get specific product by seller
    public Map<String, Object> getProductByIdAndSeller(String username, int productId) {
        String url = product_service_url + "seller/" + username + "/" + productId;
        return restTemplate.getForObject(url, Map.class);
    }

    public Map<String,Object> addProduct(String username,String productJson, MultipartFile imageFile){
    try {
            String url = product_service_url + "seller/" + username + "/addProduct";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            HttpHeaders jsonHeaders = new HttpHeaders();
                jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> productEntity = new HttpEntity<>(productJson, jsonHeaders);
                body.add("product", productEntity);
            // body.add("product", productJson);

            if (imageFile != null && !imageFile.isEmpty()) {
                ByteArrayResource fileAsResource = new ByteArrayResource(imageFile.getBytes()) {
                    @Override
                    public String getFilename() {
                        return imageFile.getOriginalFilename();
                    }
                };
                body.add("imageFile", fileAsResource);
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);

            ResponseEntity<Map> response =
                restTemplate.postForEntity(url, requestEntity, Map.class);

            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Error sending multipart request: " + e.getMessage());
        }
    }

    public Map<String,Object> updateProduct(
            String username,
            int productId,
            String productJson,
            MultipartFile imageFile) {

        try {
            String url = product_service_url + "seller/" + username + "/products/" + productId;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            HttpHeaders jsonHeaders = new HttpHeaders();
            jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> productEntity = new HttpEntity<>(productJson, jsonHeaders);

            body.add("product", productEntity);

            if (imageFile != null && !imageFile.isEmpty()) {
                ByteArrayResource fileAsResource = new ByteArrayResource(imageFile.getBytes()) {
                    @Override
                    public String getFilename() {
                        return imageFile.getOriginalFilename();
                    }
                };
                body.add("imageFile", fileAsResource);
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> response =
                    restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Map.class);

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Error sending multipart request: " + e.getMessage());
        }
    }

    public String deleteProductById(String username, int productId) {
        // Check product exists via product-service
        String getUrl = product_service_url + "seller/" + username + "/" + productId;
        Map<String, Object> product = restTemplate.getForObject(getUrl, Map.class);

        if (product == null || product.isEmpty()) {
            throw new RuntimeException("Product does not exist for seller: " + username);
        }

        // Now delete via Product Service
        String deleteUrl = product_service_url + "seller/" + username + "/products/" + productId;
        restTemplate.delete(deleteUrl);

        return "Product deleted successfully for ID: " + productId;
    }

    // Get orders received by the seller
    public List<Map<String, Object>> getOrdersBySellerUsername(String sellerUsername) {
    try {
        // Validate seller exists
        repo.findByUsername(sellerUsername)
            .orElseThrow(() -> new RuntimeException("Seller not found with username: " + sellerUsername));

        // Call Order Service API
        String url = "http://localhost:8093/orders/seller/" + sellerUsername;

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new org.springframework.core.ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        List<Map<String, Object>> orders = response.getBody();

        if (orders == null || orders.isEmpty()) {
            throw new RuntimeException("No orders found for seller: " + sellerUsername);
        }

        return orders;

    } catch (Exception e) {
        throw new RuntimeException(e.getMessage());
    }
}


    // Update order status by seller
    public String updateOrderStatus(String sellerUsername, int orderId, String newStatus) {
        //Validate seller
        repo.findByUsername(sellerUsername)
                .orElseThrow(() -> new RuntimeException("Seller not found with username: " + sellerUsername));

        // Fetch order details from Order Service
        String orderUrl = order_service_url + "/" + orderId;
        Map<String, Object> order = restTemplate.getForObject(orderUrl, Map.class);

        if (order == null || order.isEmpty()) {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }

        int productId = (int) order.get("productId");

        //Fetch product details to verify seller
        String productUrl = "http://localhost:8092/products/" + productId;
        Map<String, Object> product = restTemplate.getForObject(productUrl, Map.class);

        if (product == null || product.isEmpty()) {
            throw new RuntimeException("Product not found for this order.");
        }

        String productSeller = product.get("sellerUsername").toString();

        if (!sellerUsername.equalsIgnoreCase(productSeller)) {
            throw new RuntimeException("You are not authorized to update this order.");
        }

        //Update order status in Order Service (this will also log automatically)
        restTemplate.put(orderUrl, Map.of("status", newStatus));

        return "Order status updated successfully to '" + newStatus + "'";
    }



    // Get a specific order by seller
    public Map<String, Object> getOrderById(String username, int orderId) {
        String url = order_service_url + "/seller/" + username + "/" + orderId;
        return restTemplate.getForObject(url, Map.class);
    }
}
