package com.restaurant.recommender.service;

import com.restaurant.recommender.config.RecommendationProperties;
import com.restaurant.recommender.domain.*;
import com.restaurant.recommender.repository.RestaurantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private RestaurantRepository repository;
    @Mock
    private RestaurantFilterService filterService;
    @Mock
    private CandidateTruncationService truncationService;
    @Mock
    private PromptBuilderService promptBuilder;
    @Mock
    private GroqClient groqClient;
    @Mock
    private RecommendationValidator validator;
    @Mock
    private FallbackRankingService fallback;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RecommendationService service;

    private final Restaurant r1 = new Restaurant("1", "Tandoori Palace", "bangalore", "indiranagar",
        List.of("North Indian"), 4.5, 1200);
    private final Restaurant r2 = new Restaurant("2", "Pasta Paradise", "bangalore", "koramangala",
        List.of("Italian"), 4.3, 1400);

    private final List<Restaurant> allRestaurants = List.of(r1, r2);
    private final List<Restaurant> filtered = List.of(r1, r2);
    private final Candidates truncated = new Candidates(2, filtered);

    @BeforeEach
    void setUp() {
        var budget = new RecommendationProperties.BudgetConfig(500, 1500);
        var props = new RecommendationProperties(25, 5, budget);
        service = new RecommendationService(repository, filterService, truncationService,
            promptBuilder, groqClient, validator, fallback, objectMapper, props);
    }

    @Test
    void shouldReturnLlmRecommendationsWhenSuccessful() throws Exception {
        var prefs = new UserPreferences("bangalore", "Italian", BudgetBand.MEDIUM, 4.0, null, 3);
        var llmResponse = new LlmRecommendationResponse("Great picks", List.of(
            new LlmRecommendationResponse.LlmRecommendation("Tandoori Palace", 1, "Great", List.of()),
            new LlmRecommendationResponse.LlmRecommendation("Pasta Paradise", 2, "Nice", List.of())
        ));
        var recommendationResult = new RecommendationResult(List.of(
            new Recommendation(1, r1, "Great", List.of()),
            new Recommendation(2, r2, "Nice", List.of())
        ), "Great picks", 2);

        when(repository.findAll()).thenReturn(allRestaurants);
        when(filterService.filter(allRestaurants, prefs)).thenReturn(filtered);
        when(truncationService.truncate(filtered)).thenReturn(truncated);
        when(promptBuilder.buildSystemPrompt()).thenReturn("system prompt");
        when(promptBuilder.buildUserPrompt(prefs, truncated.truncatedList(), 3)).thenReturn("user prompt");
        when(groqClient.call("system prompt", "user prompt")).thenReturn(objectMapper.writeValueAsString(llmResponse));
        when(validator.validate(any(), eq(truncated.truncatedList()), eq(3))).thenReturn(recommendationResult);

        var result = service.recommend(prefs);

        assertEquals(2, result.recommendations().size());
        assertEquals("Great picks", result.summary());
        verify(repository).findAll();
        verify(groqClient).call("system prompt", "user prompt");
    }

    @Test
    void shouldUseFallbackWhenFiltersReturnEmpty() {
        var prefs = new UserPreferences("nowhere", "Unknown", BudgetBand.LOW, 5.0, null, 3);
        var emptyFallback = new RecommendationResult(List.of(), "No matching restaurants found. Try adjusting your filters.", 0);

        when(repository.findAll()).thenReturn(allRestaurants);
        when(filterService.filter(allRestaurants, prefs)).thenReturn(List.of());
        when(fallback.fallback(List.of(), prefs, 3)).thenReturn(emptyFallback);

        var result = service.recommend(prefs);

        assertTrue(result.recommendations().isEmpty());
        verify(groqClient, never()).call(anyString(), anyString());
    }

    @Test
    void shouldUseFallbackWhenGroqClientThrows() {
        var prefs = new UserPreferences("bangalore", "Italian", BudgetBand.MEDIUM, 4.0, null, 3);
        var fallbackResult = new RecommendationResult(List.of(
            new Recommendation(1, r1, "Fallback", List.of("fallback"))
        ), "Fallback summary", 2);

        when(repository.findAll()).thenReturn(allRestaurants);
        when(filterService.filter(allRestaurants, prefs)).thenReturn(filtered);
        when(truncationService.truncate(filtered)).thenReturn(truncated);
        when(promptBuilder.buildSystemPrompt()).thenReturn("system");
        when(promptBuilder.buildUserPrompt(prefs, truncated.truncatedList(), 3)).thenReturn("user");
        when(groqClient.call("system", "user")).thenThrow(new RuntimeException("API error"));
        when(fallback.fallback(truncated.truncatedList(), prefs, 3)).thenReturn(fallbackResult);

        var result = service.recommend(prefs);

        assertEquals(1, result.recommendations().size());
        assertTrue(result.recommendations().get(0).tags().contains("fallback"));
    }

    @Test
    void shouldUseFallbackWhenJsonParseFails() {
        var prefs = new UserPreferences("bangalore", "Italian", BudgetBand.MEDIUM, 4.0, null, 3);
        var fallbackResult = new RecommendationResult(List.of(
            new Recommendation(1, r1, "Fallback", List.of("fallback"))
        ), "Fallback summary", 2);

        when(repository.findAll()).thenReturn(allRestaurants);
        when(filterService.filter(allRestaurants, prefs)).thenReturn(filtered);
        when(truncationService.truncate(filtered)).thenReturn(truncated);
        when(promptBuilder.buildSystemPrompt()).thenReturn("system");
        when(promptBuilder.buildUserPrompt(prefs, truncated.truncatedList(), 3)).thenReturn("user");
        when(groqClient.call("system", "user")).thenReturn("invalid json{{{");
        when(fallback.fallback(truncated.truncatedList(), prefs, 3)).thenReturn(fallbackResult);

        var result = service.recommend(prefs);

        assertEquals(1, result.recommendations().size());
        assertTrue(result.recommendations().get(0).tags().contains("fallback"));
    }

    @Test
    void shouldUseFallbackWhenValidatorReturnsEmpty() throws Exception {
        var prefs = new UserPreferences("bangalore", "Italian", BudgetBand.MEDIUM, 4.0, null, 3);
        var llmResponse = new LlmRecommendationResponse("Summary", List.of(
            new LlmRecommendationResponse.LlmRecommendation("Fake Restaurant", 1, "Fake", List.of())
        ));
        var emptyValidation = new RecommendationResult(List.of(), "", 2);
        var fallbackResult = new RecommendationResult(List.of(
            new Recommendation(1, r1, "Fallback", List.of("fallback"))
        ), "Fallback summary", 2);

        when(repository.findAll()).thenReturn(allRestaurants);
        when(filterService.filter(allRestaurants, prefs)).thenReturn(filtered);
        when(truncationService.truncate(filtered)).thenReturn(truncated);
        when(promptBuilder.buildSystemPrompt()).thenReturn("system");
        when(promptBuilder.buildUserPrompt(prefs, truncated.truncatedList(), 3)).thenReturn("user");
        when(groqClient.call("system", "user")).thenReturn(objectMapper.writeValueAsString(llmResponse));
        when(validator.validate(any(), eq(truncated.truncatedList()), eq(3))).thenReturn(emptyValidation);
        when(fallback.fallback(truncated.truncatedList(), prefs, 3)).thenReturn(fallbackResult);

        var result = service.recommend(prefs);

        assertEquals(1, result.recommendations().size());
    }

    @Test
    void shouldUseDefaultTopKWhenRequestedTopKIsZero() {
        var prefs = new UserPreferences("bangalore", "Italian", BudgetBand.MEDIUM, 4.0, null, 0);
        var recommendationResult = new RecommendationResult(List.of(
            new Recommendation(1, r1, "Great", List.of()),
            new Recommendation(2, r2, "Nice", List.of())
        ), "Great picks", 2);

        when(repository.findAll()).thenReturn(allRestaurants);
        when(filterService.filter(allRestaurants, prefs)).thenReturn(filtered);
        when(truncationService.truncate(filtered)).thenReturn(truncated);
        when(promptBuilder.buildSystemPrompt()).thenReturn("system");
        when(promptBuilder.buildUserPrompt(prefs, truncated.truncatedList(), 5)).thenReturn("user");
        when(groqClient.call("system", "user")).thenReturn("{}");
        when(validator.validate(any(), eq(truncated.truncatedList()), eq(5))).thenReturn(recommendationResult);

        var result = service.recommend(prefs);

        assertEquals(2, result.recommendations().size());
    }
}
