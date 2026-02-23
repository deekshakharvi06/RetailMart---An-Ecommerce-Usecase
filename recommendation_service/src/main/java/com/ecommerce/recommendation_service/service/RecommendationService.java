// package com.ecommerce.recommendation_service.service;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.redis.core.RedisTemplate;
// // import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;

// import com.ecommerce.recommendation_service.model.RecommendationModel;
// import com.ecommerce.recommendation_service.model.UserActivity;

// // import jakarta.annotation.PostConstruct;

// import java.util.*;
// //import java.util.stream.Collectors;

// @Service
// public class RecommendationService {

//     @Autowired
//     private RedisTemplate<String, Object> redisTemplate;

//     @Autowired
//     private RestTemplate restTemplate;

//     // Recently viewed products
//     public RecommendationModel getRecentViews(String username) {
//         List<Object> ids = redisTemplate.opsForList().range("recent:" + username, 0, 9);
//         List<String> productIds = ids == null ? List.of() : ids.stream().map(Object::toString).toList();
//         return new RecommendationModel("RECENT", null, productIds);
        
//     }

//     // Trending products
//     public RecommendationModel getTrendingProducts() {
//         Set<Object> popular = redisTemplate.opsForZSet().reverseRange("trending", 0, 9);
//         List<String> productIds = popular == null ? List.of() : popular.stream().map(Object::toString).toList();
//         return new RecommendationModel("TRENDING", null, productIds);
//     }

//     // Related or similar products
//     // public RecommendationModel getRelatedProducts(String productId) {
//     //     // 1) Try Redis ZSet first
//     //     Set<Object> related = redisTemplate.opsForZSet()
//     //             .reverseRange("product:related:" + productId, 0, 9);

//     //     if (related != null && !related.isEmpty()) {
//     //         List<String> productIds = related.stream().map(Object::toString).toList();
//     //         return new RecommendationModel("RELATED", productId, productIds);
//     //     }

//     //     try {
//     //         // 2) Get metadata
//     //         String category = (String) redisTemplate.opsForHash().get("product:" + productId, "category");
//     //         String brand = (String) redisTemplate.opsForHash().get("product:" + productId, "brand");
//     //         String name = (String) redisTemplate.opsForHash().get("product:" + productId, "name");

//     //         // 3) Fetch & Cache Metadata if missing
//     //         if (category == null || brand == null || name == null) {
//     //             String productUrl = "http://localhost:8090/consumers/products/" + productId;
//     //             Map<String, Object> product = restTemplate.getForObject(productUrl, Map.class);

//     //             if (product != null) {
//     //                 category = product.get("category") != null ? product.get("category").toString().toLowerCase() : null;
//     //                 brand = product.get("brand") != null ? product.get("brand").toString().toLowerCase() : null;
//     //                 name = product.get("name") != null ? product.get("name").toString() : null;

//     //                 // Cache the core product info
//     //                 Map<String, String> metadata = new HashMap<>();
//     //                 if (category != null) metadata.put("category", category);
//     //                 if (brand != null) metadata.put("brand", brand);
//     //                 if (name != null) metadata.put("name", name);
                    
//     //                 if (!metadata.isEmpty()) {
//     //                     redisTemplate.opsForHash().putAll("product:" + productId, metadata);
//     //                 }
//     //             }
//     //         }

//     //         // 4) Build the Related Set using "Intersection" or "Union" of Category/Brand
//     //         Set<Object> combined = new LinkedHashSet<>();
//     //         if (category != null) {
//     //             Set<Object> catMembers = redisTemplate.opsForSet().members("category:" + category);
//     //             if (catMembers != null) combined.addAll(catMembers);
//     //             // Ensure this product is in its own category set for future lookups
//     //             redisTemplate.opsForSet().add("category:" + category, productId);
//     //         }
//     //         if (brand != null) {
//     //             Set<Object> brandMembers = redisTemplate.opsForSet().members("brand:" + brand);
//     //             if (brandMembers != null) combined.addAll(brandMembers);
//     //             // Ensure this product is in its own brand set
//     //             redisTemplate.opsForSet().add("brand:" + brand, productId);
//     //         }
            
//     //         combined.remove(productId); // Don't recommend self

//     //         // 5) Convert to list and perform BIDIRECTIONAL caching
//     //         List<String> productIds = combined.stream()
//     //                 .map(Object::toString)
//     //                 .limit(10)
//     //                 .collect(Collectors.toList());

