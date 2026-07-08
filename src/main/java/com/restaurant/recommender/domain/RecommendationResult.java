package com.restaurant.recommender.domain;

import java.util.List;

public record RecommendationResult(
    List<Recommendation> recommendations,
    String summary,
    int candidatesConsidered
) {}
