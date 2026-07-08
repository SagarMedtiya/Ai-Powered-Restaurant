package com.restaurant.recommender.service;

import com.restaurant.recommender.domain.BudgetBand;
import com.restaurant.recommender.domain.Restaurant;
import com.restaurant.recommender.domain.UserPreferences;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PromptBuilderServiceTest {

    private PromptBuilderService service;

    @BeforeEach
    void setUp() {
        service = new PromptBuilderService(new DefaultResourceLoader(), new ObjectMapper());
    }

    @Test
    void shouldBuildSystemPrompt() {
        String prompt = service.buildSystemPrompt();
        assertNotNull(prompt);
        assertTrue(prompt.contains("restaurant recommendation assistant"));
        assertTrue(prompt.contains("candidate list"));
        assertTrue(prompt.contains("JSON"));
    }

    @Test
    void shouldBuildUserPromptWithAllFields() {
        var prefs = new UserPreferences("Bangalore", "Italian", BudgetBand.MEDIUM, 4.0, "family-friendly", 3);
        var candidates = List.of(
            new Restaurant("1", "Pasta Place", "bangalore", "indiranagar",
                List.of("Italian", "Continental"), 4.5, 1200)
        );

        String prompt = service.buildUserPrompt(prefs, candidates, 3);

        assertNotNull(prompt);
        assertTrue(prompt.contains("Bangalore"));
        assertTrue(prompt.contains("MEDIUM"));
        assertTrue(prompt.contains("Italian"));
        assertTrue(prompt.contains("4.0"));
        assertTrue(prompt.contains("family-friendly"));
        assertTrue(prompt.contains("3"));
        assertTrue(prompt.contains("Pasta Place"));
    }

    @Test
    void shouldHandleNullPreferences() {
        var prefs = new UserPreferences(null, null, null, 0.0, null, 5);
        List<Restaurant> candidates = List.of();

        String prompt = service.buildUserPrompt(prefs, candidates, 5);

        assertNotNull(prompt);
        assertTrue(prompt.contains("Not specified"));
    }

    @Test
    void shouldSerializeCandidatesToJson() {
        var prefs = new UserPreferences("Mumbai", "Chinese", BudgetBand.HIGH, 3.5, null, 2);
        var candidates = List.of(
            new Restaurant("1", "Dragon House", "mumbai", "bandra",
                List.of("Chinese"), 4.2, 800)
        );

        String prompt = service.buildUserPrompt(prefs, candidates, 2);

        assertTrue(prompt.contains("Dragon House"));
        assertTrue(prompt.contains("4.2"));
        assertTrue(prompt.contains("800"));
    }
}
