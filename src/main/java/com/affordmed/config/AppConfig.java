package com.affordmed.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Global application configuration
 */
@Configuration
public class AppConfig {

    /**
     * RestTemplate bean for making HTTP calls to external APIs
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * ObjectMapper bean for JSON serialization/deserialization
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

