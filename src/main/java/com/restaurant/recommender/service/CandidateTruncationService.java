package com.restaurant.recommender.service;

import com.restaurant.recommender.config.RecommendationProperties;
import com.restaurant.recommender.domain.Restaurant;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class CandidateTruncationService {

    private static final Comparator<Restaurant> TRUNCATION_ORDER = Comparator
        .comparingDouble(Restaurant::rating).reversed()
        .thenComparing(Restaurant::costForTwo, Comparator.nullsLast(Comparator.naturalOrder()));

    private final int maxCandidatesForLlm;

    public CandidateTruncationService(RecommendationProperties properties) {
        this.maxCandidatesForLlm = properties.maxCandidatesForLlm();
    }

    public Candidates truncate(List<Restaurant> candidates) {
        List<Restaurant> sorted = candidates.stream()
            .sorted(TRUNCATION_ORDER)
            .limit(maxCandidatesForLlm)
            .toList();
        return new Candidates(candidates.size(), sorted);
    }
}
