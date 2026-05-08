package com.affordmed.notification.service;

import com.affordmed.middleware.LoggingMiddleware;
import com.affordmed.notification.domain.Notification;
import com.affordmed.notification.dto.NotificationItemDto;
import com.affordmed.notification.repository.NotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for notification processing and priority inbox management
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final LoggingMiddleware loggingMiddleware;
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public NotificationService(NotificationRepository notificationRepository, LoggingMiddleware loggingMiddleware) {
        this.notificationRepository = notificationRepository;
        this.loggingMiddleware = loggingMiddleware;
    }

    /**
     * Get priority inbox - top 10 notifications sorted by priority
     *
     * Priority scoring:
     * - Placement = 3
     * - Result = 2
     * - Event = 1
     * - Secondary sort by recency (newer first)
     *
     * Uses a min-heap (PriorityQueue) to efficiently maintain top 10 items
     */
    public List<NotificationItemDto> getPriorityInbox() {
        loggingMiddleware.info("service", "Starting priority inbox computation");

        // Fetch notifications
        List<Notification> notifications = notificationRepository.fetchNotifications();
        loggingMiddleware.info("service", "Fetched " + notifications.size() + " notifications");

        if (notifications == null || notifications.isEmpty()) {
            loggingMiddleware.warn("service", "No notifications available for priority inbox");
            return Collections.emptyList();
        }

        // Convert to DTOs with priority scores
        List<NotificationWithScore> scoredNotifications = new ArrayList<>();
        for (Notification notification : notifications) {
            int priorityScore = calculatePriorityScore(notification.getType());
            scoredNotifications.add(new NotificationWithScore(
                    notification,
                    priorityScore,
                    parseTimestamp(notification.getTimestamp())
            ));
        }

        loggingMiddleware.debug("service", "Converted " + scoredNotifications.size() + " notifications to scored items");

        // Use a min-heap to maintain top 10 efficiently
        PriorityQueue<NotificationWithScore> minHeap = new PriorityQueue<>(
                Comparator.comparingInt((NotificationWithScore n) -> n.priorityScore)
                        .thenComparingLong(n -> n.epochTimestamp)
        );

        int topN = 10;
        for (NotificationWithScore scored : scoredNotifications) {
            if (minHeap.size() < topN) {
                minHeap.offer(scored);
            } else if (isHigherPriority(scored, minHeap.peek())) {
                minHeap.poll();
                minHeap.offer(scored);
            }
        }

        loggingMiddleware.info("service", "Top " + minHeap.size() + " notifications selected from " + scoredNotifications.size());

        // Convert heap to sorted list (descending order by priority)
        List<NotificationItemDto> result = new ArrayList<>();
        while (!minHeap.isEmpty()) {
            NotificationWithScore item = minHeap.poll();
            result.add(0, NotificationItemDto.builder()
                    .id(item.notification.getId())
                    .type(item.notification.getType())
                    .message(item.notification.getMessage())
                    .timestamp(item.notification.getTimestamp())
                    .priorityScore(item.priorityScore)
                    .build());
        }

        loggingMiddleware.info("service", "Priority inbox computed, returning " + result.size() + " top notifications");
        return result;
    }

    /**
     * Calculate priority score based on notification type
     */
    private int calculatePriorityScore(String type) {
        if (type == null) return 0;

        return switch (type.toLowerCase()) {
            case "placement" -> 3;
            case "result" -> 2;
            case "event" -> 1;
            default -> 0;
        };
    }

    /**
     * Check if a notification has higher priority than another
     */
    private boolean isHigherPriority(NotificationWithScore a, NotificationWithScore b) {
        if (a.priorityScore != b.priorityScore) {
            return a.priorityScore > b.priorityScore;
        }
        // If priority scores are equal, higher priority if newer (later timestamp)
        return a.epochTimestamp > b.epochTimestamp;
    }

    /**
     * Parse timestamp string to epoch milliseconds for comparison
     */
    private long parseTimestamp(String timestamp) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(timestamp, dateFormatter);
            return dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            loggingMiddleware.warn("service", "Failed to parse timestamp: " + timestamp);
            return 0L;
        }
    }

    /**
     * Internal class to hold notification with priority score
     */
    private static class NotificationWithScore {
        Notification notification;
        int priorityScore;
        long epochTimestamp;

        NotificationWithScore(Notification notification, int priorityScore, long epochTimestamp) {
            this.notification = notification;
            this.priorityScore = priorityScore;
            this.epochTimestamp = epochTimestamp;
        }
    }
}

