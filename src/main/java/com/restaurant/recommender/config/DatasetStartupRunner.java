package com.restaurant.recommender.config;

import com.restaurant.recommender.data.RestaurantDataLoader;
import com.restaurant.recommender.domain.Restaurant;
import com.restaurant.recommender.repository.RestaurantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatasetStartupRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DatasetStartupRunner.class);

    private final RestaurantDataLoader dataLoader;
    private final RestaurantRepository repository;

    public DatasetStartupRunner(RestaurantDataLoader dataLoader, RestaurantRepository repository) {
        this.dataLoader = dataLoader;
        this.repository = repository;
    }

    @Override
    public void run(ApplicationArguments args) {
        long start = System.currentTimeMillis();
        log.info("Starting dataset load...");

        List<Restaurant> restaurants = dataLoader.load();
        repository.initialize(restaurants);

        long elapsed = System.currentTimeMillis() - start;
        log.info("Loaded {} restaurants in {} ms", restaurants.size(), elapsed);
    }
}
