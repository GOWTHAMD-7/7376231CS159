package com.affordmed.vehicle.controller;

import com.affordmed.middleware.LoggingMiddleware;
import com.affordmed.vehicle.domain.Depot;
import com.affordmed.vehicle.domain.Vehicle;
import com.affordmed.vehicle.dto.OptimizeRequest;
import com.affordmed.vehicle.dto.OptimizeResponse;
import com.affordmed.vehicle.repository.VehicleRepository;
import com.affordmed.vehicle.service.VehicleOptimizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for vehicle maintenance optimization endpoints
 */
@RestController
@RequestMapping("/api/v1/vehicle-scheduling")
public class VehicleOptimizationController {

    private final VehicleOptimizationService optimizationService;
    private final VehicleRepository vehicleRepository;
    private final LoggingMiddleware loggingMiddleware;

    public VehicleOptimizationController(VehicleOptimizationService optimizationService,
                                        VehicleRepository vehicleRepository,
                                        LoggingMiddleware loggingMiddleware) {
        this.optimizationService = optimizationService;
        this.vehicleRepository = vehicleRepository;
        this.loggingMiddleware = loggingMiddleware;
    }

    /**
     * Optimize maintenance tasks for a specific depot
     */
    @PostMapping("/optimize")
    public ResponseEntity<?> optimize(@RequestBody OptimizeRequest request) {
        try {
            loggingMiddleware.info("controller", "Received optimization request for depotId: " + request.getDepotId());

            if (request.getDepotId() == null || request.getDepotId() <= 0) {
                loggingMiddleware.warn("controller", "Invalid depotId in request: " + request.getDepotId());
                return ResponseEntity.badRequest().body(
                        Map.of("error", "Invalid depotId: must be positive integer")
                );
            }

            OptimizeResponse response = optimizationService.optimizeForDepot(request.getDepotId());
            loggingMiddleware.info("controller", "Optimization response prepared for depotId: " + request.getDepotId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            loggingMiddleware.error("controller", "Error processing optimization request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Get all depots
     */
    @GetMapping("/depots")
    public ResponseEntity<?> getDepots() {
        try {
            loggingMiddleware.info("controller", "Received request to fetch all depots");

            List<Depot> depots = vehicleRepository.fetchDepots();

            Map<String, Object> response = new HashMap<>();
            response.put("depots", depots);
            response.put("count", depots.size());

            loggingMiddleware.info("controller", "Returning " + depots.size() + " depots");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            loggingMiddleware.error("controller", "Error fetching depots: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Get all vehicles
     */
    @GetMapping("/vehicles")
    public ResponseEntity<?> getVehicles() {
        try {
            loggingMiddleware.info("controller", "Received request to fetch all vehicles");

            List<Vehicle> vehicles = vehicleRepository.fetchVehicles();

            Map<String, Object> response = new HashMap<>();
            response.put("vehicles", vehicles);
            response.put("count", vehicles.size());

            loggingMiddleware.info("controller", "Returning " + vehicles.size() + " vehicles");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            loggingMiddleware.error("controller", "Error fetching vehicles: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Optimize for all depots
     */
    @GetMapping("/optimize/all")
    public ResponseEntity<?> optimizeAll() {
        try {
            loggingMiddleware.info("controller", "Received request to optimize all depots");

            List<OptimizeResponse> results = optimizationService.optimizeForAllDepots();

            Map<String, Object> response = new HashMap<>();
            response.put("results", results);
            response.put("depotsProcessed", results.size());

            loggingMiddleware.info("controller", "Optimization complete for all depots: " + results.size() + " processed");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            loggingMiddleware.error("controller", "Error optimizing all depots: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        loggingMiddleware.debug("controller", "Health check endpoint called");
        return ResponseEntity.ok(Map.of("status", "healthy", "service", "vehicle-scheduling"));
    }
}

