package com.restaurant.recommender.dto.response;

import java.util.Set;

public record MetadataResponse(
    Set<String> cities,
    Set<String> cuisines,
    boolean ready
) {}
