package com.affordmed.vehicle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for optimization endpoint
 */
public class OptimizeRequest {
    
    @JsonProperty("depotId")
    private Integer depotId;

    public OptimizeRequest() {}

    public OptimizeRequest(Integer depotId) {
        this.depotId = depotId;
    }

    public Integer getDepotId() {
        return depotId;
    }

    public void setDepotId(Integer depotId) {
        this.depotId = depotId;
    }
}

