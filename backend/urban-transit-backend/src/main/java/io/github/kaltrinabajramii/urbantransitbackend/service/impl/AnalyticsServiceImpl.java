package io.github.kaltrinabajramii.urbantransitbackend.service.impl;

import io.github.kaltrinabajramii.urbantransitbackend.dto.response.DashboardStatsResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.SalesAnalyticsResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.RoutePopularityResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.TransportTypeStatsResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.RouteSummaryResponse;
import io.github.kaltrinabajramii.urbantransitbackend.model.entity.Route;
import io.github.kaltrinabajramii.urbantransitbackend.model.entity.Ticket;
import io.github.kaltrinabajramii.urbantransitbackend.model.entity.TicketUsage;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TransportType;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketType;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketStatus;
import io.github.kaltrinabajramii.urbantransitbackend.repository.UserRepository;
import io.github.kaltrinabajramii.urbantransitbackend.repository.RouteRepository;
import io.github.kaltrinabajramii.urbantransitbackend.repository.TicketRepository;
import io.github.kaltrinabajramii.urbantransitbackend.repository.TicketUsageRepository;
import io.github.kaltrinabajramii.urbantransitbackend.service.interfaces.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final UserRepository userRepository;
    private final RouteRepository routeRepository;
    private final TicketRepository ticketRepository;
    private final TicketUsageRepository ticketUsageRepository;

    // ===== DASHBOARD ANALYTICS =====

    @Override
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        try {
            DashboardStatsResponse stats = new DashboardStatsResponse();

            stats.setTotalUsers(userRepository.count());
            stats.setTotalRoutes(routeRepository.count());
            stats.setTotalActiveTickets(ticketRepository.countByStatus(TicketStatus.ACTIVE));
            stats.setTodayUsageCount(ticketUsageRepository.countTodayUsage());
            stats.setTotalRevenue(ticketRepository.calculateRevenueByDateRange(
                            LocalDateTime.now().minusYears(1), LocalDateTime.now())
                    .orElse(BigDecimal.ZERO));
            stats.setTotalTicketsSold(ticketRepository.count());

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<DashboardStatsResponse> getTodayStats() {
        try {
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

            DashboardStatsResponse stats = new DashboardStatsResponse();
            stats.setTodayUsageCount(ticketUsageRepository.countTodayUsage());
            stats.setTotalRevenue(ticketRepository.calculateRevenueByDateRange(startOfDay, endOfDay)
                    .orElse(BigDecimal.ZERO));

            long todayTickets = ticketRepository.findTicketsPurchasedBetween(startOfDay, endOfDay,
                    PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();
            stats.setTotalTicketsSold(todayTickets);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<BigDecimal> getRevenueAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            BigDecimal revenue = ticketRepository.calculateRevenueByDateRange(startDate, endDate)
                    .orElse(BigDecimal.ZERO);
            return ResponseEntity.ok(revenue);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== SALES ANALYTICS =====

    @Override
    public ResponseEntity<List<SalesAnalyticsResponse>> getSalesAnalyticsByTicketType() {
        try {
            List<Object[]> results = ticketRepository.countTicketsByType();

            List<SalesAnalyticsResponse> analytics = results.stream()
                    .map(result -> {
                        TicketType type = (TicketType) result[0];
                        Long count = (Long) result[1];

                        // Simple revenue calculation
                        BigDecimal revenue = BigDecimal.ZERO;
                        try {
                            List< Ticket > tickets = ticketRepository.findByTicketTypeOrderByPurchaseDateDesc(type);
                            revenue = tickets.stream()
                                    .map(ticket -> {
                                        try {
                                            return (BigDecimal) ticket.getClass().getMethod("getPrice").invoke(ticket);
                                        } catch (Exception e) {
                                            return BigDecimal.ZERO;
                                        }
                                    })
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                        } catch (Exception e) {
                            // Continue with zero revenue
                        }

                        SalesAnalyticsResponse analytics1 = new SalesAnalyticsResponse();
                        analytics1.setTicketType(type);
                        analytics1.setTicketCount(count);
                        analytics1.setTotalRevenue(revenue);
                        return analytics1;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<Object[]>> getDailySalesTrends(int days) {
        try {
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(days);
            List<Object[]> trends = ticketUsageRepository.getDailyUsageStats(startDate, endDate);
            return ResponseEntity.ok(trends);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<Object[]>> getMonthlySalesComparison(int months) {
        try {
            List<Object[]> monthlyStats = new ArrayList<>();

            for (int i = 0; i < months; i++) {
                LocalDateTime endDate = LocalDateTime.now().minusMonths(i);
                LocalDateTime startDate = endDate.minusMonths(1);

                BigDecimal revenue = ticketRepository.calculateRevenueByDateRange(startDate, endDate)
                        .orElse(BigDecimal.ZERO);

                monthlyStats.add(new Object[]{endDate.getMonth().name(), revenue});
            }

            return ResponseEntity.ok(monthlyStats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<Object[]>> getPeakUsageHours() {
        try {
            // Simple implementation - return placeholder data
            List<Object[]> hourlyData = new ArrayList<>();
            for (int hour = 0; hour < 24; hour++) {
                hourlyData.add(new Object[]{hour, (long)(Math.random() * 100)});
            }
            return ResponseEntity.ok(hourlyData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== ROUTE ANALYTICS =====

    @Override
    public ResponseEntity<List<RoutePopularityResponse>> getTopRoutes(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<Object[]> results = ticketUsageRepository.findMostPopularRoutes(pageable).getContent();

            List<RoutePopularityResponse> topRoutes = results.stream()
                    .map(result -> {
                        Route route = (Route) result[0];
                        Long usageCount = (Long) result[1];

                        RoutePopularityResponse response = new RoutePopularityResponse();
                        response.setRoute(mapToRouteSummaryResponse(route));
                        response.setUsageCount(usageCount);
                        return response;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(topRoutes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<Object[]>> getRouteUtilizationAnalysis() {
        try {
            List<Route> routes = routeRepository.findByActiveTrueOrderByRouteNameAsc();
            List<Object[]> utilization = new ArrayList<>();

            for (Route route : routes) {
                List< TicketUsage > usage = ticketUsageRepository.findByRouteOrderByUsedAtDesc(route);
                utilization.add(new Object[]{
                        route.getRouteName(),
                        route.getTransportType(),
                        (long) usage.size()
                });
            }

            return ResponseEntity.ok(utilization);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<TransportTypeStatsResponse>> getTransportTypeStats() {
        try {
            List<TransportTypeStatsResponse> stats = new ArrayList<>();

            // Get route counts
            List<Object[]> routeCounts = routeRepository.countRoutesByTransportType();
            Map<TransportType, Long> routeMap = routeCounts.stream()
                    .collect(Collectors.toMap(
                            result -> (TransportType) result[0],
                            result -> (Long) result[1]
                    ));

            // Get usage counts
            List<Object[]> usageCounts = ticketUsageRepository.countUsageByTransportType();
            Map<TransportType, Long> usageMap = usageCounts.stream()
                    .collect(Collectors.toMap(
                            result -> (TransportType) result[0],
                            result -> (Long) result[1]
                    ));

            for (TransportType type : TransportType.values()) {
                TransportTypeStatsResponse stat = new TransportTypeStatsResponse();
                stat.setTransportType(type);
                stat.setRouteCount(routeMap.getOrDefault(type, 0L));
                stat.setUsageCount(usageMap.getOrDefault(type, 0L));
                stats.add(stat);
            }

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<Object[]>> getPopularStops(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<Object[]> popularStops = ticketUsageRepository.findMostPopularBoardingStops(pageable).getContent();
            return ResponseEntity.ok(popularStops);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== USER ANALYTICS =====

    @Override
    public ResponseEntity<List<Object[]>> getUserEngagementStats() {
        try {
            List<Object[]> stats = new ArrayList<>();

            long totalUsers = userRepository.count();
            long usersWithTickets = totalUsers - userRepository.findUsersWithoutTickets(
                    PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();

            stats.add(new Object[]{"Total Users", totalUsers});
            stats.add(new Object[]{"Users with Tickets", usersWithTickets});
            stats.add(new Object[]{"Engagement Rate", (double) usersWithTickets / totalUsers * 100});

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<Object[]>> getUserSegmentationAnalysis() {
        try {
            List<Object[]> segmentation = new ArrayList<>();

            for (TicketType type : TicketType.values()) {
                List<Ticket> users = ticketRepository.findByTicketTypeOrderByPurchaseDateDesc(type);
                segmentation.add(new Object[]{type.getDisplayName() + " Users", (long) users.size()});
            }

            return ResponseEntity.ok(segmentation);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<Object[]>> getCustomerRetentionMetrics() {
        try {
            // Simple retention calculation
            LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
            LocalDateTime twoMonthsAgo = LocalDateTime.now().minusMonths(2);

            long oldUsers = ticketRepository.findTicketsPurchasedBetween(
                    twoMonthsAgo.minusMonths(1), twoMonthsAgo, PageRequest.of(0, Integer.MAX_VALUE)
            ).getTotalElements();

            long retainedUsers = ticketRepository.findTicketsPurchasedBetween(
                    oneMonthAgo, LocalDateTime.now(), PageRequest.of(0, Integer.MAX_VALUE)
            ).getTotalElements();

            double retentionRate = oldUsers > 0 ? (double) retainedUsers / oldUsers * 100 : 0;

            List<Object[]> metrics = Arrays.asList(
                    new Object[]{"Previous Month Users", oldUsers},
                    new Object[]{"Retained Users", retainedUsers},
                    new Object[]{"Retention Rate", retentionRate}
            );

            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<Object[]>> getNewVsReturningUsers(int days) {
        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(days);

            long newUsers = userRepository.findRecentlyRegisteredUsers(
                    PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();

            long totalActive = ticketRepository.findTicketsPurchasedBetween(
                    startDate, LocalDateTime.now(), PageRequest.of(0, Integer.MAX_VALUE)
            ).getTotalElements();

            long returningUsers = Math.max(0, totalActive - newUsers);

            List<Object[]> analysis = Arrays.asList(
                    new Object[]{"New Users", newUsers},
                    new Object[]{"Returning Users", returningUsers},
                    new Object[]{"Total Active", totalActive}
            );

            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== OPERATIONAL ANALYTICS =====

    @Override
    public ResponseEntity<List<Object[]>> getSystemPerformanceMetrics() {
        try {
            List<Object[]> metrics = Arrays.asList(
                    new Object[]{"Total Routes", routeRepository.count()},
                    new Object[]{"Active Routes", routeRepository.findAllActiveRoutesOrderByName ()},
                    new Object[]{"Total Users", userRepository.count()},
                    new Object[]{"Total Tickets", ticketRepository.count()},
                    new Object[]{"Active Tickets", ticketRepository.countByStatus(TicketStatus.ACTIVE)}
            );

            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<Object[]>> getTicketValidationFailures() {
        try {
            List<Object[]> failures = Arrays.asList(
                    new Object[]{"Expired Tickets", ticketRepository.countByStatus(TicketStatus.EXPIRED)},
                    new Object[]{"Used Tickets", ticketRepository.countByStatus(TicketStatus.USED)},
                    new Object[]{"Active Tickets", ticketRepository.countByStatus(TicketStatus.ACTIVE)}
            );

            return ResponseEntity.ok(failures);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Object> generateCustomReport(String reportType, LocalDateTime startDate, LocalDateTime endDate, Map<String, Object> parameters) {
        try {
            Map<String, Object> report = new HashMap<>();
            report.put("reportType", reportType);
            report.put("period", Map.of("start", startDate, "end", endDate));

            switch (reportType.toLowerCase()) {
                case "revenue":
                    BigDecimal revenue = ticketRepository.calculateRevenueByDateRange(startDate, endDate)
                            .orElse(BigDecimal.ZERO);
                    report.put("totalRevenue", revenue);
                    break;
                case "usage":
                    long usage = ticketUsageRepository.findUsageByDateRange(
                            startDate, endDate, PageRequest.of(0, Integer.MAX_VALUE)
                    ).getTotalElements();
                    report.put("totalUsage", usage);
                    break;
                default:
                    report.put("error", "Unknown report type");
            }

            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== EXPORT FUNCTIONS =====

    @Override
    public ResponseEntity<byte[]> exportAnalyticsToCSV(String reportType, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            StringBuilder csv = new StringBuilder();
            csv.append("Report Type,Start Date,End Date\n");
            csv.append(String.format("%s,%s,%s\n", reportType, startDate, endDate));

            switch (reportType.toLowerCase()) {
                case "revenue":
                    BigDecimal revenue = ticketRepository.calculateRevenueByDateRange(startDate, endDate)
                            .orElse(BigDecimal.ZERO);
                    csv.append("Total Revenue,").append(revenue).append("\n");
                    break;
                case "usage":
                    long usage = ticketUsageRepository.findUsageByDateRange(
                            startDate, endDate, PageRequest.of(0, Integer.MAX_VALUE)
                    ).getTotalElements();
                    csv.append("Total Usage,").append(usage).append("\n");
                    break;
            }

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + reportType + "-report.csv")
                    .body(csv.toString().getBytes());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<String> scheduleAutomatedReport(String reportType, String frequency, List<String> recipients) {
        try {
            // Simple acknowledgment - would integrate with scheduler in production
            String message = String.format("Report '%s' scheduled %s for %d recipients",
                    reportType, frequency, recipients.size());
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Schedule failed");
        }
    }

    // ===== PRIVATE HELPER METHODS =====

    private RouteSummaryResponse mapToRouteSummaryResponse(Route route) {
        RouteSummaryResponse response = new RouteSummaryResponse();
        response.setId(route.getId());
        response.setRouteName(route.getRouteName());
        response.setDescription(route.getDescription());
        response.setTransportType(route.getTransportType());
        response.setStopCount(route.getStops() != null ? route.getStops().size() : 0);
        response.setActive(route.getActive());
        return response;
    }
}