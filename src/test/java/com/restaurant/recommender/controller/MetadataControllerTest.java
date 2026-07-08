package com.restaurant.recommender.controller;

import com.restaurant.recommender.repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MetadataController.class)
class MetadataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantRepository repository;

    @Test
    void shouldReturnMetadataWhenReady() throws Exception {
        when(repository.isReady()).thenReturn(true);
        when(repository.getCities()).thenReturn(Set.of("Bangalore", "Mumbai"));
        when(repository.getCuisines()).thenReturn(Set.of("Italian", "North Indian"));

        mockMvc.perform(get("/api/v1/metadata"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cities").isArray())
            .andExpect(jsonPath("$.cities[0]").isString())
            .andExpect(jsonPath("$.cuisines").isArray())
            .andExpect(jsonPath("$.ready").value(true));
    }

    @Test
    void shouldReturnEmptySetsWhenNotReady() throws Exception {
        when(repository.isReady()).thenReturn(false);

        mockMvc.perform(get("/api/v1/metadata"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cities").isEmpty())
            .andExpect(jsonPath("$.cuisines").isEmpty())
            .andExpect(jsonPath("$.ready").value(false));
    }
}
