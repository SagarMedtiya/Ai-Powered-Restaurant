package com.restaurant.recommender.service;

import com.restaurant.recommender.config.RecommendationProperties;
import com.restaurant.recommender.domain.Restaurant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CandidateTruncationServiceTest {

    private CandidateTruncationService serviceWithSmallCap;
    private CandidateTruncationService serviceWithLargeCap;

    private final Restaurant r1 = new Restaurant("1", "Low Rated Cheap", "c", "l",
        List.of("A"), 2.0, 300);
    private final Restaurant r2 = new Restaurant("2", "High Rated Mid", "c", "l",
        List.of("A"), 4.8, 800);
    private final Restaurant r3 = new Restaurant("3", "Mid Rated Expensive", "c", "l",
        List.of("A"), 3.5, 2000);
    private final Restaurant r4 = new Restaurant("4", "High Rated Cheap", "c", "l",
        List.of("A"), 4.8, 400);
    private final Restaurant r5 = new Restaurant("5", "Top Rated Null Cost", "c", "l",
        List.of("A"), 5.0, null);

    private final List<Restaurant> candidates = List.of(r1, r2, r3, r4, r5);

    @BeforeEach
    void setUp() {
        var budget = new RecommendationProperties.BudgetConfig(500, 1500);
        var propsSmall = new RecommendationProperties(3, 5, budget);
        var propsLarge = new RecommendationProperties(100, 5, budget);
        serviceWithSmallCap = new CandidateTruncationService(propsSmall);
        serviceWithLargeCap = new CandidateTruncationService(propsLarge);
    }

    @Test
    void shouldReturnAllWhenUnderCap() {
        var result = serviceWithLargeCap.truncate(candidates);
        assertEquals(5, result.truncatedList().size());
        assertEquals(5, result.originalCount());
    }

    @Test
    void shouldReturnAllWhenExactlyAtCap() {
        var props = new RecommendationProperties(5, 5, new RecommendationProperties.BudgetConfig(500, 1500));
        var svc = new CandidateTruncationService(props);
        var result = svc.truncate(candidates);
        assertEquals(5, result.truncatedList().size());
        assertEquals(5, result.originalCount());
    }

    @Test
    void shouldTruncateAndSortByRatingDescThenCostAsc() {
        var result = serviceWithSmallCap.truncate(candidates);
        assertEquals(3, result.truncatedList().size());
        assertEquals(5, result.originalCount());

        var list = result.truncatedList();
        assertEquals("5", list.get(0).id());
        assertEquals("4", list.get(1).id());
        assertEquals("2", list.get(2).id());
    }

    @Test
    void shouldHandleEmptyList() {
        var result = serviceWithSmallCap.truncate(List.of());
        assertEquals(0, result.truncatedList().size());
        assertEquals(0, result.originalCount());
    }

    @Test
    void shouldHandleSingleElement() {
        var result = serviceWithSmallCap.truncate(List.of(r1));
        assertEquals(1, result.truncatedList().size());
        assertEquals(1, result.originalCount());
        assertEquals("1", result.truncatedList().get(0).id());
    }

    @Test
    void shouldPreserveOriginalCountWhenTruncated() {
        var result = serviceWithSmallCap.truncate(candidates);
        assertEquals(5, result.originalCount());
        assertEquals(3, result.truncatedList().size());
    }

    @Test
    void shouldPlaceNullCostLastWhenRatingsEqual() {
        var rHighWithCost = new Restaurant("10", "High With Cost", "c", "l",
            List.of("A"), 5.0, 1000);
        var list = List.of(r5, rHighWithCost);
        var result = serviceWithLargeCap.truncate(list);

        assertEquals(2, result.truncatedList().size());
        assertEquals("10", result.truncatedList().get(0).id());
        assertEquals("5", result.truncatedList().get(1).id());
    }

    @Test
    void shouldSortEvenWhenUnderCap() {
        var list = List.of(r3, r1, r4, r2, r5);
        var result = serviceWithLargeCap.truncate(list);
        assertEquals(List.of(r5, r4, r2, r3, r1), result.truncatedList());
    }
}
