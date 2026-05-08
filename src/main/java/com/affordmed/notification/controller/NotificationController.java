package com.affordmed.notification.controller;

import com.affordmed.middleware.LoggingMiddleware;
import com.affordmed.notification.dto.NotificationItemDto;
import com.affordmed.notification.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for notification endpoints
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final LoggingMiddleware loggingMiddleware;

    public NotificationController(NotificationService notificationService, LoggingMiddleware loggingMiddleware) {
        this.notificationService = notificationService;
        this.loggingMiddleware = loggingMiddleware;
    }

    /**
     * Get priority inbox - top 10 notifications
     */
    @GetMapping("/priority-inbox")
    public ResponseEntity<?> getPriorityInbox() {
        try {
            loggingMiddleware.info("controller", "Received request for priority inbox");

            List<NotificationItemDto> notifications = notificationService.getPriorityInbox();

            Map<String, Object> response = new HashMap<>();
            response.put("topNotifications", notifications);
            response.put("count", notifications.size());

            loggingMiddleware.info("controller", "Priority inbox response prepared with " + notifications.size() + " items");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            loggingMiddleware.error("controller", "Error retrieving priority inbox: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        loggingMiddleware.debug("controller", "Health check endpoint called for notifications");
        return ResponseEntity.ok(Map.of("status", "healthy", "service", "notifications"));
    }
}

