package com.restaurant.recommender.dto.response;

import java.util.List;

public record RecommendationResponse(
    List<RecommendationItemDto> recommendations,
    String summary,
    int candidatesConsidered,
    boolean usedFallback
) {}
