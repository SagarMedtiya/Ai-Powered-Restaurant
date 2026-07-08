package com.restaurant.recommender.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record LlmRecommendationResponse(
    String summary,
    List<LlmRecommendation> recommendations
) {
    public record LlmRecommendation(
        @JsonProperty("restaurantName") String restaurantName,
        int rank,
        String explanation,
        List<String> tags
    ) {}
}
