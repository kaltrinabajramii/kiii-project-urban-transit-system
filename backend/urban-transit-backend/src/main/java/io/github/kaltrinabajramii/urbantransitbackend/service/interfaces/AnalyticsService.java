package io.github.kaltrinabajramii.urbantransitbackend.service.interfaces;

import io.github.kaltrinabajramii.urbantransitbackend.dto.response.DashboardStatsResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.SalesAnalyticsResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.RoutePopularityResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.TransportTypeStatsResponse;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TransportType;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for system analytics and reporting
 * Handles dashboard statistics, usage analytics, and business intelligence
 */
public interface AnalyticsService {

    // ===== DASHBOARD ANALYTICS =====

    /**
     * Get main dashboard statistics
     *
     * @return ResponseEntity containing overall system statistics
     */
    ResponseEntity<DashboardStatsResponse> getDashboardStats();

    /**
     * Get today's usage statistics
     *
     * @return ResponseEntity containing today's activity metrics
     */
    ResponseEntity<DashboardStatsResponse> getTodayStats();

    /**
     * Get revenue analytics for specified period
     *
     * @param startDate Start date for analytics
     * @param endDate End date for analytics
     * @return ResponseEntity containing revenue breakdown
     */
    ResponseEntity<java.math.BigDecimal> getRevenueAnalytics(LocalDateTime startDate, LocalDateTime endDate);

    // ===== SALES ANALYTICS =====

    /**
     * Get sales performance by ticket type
     *
     * @return ResponseEntity containing sales breakdown by ticket type
     */
    ResponseEntity<List<SalesAnalyticsResponse>> getSalesAnalyticsByTicketType();

    /**
     * Get daily sales trends for specified period
     *
     * @param days Number of days to analyze
     * @return ResponseEntity containing daily sales data
     */
    ResponseEntity<List<Object[]>> getDailySalesTrends(int days);

    /**
     * Get monthly sales comparison
     *
     * @param months Number of months to compare
     * @return ResponseEntity containing monthly sales comparison
     */
    ResponseEntity<List<Object[]>> getMonthlySalesComparison(int months);

    /**
     * Get peak usage hours analysis
     *
     * @return ResponseEntity containing usage patterns by hour
     */
    ResponseEntity<List<Object[]>> getPeakUsageHours();

    // ===== ROUTE ANALYTICS =====

    /**
     * Get most popular routes with usage statistics
     *
     * @param limit Number of top routes to return
     * @return ResponseEntity containing popular routes with usage counts
     */
    ResponseEntity<List<RoutePopularityResponse>> getTopRoutes(int limit);

    /**
     * Get route utilization analysis
     *
     * @return ResponseEntity containing route usage efficiency metrics
     */
    ResponseEntity<List<Object[]>> getRouteUtilizationAnalysis();

    /**
     * Get transport type usage statistics
     *
     * @return ResponseEntity containing usage breakdown by transport type
     */
    ResponseEntity<List<TransportTypeStatsResponse>> getTransportTypeStats();

    /**
     * Get stop popularity analysis
     *
     * @param limit Number of top stops to return
     * @return ResponseEntity containing most popular boarding/destination stops
     */
    ResponseEntity<List<Object[]>> getPopularStops(int limit);

    // ===== USER ANALYTICS =====

    /**
     * Get user engagement statistics
     *
     * @return ResponseEntity containing user activity metrics
     */
    ResponseEntity<List<Object[]>> getUserEngagementStats();

    /**
     * Get user segmentation analysis
     *
     * @return ResponseEntity containing user behavior patterns
     */
    ResponseEntity<List<Object[]>> getUserSegmentationAnalysis();

    /**
     * Get customer retention metrics
     *
     * @return ResponseEntity containing retention rates and trends
     */
    ResponseEntity<List<Object[]>> getCustomerRetentionMetrics();

    /**
     * Get new vs returning user analysis
     *
     * @param days Number of days to analyze
     * @return ResponseEntity containing user acquisition vs retention data
     */
    ResponseEntity<List<Object[]>> getNewVsReturningUsers(int days);

    // ===== OPERATIONAL ANALYTICS =====

    /**
     * Get system performance metrics
     *
     * @return ResponseEntity containing system health and performance data
     */
    ResponseEntity<List<Object[]>> getSystemPerformanceMetrics();

    /**
     * Get ticket validation failure analysis
     *
     * @return ResponseEntity containing failed validation reasons and counts
     */
    ResponseEntity<List<Object[]>> getTicketValidationFailures();

    /**
     * Generate custom analytics report
     *
     * @param reportType Type of report to generate
     * @param startDate Start date for report
     * @param endDate End date for report
     * @param parameters Additional report parameters
     * @return ResponseEntity containing custom report data
     */
    ResponseEntity<Object> generateCustomReport(String reportType, LocalDateTime startDate, LocalDateTime endDate, java.util.Map<String, Object> parameters);

    // ===== EXPORT FUNCTIONS =====

    /**
     * Export analytics data to CSV format
     *
     * @param reportType Type of analytics to export
     * @param startDate Start date for export
     * @param endDate End date for export
     * @return ResponseEntity containing CSV export data
     */
    ResponseEntity<byte[]> exportAnalyticsToCSV(String reportType, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Schedule automated report generation
     *
     * @param reportType Type of report to schedule
     * @param frequency Report frequency (daily, weekly, monthly)
     * @param recipients Email recipients for the report
     * @return ResponseEntity with scheduling confirmation
     */
    ResponseEntity<String> scheduleAutomatedReport(String reportType, String frequency, List<String> recipients);
}