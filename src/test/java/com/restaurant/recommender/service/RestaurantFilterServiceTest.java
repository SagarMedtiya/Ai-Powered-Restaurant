package com.restaurant.recommender.service;

import com.restaurant.recommender.config.RecommendationProperties;
import com.restaurant.recommender.domain.BudgetBand;
import com.restaurant.recommender.domain.Restaurant;
import com.restaurant.recommender.domain.UserPreferences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantFilterServiceTest {

    private RestaurantFilterService service;

    private final Restaurant r1 = new Restaurant("1", "Tandoori Palace", "bangalore", "indiranagar",
        List.of("North Indian", "Mughlai"), 4.5, 1200);
    private final Restaurant r2 = new Restaurant("2", "Pasta Paradise", "bangalore", "koramangala",
        List.of("Italian", "Continental"), 4.3, 1400);
    private final Restaurant r3 = new Restaurant("3", "Sushi Zen", "mumbai", "bandra",
        List.of("Japanese", "Sushi"), 4.7, 2500);
    private final Restaurant r4 = new Restaurant("4", "Dosa Express", "bangalore", "jayanagar",
        List.of("South Indian"), 3.9, 300);
    private final Restaurant r5 = new Restaurant("5", "Pizza Night", "bangalore", "indiranagar",
        List.of("Italian", "Fast Food"), 4.1, null);
    private final Restaurant r6 = new Restaurant("6", "Empty City", "", "",
        List.of("Other"), 0.0, null);

    private final List<Restaurant> all = List.of(r1, r2, r3, r4, r5, r6);

    @BeforeEach
    void setUp() {
        var budget = new RecommendationProperties.BudgetConfig(500, 1500);
        var props = new RecommendationProperties(25, 5, budget);
        service = new RestaurantFilterService(props);
    }

    @Test
    void shouldFilterByLocationExact() {
        var prefs = new UserPreferences("Bangalore", null, null, 0.0, null, 5);
        var result = service.filter(all, prefs);
        assertEquals(List.of(r1, r2, r4, r5), result);
    }

    @Test
    void shouldFilterByLocationSubstring() {
        var prefs = new UserPreferences("ban", null, null, 0.0, null, 5);
        var result = service.filter(all, prefs);
        assertEquals(List.of(r1, r2, r3, r4, r5), result);
    }

    @Test
    void shouldFilterByLocationInLocationField() {
        var prefs = new UserPreferences("Koramangala", null, null, 0.0, null, 5);
        var result = service.filter(all, prefs);
        assertEquals(List.of(r2), result);
    }

    @Test
    void shouldFilterByLocationAndCityField() {
        var prefs = new UserPreferences("indiranagar", null, null, 0.0, null, 5);
        var result = service.filter(all, prefs);
        assertEquals(List.of(r1, r5), result);
    }

    @Test
    void shouldReturnAllWhenLocationIsNull() {
        var prefs = new UserPreferences(null, null, null, 0.0, null, 5);
        var result = service.filter(all, prefs);
        assertEquals(all, result);
    }

    @Test
    void shouldReturnAllWhenLocationIsBlank() {
        var prefs = new UserPreferences("", null, null, 0.0, null, 5);
        var result = service.filter(all, prefs);
        assertEquals(all, result);
    }

    @Test
    void shouldReturnEmptyWhenLocationNoMatch() {
        var prefs = new UserPreferences("chennai", null, null, 0.0, null, 5);
        var result = service.filter(all, prefs);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFilterByCuisineExact() {
        var prefs = new UserPreferences(null, "Italian", null, 0.0, null, 5);
        var result = service.filter(all, prefs);
        assertEquals(List.of(r2, r5), result);
    }

    @Test
    void shouldFilterByCuisineSubstring() {
        var prefs = new UserPreferences(null, "ital", null, 0.0, null, 5);
        var result = service.filter(all, prefs);
        assertEquals(List.of(r2, r5), result);
    }

    @Test
    void shouldFilterByCuisineCaseInsensitive() {
        var prefs = new UserPreferences(null, "italian", null, 0.0, null, 5);
        var result = service.filter(all, prefs);
        assertEquals(List.of(r2, r5), result);
    }

    @Test
    void shouldReturnAllWhenCuisineIsNull() {
        var prefs = new UserPreferences(null, null, null, 0.0, null, 5);
        var result = service.filter(all, prefs);
        assertEquals(all, result);
    }

    @Test
    void shouldReturnAllWhenCuisineIsBlank() {
        var prefs = new UserPreferences(null, "", null, 0.0, null, 5);
        var result = service.filter(all, prefs);
        assertEquals(all, result);
    }

    @Test
    void shouldReturnEmptyWhenCuisineNoMatch() {
        var prefs = new UserPreferences(null, "Thai", null, 0.0, null, 5);
        var result = service.filter(all, prefs);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFilterByMinRating() {
        var prefs = new UserPreferences(null, null, null, 4.0, null, 5);
        var result = service.filter(all, prefs);
        assertEquals(List.of(r1, r2, r3, r5), result);
    }

    @Test
    void shouldFilterByMinRatingExactBoundary() {
        var prefs = new UserPreferences(null, null, null, 4.5, null, 5);
        var result = service.filter(all, prefs);
        assertEquals(List.of(r1, r3), result);
    }

    @Test
    void shouldIncludeZeroRatingWhenMinRatingIsZero() {
        var prefs = new UserPreferences(null, null, null, 0.0, null, 5);
        var result = service.filter(all, prefs);
        assertEquals(all, result);
    }

    @Test
    void shouldPassHighRatingThreshold() {
        var prefs = new UserPreferences(null, null, null, 5.0, null, 5);
        var result = service.filter(all, prefs);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldFilterByLowBudget() {
        var prefs = new UserPreferences(null, null, BudgetBand.LOW, 0.0, null, 5);
        var result = service.filter(all, prefs);
        assertEquals(List.of(r4), result);
    }

    @Test
    void shouldFilterByMediumBudget() {
        var prefs = new UserPreferences(null, null, BudgetBand.MEDIUM, 0.0, null, 5);
        var result = service.filter(all, prefs);
        assertEquals(List.of(r1, r2), result);
    }

    @Test
    void shouldFilterByHighBudget() {
        var prefs = new UserPreferences(null, null, BudgetBand.HIGH, 0.0, null, 5);
        var result = service.filter(all, prefs);
        assertEquals(List.of(r3), result);
    }

    @Test
    void shouldExcludeNullCostForTwoWhenBudgetFiltered() {
        var prefs = new UserPreferences(null, null, BudgetBand.MEDIUM, 0.0, null, 5);
        var result = service.filter(all, prefs);
        assertFalse(result.contains(r5));
    }

    @Test
    void shouldReturnAllWhenBudgetIsNull() {
        var prefs = new UserPreferences(null, null, null, 0.0, null, 5);
        var result = service.filter(all, prefs);
        assertEquals(all, result);
    }

    @Test
    void shouldApplyAllFiltersSimultaneously() {
        var prefs = new UserPreferences("bangalore", "Italian", BudgetBand.MEDIUM, 4.0, null, 5);
        var result = service.filter(all, prefs);
        assertEquals(List.of(r2), result);
    }

    @Test
    void shouldReturnEmptyForImpossibleCombination() {
        var prefs = new UserPreferences("mumbai", "Italian", BudgetBand.LOW, 4.5, null, 5);
        var result = service.filter(all, prefs);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldNotThrowOnEmptyInput() {
        var prefs = new UserPreferences("bangalore", null, null, 0.0, null, 5);
        var result = service.filter(List.of(), prefs);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldMatchLocationWhenQueryIsEmptyString() {
        var prefs = new UserPreferences("", null, null, 0.0, null, 5);
        var result = service.filter(all, prefs);
        assertEquals(6, result.size());
    }
}
