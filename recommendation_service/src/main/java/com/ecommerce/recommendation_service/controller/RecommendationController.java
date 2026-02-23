package com.ecommerce.recommendation_service.controller;

import com.ecommerce.recommendation_service.model.RecommendationModel;
import com.ecommerce.recommendation_service.model.UserActivity;
import com.ecommerce.recommendation_service.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/recommendations")
@CrossOrigin(origins = "http://localhost:5173",allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    // Recently viewed
    @GetMapping("/recent/{username}")
    public RecommendationModel getRecent(@PathVariable String username) {
        return recommendationService.getRecentViews(username);
    }

    // Trending
    @GetMapping("/trending")
    public RecommendationModel getTrending() {
        return recommendationService.getTrendingProducts();
    }

    // Related
    @GetMapping("/related/{productId}")
    public RecommendationModel getRelated(@PathVariable String productId) {
        return recommendationService.getRelatedProducts(productId);
    }

    // user Activity 
    @PostMapping("/activity")
    public ResponseEntity<String> logActivity(@RequestBody UserActivity activity) {
        recommendationService.logUserActivity(activity);
        return ResponseEntity.ok("Activity logged successfully");
    }
}
