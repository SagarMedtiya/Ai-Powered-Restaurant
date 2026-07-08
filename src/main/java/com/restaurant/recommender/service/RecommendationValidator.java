package com.restaurant.recommender.service;

import com.restaurant.recommender.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationValidator {

    private static final Logger log = LoggerFactory.getLogger(RecommendationValidator.class);

    public RecommendationResult validate(LlmRecommendationResponse llmResponse, List<Restaurant> candidates, int topK) {
        if (llmResponse == null || llmResponse.recommendations() == null) {
            log.warn("LLM returned null response");
            return new RecommendationResult(List.of(), "", candidates.size());
        }

        Map<String, Restaurant> candidateMap = candidates.stream()
            .collect(Collectors.toMap(
                r -> r.name().toLowerCase().strip(),
                r -> r,
                (a, b) -> a
            ));

        List<Recommendation> valid = new ArrayList<>();
        for (var llmRec : llmResponse.recommendations()) {
            String name = llmRec.restaurantName();
            if (name == null || name.isBlank()) {
                log.warn("LLM returned recommendation with null/blank name, skipping");
                continue;
            }
            Restaurant restaurant = candidateMap.get(name.toLowerCase().strip());
            if (restaurant == null) {
                log.warn("LLM hallucinated restaurant '{}' — not in candidate set, dropping", name);
                continue;
            }
            valid.add(new Recommendation(llmRec.rank(), restaurant, llmRec.explanation(), llmRec.tags()));
        }

        if (valid.size() > topK) {
            valid = valid.subList(0, topK);
        }

        renumberRanks(valid);

        String summary = llmResponse.summary() != null ? llmResponse.summary() : "";
        return new RecommendationResult(valid, summary, candidates.size());
    }

    private void renumberRanks(List<Recommendation> recommendations) {
        for (int i = 0; i < recommendations.size(); i++) {
            Recommendation r = recommendations.get(i);
            recommendations.set(i, new Recommendation(i + 1, r.restaurant(), r.explanation(), r.tags()));
        }
    }
}
