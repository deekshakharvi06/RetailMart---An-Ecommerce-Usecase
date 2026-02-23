package com.ecommerce.product_service.service;

// import java.io.IOException;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ecommerce.product_service.model.ProductModel;
import com.ecommerce.product_service.repository.ProductRepository;

import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.search.FTCreateParams;
import redis.clients.jedis.search.schemafields.TextField; 
import redis.clients.jedis.search.schemafields.NumericField;
import redis.clients.jedis.search.schemafields.TagField;
import redis.clients.jedis.search.schemafields.SchemaField;

@Service
public class ProductService {
    
    @Autowired
    ProductRepository repo;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${file.upload-dir}")
    private String uploadDir;
    
    public String calculatePriceRange(BigDecimal price) {
        if (price == null) return "priceRange:unknown";
        if (price.compareTo(BigDecimal.valueOf(500)) < 0)
            return "priceRange:0-499";
        if (price.compareTo(BigDecimal.valueOf(1000)) < 0)
            return "priceRange:500-999";
        if (price.compareTo(BigDecimal.valueOf(5000)) < 0)
            return "priceRange:1000-4999";
        return "priceRange:5000+";
    }

    public List<ProductModel> getAllProducts() {
        return repo.findAll();
    }

    public ProductModel getProductById(int productId) {
        return repo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found."));
    }

