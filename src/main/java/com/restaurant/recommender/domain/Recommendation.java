package com.restaurant.recommender.domain;

import java.util.List;

public record Recommendation(
    int rank,
    Restaurant restaurant,
    String explanation,
    List<String> tags
) {}
