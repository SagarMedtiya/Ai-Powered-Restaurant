package com.restaurant.recommender.service;

import com.restaurant.recommender.config.RecommendationProperties;
import com.restaurant.recommender.domain.BudgetBand;
import com.restaurant.recommender.domain.Restaurant;
import com.restaurant.recommender.domain.UserPreferences;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantFilterService {

    private final RecommendationProperties properties;

    public RestaurantFilterService(RecommendationProperties properties) {
        this.properties = properties;
    }

    public List<Restaurant> filter(List<Restaurant> candidates, UserPreferences prefs) {
        return candidates.stream()
            .filter(r -> matchesLocation(r, prefs.location()))
            .filter(r -> matchesCuisine(r, prefs.cuisine()))
            .filter(r -> matchesRating(r, prefs.minRating()))
            .filter(r -> matchesBudget(r, prefs.budget()))
            .toList();
    }

    boolean matchesLocation(Restaurant restaurant, String query) {
        if (query == null || query.isBlank()) return true;
        String q = query.toLowerCase().strip();
        return restaurant.city().toLowerCase().contains(q)
            || restaurant.location().toLowerCase().contains(q);
    }

    boolean matchesCuisine(Restaurant restaurant, String query) {
        if (query == null || query.isBlank()) return true;
        String q = query.toLowerCase().strip();
        return restaurant.cuisines().stream()
            .anyMatch(c -> c.toLowerCase().contains(q));
    }

    boolean matchesRating(Restaurant restaurant, double minRating) {
        return restaurant.rating() >= minRating;
    }

    boolean matchesBudget(Restaurant restaurant, BudgetBand budget) {
        if (budget == null) return true;
        return properties.budget().isWithin(budget, restaurant.costForTwo());
    }
}
