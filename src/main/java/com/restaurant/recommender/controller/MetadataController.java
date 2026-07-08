package com.restaurant.recommender.controller;

import com.restaurant.recommender.dto.response.MetadataResponse;
import com.restaurant.recommender.repository.RestaurantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MetadataController {

    private final RestaurantRepository repository;

    public MetadataController(RestaurantRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/metadata")
    public ResponseEntity<MetadataResponse> getMetadata() {
        boolean ready = repository.isReady();
        MetadataResponse response = new MetadataResponse(
            ready ? repository.getCities() : java.util.Set.of(),
            ready ? repository.getCuisines() : java.util.Set.of(),
            ready
        );
        return ResponseEntity.ok(response);
    }
}
