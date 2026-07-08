package com.restaurant.recommender.service;

import com.restaurant.recommender.config.RecommendationProperties;
import com.restaurant.recommender.domain.Recommendation;
import com.restaurant.recommender.domain.RecommendationResult;
import com.restaurant.recommender.domain.Restaurant;
import com.restaurant.recommender.domain.UserPreferences;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FallbackRankingService {

    private final int defaultTopK;

    public FallbackRankingService(RecommendationProperties properties) {
        this.defaultTopK = properties.defaultTopK();
    }

    public RecommendationResult fallback(List<Restaurant> candidates, UserPreferences prefs, int topK) {
        int k = Math.min(topK > 0 ? topK : defaultTopK, candidates.size());
        List<Recommendation> recommendations = candidates.stream()
            .sorted(Comparator.comparingDouble(Restaurant::rating).reversed())
            .limit(k)
            .map(r -> {
                String explanation = buildExplanation(r, prefs);
                return new Recommendation(0, r, explanation, List.of("fallback"));
            })
            .collect(Collectors.toCollection(ArrayList::new));

        List<Recommendation> ranked = renumber(recommendations);
        return new RecommendationResult(ranked, buildSummary(ranked.size(), candidates.size()), candidates.size());
    }

    private String buildExplanation(Restaurant r, UserPreferences prefs) {
        String cuisine = prefs.cuisine() != null && !prefs.cuisine().isBlank() ? prefs.cuisine() : "food";
        String location = prefs.location() != null && !prefs.location().isBlank() ? prefs.location() : "your area";
        return String.format(
            "Top-rated %s option in %s within your budget and rating criteria.",
            cuisine, location
        );
    }

    private String buildSummary(int returned, int totalConsidered) {
        if (returned == 0) {
            return "No matching restaurants found. Try adjusting your filters.";
        }
        return String.format(
            "Found %d matching restaurant(s) based on your preferences (from %d candidates).",
            returned, totalConsidered
        );
    }

    private List<Recommendation> renumber(List<Recommendation> recommendations) {
        for (int i = 0; i < recommendations.size(); i++) {
            Recommendation r = recommendations.get(i);
            recommendations.set(i, new Recommendation(i + 1, r.restaurant(), r.explanation(), r.tags()));
        }
        return recommendations;
    }
}
