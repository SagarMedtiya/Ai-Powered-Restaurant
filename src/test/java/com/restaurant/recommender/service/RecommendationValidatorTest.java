package com.restaurant.recommender.service;

import com.restaurant.recommender.domain.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecommendationValidatorTest {

    private final RecommendationValidator validator = new RecommendationValidator();

    private final Restaurant r1 = new Restaurant("1", "Tandoori Palace", "bangalore", "indiranagar",
        List.of("North Indian"), 4.5, 1200);
    private final Restaurant r2 = new Restaurant("2", "Pasta Paradise", "bangalore", "koramangala",
        List.of("Italian"), 4.3, 1400);
    private final Restaurant r3 = new Restaurant("3", "Sushi Zen", "mumbai", "bandra",
        List.of("Japanese"), 4.7, 2500);

    private final List<Restaurant> candidates = List.of(r1, r2, r3);

    @Test
    void shouldAcceptValidRecommendations() {
        var llmRecs = List.of(
            new LlmRecommendationResponse.LlmRecommendation("Tandoori Palace", 1, "Great food", List.of("best")),
            new LlmRecommendationResponse.LlmRecommendation("Pasta Paradise", 2, "Loved it", List.of())
        );
        var llmResponse = new LlmRecommendationResponse("Two great options", llmRecs);

        var result = validator.validate(llmResponse, candidates, 5);

        assertEquals(2, result.recommendations().size());
        assertEquals("Tandoori Palace", result.recommendations().get(0).restaurant().name());
        assertEquals("Pasta Paradise", result.recommendations().get(1).restaurant().name());
        assertEquals(3, result.candidatesConsidered());
    }

    @Test
    void shouldRejectHallucinatedNames() {
        var llmRecs = List.of(
            new LlmRecommendationResponse.LlmRecommendation("Tandoori Palace", 1, "Great", List.of()),
            new LlmRecommendationResponse.LlmRecommendation("Fake Restaurant", 2, "Not real", List.of()),
            new LlmRecommendationResponse.LlmRecommendation("Pasta Paradise", 3, "Nice", List.of())
        );
        var llmResponse = new LlmRecommendationResponse("Summary", llmRecs);

        var result = validator.validate(llmResponse, candidates, 5);

        assertEquals(2, result.recommendations().size());
        assertTrue(result.recommendations().stream().noneMatch(r -> r.restaurant().name().equals("Fake Restaurant")));
    }

    @Test
    void shouldRejectNullName() {
        var llmRecs = List.of(
            new LlmRecommendationResponse.LlmRecommendation(null, 1, "No name", List.of())
        );
        var llmResponse = new LlmRecommendationResponse("Summary", llmRecs);

        var result = validator.validate(llmResponse, candidates, 5);

        assertTrue(result.recommendations().isEmpty());
    }

    @Test
    void shouldRejectBlankName() {
        var llmRecs = List.of(
            new LlmRecommendationResponse.LlmRecommendation("", 1, "Blank name", List.of())
        );
        var llmResponse = new LlmRecommendationResponse("Summary", llmRecs);

        var result = validator.validate(llmResponse, candidates, 5);

        assertTrue(result.recommendations().isEmpty());
    }

    @Test
    void shouldHandleNullLlmResponse() {
        var result = validator.validate(null, candidates, 5);

        assertTrue(result.recommendations().isEmpty());
        assertTrue(result.summary().isEmpty());
        assertEquals(3, result.candidatesConsidered());
    }

    @Test
    void shouldHandleNullRecommendationsList() {
        var llmResponse = new LlmRecommendationResponse("Summary", null);

        var result = validator.validate(llmResponse, candidates, 5);

        assertTrue(result.recommendations().isEmpty());
    }

    @Test
    void shouldCapAtTopK() {
        var llmRecs = List.of(
            new LlmRecommendationResponse.LlmRecommendation("Tandoori Palace", 1, "A", List.of()),
            new LlmRecommendationResponse.LlmRecommendation("Pasta Paradise", 2, "B", List.of()),
            new LlmRecommendationResponse.LlmRecommendation("Sushi Zen", 3, "C", List.of())
        );
        var llmResponse = new LlmRecommendationResponse("Summary", llmRecs);

        var result = validator.validate(llmResponse, candidates, 2);

        assertEquals(2, result.recommendations().size());
    }

    @Test
    void shouldRenumberRanksAfterDroppingHallucinations() {
        var llmRecs = List.of(
            new LlmRecommendationResponse.LlmRecommendation("Tandoori Palace", 1, "Great", List.of()),
            new LlmRecommendationResponse.LlmRecommendation("Fake Restaurant", 2, "Not real", List.of()),
            new LlmRecommendationResponse.LlmRecommendation("Pasta Paradise", 3, "Nice", List.of())
        );
        var llmResponse = new LlmRecommendationResponse("Summary", llmRecs);

        var result = validator.validate(llmResponse, candidates, 5);

        assertEquals(1, result.recommendations().get(0).rank());
        assertEquals(2, result.recommendations().get(1).rank());
    }

    @Test
    void shouldMatchCaseInsensitive() {
        var llmRecs = List.of(
            new LlmRecommendationResponse.LlmRecommendation("tandoori palace", 1, "Great", List.of())
        );
        var llmResponse = new LlmRecommendationResponse("Summary", llmRecs);

        var result = validator.validate(llmResponse, candidates, 5);

        assertEquals(1, result.recommendations().size());
        assertEquals("Tandoori Palace", result.recommendations().get(0).restaurant().name());
    }

    @Test
    void shouldReturnEmptyWhenAllRejected() {
        var llmRecs = List.of(
            new LlmRecommendationResponse.LlmRecommendation("Fake One", 1, "Fake", List.of()),
            new LlmRecommendationResponse.LlmRecommendation("Fake Two", 2, "Fake", List.of())
        );
        var llmResponse = new LlmRecommendationResponse("Summary", llmRecs);

        var result = validator.validate(llmResponse, candidates, 5);

        assertTrue(result.recommendations().isEmpty());
    }
}