//     //         if (!productIds.isEmpty()) {
//     //             for (String otherId : productIds) {
//     //                 // LINK BOTH WAYS: A -> B and B -> A
//     //                 // We use incrementScore so that every time they are "found" together, 
//     //                 // their relationship strength grows.
//     //                 redisTemplate.opsForZSet().incrementScore("product:related:" + productId, otherId, 1);
//     //                 redisTemplate.opsForZSet().incrementScore("product:related:" + otherId, productId, 1);
                    
//     //                 // Set TTL so the "graph" refreshes occasionally
//     //                 redisTemplate.expire("product:related:" + productId, java.time.Duration.ofDays(7));
//     //                 redisTemplate.expire("product:related:" + otherId, java.time.Duration.ofDays(7));
//     //             }
//     //         }

//     //         return new RecommendationModel("RELATED", productId, productIds);

//     //     } catch (Exception e) {
//     //         e.printStackTrace();
//     //         return new RecommendationModel("RELATED", productId, Collections.emptyList());
//     //     }
//     // }

//     public RecommendationModel getRelatedProducts(String productId) {
//     try {
//         // 1️⃣ Try cache first
//         Set<Object> cached = redisTemplate.opsForZSet()
//                 .reverseRange("product:related:" + productId, 0, 9);
//         if (cached != null && !cached.isEmpty()) {
//             List<String> cachedList = cached.stream().map(Object::toString).toList();
//             return new RecommendationModel("RELATED", productId, cachedList);
//         }

//         // 2️⃣ Fetch the base product
//         String productUrl = "http://localhost:8090/consumers/products/" + productId;
//         Map<String, Object> base = restTemplate.getForObject(productUrl, Map.class);
//         if (base == null) return new RecommendationModel("RELATED", productId, List.of());

//         String baseCategory = getString(base.get("category"));
//         String baseBrand = getString(base.get("brand"));
//         String baseName = getString(base.get("name"));
//         double basePrice = base.get("price") != null ? Double.parseDouble(base.get("price").toString()) : 0.0;

//         // 3️⃣ Fetch all products (live)
//         Map<String, Object>[] all = restTemplate.getForObject(
//                 "http://localhost:8090/consumers/products", Map[].class);

//         if (all == null || all.length == 0)
//             return new RecommendationModel("RELATED", productId, List.of());

//         Set<String> relatedSet = new LinkedHashSet<>();

//         // 4️⃣ Helper: filter and collect
//         for (Map<String, Object> p : all) {
//             if (p.get("productId") == null) continue;
//             String pid = p.get("productId").toString();
//             if (pid.equals(productId)) continue;

//             int qty = p.get("quantity") != null ? Integer.parseInt(p.get("quantity").toString()) : 1;
//             if (qty <= 0) continue; // skip out-of-stock

//             String cat = getString(p.get("category"));
//             String brand = getString(p.get("brand"));
//             String name = getString(p.get("name"));
//             double price = p.get("price") != null ? Double.parseDouble(p.get("price").toString()) : 0.0;

//             boolean priceClose = (basePrice > 0 && price > 0 &&
//                     price >= basePrice * 0.7 && price <= basePrice * 1.3);

//             // 🥇 Rule 1: Same category + price range
//             if (baseCategory != null && cat.equals(baseCategory) && priceClose) {
//                 relatedSet.add(pid);
//                 continue;
//             }

//             // 🥈 Rule 2: Same brand + price range
//             if (baseBrand != null && brand.equals(baseBrand) && priceClose) {
//                 relatedSet.add(pid);
//                 continue;
//             }

//             // 🥉 Rule 3: Same category only
//             if (baseCategory != null && cat.equals(baseCategory)) {
//                 relatedSet.add(pid);
//                 continue;
//             }

//             // 🪶 Rule 4: Keyword match (fallback)
//             if (baseName != null && name != null) {
//                 String key = baseName.split("\\s+")[0].toLowerCase();
//                 if (name.toLowerCase().contains(key)) {
//                     relatedSet.add(pid);
//                 }
//             }
//         }

//         // 5️⃣ Limit top 10
//         List<String> relatedList = relatedSet.stream().limit(10).toList();

