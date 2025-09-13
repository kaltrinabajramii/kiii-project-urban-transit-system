package io.github.kaltrinabajramii.urbantransitbackend.service.interfaces;

import io.github.kaltrinabajramii.urbantransitbackend.dto.response.TicketUsageResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.PagedResponse;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TransportType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for ticket usage tracking and management
 * Handles usage recording, travel history, and usage analytics
 */
public interface TicketUsageService {

    // ===== USAGE RECORDING =====

    /**
     * Record ticket usage for a journey
     *
     * @param ticketId Ticket ID being used
     * @param routeId Route ID for the journey
     * @param transportType Type of transport used
     * @param boardingStop Boarding stop name
     * @param destinationStop Destination stop name
     * @return ResponseEntity containing usage record details
     */
    ResponseEntity<TicketUsageResponse> recordTicketUsage(Long ticketId, Long routeId, TransportType transportType, String boardingStop, String destinationStop);

    /**
     * Bulk record usage for multiple tickets (for offline synchronization)
     *
     * @param usageRecords List of usage records to create
     * @return ResponseEntity with processing summary
     */
    ResponseEntity<String> bulkRecordUsage(List<TicketUsageResponse> usageRecords);

    // ===== USER USAGE HISTORY =====

    /**
     * Get user's complete travel history
     *
     * @param userDetails Current authenticated user
     * @param page Page number
     * @param size Page size
     * @return ResponseEntity containing PagedResponse with user's travel history
     */
    ResponseEntity<PagedResponse<TicketUsageResponse>> getUserTravelHistory(UserDetails userDetails, int page, int size);

    /**
     * Get user's travel history filtered by date range
     *
     * @param userDetails Current authenticated user
     * @param startDate Start date for filter
     * @param endDate End date for filter
     * @param page Page number
     * @param size Page size
     * @return ResponseEntity containing filtered travel history
     */
    ResponseEntity<PagedResponse<TicketUsageResponse>> getUserTravelHistoryByDateRange(UserDetails userDetails, LocalDateTime startDate, LocalDateTime endDate, int page, int size);

    /**
     * Get user's usage statistics summary
     *
     * @param userDetails Current authenticated user
     * @return ResponseEntity containing user's usage summary (total rides, favorite routes, etc.)
     */
    ResponseEntity<java.util.Map<String, Object>> getUserUsageStatistics(UserDetails userDetails);

    // ===== ADMIN USAGE MANAGEMENT =====

    /**
     * Get all usage records with pagination (admin only)
     *
     * @param page Page number
     * @param size Page size
     * @return ResponseEntity containing PagedResponse with all usage records
     */
    ResponseEntity<PagedResponse<TicketUsageResponse>> getAllUsageRecords(int page, int size);

    /**
     * Get usage records by route (admin only)
     *
     * @param routeId Route ID to filter by
     * @param page Page number
     * @param size Page size
     * @return ResponseEntity containing usage records for specific route
     */
    ResponseEntity<PagedResponse<TicketUsageResponse>> getUsageByRoute(Long routeId, int page, int size);

    /**
     * Get usage records by transport type (admin only)
     *
     * @param transportType Transport type to filter by
     * @param page Page number
     * @param size Page size
     * @return ResponseEntity containing usage records for transport type
     */
    ResponseEntity<PagedResponse<TicketUsageResponse>> getUsageByTransportType(TransportType transportType, int page, int size);

    /**
     * Delete usage record (admin only)
     *
     * @param usageId Usage record ID to delete
     * @return ResponseEntity with confirmation message
     */
    ResponseEntity<String> deleteUsageRecord(Long usageId);

    // ===== USAGE ANALYTICS =====

    /**
     * Get most popular boarding stops
     *
     * @param limit Number of top stops to return
     * @return ResponseEntity containing popular boarding stops with counts
     */
    ResponseEntity<List<Object[]>> getMostPopularBoardingStops(int limit);

    /**
     * Get most popular destinations
     *
     * @param limit Number of top destinations to return
     * @return ResponseEntity containing popular destinations with counts
     */
    ResponseEntity<List<Object[]>> getMostPopularDestinations(int limit);

    /**
     * Get usage patterns by time of day
     *
     * @return ResponseEntity containing hourly usage distribution
     */
    ResponseEntity<List<Object[]>> getUsagePatternsByTimeOfDay();

    /**
     * Get usage trends by day of week
     *
     * @return ResponseEntity containing daily usage patterns
     */
    ResponseEntity<List<Object[]>> getUsagePatternsByDayOfWeek();

    /**
     * Get route popularity rankings
     *
     * @param limit Number of top routes to return
     * @return ResponseEntity containing routes ranked by usage
     */
    ResponseEntity<List<Object[]>> getRoutePopularityRankings(int limit);

    // ===== UTILITY METHODS =====

    /**
     * Calculate user's total rides count
     *
     * @param userId User ID to calculate for
     * @return Total number of rides taken by user
     */
    Long calculateUserTotalRides(Long userId);

    /**
     * Get user's most used route
     *
     * @param userId User ID to analyze
     * @return ResponseEntity containing user's favorite route information
     */
    ResponseEntity<Object> getUserMostUsedRoute(Long userId);

    /**
     * Calculate average journey duration (if implementing time tracking)
     *
     * @param routeId Route ID to calculate for
     * @return Average journey time in minutes
     */
    Double calculateAverageJourneyDuration(Long routeId);
}