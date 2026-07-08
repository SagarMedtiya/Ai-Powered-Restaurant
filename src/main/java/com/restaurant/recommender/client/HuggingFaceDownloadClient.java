package com.restaurant.recommender.client;

import com.restaurant.recommender.config.DatasetProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class HuggingFaceDownloadClient {
    private static final Logger log = LoggerFactory.getLogger(HuggingFaceDownloadClient.class);

    private final RestClient restClient;
    private final DatasetProperties datasetProperties;

    public HuggingFaceDownloadClient(RestClient.Builder restClientBuilder, DatasetProperties datasetProperties) {
        this.restClient = restClientBuilder
            .baseUrl(datasetProperties.url())
            .build();
        this.datasetProperties = datasetProperties;
    }

    public Path download() {
        Path cachePath = Paths.get(datasetProperties.cachePath());
        try {
            Files.createDirectories(cachePath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create cache directory", e);
        }

        log.info("Downloading dataset from {} to {}", datasetProperties.url(), cachePath);

        byte[] data = restClient.get()
            .retrieve()
            .body(byte[].class);

        if (data == null) {
            throw new RuntimeException("Download returned empty response");
        }

        try (FileOutputStream fos = new FileOutputStream(cachePath.toFile())) {
            fos.write(data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write dataset to cache", e);
        }

        log.info("Downloaded {} bytes to {}", data.length, cachePath);
        return cachePath;
    }
}
