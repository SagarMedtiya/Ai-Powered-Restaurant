package com.restaurant.recommender.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Direct smoke test against the Groq API (OpenAI-compatible endpoint).
 * Requires LLM_API_KEY to be set as environment variable or in .env file.
 * Tagged as "integration" — skipped by default in `mvnw test`.
 *
 * Run with: ./mvnw test -Dtest=GroqApiSmokeTest -Dgroups=integration
 */
@Tag("integration")
class GroqApiSmokeTest {

    private static final RestClient restClient = RestClient.builder().build();
    private static final ObjectMapper mapper = new ObjectMapper();
    private static String apiKey;
    private static final String BASE_URL = "https://api.groq.com/openai/v1"; // Smoke test calls API directly, so includes /v1

    @BeforeAll
    static void setup() {
        apiKey = resolveApiKey();
        org.junit.jupiter.api.Assumptions.assumeTrue(apiKey != null,
            "LLM_API_KEY not found — skipping integration test. Set it as env var or in .env file.");
        System.out.println("Groq API key resolved (length=" + apiKey.length() + ")");
    }

    @Test
    void shouldRespondToSimpleChatCompletion() throws IOException {
        var body = Map.of(
            "model", "llama-3.3-70b-versatile",
            "messages", List.of(
                Map.of("role", "system", "content", "You are a helpful assistant. Reply with one short sentence."),
                Map.of("role", "user", "content", "Say 'API connection works'")
            ),
            "temperature", 0.4,
            "max_tokens", 50
        );

        String json = restClient.post()
            .uri(BASE_URL + "/chat/completions")
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .body(mapper.writeValueAsString(body))
            .retrieve()
            .body(String.class);

        assertNotNull(json);
        JsonNode root = mapper.readTree(json);
        JsonNode content = root.path("choices").get(0).path("message").path("content");
        assertFalse(content.isMissingNode(), "Response should contain choices[0].message.content");
        String text = content.asText();
        System.out.println("Groq response: " + text);
        assertTrue(text.toLowerCase().contains("connection") || text.toLowerCase().contains("works"),
            "Response should acknowledge the connection check. Got: " + text);
    }

    @Test
    void shouldReturnStructuredRecommendationJson() throws IOException {
        var candidatesJson = """
            [
              {"name": "Pasta Paradise", "cuisines": ["Italian", "Continental"], "rating": 4.3, "costForTwo": 1400},
              {"name": "Tandoori Palace", "cuisines": ["North Indian", "Mughlai"], "rating": 4.5, "costForTwo": 1200},
              {"name": "Sushi Zen", "cuisines": ["Japanese", "Sushi"], "rating": 4.7, "costForTwo": 2500}
            ]
            """;

        var systemPrompt = """
            You are a restaurant recommendation assistant.
            Only recommend from the given candidate list. Output valid JSON only — no markdown, no code fences.
            JSON schema: {"summary": "...", "recommendations": [{"restaurantName": "...", "rank": 1, "explanation": "...", "tags": []}]}
            """;

        var userPrompt = """
            User Preferences:
            - Location: Bangalore
            - Budget: MEDIUM
            - Cuisine: Italian
            - Minimum rating: 4.0
            
            Candidate Restaurants:
            %s
            
            Return exactly 2 recommendations.
            """.formatted(candidatesJson);

        var body = Map.of(
            "model", "llama-3.3-70b-versatile",
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
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
        JsonNode root = mapper.readTree(json);
        String content = root.path("choices").get(0).path("message").path("content").asText();
        System.out.println("Raw LLM response:\n" + content);

        JsonNode parsed;
        try {
            parsed = mapper.readTree(content);
        } catch (Exception e) {
            fail("LLM response is not valid JSON: " + content);
            return;
        }

        assertTrue(parsed.has("summary"), "Response should contain 'summary'");
        assertTrue(parsed.has("recommendations"), "Response should contain 'recommendations'");
        assertTrue(parsed.get("recommendations").isArray(), "recommendations should be an array");

        for (JsonNode rec : parsed.get("recommendations")) {
            assertTrue(rec.has("restaurantName"), "Each recommendation needs restaurantName");
            assertTrue(rec.has("explanation"), "Each recommendation needs explanation");
            assertTrue(rec.has("rank"), "Each recommendation needs rank");
        }
    }

    private static String resolveApiKey() {
        String key = System.getenv("LLM_API_KEY");
        if (key != null && !key.isBlank()) return key;

        key = System.getProperty("LLM_API_KEY");
        if (key != null && !key.isBlank()) return key;

        try {
            Path envPath = Paths.get(".env");
            if (Files.exists(envPath)) {
                for (String line : Files.readAllLines(envPath)) {
                    line = line.strip();
                    if (line.startsWith("LLM_API_KEY=")) {
                        return line.substring("LLM_API_KEY=".length()).strip();
                    }
                }
            }
        } catch (IOException e) {
            // ignore
        }

        return null;
    }
}