//         // 6️⃣ Cache results bidirectionally
//         if (!relatedList.isEmpty()) {
//             for (String id : relatedList) {
//                 redisTemplate.opsForZSet().add("product:related:" + productId, id, 1);
//                 redisTemplate.opsForZSet().add("product:related:" + id, productId, 1);
//             }
//             redisTemplate.expire("product:related:" + productId, java.time.Duration.ofHours(12));
//         }

//         return new RecommendationModel("RELATED", productId, relatedList);

//     } catch (Exception e) {
//         e.printStackTrace();
//         return new RecommendationModel("RELATED", productId, List.of());
//     }
// }

// private String getString(Object obj) {
//     return obj != null ? obj.toString().trim().toLowerCase() : null;
// }



//     // User activity tracking
//     public void logUserActivity(UserActivity activity) {
//         String userKey = "recent:" + activity.getUsername();
//         String trendingKey = "trending";
//         redisTemplate.expire("recent:" + activity.getUsername(), java.time.Duration.ofHours(24));
//         switch (activity.getAction().toLowerCase()) {
//             case "view":
//                 redisTemplate.opsForList().remove(userKey, 0, activity.getProductId());
//                 redisTemplate.opsForList().leftPush(userKey, activity.getProductId());
//                 redisTemplate.opsForList().trim(userKey, 0, 9);
//                 redisTemplate.opsForZSet().incrementScore(trendingKey, activity.getProductId(), 1);
//                 break;
//             case "like":
//                 redisTemplate.opsForZSet().incrementScore(trendingKey, activity.getProductId(), 2);
//                 break;
//             case "order":
//                 redisTemplate.opsForZSet().incrementScore(trendingKey, activity.getProductId(), 3);
//                 break;
//             default:
//                 System.out.println("Unknown action type: " + activity.getAction());
//         }
//     }
// }




// package com.ecommerce.recommendation_service.service;

// import com.ecommerce.recommendation_service.model.RecommendationModel;
// import com.ecommerce.recommendation_service.model.UserActivity;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;

// import java.util.*;
// import java.util.stream.Collectors;

// @Service
// public class RecommendationService {

//     @Autowired
//     private RedisTemplate<String, Object> redisTemplate;

//     @Autowired
//     private RestTemplate restTemplate;

//     private static final String PRODUCT_API = "http://localhost:8090/consumers/products";

//     // 1. Get Recently Viewed
//     public RecommendationModel getRecentViews(String username) {
//         List<Object> ids = redisTemplate.opsForList().range("recent:" + username, 0, 9);
//         List<String> productIds = ids == null ? List.of() : ids.stream().map(Object::toString).toList();
//         return new RecommendationModel("RECENT", null, productIds);
//     }

//     // 2. Get Trending
//     public RecommendationModel getTrendingProducts() {
//         Set<Object> popular = redisTemplate.opsForZSet().reverseRange("trending", 0, 9);
//         List<String> productIds = popular == null ? List.of() : popular.stream().map(Object::toString).toList();
//         return new RecommendationModel("TRENDING", null, productIds);
//     }

//     // 3. The "Smart" Related Products Engine
//     public RecommendationModel getRelatedProducts(String productId) {
//         try {
//             // Check Cache first
//             Set<Object> cached = redisTemplate.opsForZSet().reverseRange("product:related:" + productId, 0, 11);
//             if (cached != null && cached.size() > 5) {
//                 return new RecommendationModel("RELATED", productId, cached.stream().map(Object::toString).toList());
//             }

//             // Fetch Base Product and All Products
//             Map<String, Object> base = restTemplate.getForObject(PRODUCT_API + "/" + productId, Map.class);
//             Map<String, Object>[] allProducts = restTemplate.getForObject(PRODUCT_API, Map[].class);

//             if (base == null || allProducts == null) return new RecommendationModel("RELATED", productId, List.of());

//             List<ProductScore> scoredProducts = new ArrayList<>();

//             for (Map<String, Object> p : allProducts) {
//                 String pid = String.valueOf(p.get("productId"));
//                 if (pid.equals(productId)) continue;

//                 double score = calculateSimilarityScore(base, p);
                
//                 // Add a small "Randomness/Discovery" boost (0.0 to 1.0)
//                 score += Math.random(); 

//                 scoredProducts.add(new ProductScore(pid, score));
//             }

//             // Sort by highest score and take top 10
//             List<String> finalIds = scoredProducts.stream()
//                     .sorted(Comparator.comparingDouble(ProductScore::getScore).reversed())
//                     .map(ProductScore::getId)
//                     .collect(Collectors.toList());

