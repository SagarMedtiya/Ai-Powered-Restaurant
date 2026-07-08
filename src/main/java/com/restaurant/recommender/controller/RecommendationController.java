package com.restaurant.recommender.controller;

import com.restaurant.recommender.domain.BudgetBand;
import com.restaurant.recommender.domain.UserPreferences;
import com.restaurant.recommender.dto.request.RecommendRequest;
import com.restaurant.recommender.dto.response.RecommendationItemDto;
import com.restaurant.recommender.dto.response.RecommendationResponse;
import com.restaurant.recommender.service.RecommendationService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping(value = "/recommend", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RecommendationResponse> recommend(@Valid @RequestBody RecommendRequest request) {
        var prefs = new UserPreferences(
            request.location(),
            request.cuisine(),
            request.budget(),
            request.minRating(),
            request.additionalPreferences(),
            request.topK() != null ? request.topK() : 0
        );

        var result = recommendationService.recommend(prefs);

        boolean usedFallback = result.recommendations().stream()
            .anyMatch(r -> r.tags().contains("fallback"));

        var items = result.recommendations().stream()
            .map(r -> new RecommendationItemDto(
                r.rank(),
                r.restaurant().name(),
                r.restaurant().city(),
                r.restaurant().location(),
                r.restaurant().cuisines(),
                r.restaurant().rating(),
                r.restaurant().costForTwo(),
                r.explanation(),
                r.tags()
            ))
            .toList();

        var response = new RecommendationResponse(
            items,
            result.summary(),
            result.candidatesConsidered(),
            usedFallback
        );

        return ResponseEntity.ok(response);
    }
}
