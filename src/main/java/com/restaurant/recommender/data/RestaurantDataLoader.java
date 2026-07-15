package com.restaurant.recommender.data;

import com.restaurant.recommender.client.HuggingFaceDownloadClient;
import com.restaurant.recommender.config.DatasetProperties;
import com.restaurant.recommender.domain.Restaurant;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class RestaurantDataLoader {
    private static final Logger log = LoggerFactory.getLogger(RestaurantDataLoader.class);

    private final RestaurantNormalizer normalizer;
    private final DatasetProperties datasetProperties;
    private final ResourceLoader resourceLoader;
    private final HuggingFaceDownloadClient downloadClient;

    public RestaurantDataLoader(RestaurantNormalizer normalizer,
                                DatasetProperties datasetProperties,
                                ResourceLoader resourceLoader,
                                HuggingFaceDownloadClient downloadClient) {
        this.normalizer = normalizer;
        this.datasetProperties = datasetProperties;
        this.resourceLoader = resourceLoader;
        this.downloadClient = downloadClient;
    }

    public List<Restaurant> load() {
        List<String> sources = new ArrayList<>();
        sources.add("cache");
        sources.add("download");
        sources.add("classpath");

        for (String source : sources) {
            try {
                List<Restaurant> result = tryLoad(source);
                if (!result.isEmpty()) {
                    log.info("Loaded {} restaurants from {} source", result.size(), source);
                    return result;
                }
            } catch (Exception e) {
                log.debug("Failed to load from {} source: {}", source, e.getMessage());
            }
        }

        log.warn("No restaurants could be loaded from any source");
        return List.of();
    }

    private List<Restaurant> tryLoad(String source) {
        return switch (source) {
            case "cache" -> loadFromCache();
            case "download" -> loadFromDownload();
            case "classpath" -> loadFromClasspath();
            default -> throw new IllegalArgumentException("Unknown source: " + source);
        };
    }

    List<Restaurant> loadFromCache() {
        Path cachePath = Paths.get(datasetProperties.cachePath());
        if (!Files.exists(cachePath)) {
            throw new RuntimeException("Cache file not found: " + cachePath);
        }
        return parseCsv(cachePath);
    }

    List<Restaurant> loadFromDownload() {
        var url = datasetProperties.url();
        if (url == null || url.isBlank()) {
            throw new RuntimeException("DATASET_URL is not configured, skipping download");
        }
        if (datasetProperties.forceDownload() || !Files.exists(Paths.get(datasetProperties.cachePath()))) {
            Path downloaded = downloadClient.download();
            return parseCsv(downloaded);
        }
        return loadFromCache();
    }

    List<Restaurant> loadFromClasspath() {
        try {
            var resource = resourceLoader.getResource("classpath:data/restaurants-sample.csv");
            if (!resource.exists()) {
                throw new RuntimeException("Classpath resource not found");
            }
            try (InputStream is = resource.getInputStream()) {
                return parseCsv(is);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load from classpath", e);
        }
    }

    List<Restaurant> parseCsv(Path path) {
        try (InputStream is = Files.newInputStream(path)) {
            return parseCsv(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse CSV: " + path, e);
        }
    }

    List<Restaurant> parseCsv(InputStream is) throws IOException {
        List<Restaurant> restaurants = new ArrayList<>();
        try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT
                 .withFirstRecordAsHeader()
                 .withIgnoreEmptyLines()
                 .withTrim()
                 .parse(reader)) {

            int rowIndex = 0;
            for (CSVRecord record : parser) {
                try {
                    String[] row = new String[record.size()];
                    for (int i = 0; i < record.size(); i++) {
                        row[i] = record.get(i);
                    }
                    Restaurant restaurant = normalizer.normalize(row, rowIndex);
                    restaurants.add(restaurant);
                } catch (Exception e) {
                    log.warn("Skipping row {}: {}", rowIndex, e.getMessage());
                }
                rowIndex++;
            }
        }
        return restaurants;
    }
}
