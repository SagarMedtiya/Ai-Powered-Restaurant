package com.restaurant.recommender.dto.request;

import com.restaurant.recommender.domain.BudgetBand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record RecommendRequest(
    @Size(max = 100, message = "Location must be at most 100 characters")
    String location,

    @Size(max = 100, message = "Cuisine must be at most 100 characters")
    String cuisine,

    BudgetBand budget,

    @Min(value = 0, message = "Minimum rating must be between 0 and 5")
    @Max(value = 5, message = "Minimum rating must be between 0 and 5")
    double minRating,

    @Size(max = 500, message = "Additional preferences must be at most 500 characters")
    String additionalPreferences,

    @Min(value = 1, message = "topK must be at least 1")
    @Max(value = 20, message = "topK must be at most 20")
    Integer topK
) {}
