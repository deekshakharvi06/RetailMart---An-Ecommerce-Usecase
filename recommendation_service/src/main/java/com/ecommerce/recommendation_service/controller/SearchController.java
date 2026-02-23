package com.ecommerce.recommendation_service.controller;

import com.ecommerce.recommendation_service.service.SearchService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173",allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<?> searchProducts(@RequestParam("query") String query) {
        var result = searchService.searchProducts(query);
        return ResponseEntity.ok(result);
    }
}
