package io.github.kaltrinabajramii.urbantransitbackend.controller.rest;

import io.github.kaltrinabajramii.urbantransitbackend.dto.response.DashboardStatsResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.SalesAnalyticsResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.RoutePopularityResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.TransportTypeStatsResponse;
import io.github.kaltrinabajramii.urbantransitbackend.service.interfaces.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return analyticsService.getDashboardStats();
    }

    @GetMapping("/dashboard/today")
    public ResponseEntity<DashboardStatsResponse> getTodayStats() {
        return analyticsService.getTodayStats();
    }

    @GetMapping("/revenue")
    public ResponseEntity<BigDecimal> getRevenueAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return analyticsService.getRevenueAnalytics(startDate, endDate);
    }

    @GetMapping("/sales/by-ticket-type")
    public ResponseEntity<List<SalesAnalyticsResponse>> getSalesAnalyticsByTicketType() {
        return analyticsService.getSalesAnalyticsByTicketType();
    }

    @GetMapping("/sales/daily-trends")
    public ResponseEntity<List<Object[]>> getDailySalesTrends(@RequestParam(defaultValue = "30") int days) {
        return analyticsService.getDailySalesTrends(days);
    }

    @GetMapping("/sales/monthly-comparison")
    public ResponseEntity<List<Object[]>> getMonthlySalesComparison(@RequestParam(defaultValue = "12") int months) {
        return analyticsService.getMonthlySalesComparison(months);
    }

    @GetMapping("/usage/peak-hours")
    public ResponseEntity<List<Object[]>> getPeakUsageHours() {
        return analyticsService.getPeakUsageHours();
    }

    @GetMapping("/routes/top")
    public ResponseEntity<List<RoutePopularityResponse>> getTopRoutes(@RequestParam(defaultValue = "10") int limit) {
        return analyticsService.getTopRoutes(limit);
    }

    @GetMapping("/routes/utilization")
    public ResponseEntity<List<Object[]>> getRouteUtilizationAnalysis() {
        return analyticsService.getRouteUtilizationAnalysis();
    }

    @GetMapping("/transport-types")
    public ResponseEntity<List<TransportTypeStatsResponse>> getTransportTypeStats() {
        return analyticsService.getTransportTypeStats();
    }

    @GetMapping("/stops/popular")
    public ResponseEntity<List<Object[]>> getPopularStops(@RequestParam(defaultValue = "10") int limit) {
        return analyticsService.getPopularStops(limit);
    }

    @GetMapping("/users/engagement")
    public ResponseEntity<List<Object[]>> getUserEngagementStats() {
        return analyticsService.getUserEngagementStats();
    }

    @GetMapping("/users/segmentation")
    public ResponseEntity<List<Object[]>> getUserSegmentationAnalysis() {
        return analyticsService.getUserSegmentationAnalysis();
    }

    @GetMapping("/users/retention")
    public ResponseEntity<List<Object[]>> getCustomerRetentionMetrics() {
        return analyticsService.getCustomerRetentionMetrics();
    }

    @GetMapping("/users/new-vs-returning")
    public ResponseEntity<List<Object[]>> getNewVsReturningUsers(@RequestParam(defaultValue = "30") int days) {
        return analyticsService.getNewVsReturningUsers(days);
    }

    @GetMapping("/system/performance")
    public ResponseEntity<List<Object[]>> getSystemPerformanceMetrics() {
        return analyticsService.getSystemPerformanceMetrics();
    }

    @GetMapping("/tickets/validation-failures")
    public ResponseEntity<List<Object[]>> getTicketValidationFailures() {
        return analyticsService.getTicketValidationFailures();
    }

    @PostMapping("/reports/custom")
    public ResponseEntity<Object> generateCustomReport(
            @RequestParam String reportType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestBody Map<String, Object> parameters) {
        return analyticsService.generateCustomReport(reportType, startDate, endDate, parameters);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportAnalyticsToCSV(
            @RequestParam String reportType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return analyticsService.exportAnalyticsToCSV(reportType, startDate, endDate);
    }

    @PostMapping("/reports/schedule")
    public ResponseEntity<String> scheduleAutomatedReport(
            @RequestParam String reportType,
            @RequestParam String frequency,
            @RequestBody List<String> recipients) {
        return analyticsService.scheduleAutomatedReport(reportType, frequency, recipients);
    }
}
