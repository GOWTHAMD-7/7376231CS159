package com.affordmed.middleware;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Centralized logging middleware for the entire backend.
 * Posts all logs to the external Affordmed evaluation service.
 *
 * No System.out.println or traditional logging libraries are used.
 * All application events are logged via this middleware.
 */
@Component
public class LoggingMiddleware {

    @Value("${affordmed.base-url}")
    private String baseUrl;

    @Value("${affordmed.token}")
    private String bearerToken;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private static final String LOGS_ENDPOINT = "/evaluation-service/logs";
    private static final String STACK = "backend";
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LoggingMiddleware(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Log an event to the external evaluation service.
     *
     * @param level One of: debug, info, warn, error, fatal
     * @param packageName One of: cache, controller, cron_job, db, domain, handler,
     *                    repository, route, service, auth, config, middleware, utils
     * @param message Descriptive log message
     */
    public void log(String level, String packageName, String message) {
        try {
            LogEvent logEvent = LogEvent.builder()
                    .stack(STACK)
                    .level(level)
                    .packageName(packageName)
                    .message(message)
                    .timestamp(LocalDateTime.now().format(timeFormatter))
                    .build();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + bearerToken);

            String body = objectMapper.writeValueAsString(logEvent);
            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            String url = baseUrl + LOGS_ENDPOINT;
            restTemplate.postForObject(url, entity, String.class);

        } catch (Exception e) {
            // Fallback: print to stderr to avoid losing critical logs
            System.err.println("[LOGGING_MIDDLEWARE_ERROR] Failed to send log: " + e.getMessage());
        }
    }

    /**
     * Convenience method for info-level logs
     */
    public void info(String packageName, String message) {
        log("info", packageName, message);
    }

    /**
     * Convenience method for error-level logs
     */
    public void error(String packageName, String message) {
        log("error", packageName, message);
    }

    /**
     * Convenience method for warn-level logs
     */
    public void warn(String packageName, String message) {
        log("warn", packageName, message);
    }

    /**
     * Convenience method for debug-level logs
     */
    public void debug(String packageName, String message) {
        log("debug", packageName, message);
    }

    /**
     * Internal DTO for log events
     */
    public static class LogEvent {
        public String stack;
        public String level;
        public String packageName;
        public String message;
        public String timestamp;

        public LogEvent() {}

        private LogEvent(Builder builder) {
            this.stack = builder.stack;
            this.level = builder.level;
            this.packageName = builder.packageName;
            this.message = builder.message;
            this.timestamp = builder.timestamp;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String stack;
            private String level;
            private String packageName;
            private String message;
            private String timestamp;

            public Builder stack(String stack) {
                this.stack = stack;
                return this;
            }

            public Builder level(String level) {
                this.level = level;
                return this;
            }

            public Builder packageName(String packageName) {
                this.packageName = packageName;
                return this;
            }

            public Builder message(String message) {
                this.message = message;
                return this;
            }

            public Builder timestamp(String timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public LogEvent build() {
                return new LogEvent(this);
            }
        }
    }
}

