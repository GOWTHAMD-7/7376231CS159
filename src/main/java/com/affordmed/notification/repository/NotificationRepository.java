package com.affordmed.notification.repository;

import com.affordmed.middleware.LoggingMiddleware;
import com.affordmed.notification.domain.Notification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.util.Collections;
import java.util.List;

/**
 * Repository for notification data from external APIs
 */
@Repository
public class NotificationRepository {

    @Value("${affordmed.base-url}")
    private String baseUrl;

    @Value("${affordmed.token}")
    private String bearerToken;

    private final RestTemplate restTemplate;
    private final LoggingMiddleware loggingMiddleware;

    public NotificationRepository(RestTemplate restTemplate, LoggingMiddleware loggingMiddleware) {
        this.restTemplate = restTemplate;
        this.loggingMiddleware = loggingMiddleware;
    }

    /**
     * Fetch all notifications from external API
     */
    public List<Notification> fetchNotifications() {
        try {
            loggingMiddleware.info("repository", "Fetching notifications from external API");

            String url = baseUrl + "/evaluation-service/notifications";
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());

            ResponseEntity<NotificationsResponse> response = restTemplate.getForEntity(url, NotificationsResponse.class, entity);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Notification> notifications = response.getBody().notifications;
                loggingMiddleware.info("repository", "Notifications fetched successfully, count: " + (notifications != null ? notifications.size() : 0));
                return notifications != null ? notifications : Collections.emptyList();
            } else {
                loggingMiddleware.warn("repository", "Notifications API returned non-success status: " + response.getStatusCode());
                return Collections.emptyList();
            }
        } catch (RestClientException e) {
            loggingMiddleware.error("repository", "Error fetching notifications: " + e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            loggingMiddleware.error("repository", "Unexpected error fetching notifications: " + e.getMessage());
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
     * Response wrapper for notifications API
     */
    public static class NotificationsResponse {
        public List<Notification> notifications;
    }
}

