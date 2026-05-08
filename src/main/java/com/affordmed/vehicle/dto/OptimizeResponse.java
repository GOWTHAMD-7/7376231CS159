package com.affordmed.vehicle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response DTO for optimization endpoint
 */
public class OptimizeResponse {
    
    @JsonProperty("depotId")
    private Integer depotId;
    
    @JsonProperty("mechanicHoursBudget")
    private Integer mechanicHoursBudget;
    
    @JsonProperty("totalImpactScore")
    private Integer totalImpactScore;
    
    @JsonProperty("totalDuration")
    private Integer totalDuration;
    
    @JsonProperty("selectedTasks")
    private List<SelectedTask> selectedTasks;

    public OptimizeResponse() {}

    public OptimizeResponse(Integer depotId, Integer mechanicHoursBudget, Integer totalImpactScore,
                           Integer totalDuration, List<SelectedTask> selectedTasks) {
        this.depotId = depotId;
        this.mechanicHoursBudget = mechanicHoursBudget;
        this.totalImpactScore = totalImpactScore;
        this.totalDuration = totalDuration;
        this.selectedTasks = selectedTasks;
    }

    public Integer getDepotId() {
        return depotId;
    }

    public void setDepotId(Integer depotId) {
        this.depotId = depotId;
    }

    public Integer getMechanicHoursBudget() {
        return mechanicHoursBudget;
    }

    public void setMechanicHoursBudget(Integer mechanicHoursBudget) {
        this.mechanicHoursBudget = mechanicHoursBudget;
    }

    public Integer getTotalImpactScore() {
        return totalImpactScore;
    }

    public void setTotalImpactScore(Integer totalImpactScore) {
        this.totalImpactScore = totalImpactScore;
    }

    public Integer getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(Integer totalDuration) {
        this.totalDuration = totalDuration;
    }

    public List<SelectedTask> getSelectedTasks() {
        return selectedTasks;
    }

    public void setSelectedTasks(List<SelectedTask> selectedTasks) {
        this.selectedTasks = selectedTasks;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer depotId;
        private Integer mechanicHoursBudget;
        private Integer totalImpactScore;
        private Integer totalDuration;
        private List<SelectedTask> selectedTasks;

        public Builder depotId(Integer depotId) {
            this.depotId = depotId;
            return this;
        }

        public Builder mechanicHoursBudget(Integer mechanicHoursBudget) {
            this.mechanicHoursBudget = mechanicHoursBudget;
            return this;
        }

        public Builder totalImpactScore(Integer totalImpactScore) {
            this.totalImpactScore = totalImpactScore;
            return this;
        }

        public Builder totalDuration(Integer totalDuration) {
            this.totalDuration = totalDuration;
            return this;
        }

        public Builder selectedTasks(List<SelectedTask> selectedTasks) {
            this.selectedTasks = selectedTasks;
            return this;
        }

        public OptimizeResponse build() {
            return new OptimizeResponse(depotId, mechanicHoursBudget, totalImpactScore, totalDuration, selectedTasks);
        }
    }
}

