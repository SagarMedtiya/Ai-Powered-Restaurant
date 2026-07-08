package com.restaurant.recommender.domain;

public record UserPreferences(
    String location,
    String cuisine,
    BudgetBand budget,
    double minRating,
    String additionalPreferences,
    int topK
) {}
