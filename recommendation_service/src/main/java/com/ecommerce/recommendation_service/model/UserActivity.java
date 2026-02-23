package com.ecommerce.recommendation_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a user’s interaction with a product.
 * It is the input model for logging activity in Redis.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserActivity {

    // The username performing the action
    private String username;

    // The product ID the user interacted with
    private String productId;

    // Action type: "view", "like", or "order"
    private String action;

    // Optional metadata for future recommendations
    private String category;
    private String brand;
}
