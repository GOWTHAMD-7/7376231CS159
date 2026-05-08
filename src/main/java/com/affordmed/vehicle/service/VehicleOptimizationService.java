package com.affordmed.vehicle.service;

import com.affordmed.middleware.LoggingMiddleware;
import com.affordmed.vehicle.domain.Depot;
import com.affordmed.vehicle.domain.Vehicle;
import com.affordmed.vehicle.dto.OptimizeResponse;
import com.affordmed.vehicle.dto.SelectedTask;
import com.affordmed.vehicle.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for vehicle maintenance optimization using 0/1 Knapsack algorithm
 */
@Service
public class VehicleOptimizationService {

    private final VehicleRepository vehicleRepository;
    private final LoggingMiddleware loggingMiddleware;

    public VehicleOptimizationService(VehicleRepository vehicleRepository, LoggingMiddleware loggingMiddleware) {
        this.vehicleRepository = vehicleRepository;
        this.loggingMiddleware = loggingMiddleware;
    }

    /**
     * Optimize maintenance tasks for a specific depot using 0/1 Knapsack DP
     *
     * @param depotId The depot ID to optimize for
     * @return Optimization result with selected tasks
     */
    public OptimizeResponse optimizeForDepot(Integer depotId) {
        loggingMiddleware.info("service", "Starting optimization for depotId: " + depotId);

        // Fetch depot data
        List<Depot> depots = vehicleRepository.fetchDepots();
        Depot targetDepot = depots.stream()
                .filter(d -> d.getId().equals(depotId))
                .findFirst()
                .orElse(null);

        if (targetDepot == null) {
            loggingMiddleware.warn("service", "Depot not found with ID: " + depotId);
            return OptimizeResponse.builder()
                    .depotId(depotId)
                    .mechanicHoursBudget(0)
                    .totalImpactScore(0)
                    .totalDuration(0)
                    .selectedTasks(new ArrayList<>())
                    .build();
        }

        // Fetch vehicles
        List<Vehicle> vehicles = vehicleRepository.fetchVehicles();
        if (vehicles == null || vehicles.isEmpty()) {
            loggingMiddleware.warn("service", "No vehicles found for optimization");
            return OptimizeResponse.builder()
                    .depotId(depotId)
                    .mechanicHoursBudget(targetDepot.getMechanicHours())
                    .totalImpactScore(0)
                    .totalDuration(0)
                    .selectedTasks(new ArrayList<>())
                    .build();
        }

        int capacity = targetDepot.getMechanicHours();
        loggingMiddleware.info("service",
                "Starting knapsack optimization for depotId=" + depotId +
                ", budget=" + capacity +
                ", vehicleCount=" + vehicles.size());

        // Run 0/1 Knapsack algorithm
        KnapsackResult result = solve0_1Knapsack(vehicles, capacity);

        // Build response
        OptimizeResponse response = OptimizeResponse.builder()
                .depotId(depotId)
                .mechanicHoursBudget(capacity)
                .totalImpactScore(result.maxValue)
                .totalDuration(result.totalWeight)
                .selectedTasks(result.selectedTasks)
                .build();

        loggingMiddleware.info("service",
                "Optimization complete for depotId=" + depotId +
                ", totalImpact=" + result.maxValue +
                ", tasksSelected=" + result.selectedTasks.size());

        return response;
    }

    /**
     * Optimize maintenance tasks for all depots
     */
    public List<OptimizeResponse> optimizeForAllDepots() {
        loggingMiddleware.info("service", "Starting optimization for all depots");

        List<Depot> depots = vehicleRepository.fetchDepots();
        List<OptimizeResponse> responses = new ArrayList<>();

        for (Depot depot : depots) {
            responses.add(optimizeForDepot(depot.getId()));
        }

        loggingMiddleware.info("service", "All depots optimized: " + depots.size() + " depots processed");
        return responses;
    }

    /**
     * 0/1 Knapsack Dynamic Programming Algorithm
     *
     * Time Complexity: O(n * capacity)
     * Space Complexity: O(n * capacity)
     *
     * @param vehicles List of vehicles with duration (weight) and impact (value)
     * @param capacity Maximum mechanic hours available
     * @return Result containing selected tasks and total metrics
     */
    private KnapsackResult solve0_1Knapsack(List<Vehicle> vehicles, int capacity) {
        int n = vehicles.size();

        // DP table: dp[i][w] = maximum impact using first i items with capacity w
        int[][] dp = new int[n + 1][capacity + 1];

        // Fill the DP table
        for (int i = 1; i <= n; i++) {
            Vehicle vehicle = vehicles.get(i - 1);
            int weight = vehicle.getDuration();
            int value = vehicle.getImpact();

            for (int w = 1; w <= capacity; w++) {
                // Option 1: Don't include this vehicle
                dp[i][w] = dp[i - 1][w];

                // Option 2: Include this vehicle (if it fits)
                if (weight <= w) {
                    dp[i][w] = Math.max(dp[i][w], dp[i - 1][w - weight] + value);
                }
            }
        }

        // Backtrack to find selected items
        List<SelectedTask> selectedTasks = new ArrayList<>();
        int totalWeight = 0;
        int totalValue = dp[n][capacity];

        int w = capacity;
        for (int i = n; i > 0 && w > 0; i--) {
            // If this item was included in the optimal solution
            if (dp[i][w] != dp[i - 1][w]) {
                Vehicle vehicle = vehicles.get(i - 1);
                selectedTasks.add(0, SelectedTask.builder()
                        .taskId(vehicle.getTaskId())
                        .duration(vehicle.getDuration())
                        .impact(vehicle.getImpact())
                        .build());

                w -= vehicle.getDuration();
                totalWeight += vehicle.getDuration();
            }
        }

        return new KnapsackResult(totalValue, totalWeight, selectedTasks);
    }

    /**
     * Internal class to hold knapsack result
     */
    private static class KnapsackResult {
        int maxValue;
        int totalWeight;
        List<SelectedTask> selectedTasks;

        KnapsackResult(int maxValue, int totalWeight, List<SelectedTask> selectedTasks) {
            this.maxValue = maxValue;
            this.totalWeight = totalWeight;
            this.selectedTasks = selectedTasks;
        }
    }
}

