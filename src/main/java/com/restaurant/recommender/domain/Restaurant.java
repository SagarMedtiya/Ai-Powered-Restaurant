package com.restaurant.recommender.domain;

import java.util.List;

public record Restaurant(
    String id,
    String name,
    String city,
    String location,
    List<String> cuisines,
    double rating,
    Integer costForTwo
) {}
