package com.restaurant.recommender.config;

import com.restaurant.recommender.repository.RestaurantRepository;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CatalogHealthIndicator implements HealthIndicator {
    private final RestaurantRepository repository;

    public CatalogHealthIndicator(RestaurantRepository repository) {
        this.repository = repository;
    }

    @Override
    public Health health() {
        if (repository.isReady()) {
            return Health.up()
                .withDetail("catalog", "loaded")
                .withDetail("restaurantCount", repository.findAll().size())
                .build();
        }
        return Health.down()
            .withDetail("catalog", "loading")
            .build();
    }
}
