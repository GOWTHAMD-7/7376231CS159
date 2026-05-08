package com.affordmed.vehicle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Selected task DTO within optimization response
 */
public class SelectedTask {
    
    @JsonProperty("taskId")
    private String taskId;
    
    @JsonProperty("duration")
    private Integer duration;
    
    @JsonProperty("impact")
    private Integer impact;

    public SelectedTask() {}

    public SelectedTask(String taskId, Integer duration, Integer impact) {
        this.taskId = taskId;
        this.duration = duration;
        this.impact = impact;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Integer getImpact() {
        return impact;
    }

    public void setImpact(Integer impact) {
        this.impact = impact;
    }

    // Builder pattern support
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String taskId;
        private Integer duration;
        private Integer impact;

        public Builder taskId(String taskId) {
            this.taskId = taskId;
            return this;
        }

        public Builder duration(Integer duration) {
            this.duration = duration;
            return this;
        }

        public Builder impact(Integer impact) {
            this.impact = impact;
            return this;
        }

        public SelectedTask build() {
            return new SelectedTask(taskId, duration, impact);
        }
    }
}

