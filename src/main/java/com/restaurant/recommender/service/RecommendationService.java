package com.restaurant.recommender.service;

import com.restaurant.recommender.config.RecommendationProperties;
import com.restaurant.recommender.domain.*;
import com.restaurant.recommender.repository.RestaurantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final RestaurantRepository repository;
    private final RestaurantFilterService filterService;
    private final CandidateTruncationService truncationService;
    private final PromptBuilderService promptBuilder;
    private final GroqClient groqClient;
    private final RecommendationValidator validator;
    private final FallbackRankingService fallback;
    private final ObjectMapper objectMapper;
    private final int defaultTopK;

    public RecommendationService(
        RestaurantRepository repository,
        RestaurantFilterService filterService,
        CandidateTruncationService truncationService,
        PromptBuilderService promptBuilder,
        GroqClient groqClient,
        RecommendationValidator validator,
        FallbackRankingService fallback,
        ObjectMapper objectMapper,
        RecommendationProperties properties
    ) {
        this.repository = repository;
        this.filterService = filterService;
        this.truncationService = truncationService;
        this.promptBuilder = promptBuilder;
        this.groqClient = groqClient;
        this.validator = validator;
        this.fallback = fallback;
        this.objectMapper = objectMapper;
        this.defaultTopK = properties.defaultTopK();
    }

    public RecommendationResult recommend(UserPreferences prefs) {
        int topK = prefs.topK() > 0 ? prefs.topK() : defaultTopK;

        List<Restaurant> all = repository.findAll();

        List<Restaurant> filtered = filterService.filter(all, prefs);
        log.debug("Filtered {} -> {} candidates", all.size(), filtered.size());

        if (filtered.isEmpty()) {
            log.info("No candidates match the user's filters");
            return fallback.fallback(filtered, prefs, topK);
        }

        Candidates truncated = truncationService.truncate(filtered);
        log.debug("Truncated {} -> {} candidates for LLM", truncated.originalCount(), truncated.truncatedList().size());

        try {
            String system = promptBuilder.buildSystemPrompt();
            String user = promptBuilder.buildUserPrompt(prefs, truncated.truncatedList(), topK);

            String llmRaw = groqClient.call(system, user);
            log.debug("LLM response received ({} chars)", llmRaw.length());

            LlmRecommendationResponse llmResponse = objectMapper.readValue(llmRaw, LlmRecommendationResponse.class);
            RecommendationResult validated = validator.validate(llmResponse, truncated.truncatedList(), topK);

            if (validated.recommendations().isEmpty()) {
                log.warn("LLM returned no valid recommendations, falling back");
                return fallback.fallback(truncated.truncatedList(), prefs, topK);
            }

            return validated;
        } catch (Exception e) {
            log.warn("LLM processing failed, using fallback: {}", e.getMessage());
            return fallback.fallback(truncated.truncatedList(), prefs, topK);
        }
    }
}
