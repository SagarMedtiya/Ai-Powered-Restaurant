package com.restaurant.recommender.dto.response;

import java.util.List;

public record RecommendationItemDto(
    int rank,
    String restaurantName,
    String city,
    String location,
    List<String> cuisines,
    double rating,
    Integer costForTwo,
    String explanation,
    List<String> tags
) {}
