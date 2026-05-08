package com.affordmed.vehicle.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a maintenance depot with available mechanic hours
 */
public class Depot {
    
    @JsonProperty("ID")
    private Integer id;
    
    @JsonProperty("MechanicHours")
    private Integer mechanicHours;

    public Depot() {}

    public Depot(Integer id, Integer mechanicHours) {
        this.id = id;
        this.mechanicHours = mechanicHours;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMechanicHours() {
        return mechanicHours;
    }

    public void setMechanicHours(Integer mechanicHours) {
        this.mechanicHours = mechanicHours;
    }
}

