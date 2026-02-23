package com.ecommerce.recommendation_service.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecommendationModel {
    private String type;                      
    private String baseProductId;             
    private List<String> productIds;
}
