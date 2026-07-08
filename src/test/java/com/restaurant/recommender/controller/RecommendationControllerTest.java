package com.restaurant.recommender.controller;

import com.restaurant.recommender.domain.*;
import com.restaurant.recommender.service.RecommendationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecommendationController.class)
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RecommendationService recommendationService;

    private final Restaurant r1 = new Restaurant("1", "Tandoori Palace", "bangalore", "indiranagar",
        List.of("North Indian"), 4.5, 1200);

    @Test
    void shouldReturn200WithRecommendations() throws Exception {
        var result = new RecommendationResult(List.of(
            new Recommendation(1, r1, "Great choice", List.of("best_match"))
        ), "Found 1 restaurant", 5);

        when(recommendationService.recommend(any())).thenReturn(result);

        var requestJson = """
            {
                "location": "bangalore",
                "cuisine": "North Indian",
                "budget": "MEDIUM",
                "minRating": 4.0,
                "topK": 3
            }
            """;

        mockMvc.perform(post("/api/v1/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recommendations").isArray())
            .andExpect(jsonPath("$.recommendations[0].restaurantName").value("Tandoori Palace"))
            .andExpect(jsonPath("$.recommendations[0].rank").value(1))
            .andExpect(jsonPath("$.recommendations[0].explanation").value("Great choice"))
            .andExpect(jsonPath("$.summary").value("Found 1 restaurant"))
            .andExpect(jsonPath("$.candidatesConsidered").value(5))
            .andExpect(jsonPath("$.usedFallback").value(false));
    }

    @Test
    void shouldReturn200WithFallback() throws Exception {
        var result = new RecommendationResult(List.of(
            new Recommendation(1, r1, "Fallback", List.of("fallback"))
        ), "Fallback summary", 5);

        when(recommendationService.recommend(any())).thenReturn(result);

        var requestJson = """
            {
                "location": "bangalore",
                "cuisine": "Unknown",
                "budget": "LOW",
                "minRating": 4.0,
                "topK": 3
            }
            """;

        mockMvc.perform(post("/api/v1/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.usedFallback").value(true));
    }

    @Test
    void shouldReturn400WhenBudgetIsInvalid() throws Exception {
        mockMvc.perform(post("/api/v1/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "budget": "INVALID",
                        "minRating": 4.0,
                        "topK": 3
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenTopKIsOutOfRange() throws Exception {
        mockMvc.perform(post("/api/v1/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "minRating": 4.0,
                        "topK": 50
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenMinRatingIsOutOfRange() throws Exception {
        mockMvc.perform(post("/api/v1/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "minRating": 10,
                        "topK": 3
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn200WhenRequestBodyIsEmpty() throws Exception {
        var result = new RecommendationResult(List.of(), "No results", 0);
        when(recommendationService.recommend(any())).thenReturn(result);

        mockMvc.perform(post("/api/v1/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/v1/recommend")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenLocationIsTooLong() throws Exception {
        var requestJson = """
            {
                "location": "%s",
                "minRating": 4.0,
                "topK": 3
            }
            """.formatted("a".repeat(101));

        mockMvc.perform(post("/api/v1/recommend")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
            .andExpect(status().isBadRequest());
    }
}
