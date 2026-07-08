package com.restaurant.recommender.service;

import com.restaurant.recommender.exception.LlmServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class GroqClient {

    private static final Logger log = LoggerFactory.getLogger(GroqClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiUrl;
    private final String model;

    public GroqClient(
        RestClient.Builder restClientBuilder,
        ObjectMapper objectMapper,
        @Value("${spring.ai.openai.api-key}") String apiKey,
        @Value("${spring.ai.openai.base-url}") String baseUrl,
        @Value("${spring.ai.openai.chat.options.model}") String model
    ) {
        this.objectMapper = objectMapper;
        this.model = model;
        String normalizedBase = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiUrl = normalizedBase + "/v1/chat/completions";
        this.restClient = restClientBuilder
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    public String call(String systemPrompt, String userPrompt) {
        var body = Map.of(
            "model", model,
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
            ),
            "temperature", 0.4,
            "max_tokens", 2000
        );

        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            String response = restClient.post()
                .uri(apiUrl)
                .body(jsonBody)
                .retrieve()
                .body(String.class);

            if (response == null) {
                throw new LlmServiceException("Groq API returned null response", null);
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode content = root.path("choices").get(0).path("message").path("content");
            if (content.isMissingNode()) {
                throw new LlmServiceException("Groq API response missing content", null);
            }

            return content.asText();
        } catch (LlmServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Groq API call failed", e);
            throw new LlmServiceException("Groq API call failed: " + e.getMessage(), e);
        }
    }
}
