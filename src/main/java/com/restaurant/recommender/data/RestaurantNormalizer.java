package com.restaurant.recommender.data;

import com.restaurant.recommender.domain.Restaurant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class RestaurantNormalizer {
    private static final Logger log = LoggerFactory.getLogger(RestaurantNormalizer.class);

    public Restaurant normalize(String[] row, int rowIndex) {
        String name = trim(row, 2);
        String city = normalizeLower(trim(row, 16));
        String location = normalizeLower(trim(row, 9));
        List<String> cuisines = parseCuisines(trim(row, 11));
        double rating = parseRating(trim(row, 5));
        Integer costForTwo = parseCost(trim(row, 12));
        String id = generateId(name, city, rowIndex);

        return new Restaurant(id, name, city, location, cuisines, rating, costForTwo);
    }

    String trim(String[] row, int index) {
        if (index < 0 || index >= row.length) return "";
        String val = row[index];
        return val == null ? "" : val.strip();
    }

    String normalizeLower(String value) {
        return value.toLowerCase().strip();
    }

    List<String> parseCuisines(String cuisinesStr) {
        if (cuisinesStr == null || cuisinesStr.isBlank()) {
            return List.of("Other");
        }
        return Arrays.stream(cuisinesStr.split(","))
            .map(String::strip)
            .filter(s -> !s.isEmpty())
            .toList();
    }

    double parseRating(String ratingStr) {
        if (ratingStr == null || ratingStr.isBlank()) return 0.0;
        String cleaned = ratingStr.replace("/5", "").strip();
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    Integer parseCost(String costStr) {
        if (costStr == null || costStr.isBlank()) return null;
        String cleaned = costStr
            .replaceAll("(?i)\\s*for\\s+two.*", "")
            .replaceAll("[₹,\\s]", "")
            .strip();
        if (cleaned.isEmpty()) return null;
        try {
            return Integer.parseInt(cleaned);
        } catch (NumberFormatException e) {
            log.warn("Unable to parse cost: '{}'", costStr);
            return null;
        }
    }

    String generateId(String name, String city, int rowIndex) {
        if (name != null && !name.isBlank() && city != null && !city.isBlank()) {
            return (name.hashCode() + "-" + city.hashCode()).replace("-", "m");
        }
        return "row-" + rowIndex;
    }
}
