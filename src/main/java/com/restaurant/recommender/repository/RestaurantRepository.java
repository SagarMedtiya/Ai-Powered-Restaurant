package com.restaurant.recommender.repository;

import com.restaurant.recommender.domain.Restaurant;
import java.util.List;
import java.util.Set;

public interface RestaurantRepository {
    void initialize(List<Restaurant> restaurants);
    List<Restaurant> findAll();
    Set<String> getCities();
    Set<String> getCuisines();
    boolean isReady();
}
