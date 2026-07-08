package com.restaurant.recommender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.dataset")
public record DatasetProperties(
    String url,
    String cachePath,
    boolean forceDownload
) {}
