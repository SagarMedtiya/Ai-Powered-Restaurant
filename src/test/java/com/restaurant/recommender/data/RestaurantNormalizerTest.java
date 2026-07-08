package com.restaurant.recommender.data;

import com.restaurant.recommender.domain.Restaurant;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantNormalizerTest {

    private final RestaurantNormalizer normalizer = new RestaurantNormalizer();

    @Test
    void shouldNormalizeValidRow() {
        String[] row = {
            "url", "address", "Test Restaurant", "Yes", "No", "4.1/5", "100", "phone",
            "location", "Test Area", "Casual Dining", "North Indian, Chinese", "800",
            "reviews", "menu", "type", "Test City"
        };

        Restaurant r = normalizer.normalize(row, 0);

        assertEquals("Test Restaurant", r.name());
        assertEquals("test city", r.city());
        assertEquals("test area", r.location());
        assertEquals(List.of("North Indian", "Chinese"), r.cuisines());
        assertEquals(4.1, r.rating(), 0.001);
        assertEquals(800, r.costForTwo());
        assertNotNull(r.id());
    }

    @Test
    void shouldParseRatingWithSlash5() {
        assertEquals(4.5, normalizer.parseRating("4.5/5"), 0.001);
    }

    @Test
    void shouldDefaultInvalidRatingToZero() {
        assertEquals(0.0, normalizer.parseRating("NEW"), 0.001);
        assertEquals(0.0, normalizer.parseRating("-"), 0.001);
        assertEquals(0.0, normalizer.parseRating(""), 0.001);
        assertEquals(0.0, normalizer.parseRating(null), 0.001);
    }

    @Test
    void shouldParseCostWithRupeeSymbol() {
        assertEquals(800, normalizer.parseCost("₹800"));
    }

    @Test
    void shouldParseCostWithCommas() {
        assertEquals(1200, normalizer.parseCost("1,200"));
    }

    @Test
    void shouldParseCostWithForTwo() {
        assertEquals(800, normalizer.parseCost("800 for two"));
    }

    @Test
    void shouldReturnNullForBlankCost() {
        assertNull(normalizer.parseCost(""));
        assertNull(normalizer.parseCost(null));
        assertNull(normalizer.parseCost("-"));
    }

    @Test
    void shouldSplitCuisines() {
        assertEquals(List.of("North Indian", "Chinese"), normalizer.parseCuisines("North Indian, Chinese"));
        assertEquals(List.of("Cafe"), normalizer.parseCuisines("Cafe"));
        assertEquals(List.of("Other"), normalizer.parseCuisines(""));
        assertEquals(List.of("Other"), normalizer.parseCuisines(null));
    }

    @Test
    void shouldNormalizeCityToLowerCase() {
        String[] row = new String[17];
        row[2] = "Restaurant";
        row[16] = "Bangalore";

        Restaurant r = normalizer.normalize(row, 0);
        assertEquals("bangalore", r.city());
    }

    @Test
    void shouldGenerateStableId() {
        String[] row1 = new String[17];
        row1[2] = "Same Name";
        row1[16] = "City";

        String[] row2 = new String[17];
        row2[2] = "Same Name";
        row2[16] = "City";

        Restaurant r1 = normalizer.normalize(row1, 0);
        Restaurant r2 = normalizer.normalize(row2, 0);

        assertEquals(r1.id(), r2.id());
    }

    @Test
    void shouldHandleMissingOptionalFields() {
        String[] row = new String[17];
        row[2] = "Name";

        Restaurant r = normalizer.normalize(row, 0);
        assertEquals("Name", r.name());
        assertEquals("", r.city());
        assertEquals("", r.location());
        assertEquals(List.of("Other"), r.cuisines());
        assertEquals(0.0, r.rating(), 0.001);
        assertNull(r.costForTwo());
    }
}