//             // Bidirectional Cache for fluid navigation
//             for (String id : finalIds) {
//                 redisTemplate.opsForZSet().incrementScore("product:related:" + productId, id, 1);
//                 redisTemplate.opsForZSet().incrementScore("product:related:" + id, productId, 1);
//             }
//             redisTemplate.expire("product:related:" + productId, java.time.Duration.ofHours(6));

//             return new RecommendationModel("RELATED", productId, finalIds);

//         } catch (Exception e) {
//             return new RecommendationModel("RELATED", productId, List.of());
//         }
//     }

//     // 4. Similarity Logic: Category + Brand + Price + Description
//     private double calculateSimilarityScore(Map<String, Object> base, Map<String, Object> target) {
//         double score = 0.0;

//         // Same Main Category (Huge boost)
//         if (clean(base.get("category")).equals(clean(target.get("category")))) score += 5.0;

//         // Same Brand (Medium boost)
//         if (clean(base.get("brand")).equals(clean(target.get("brand")))) score += 3.0;

//         // Price Proximity (The closer the price, the higher the score - max 4.0 points)
//         double p1 = Double.parseDouble(base.get("price").toString());
//         double p2 = Double.parseDouble(target.get("price").toString());
//         double diff = Math.abs(p1 - p2);
//         if (diff < p1 * 0.2) score += 4.0; 
//         else if (diff < p1 * 0.5) score += 2.0;

//         // Description Keyword Match (Basic NLP)
//         String desc1 = clean(base.get("description") + " " + base.get("name"));
//         String desc2 = clean(target.get("description") + " " + target.get("name"));
//         String[] keywords = desc1.split("\\s+");
//         for (String word : keywords) {
//             if (word.length() > 3 && desc2.contains(word)) score += 0.5; // Every matching word adds value
//         }

//         return score;
//     }

//     private String clean(Object obj) {
//         return obj != null ? obj.toString().trim().toLowerCase() : "";
//     }

//     // 5. Improved User Activity Tracking
//     public void logUserActivity(UserActivity activity) {
//         String userKey = "recent:" + activity.getUsername();
//         String trendingKey = "trending";
//         String productKey = activity.getProductId();

//         // Store activity in a List for sequence tracking
//         redisTemplate.opsForList().remove(userKey, 0, productKey);
//         redisTemplate.opsForList().leftPush(userKey, productKey);
//         redisTemplate.opsForList().trim(userKey, 0, 19); // Keep last 20 actions
        
//         // Weighting the trending score
//         int weight = switch (activity.getAction().toLowerCase()) {
//             case "view" -> 1;
//             case "like" -> 3;
//             case "order" -> 5;
//             default -> 1;
//         };

//         redisTemplate.opsForZSet().incrementScore(trendingKey, productKey, weight);
//     }

//     // Helper class for ranking
//     private static class ProductScore {
//         String id; double score;
//         ProductScore(String id, double score) { this.id = id; this.score = score; }
//         public String getId() { return id; }
//         public double getScore() { return score; }
//     }
// }



//final service class

package com.ecommerce.recommendation_service.service;

