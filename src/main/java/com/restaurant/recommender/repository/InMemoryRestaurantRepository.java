package com.restaurant.recommender.repository;

import com.restaurant.recommender.domain.Restaurant;
import com.restaurant.recommender.exception.CatalogNotReadyException;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemoryRestaurantRepository implements RestaurantRepository {
    private volatile boolean ready = false;
    private List<Restaurant> restaurants = List.of();
    private Set<String> cities = Set.of();
    private Set<String> cuisines = Set.of();

    @Override
    public synchronized void initialize(List<Restaurant> restaurants) {
        this.restaurants = List.copyOf(restaurants);
        this.cities = restaurants.stream()
            .map(Restaurant::city)
            .filter(c -> c != null && !c.isBlank())
            .collect(Collectors.toCollection(TreeSet::new));
        this.cuisines = restaurants.stream()
            .flatMap(r -> r.cuisines().stream())
            .filter(c -> c != null && !c.isBlank())
            .collect(Collectors.toCollection(TreeSet::new));
        this.ready = true;
    }

    @Override
    public List<Restaurant> findAll() {
        if (!ready) throw new CatalogNotReadyException();
        return restaurants;
    }

    @Override
    public Set<String> getCities() {
        if (!ready) throw new CatalogNotReadyException();
        return cities;
    }

    @Override
    public Set<String> getCuisines() {
        if (!ready) throw new CatalogNotReadyException();
        return cuisines;
    }

    @Override
    public boolean isReady() {
        return ready;
    }
}
