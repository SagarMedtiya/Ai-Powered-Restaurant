package com.restaurant.recommender.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
public class SpringAiConfig {

    @Bean
    ChatClient recommendationChatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
