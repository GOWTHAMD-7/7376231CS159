package com.affordmed.vehicle.repository;

import com.affordmed.middleware.LoggingMiddleware;
import com.affordmed.vehicle.domain.Depot;
import com.affordmed.vehicle.domain.Vehicle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;

/**
 * Repository for vehicle maintenance data from external APIs
 */
@Repository
public class VehicleRepository {

    @Value("${affordmed.base-url}")
    private String baseUrl;

    @Value("${affordmed.token}")
    private String bearerToken;

    private final RestTemplate restTemplate;
    private final LoggingMiddleware loggingMiddleware;

    public VehicleRepository(RestTemplate restTemplate, LoggingMiddleware loggingMiddleware) {
        this.restTemplate = restTemplate;
        this.loggingMiddleware = loggingMiddleware;
    }

    /**
     * Fetch all depots from external API
     */
    public List<Depot> fetchDepots() {
        try {
            loggingMiddleware.info("repository", "Fetching depots from external API");

            String url = baseUrl + "/evaluation-service/depots";
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());

            ResponseEntity<DepotsResponse> response = restTemplate.getForEntity(url, DepotsResponse.class, entity);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Depot> depots = response.getBody().depots;
                loggingMiddleware.info("repository", "Depots fetched successfully, count: " + (depots != null ? depots.size() : 0));
                return depots != null ? depots : Collections.emptyList();
            } else {
                loggingMiddleware.warn("repository", "Depots API returned non-success status: " + response.getStatusCode());
                return Collections.emptyList();
            }
        } catch (RestClientException e) {
            loggingMiddleware.error("repository", "Error fetching depots: " + e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            loggingMiddleware.error("repository", "Unexpected error fetching depots: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Fetch all vehicles from external API
     */
    public List<Vehicle> fetchVehicles() {
        try {
            loggingMiddleware.info("repository", "Fetching vehicles from external API");

            String url = baseUrl + "/evaluation-service/vehicles";
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());

            ResponseEntity<VehiclesResponse> response = restTemplate.getForEntity(url, VehiclesResponse.class, entity);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Vehicle> vehicles = response.getBody().vehicles;
                loggingMiddleware.info("repository", "Vehicles fetched successfully, count: " + (vehicles != null ? vehicles.size() : 0));
                return vehicles != null ? vehicles : Collections.emptyList();
            } else {
                loggingMiddleware.warn("repository", "Vehicles API returned non-success status: " + response.getStatusCode());
                return Collections.emptyList();
            }
        } catch (RestClientException e) {
            loggingMiddleware.error("repository", "Error fetching vehicles: " + e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            loggingMiddleware.error("repository", "Unexpected error fetching vehicles: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Build HTTP headers with Bearer token
     */
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + bearerToken);
        headers.set("Content-Type", "application/json");
        return headers;
    }

    /**
     * Response wrapper for depots API
     */
    public static class DepotsResponse {
        public List<Depot> depots;
    }

    /**
     * Response wrapper for vehicles API
     */
    public static class VehiclesResponse {
        public List<Vehicle> vehicles;
    }
}

