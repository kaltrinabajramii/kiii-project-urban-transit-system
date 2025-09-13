package io.github.kaltrinabajramii.urbantransitbackend.service.interfaces;

import io.github.kaltrinabajramii.urbantransitbackend.dto.request.CreateRouteRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.request.UpdateRouteRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.request.RouteSearchRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.RouteResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.RouteSummaryResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.RoutePopularityResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.PagedResponse;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TransportType;
import org.springframework.http.ResponseEntity;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * Service interface for route management operations
 * Handles route CRUD operations, search functionality, and route analytics
 */
public interface RouteService {

    // ===== PUBLIC ROUTE OPERATIONS (Available to all users) =====

    /**
     * Get all active routes with basic information
     *
     * @return ResponseEntity containing list of active routes
     */
    ResponseEntity<List<RouteSummaryResponse>> getAllActiveRoutes();

    /**
     * Get active routes with pagination
     *
     * @param page Page number (0-based)
     * @param size Page size
     * @return ResponseEntity containing PagedResponse with active routes
     */
    ResponseEntity<PagedResponse<RouteSummaryResponse>> getActiveRoutes(int page, int size);

    /**
     * Get route details by ID
     *
     * @param routeId Route ID to lookup
     * @return ResponseEntity containing detailed route information
     * @throws RuntimeException if route not found or inactive
     */
    ResponseEntity<RouteResponse> getRouteById(Long routeId);

    /**
     * Get route details by name
     *
     * @param routeName Route name to lookup
     * @return ResponseEntity containing detailed route information
     * @throws RuntimeException if route not found or inactive
     */
    ResponseEntity<RouteResponse> getRouteByName(String routeName);

    // ===== ROUTE SEARCH AND FILTERING =====

    /**
     * Search routes by name or description
     *
     * @param searchTerm Search term to match against route name/description
     * @return ResponseEntity containing list of matching routes
     */
    ResponseEntity<List<RouteSummaryResponse>> searchRoutes(String searchTerm);

    /**
     * Advanced route search with filters and pagination
     *
     * @param searchRequest Search criteria including filters
     * @return ResponseEntity containing PagedResponse with matching routes
     */
    ResponseEntity<PagedResponse<RouteSummaryResponse>> searchRoutesAdvanced(RouteSearchRequest searchRequest);

    /**
     * Get routes by transport type
     *
     * @param transportType Transport type to filter by
     * @return ResponseEntity containing routes of specified transport type
     */
    ResponseEntity<List<RouteSummaryResponse>> getRoutesByTransportType(TransportType transportType);

    /**
     * Find routes that serve a specific stop
     *
     * @param stopName Name of the stop to search for
     * @return ResponseEntity containing routes that include the stop
     */
    ResponseEntity<List<RouteSummaryResponse>> getRoutesByStop(String stopName);

    /**
     * Get routes operating at a specific time
     *
     * @param currentTime Time to check for operating routes
     * @return ResponseEntity containing routes operating at the specified time
     */
    ResponseEntity<List<RouteSummaryResponse>> getRoutesOperatingAtTime(LocalTime currentTime);

    // ===== ADMIN ROUTE MANAGEMENT =====

    /**
     * Create a new route (admin only)
     *
     * @param createRequest Route creation data
     * @return ResponseEntity containing created route details
     * @throws RuntimeException if route name already exists
     */
    ResponseEntity<RouteResponse> createRoute(CreateRouteRequest createRequest);

    /**
     * Update existing route (admin only)
     *
     * @param routeId Route ID to update
     * @param updateRequest Route update data
     * @return ResponseEntity containing updated route details
     * @throws RuntimeException if route not found
     */
    ResponseEntity<RouteResponse> updateRoute(Long routeId, UpdateRouteRequest updateRequest);

    /**
     * Delete route (admin only) - soft delete by setting active to false
     *
     * @param routeId Route ID to delete
     * @return ResponseEntity with confirmation message
     * @throws RuntimeException if route not found or has active tickets
     */
    ResponseEntity<String> deleteRoute(Long routeId);

    /**
     * Activate/Deactivate route (admin only)
     *
     * @param routeId Route ID to update
     * @param active New active status
     * @return ResponseEntity with confirmation message
     */
    ResponseEntity<String> updateRouteStatus(Long routeId, boolean active);

    /**
     * Get all routes including inactive ones (admin only)
     *
     * @param page Page number
     * @param size Page size
     * @return ResponseEntity containing PagedResponse with all routes
     */
    ResponseEntity<PagedResponse<RouteResponse>> getAllRoutesForAdmin(int page, int size);

    // ===== ROUTE ANALYTICS =====

    /**
     * Get most popular routes based on usage
     *
     * @param limit Number of top routes to return
     * @return ResponseEntity containing list of popular routes with usage counts
     */
    ResponseEntity<List<RoutePopularityResponse>> getMostPopularRoutes(int limit);

    /**
     * Get route count by transport type
     *
     * @return ResponseEntity containing route statistics by transport type
     */
    ResponseEntity<Map<TransportType, Long>> getRouteCountByTransportType();

    /**
     * Get routes with no recent usage (for route optimization)
     *
     * @param daysSinceLastUsage Number of days since last usage
     * @return ResponseEntity containing underutilized routes
     */
    ResponseEntity<List<RouteSummaryResponse>> getUnderutilizedRoutes(int daysSinceLastUsage);

    // ===== UTILITY METHODS =====

    /**
     * Check if route exists and is active
     *
     * @param routeId Route ID to check
     * @return true if route exists and is active
     */
    boolean existsActiveRoute(Long routeId);

    /**
     * Check if route name is available for use
     *
     * @param routeName Route name to check
     * @param excludeRouteId Route ID to exclude from check (for updates)
     * @return true if name is available
     */
    boolean isRouteNameAvailable(String routeName, Long excludeRouteId);

    /**
     * Validate route stops (ensure minimum 2 stops, no duplicates)
     *
     * @param stops List of stops to validate
     * @return true if stops are valid
     */
    boolean validateRouteStops(List<String> stops);
}