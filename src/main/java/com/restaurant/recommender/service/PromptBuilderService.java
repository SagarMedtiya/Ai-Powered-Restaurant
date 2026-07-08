package com.restaurant.recommender.service;

import com.restaurant.recommender.domain.Restaurant;
import com.restaurant.recommender.domain.UserPreferences;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PromptBuilderService {

    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    private String systemTemplate;
    private String userTemplate;

    public PromptBuilderService(ResourceLoader resourceLoader, ObjectMapper objectMapper) {
        this.resourceLoader = resourceLoader;
        this.objectMapper = objectMapper;
    }

    public String buildSystemPrompt() {
        if (systemTemplate == null) {
            systemTemplate = loadTemplate("classpath:prompts/system.st");
        }
        return systemTemplate;
    }

    public String buildUserPrompt(UserPreferences prefs, List<Restaurant> candidates, int topK) {
        if (userTemplate == null) {
            userTemplate = loadTemplate("classpath:prompts/user.st");
        }
        String candidatesJson = toCompactJson(candidates);
        return userTemplate
            .replace("{{location}}", safe(prefs.location()))
            .replace("{{budget}}", prefs.budget() != null ? prefs.budget().name() : "Any")
            .replace("{{cuisine}}", safe(prefs.cuisine()))
            .replace("{{minRating}}", String.valueOf(prefs.minRating()))
            .replace("{{additionalPreferences}}", safe(prefs.additionalPreferences()))
            .replace("{{topK}}", String.valueOf(topK))
            .replace("{{candidatesJson}}", candidatesJson);
    }

    private String toCompactJson(List<Restaurant> candidates) {
        var list = candidates.stream()
            .map(r -> Map.of(
                "name", r.name(),
                "city", r.city(),
                "location", r.location(),
                "cuisines", r.cuisines(),
                "rating", r.rating(),
                "costForTwo", r.costForTwo() != null ? r.costForTwo() : "N/A"
            ))
            .collect(Collectors.toList());
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize candidates to JSON", e);
        }
    }

    private String loadTemplate(String path) {
        Resource resource = resourceLoader.getResource(path);
        try (var is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load prompt template: " + path, e);
        }
    }

    private static String safe(String value) {
        return value != null && !value.isBlank() ? value : "Not specified";
    }
}