import com.ecommerce.recommendation_service.model.RecommendationModel;
import com.ecommerce.recommendation_service.model.UserActivity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class RecommendationService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RestTemplate restTemplate;

    private static final String PRODUCT_API = "http://localhost:8090/consumers/products";
    // We only return cached results if they are high-quality and plentiful
    private static final int MIN_CACHE_SIZE = 5; 

    public RecommendationModel getRecentViews(String username) {
        List<Object> ids = redisTemplate.opsForList().range("recent:" + username, 0, 9);
        List<String> productIds = ids == null ? List.of() : ids.stream().map(Object::toString).toList();
        return new RecommendationModel("RECENT", null, productIds);
    }

    public RecommendationModel getTrendingProducts() {
        Set<Object> popular = redisTemplate.opsForZSet().reverseRange("trending", 0, 9);
        List<String> productIds = popular == null ? List.of() : popular.stream().map(Object::toString).toList();
        return new RecommendationModel("TRENDING", null, productIds);
    }

    public RecommendationModel getRelatedProducts(String productId) {
        try {
            // 1. Always check cache for pre-ranked items first
            Set<Object> cached = redisTemplate.opsForZSet().reverseRange("product:related:" + productId, 0, 9);
            
            // If we have a healthy list of related items, return them (they are already sorted by score)
            if (cached != null && cached.size() >= MIN_CACHE_SIZE) {
                return new RecommendationModel("RELATED", productId, cached.stream().map(Object::toString).toList());
            }

            // 2. Refresh Logic: Fetch products to build/rebuild the relationship graph
            Map<String, Object> base = restTemplate.getForObject(PRODUCT_API + "/" + productId, Map.class);
            Map<String, Object>[] all = restTemplate.getForObject(PRODUCT_API, Map[].class);

            if (base == null || all == null) return new RecommendationModel("RELATED", productId, List.of());

            List<ProductScore> scoredProducts = new ArrayList<>();

            for (Map<String, Object> p : all) {
                String targetId = String.valueOf(p.get("productId"));
                if (targetId.equals(productId)) continue;

                // Calculate a deep similarity score
                double score = calculateSimilarityScore(base, p);

                // Only include products that have some baseline similarity
                if (score > 2.0) {
                    scoredProducts.add(new ProductScore(targetId, score));
                }
            }

            // 3. Sort by Score (Highest First)
            scoredProducts.sort(Comparator.comparingDouble(ProductScore::getScore).reversed());
            List<ProductScore> topMatches = scoredProducts.stream().limit(10).toList();

            // 4. Bidirectional Update: Ensure A knows B, and B knows A
            for (ProductScore match : topMatches) {
                // Add to Current Product's list
                redisTemplate.opsForZSet().add("product:related:" + productId, match.id, match.score);
                // Add Current Product to the Target's list (Symmetry)
                redisTemplate.opsForZSet().add("product:related:" + match.id, productId, match.score);
                
                redisTemplate.expire("product:related:" + productId, java.time.Duration.ofDays(7));
                redisTemplate.expire("product:related:" + match.id, java.time.Duration.ofDays(7));
            }

            List<String> resultIds = topMatches.stream().map(ProductScore::getId).toList();
            return new RecommendationModel("RELATED", productId, resultIds);

        } catch (Exception e) {
            return new RecommendationModel("RELATED", productId, List.of());
        }
    }

    private double calculateSimilarityScore(Map<String, Object> base, Map<String, Object> target) {
        double score = 0.0;

        // CRITICAL: Category Match (The most important factor)
        if (clean(base.get("category")).equals(clean(target.get("category")))) {
            score += 10.0;
        }

        // Brand Match
        if (clean(base.get("brand")).equals(clean(target.get("brand")))) {
            score += 5.0;
        }

        // Price Similarity (Higher score for products in the same price bracket)
        try {
            double p1 = Double.parseDouble(base.get("price").toString());
            double p2 = Double.parseDouble(target.get("price").toString());
            double diff = Math.abs(p1 - p2);
            if (diff <= p1 * 0.1) score += 8.0; // Within 10% price
            else if (diff <= p1 * 0.3) score += 4.0; // Within 30% price
        } catch (Exception e) {}

        // Semantic Match (Description Keywords)
        String baseDesc = clean(base.get("name") + " " + base.get("description"));
        String targetDesc = clean(target.get("name") + " " + target.get("description"));
        String[] keywords = baseDesc.split("\\s+");
        
        for (String word : keywords) {
            if (word.length() > 3 && targetDesc.contains(word)) {
                score += 1.2; // Every shared keyword boosts the "Related" rank
            }
        }

        return score;
    }

    private String clean(Object obj) {
        return obj != null ? obj.toString().trim().toLowerCase() : "";
    }

    public void logUserActivity(UserActivity activity) {
        String userKey = "recent:" + activity.getUsername();
        String trendingKey = "trending";
        String pId = activity.getProductId();

        // Update Recent List
        redisTemplate.opsForList().remove(userKey, 0, pId);
        redisTemplate.opsForList().leftPush(userKey, pId);
        redisTemplate.opsForList().trim(userKey, 0, 9);

        // Update Trending Scores
        int weight = switch(activity.getAction().toLowerCase()) {
            case "view" -> 1;
            case "like" -> 3;
            case "order" -> 10;
            default -> 1;
        };
        redisTemplate.opsForZSet().incrementScore(trendingKey, pId, weight);
    }

    private static class ProductScore {
        String id; double score;
        ProductScore(String id, double score) { this.id = id; this.score = score; }
        public String getId() { return id; }
        public double getScore() { return score; }
    }
}