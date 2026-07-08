package com.restaurant.recommender.service;

import com.restaurant.recommender.domain.Restaurant;
import java.util.List;

public record Candidates(int originalCount, List<Restaurant> truncatedList) {
    public static Candidates from(List<Restaurant> candidates) {
        return new Candidates(candidates.size(), candidates);
    }
}
