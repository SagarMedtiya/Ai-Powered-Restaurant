package com.restaurant.recommender.config;

import com.restaurant.recommender.domain.BudgetBand;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "app.recommendation")
public record RecommendationProperties(
    int maxCandidatesForLlm,
    int defaultTopK,
    BudgetConfig budget
) {
    public record BudgetConfig(int lowMax, int mediumMax) {
        public boolean isWithin(BudgetBand band, Integer costForTwo) {
            if (costForTwo == null) return false;
            return switch (band) {
                case LOW -> costForTwo <= lowMax;
                case MEDIUM -> costForTwo > lowMax && costForTwo <= mediumMax;
                case HIGH -> costForTwo > mediumMax;
            };
        }
    }
}