    public ProductModel addProduct(String sellerUsername, ProductModel product, MultipartFile imageFile) {
        product.setSellerUsername(sellerUsername);
        try {
            if (imageFile != null && !imageFile.isEmpty()) {

                // Upload folder
                String uploadDir = "uploads";
                File folder = new File(uploadDir);
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                // Create unique file name
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, fileName);

                // Save file to folder
                Files.write(filePath, imageFile.getBytes());

                //Save image metadata in DB
                product.setImageName(fileName);
                product.setImageType(imageFile.getContentType());
                product.setImageUrl("/images/" + fileName);
            }
                ProductModel savedProduct = repo.save(product);
                
                Map<String, String> productMap = new HashMap<>();
                productMap.put("name", savedProduct.getName());
                productMap.put("description", savedProduct.getDescription());
                productMap.put("brand", savedProduct.getBrand());
                productMap.put("category", savedProduct.getCategory());
                productMap.put("price", String.valueOf(savedProduct.getPrice()));
                productMap.put("imageUrl", savedProduct.getImageUrl());
                productMap.put("priceRange", calculatePriceRange(savedProduct.getPrice()));

                String redisKey = "product:meta:" + savedProduct.getProductId();
                redisTemplate.opsForHash().putAll(redisKey, productMap);

                return savedProduct;
            
            
        }catch (Exception e) {
            throw new RuntimeException("Error while uploading image: " + e.getMessage());
        }  
    }
    

    public List<ProductModel> getProductsBySeller(String sellerUsername) {
        return repo.findBySellerUsername(sellerUsername);
    }

    public ProductModel getProductBySellerAndId(String sellerUsername, int productId) {
        ProductModel product = repo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));
        if (!product.getSellerUsername().equals(sellerUsername)) {
            throw new RuntimeException("Unauthorized access: This product does not belong to seller " + sellerUsername);
        }
        return product;
    }

    // reduce stock when order placed
    public boolean reduceStock(int productId, int orderedQty) {
        ProductModel product = repo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getQuantity() < orderedQty) {
            throw new RuntimeException("Not enough stock available");
        }

        product.setQuantity(product.getQuantity() - orderedQty);
        repo.save(product);
        return product.getQuantity() == 0; // true if now out of stock
    }

    // update stock when seller modifies quantity
    public ProductModel updateStock(int productId, int newQuantity) {
        ProductModel product = repo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setQuantity(newQuantity);
        return repo.save(product);
    }

    public ProductModel updateProduct(
            String sellerUsername,
            int productId,
            ProductModel productDetails,
            MultipartFile imageFile) {

        ProductModel existing = repo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found."));

        if (!existing.getSellerUsername().equals(sellerUsername)) {
            throw new RuntimeException("You can update only your own products.");
        }

        // Update basic fields
        if (productDetails.getName() != null) {
            existing.setName(productDetails.getName());
        }

        if (productDetails.getPrice() != null) {
            existing.setPrice(productDetails.getPrice());
        }

        if (productDetails.getCategory() != null) {
            existing.setCategory(productDetails.getCategory());
        }

        if (productDetails.getBrand() != null) {
            existing.setBrand(productDetails.getBrand());
        }

        if (productDetails.getQuantity() != null) {
            existing.setQuantity(productDetails.getQuantity());
        }

        if (productDetails.getDescription() != null) {
            existing.setDescription(productDetails.getDescription());
        }

        // Process new image file if provided
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // Save new image file
                String fileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, fileName);
                Files.write(filePath, imageFile.getBytes());

                // Update image fields
                existing.setImageName(fileName);
                existing.setImageType(imageFile.getContentType());
                existing.setImageUrl("/images/" + fileName);

            } catch (Exception e) {
                throw new RuntimeException("Error while uploading new image: " + e.getMessage());
            }
        }

        Map<String, String> productMap = new HashMap<>();
        productMap.put("name", productDetails.getName());
        productMap.put("description", productDetails.getDescription());
        productMap.put("brand", productDetails.getBrand());
        productMap.put("category", productDetails.getCategory());
        productMap.put("price", String.valueOf(productDetails.getPrice()));
        productMap.put("imageUrl", productDetails.getImageUrl());
        productMap.put("priceRange", calculatePriceRange(productDetails.getPrice()));

        String redisKey = "product:meta:" + productDetails.getProductId();
        redisTemplate.opsForHash().putAll(redisKey, productMap);

        return repo.save(existing);
        
    }


    public void deleteProduct(String sellerUsername, int productId) {
        ProductModel existing = repo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found."));

        if (!existing.getSellerUsername().equals(sellerUsername)) {
            throw new RuntimeException("You can delete only your own products.");
        }

        repo.delete(existing);
    }


    // // ⚠️ Temporary method for one-time backfill
    // public void syncExistingProductsToRedis() {
    //     List<ProductModel> products = repo.findAll();
    // System.out.println("🔄 Starting sync for " + products.size() + " products...");

    // for (ProductModel p : products) {
    //     String redisKey = "product:meta:" + p.getProductId();
    //     Map<String, String> productMap = new HashMap<>();

    //     // Pehle Map mein data bharo, phir Redis mein dalo!
    //     productMap.put("name", p.getName() != null ? p.getName() : "");
    //     productMap.put("description", p.getDescription() != null ? p.getDescription() : "");
    //     productMap.put("brand", p.getBrand() != null ? p.getBrand() : "");
    //     productMap.put("category", p.getCategory() != null ? p.getCategory() : "");
    //     productMap.put("price", p.getPrice() != null ? p.getPrice().toString() : "0");
    //     productMap.put("imageUrl", p.getImageUrl() != null ? p.getImageUrl() : "");

    //     // Price Range calculation (Jo aapne index mein TAG rakha hai)
    //     BigDecimal price = (p.getPrice() != null) ? p.getPrice() : BigDecimal.ZERO;
    //     String range = calculatePriceRange(price);
    //     productMap.put("priceRange", range);

    private final JedisPooled jedis = new JedisPooled("127.0.0.1",6379);
    
    public void initializeRedisSearch() {
        try {
            System.out.println("🛠️ Step 1: Checking Redis Index...");
            ensureIndexAndSynonyms();
            
            System.out.println("🛠️ Step 2: Syncing Data from DB...");
            syncExistingProductsToRedis();
            
            System.out.println("✅ Redis Search fully initialized.");
        } catch (Exception e) {
            System.err.println("❌ Critical Error during Redis Init: " + e.getMessage());
            // stop-writes-on-bgsave-error wala issue yahan catch hoga
        }
    }

    private void ensureIndexAndSynonyms() {
        try {
        // Index exist karta hai ya nahi check karein
        jedis.ftInfo("idx:products");
        System.out.println("🔎 Index 'idx:products' already exists.");
    } catch (Exception e) {
        System.out.println("🚀 Jedis 5: Recreating Index and Synonyms...");
        
        try {
            FTCreateParams params = FTCreateParams.createParams()
                    .addPrefix("product:meta:");

            // 2. Fields ki List (Aapka current logic sahi hai)
            List<SchemaField> fields = new ArrayList<>();
            fields.add(TextField.of("name").weight(5.0));
            fields.add(TextField.of("description").weight(3.0));
            fields.add(TextField.of("brand").weight(2.0));
            fields.add(TextField.of("category").weight(1.5));
            fields.add(NumericField.of("price"));
            fields.add(TextField.of("imageUrl"));
            fields.add(TagField.of("priceRange"));

            // 3. FINAL FIX: Ye signature Jedis 5 mein 100% valid hai
            // ftCreate(String indexName, FTCreateParams params, Iterable<SchemaField> fields)
            jedis.ftCreate("idx:products", params, fields);
            addSynonymGroups();
            System.out.println("✅ Jedis 5: Index & Synonyms Restored Successfully.");
            
        } catch (Exception exception) {
            System.err.println("❌ Index Creation Failed: " + exception.getMessage());
        }
    }
    }

    private void addSynonymGroups() {
        jedis.ftSynUpdate("idx:products", "skincare_group", "skincare", "face wash", "facewash", "face mask", "facemask", "sunscreen");
        jedis.ftSynUpdate("idx:products", "electronics_group", "electronics", "smartphones", "mobiles", "phones");
        jedis.ftSynUpdate("idx:products", "beauty_group", "beauty", "makeup", "grooming");
        // ... (Baki synonyms bhi isi format mein add kar dein)
    }

    public void syncExistingProductsToRedis() {
        List<ProductModel> dbProducts = repo.findAll();
        for (ProductModel p : dbProducts) {
            String redisKey = "product:meta:" + p.getProductId();
            Map<String, String> map = new HashMap<>();
            map.put("name", p.getName() != null ? p.getName() : "");
            map.put("brand", p.getBrand() != null ? p.getBrand() : "");
            map.put("category", p.getCategory() != null ? p.getCategory() : "");
            map.put("description", p.getDescription() != null ? p.getDescription() : "");
            map.put("imageUrl", p.getImageUrl() != null ? p.getImageUrl() : "");
            
            BigDecimal price = p.getPrice() != null ? p.getPrice() : BigDecimal.ZERO;
            map.put("price", price.toPlainString());
            
            jedis.hset(redisKey, map);
        }
    }
}


