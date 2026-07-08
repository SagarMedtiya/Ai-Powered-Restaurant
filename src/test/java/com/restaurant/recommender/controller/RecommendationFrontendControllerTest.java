package com.restaurant.recommender.controller;

import com.restaurant.recommender.domain.*;
import com.restaurant.recommender.service.RecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecommendationFrontendController.class)
class RecommendationFrontendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendationService recommendationService;

    private final Restaurant r1 = new Restaurant("1", "Tandoori Palace", "bangalore", "indiranagar",
        List.of("North Indian"), 4.5, 1200);

    @Test
    void shouldReturnHtmlFragmentWithRecommendations() throws Exception {
        var result = new RecommendationResult(List.of(
            new Recommendation(1, r1, "Great choice", List.of("best_match"))
        ), "Found 1 restaurant", 5);

        when(recommendationService.recommend(any())).thenReturn(result);

        mockMvc.perform(post("/frontend/recommend")
                .param("location", "bangalore")
                .param("cuisine", "North Indian")
                .param("budget", "MEDIUM")
                .param("minRating", "4.0")
                .param("topK", "3"))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("text/html"))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Tandoori Palace")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Great choice")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("#1")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("Considered 5 restaurants")));
    }

    @Test
    void shouldReturnEmptyStateWhenNoResults() throws Exception {
        var result = new RecommendationResult(List.of(), "No matching restaurants found. Try adjusting your filters.", 0);

        when(recommendationService.recommend(any())).thenReturn(result);

        mockMvc.perform(post("/frontend/recommend")
                .param("location", "unknown")
                .param("cuisine", "unknown"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("No Recommendations")))
            .andExpect(content().string(org.hamcrest.Matchers.containsString("No matching restaurants found")));
    }

    @Test
    void shouldHandleMinimalParams() throws Exception {
        var result = new RecommendationResult(List.of(), "No results", 0);
        when(recommendationService.recommend(any())).thenReturn(result);

        mockMvc.perform(post("/frontend/recommend"))
            .andExpect(status().isOk());
    }

    @Test
    void shouldRenderCostWhenPresent() throws Exception {
        var result = new RecommendationResult(List.of(
            new Recommendation(1, r1, "Great", List.of())
        ), "Found", 1);

        when(recommendationService.recommend(any())).thenReturn(result);

        mockMvc.perform(post("/frontend/recommend")
                .param("location", "bangalore"))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("₹1200")));
    }
}
