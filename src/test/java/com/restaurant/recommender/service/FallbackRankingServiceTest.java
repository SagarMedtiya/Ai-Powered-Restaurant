package com.restaurant.recommender.service;

import com.restaurant.recommender.config.RecommendationProperties;
import com.restaurant.recommender.domain.BudgetBand;
import com.restaurant.recommender.domain.Restaurant;
import com.restaurant.recommender.domain.UserPreferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FallbackRankingServiceTest {

    private FallbackRankingService service;

    private final Restaurant r1 = new Restaurant("1", "Low Rated", "c", "l",
        List.of("A"), 2.0, 300);
    private final Restaurant r2 = new Restaurant("2", "High Rated", "c", "l",
        List.of("B"), 4.8, 800);
    private final Restaurant r3 = new Restaurant("3", "Mid Rated", "c", "l",
        List.of("C"), 3.5, 2000);
    private final Restaurant r4 = new Restaurant("4", "Top Rated", "c", "l",
        List.of("D"), 5.0, 400);

    @BeforeEach
    void setUp() {
        var budget = new RecommendationProperties.BudgetConfig(500, 1500);
        var props = new RecommendationProperties(25, 5, budget);
        service = new FallbackRankingService(props);
    }

    @Test
    void shouldRankByRatingDesc() {
        var prefs = new UserPreferences("Bangalore", "Italian", BudgetBand.MEDIUM, 0.0, null, 5);
        var result = service.fallback(List.of(r1, r2, r3, r4), prefs, 5);

        assertEquals(4, result.recommendations().size());
        assertEquals("Top Rated", result.recommendations().get(0).restaurant().name());
        assertEquals("High Rated", result.recommendations().get(1).restaurant().name());
        assertEquals("Mid Rated", result.recommendations().get(2).restaurant().name());
        assertEquals("Low Rated", result.recommendations().get(3).restaurant().name());
    }

    @Test
    void shouldRespectTopK() {
        var prefs = new UserPreferences("Bangalore", "Italian", BudgetBand.MEDIUM, 0.0, null, 5);
        var result = service.fallback(List.of(r1, r2, r3, r4), prefs, 2);

        assertEquals(2, result.recommendations().size());
        assertEquals("Top Rated", result.recommendations().get(0).restaurant().name());
        assertEquals("High Rated", result.recommendations().get(1).restaurant().name());
    }

    @Test
    void shouldHandleEmptyCandidates() {
        var prefs = new UserPreferences("Bangalore", "Italian", BudgetBand.MEDIUM, 0.0, null, 5);
        var result = service.fallback(List.of(), prefs, 5);

        assertTrue(result.recommendations().isEmpty());
        assertEquals("No matching restaurants found. Try adjusting your filters.", result.summary());
    }

    @Test
    void shouldHandleSingleCandidate() {
        var prefs = new UserPreferences("Mumbai", "Chinese", BudgetBand.HIGH, 0.0, null, 5);
        var result = service.fallback(List.of(r3), prefs, 5);

        assertEquals(1, result.recommendations().size());
        assertEquals("Mid Rated", result.recommendations().get(0).restaurant().name());
    }

    @Test
    void shouldUseDefaultTopKWhenTopKIsZero() {
        var prefs = new UserPreferences("Bangalore", "Italian", BudgetBand.MEDIUM, 0.0, null, 0);
        var result = service.fallback(List.of(r1, r2, r3, r4), prefs, 0);

        assertEquals(4, result.recommendations().size());
    }

    @Test
    void shouldRenumberRanks() {
        var prefs = new UserPreferences("Bangalore", "Italian", BudgetBand.MEDIUM, 0.0, null, 5);
        var result = service.fallback(List.of(r2, r4), prefs, 5);

        assertEquals(1, result.recommendations().get(0).rank());
        assertEquals(2, result.recommendations().get(1).rank());
    }

    @Test
    void shouldGenerateExplanations() {
        var prefs = new UserPreferences("Bangalore", "Italian", BudgetBand.MEDIUM, 0.0, null, 5);
        var result = service.fallback(List.of(r2), prefs, 5);

        assertEquals(1, result.recommendations().size());
        String explanation = result.recommendations().get(0).explanation();
        assertTrue(explanation.contains("Italian"));
        assertTrue(explanation.contains("Bangalore"));
    }

    @Test
    void shouldReportCandidatesConsidered() {
        var prefs = new UserPreferences("Bangalore", "Italian", BudgetBand.MEDIUM, 0.0, null, 5);
        var result = service.fallback(List.of(r1, r2, r3), prefs, 2);

        assertEquals(3, result.candidatesConsidered());
        assertEquals(2, result.recommendations().size());
    }

    @Test
    void shouldAddFallbackTag() {
        var prefs = new UserPreferences("Bangalore", "Italian", BudgetBand.MEDIUM, 0.0, null, 5);
        var result = service.fallback(List.of(r2), prefs, 5);

        assertTrue(result.recommendations().get(0).tags().contains("fallback"));
    }
}
