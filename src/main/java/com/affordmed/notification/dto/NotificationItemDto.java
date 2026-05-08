package com.affordmed.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Notification item DTO for priority inbox
 */
public class NotificationItemDto {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    @JsonProperty("priorityScore")
    private Integer priorityScore;

    public NotificationItemDto() {}

    public NotificationItemDto(String id, String type, String message, String timestamp, Integer priorityScore) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
        this.priorityScore = priorityScore;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getPriorityScore() {
        return priorityScore;
    }

    public void setPriorityScore(Integer priorityScore) {
        this.priorityScore = priorityScore;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String type;
        private String message;
        private String timestamp;
        private Integer priorityScore;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
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

        public Builder priorityScore(Integer priorityScore) {
            this.priorityScore = priorityScore;
            return this;
        }

        public NotificationItemDto build() {
            return new NotificationItemDto(id, type, message, timestamp, priorityScore);
        }
    }
}

