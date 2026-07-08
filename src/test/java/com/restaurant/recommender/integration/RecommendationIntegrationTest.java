package com.restaurant.recommender.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.recommender.config.RecommendationProperties;
import com.restaurant.recommender.domain.*;
import com.restaurant.recommender.service.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Full pipeline integration test using the real Groq API.
 * Calls the Groq API directly via RestClient (bypasses Spring AI 1.0.0-M3
 * streaming compatibility issue) but tests all other Spring beans.
 * Requires LLM_API_KEY to be set as environment variable or in .env file.
 * Tagged as "integration" — skipped by default in `mvnw test`.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("dev")
@Tag("integration")
class RecommendationIntegrationTest {

    @Autowired
    private PromptBuilderService promptBuilder;

    @Autowired
    private RecommendationValidator validator;

    @Autowired
    private FallbackRankingService fallback;

    @Autowired
    private RecommendationProperties properties;

    private final RestClient restClient = RestClient.builder().build();
    private final ObjectMapper mapper = new ObjectMapper();

    private static String apiKey;
    private static final String BASE_URL = "https://api.groq.com/openai/v1";

    private final Restaurant r1 = new Restaurant("1", "Pasta Paradise", "bangalore", "koramangala",
        List.of("Italian", "Continental"), 4.3, 1400);
    private final Restaurant r2 = new Restaurant("2", "Tandoori Palace", "bangalore", "indiranagar",
        List.of("North Indian", "Mughlai"), 4.5, 1200);
    private final Restaurant r3 = new Restaurant("3", "Sushi Zen", "mumbai", "bandra",
        List.of("Japanese", "Sushi"), 4.7, 2500);
    private final List<Restaurant> candidates = List.of(r1, r2, r3);

    @BeforeAll
    static void checkApiKey() throws IOException {
        apiKey = resolveApiKey();
        org.junit.jupiter.api.Assumptions.assumeTrue(apiKey != null,
            "LLM_API_KEY not found — skipping integration test. Set it as env var or in .env file.");
    }

    @Test
    void fullPipelineWithRealLlm() throws Exception {
        var prefs = new UserPreferences("Bangalore", "Italian", BudgetBand.MEDIUM, 4.0, "family-friendly", 3);

        String system = promptBuilder.buildSystemPrompt();
        String user = promptBuilder.buildUserPrompt(prefs, candidates, 3);

        var body = Map.of(
            "model", "llama-3.3-70b-versatile",
            "messages", List.of(
                Map.of("role", "system", "content", system),
                Map.of("role", "user", "content", user)
            ),
            "temperature", 0.4,
            "max_tokens", 1000
        );

        String json = restClient.post()
            .uri(BASE_URL + "/chat/completions")
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .body(mapper.writeValueAsString(body))
            .retrieve()
            .body(String.class);

        assertNotNull(json);
        String content = mapper.readTree(json).path("choices").get(0).path("message").path("content").asText();
        assertNotNull(content);
        System.out.println("Raw LLM response:\n" + content);

        LlmRecommendationResponse response = mapper.readValue(content, LlmRecommendationResponse.class);
        assertNotNull(response);
        assertNotNull(response.recommendations());
        assertFalse(response.recommendations().isEmpty(), "Should have at least one recommendation");

        System.out.println("LLM Summary: " + response.summary());
        for (var rec : response.recommendations()) {
            System.out.println("  #" + rec.rank() + " " + rec.restaurantName() + " — " + rec.explanation());
        }

        var validated = validator.validate(response, candidates, 3);
        assertNotNull(validated);
        assertFalse(validated.recommendations().isEmpty(), "Validator should accept at least one recommendation");

        for (var rec : validated.recommendations()) {
            assertNotNull(rec.restaurant(), "Each recommendation must have a valid Restaurant object");
            assertTrue(rec.rank() > 0, "Rank must be positive");
            assertNotNull(rec.explanation(), "Each recommendation must have an explanation");
        }

        System.out.println("Validation passed: " + validated.recommendations().size() + " valid recommendations");
    }

    @Test
    void fallbackWhenLlmReturnsEmpty() {
        var prefs = new UserPreferences("Bangalore", "Italian", BudgetBand.MEDIUM, 4.0, null, 3);

        var result = fallback.fallback(candidates, prefs, 3);

        assertNotNull(result);
        assertEquals(3, result.recommendations().size());
        assertEquals(3, result.candidatesConsidered());

        var first = result.recommendations().get(0);
        assertTrue(first.rank() > 0);
        assertNotNull(first.explanation());
        assertTrue(first.explanation().contains("Italian"));
    }

    private static String resolveApiKey() throws IOException {
        String key = System.getenv("LLM_API_KEY");
        if (key != null && !key.isBlank()) return key;

        key = System.getProperty("LLM_API_KEY");
        if (key != null && !key.isBlank()) return key;

        Path envPath = Paths.get(".env");
        if (Files.exists(envPath)) {
            for (String line : Files.readAllLines(envPath)) {
                line = line.strip();
                if (line.startsWith("LLM_API_KEY=")) {
                    return line.substring("LLM_API_KEY=".length()).strip();
                }
            }
        }
        return null;
    }
}
