package com.restaurant.recommender.controller;

import com.restaurant.recommender.domain.BudgetBand;
import com.restaurant.recommender.domain.UserPreferences;
import com.restaurant.recommender.dto.response.RecommendationResponse;
import com.restaurant.recommender.dto.response.RecommendationItemDto;
import com.restaurant.recommender.service.RecommendationService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/frontend")
public class RecommendationFrontendController {

    private final RecommendationService recommendationService;

    public RecommendationFrontendController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping(value = "/recommend", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String recommend(
        @RequestParam(required = false) String location,
        @RequestParam(required = false) String cuisine,
        @RequestParam(required = false) String budget,
        @RequestParam(defaultValue = "0") double minRating,
        @RequestParam(required = false) String additionalPreferences,
        @RequestParam(defaultValue = "0") int topK
    ) {
        BudgetBand budgetBand = null;
        if (budget != null && !budget.isBlank()) {
            try { budgetBand = BudgetBand.valueOf(budget.toUpperCase()); }
            catch (IllegalArgumentException e) { budgetBand = null; }
        }

        var prefs = new UserPreferences(location, cuisine, budgetBand, minRating, additionalPreferences, topK);
        var result = recommendationService.recommend(prefs);

        boolean usedFallback = result.recommendations().stream()
            .anyMatch(r -> r.tags().contains("fallback"));

        var items = result.recommendations().stream()
            .map(r -> new RecommendationItemDto(
                r.rank(),
                r.restaurant().name(),
                r.restaurant().city(),
                r.restaurant().location(),
                r.restaurant().cuisines(),
                r.restaurant().rating(),
                r.restaurant().costForTwo(),
                r.explanation(),
                r.tags()
            ))
            .toList();

        var response = new RecommendationResponse(items, result.summary(), result.candidatesConsidered(), usedFallback);
        return renderFragment(response);
    }

    private String renderFragment(RecommendationResponse response) {
        var sb = new StringBuilder();
        if (response.recommendations().isEmpty()) {
            sb.append("""
                <div class="card empty-state">
                    <h2>No Recommendations</h2>
                    <p>%s</p>
                </div>
                """.formatted(escape(response.summary())));
        } else {
            sb.append("""
                <div class="results-header">
                    <p class="summary">%s</p>
                    <p class="candidates-count">Considered %d restaurants</p>
                </div>
                <div class="recommendations-grid">
                """.formatted(escape(response.summary()), response.candidatesConsidered()));

            for (var item : response.recommendations()) {
                String costHtml = item.costForTwo() != null
                    ? "<span class=\"badge badge-cost\">₹%d</span>".formatted(item.costForTwo())
                    : "";
                String tagsHtml = "";
                if (item.tags() != null && !item.tags().isEmpty()) {
                    var tagSb = new StringBuilder("<div class=\"card-tags\">");
                    for (var tag : item.tags()) {
                        tagSb.append("<span class=\"tag\">").append(escape(tag)).append("</span>");
                    }
                    tagSb.append("</div>");
                    tagsHtml = tagSb.toString();
                }

                sb.append("""
                    <div class="recommendation-card">
                        <div class="card-rank">#%d</div>
                        <div class="card-body">
                            <h3 class="card-title">%s</h3>
                            <div class="card-meta">
                                <span class="badge badge-rating">%s ★</span>
                                %s
                                <span class="badge badge-location">%s</span>
                            </div>
                            <p class="card-location">%s</p>
                            <p class="card-cuisines">%s</p>
                            <p class="card-explanation">%s</p>
                            %s
                        </div>
                    </div>
                    """.formatted(
                        item.rank(),
                        escape(item.restaurantName()),
                        item.rating(),
                        costHtml,
                        escape(item.city()),
                        escape(item.location()),
                        String.join(", ", item.cuisines()),
                        escape(item.explanation()),
                        tagsHtml
                    ));
            }
            sb.append("</div>");
        }
        return sb.toString();
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
