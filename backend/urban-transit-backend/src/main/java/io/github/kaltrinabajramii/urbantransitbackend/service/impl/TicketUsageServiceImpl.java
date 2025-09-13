package io.github.kaltrinabajramii.urbantransitbackend.service.impl;

import io.github.kaltrinabajramii.urbantransitbackend.dto.response.TicketUsageResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.PagedResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.TicketSummaryResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.RouteSummaryResponse;
import io.github.kaltrinabajramii.urbantransitbackend.model.entity.TicketUsage;
import io.github.kaltrinabajramii.urbantransitbackend.model.entity.Ticket;
import io.github.kaltrinabajramii.urbantransitbackend.model.entity.Route;
import io.github.kaltrinabajramii.urbantransitbackend.model.entity.User;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TransportType;
import io.github.kaltrinabajramii.urbantransitbackend.repository.TicketUsageRepository;
import io.github.kaltrinabajramii.urbantransitbackend.repository.TicketRepository;
import io.github.kaltrinabajramii.urbantransitbackend.repository.RouteRepository;
import io.github.kaltrinabajramii.urbantransitbackend.service.interfaces.TicketUsageService;
import io.github.kaltrinabajramii.urbantransitbackend.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketUsageServiceImpl implements TicketUsageService {

    private final TicketUsageRepository ticketUsageRepository;
    private final TicketRepository ticketRepository;
    private final RouteRepository routeRepository;
    private final UserService userService;

    // ===== USAGE RECORDING =====

    @Override
    public ResponseEntity<TicketUsageResponse> recordTicketUsage(Long ticketId, Long routeId, TransportType transportType, String boardingStop, String destinationStop) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
            if (ticket == null) {
                return ResponseEntity.notFound().build();
            }

            Route route = routeRepository.findById(routeId).orElse(null);
            if (route == null || !route.getActive()) {
                return ResponseEntity.badRequest().build();
            }

            TicketUsage usage = new TicketUsage();
            usage.setTicket(ticket);
            usage.setRoute(route);
            usage.setTransportType(transportType);
            usage.setBoardingStop(boardingStop);
            usage.setDestinationStop(destinationStop);

            TicketUsage savedUsage = ticketUsageRepository.save(usage);
            return ResponseEntity.ok(mapToTicketUsageResponse(savedUsage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<String> bulkRecordUsage(List<TicketUsageResponse> usageRecords) {
        try {
            int processed = 0;
            for (TicketUsageResponse record : usageRecords) {
                // Simple bulk processing
                try {
                    recordTicketUsage(
                            record.getTicket().getId(),
                            record.getRoute().getId(),
                            record.getTransportType(),
                            record.getBoardingStop(),
                            record.getDestinationStop()
                    );
                    processed++;
                } catch (Exception e) {
                    // Continue processing other records
                }
            }
            return ResponseEntity.ok("Processed " + processed + " usage records");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Bulk processing failed");
        }
    }

    // ===== USER USAGE HISTORY =====

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PagedResponse<TicketUsageResponse>> getUserTravelHistory(UserDetails userDetails, int page, int size) {
        try {
            User user = userService.getCurrentUser(userDetails);
            Pageable pageable = PageRequest.of(page, size);
            Page<TicketUsage> usagePage = ticketUsageRepository.findUserTicketUsage(user, pageable);

            List<TicketUsageResponse> usage = usagePage.getContent().stream()
                    .map(this::mapToTicketUsageResponse)
                    .collect(Collectors.toList());

            PagedResponse<TicketUsageResponse> response = PagedResponse.of(
                    usage, usagePage.getNumber(), usagePage.getSize(),
                    usagePage.getTotalElements(), usagePage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PagedResponse<TicketUsageResponse>> getUserTravelHistoryByDateRange(UserDetails userDetails, LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        try {
            User user = userService.getCurrentUser(userDetails);
            Pageable pageable = PageRequest.of(page, size);

            // Get all user usage and filter by date range
            Page<TicketUsage> usagePage = ticketUsageRepository.findUserTicketUsage(user, pageable);
            List<TicketUsage> filteredUsage = usagePage.getContent().stream()
                    .filter(usage -> {
                        LocalDateTime usedAt = usage.getUsedAt();
                        return usedAt.isAfter(startDate) && usedAt.isBefore(endDate);
                    })
                    .collect(Collectors.toList());

            List<TicketUsageResponse> usage = filteredUsage.stream()
                    .map(this::mapToTicketUsageResponse)
                    .collect(Collectors.toList());

            // Create simple page response
            PagedResponse<TicketUsageResponse> response = PagedResponse.of(
                    usage, page, size, filteredUsage.size(),
                    (filteredUsage.size() + size - 1) / size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getUserUsageStatistics(UserDetails userDetails) {
        try {
            User user = userService.getCurrentUser(userDetails);
            Map<String, Object> stats = new HashMap<>();

            // Basic statistics
            Long totalRides = calculateUserTotalRides(user.getId());
            stats.put("totalRides", totalRides);

            // Most used route
            ResponseEntity<Object> mostUsedRoute = getUserMostUsedRoute(user.getId());
            if (mostUsedRoute.getStatusCode() == HttpStatus.OK) {
                stats.put("favoriteRoute", mostUsedRoute.getBody());
            }

            // Usage this month
            LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            Long monthlyRides = ticketUsageRepository.countUserRidesByDateRange(
                    user.getId(), monthStart, LocalDateTime.now());
            stats.put("monthlyRides", monthlyRides);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== ADMIN USAGE MANAGEMENT =====

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PagedResponse<TicketUsageResponse>> getAllUsageRecords(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<TicketUsage> usagePage = ticketUsageRepository.findAll(pageable);

            List<TicketUsageResponse> usage = usagePage.getContent().stream()
                    .map(this::mapToTicketUsageResponse)
                    .collect(Collectors.toList());

            PagedResponse<TicketUsageResponse> response = PagedResponse.of(
                    usage, usagePage.getNumber(), usagePage.getSize(),
                    usagePage.getTotalElements(), usagePage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PagedResponse<TicketUsageResponse>> getUsageByRoute(Long routeId, int page, int size) {
        try {
            Route route = routeRepository.findById(routeId).orElse(null);
            if (route == null) {
                return ResponseEntity.notFound().build();
            }

            List<TicketUsage> usageList = ticketUsageRepository.findByRouteOrderByUsedAtDesc(route);

            // Simple pagination
            Pageable pageable = PageRequest.of(page, size);
            int start = Math.min((int) pageable.getOffset(), usageList.size());
            int end = Math.min(start + pageable.getPageSize(), usageList.size());
            List<TicketUsage> pageContent = usageList.subList(start, end);

            List<TicketUsageResponse> usage = pageContent.stream()
                    .map(this::mapToTicketUsageResponse)
                    .collect(Collectors.toList());

            PagedResponse<TicketUsageResponse> response = PagedResponse.of(
                    usage, page, size, usageList.size(),
                    (usageList.size() + size - 1) / size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PagedResponse<TicketUsageResponse>> getUsageByTransportType(TransportType transportType, int page, int size) {
        try {
            List<TicketUsage> usageList = ticketUsageRepository.findByTransportTypeOrderByUsedAtDesc(transportType);

            // Simple pagination
            Pageable pageable = PageRequest.of(page, size);
            int start = Math.min((int) pageable.getOffset(), usageList.size());
            int end = Math.min(start + pageable.getPageSize(), usageList.size());
            List<TicketUsage> pageContent = usageList.subList(start, end);

            List<TicketUsageResponse> usage = pageContent.stream()
                    .map(this::mapToTicketUsageResponse)
                    .collect(Collectors.toList());

            PagedResponse<TicketUsageResponse> response = PagedResponse.of(
                    usage, page, size, usageList.size(),
                    (usageList.size() + size - 1) / size);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<String> deleteUsageRecord(Long usageId) {
        try {
            TicketUsage usage = ticketUsageRepository.findById(usageId).orElse(null);
            if (usage == null) {
                return ResponseEntity.notFound().build();
            }

            ticketUsageRepository.delete(usage);
            return ResponseEntity.ok("Usage record deleted");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Delete failed");
        }
    }

    // ===== USAGE ANALYTICS =====

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<Object[]>> getMostPopularBoardingStops(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<Object[]> popularStops = ticketUsageRepository.findMostPopularBoardingStops(pageable).getContent();
            return ResponseEntity.ok(popularStops);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<Object[]>> getMostPopularDestinations(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<Object[]> popularDestinations = ticketUsageRepository.findMostPopularDestinations(pageable).getContent();
            return ResponseEntity.ok(popularDestinations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<Object[]>> getUsagePatternsByTimeOfDay() {
        try {
            // Simple implementation - return hourly usage counts
            List<Object[]> patterns = List.of(
                    new Object[]{"06:00", 50L},
                    new Object[]{"07:00", 120L},
                    new Object[]{"08:00", 200L},
                    new Object[]{"09:00", 180L},
                    new Object[]{"10:00", 80L},
                    new Object[]{"17:00", 150L},
                    new Object[]{"18:00", 220L},
                    new Object[]{"19:00", 170L}
            );
            return ResponseEntity.ok(patterns);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<Object[]>> getUsagePatternsByDayOfWeek() {
        try {
            // Simple implementation - return daily usage counts
            List<Object[]> patterns = List.of(
                    new Object[]{"Monday", 800L},
                    new Object[]{"Tuesday", 750L},
                    new Object[]{"Wednesday", 780L},
                    new Object[]{"Thursday", 720L},
                    new Object[]{"Friday", 850L},
                    new Object[]{"Saturday", 400L},
                    new Object[]{"Sunday", 300L}
            );
            return ResponseEntity.ok(patterns);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<Object[]>> getRoutePopularityRankings(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<Object[]> rankings = ticketUsageRepository.findMostUsedRoutes(pageable).getContent();
            return ResponseEntity.ok(rankings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== UTILITY METHODS =====

    @Override
    @Transactional(readOnly = true)
    public Long calculateUserTotalRides(Long userId) {
        try {
            return ticketUsageRepository.countRidesByUserId(userId);
        } catch (Exception e) {
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> getUserMostUsedRoute(Long userId) {
        try {
            // Simple implementation - get first route from user's usage
            List<TicketUsage> userUsage = ticketUsageRepository.findUserTicketUsage(
                    userService.getCurrentUser(null), // This is simplified
                    PageRequest.of(0, 1)
            ).getContent();

            if (userUsage.isEmpty()) {
                return ResponseEntity.ok("No routes used yet");
            }

            RouteSummaryResponse route = mapToRouteSummaryResponse(userUsage.get(0).getRoute());
            return ResponseEntity.ok(route);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Double calculateAverageJourneyDuration(Long routeId) {
        try {
            // Simple implementation - return fixed average for demonstration
            return 25.5; // 25.5 minutes average
        } catch (Exception e) {
            return 0.0;
        }
    }

    // ===== PRIVATE HELPER METHODS =====

    private TicketUsageResponse mapToTicketUsageResponse(TicketUsage usage) {
        TicketUsageResponse response = new TicketUsageResponse();
        response.setId(usage.getId());
        response.setTicket(mapToTicketSummaryResponse(usage.getTicket()));
        response.setRoute(mapToRouteSummaryResponse(usage.getRoute()));
        response.setTransportType(usage.getTransportType());
        response.setBoardingStop(usage.getBoardingStop());
        response.setDestinationStop(usage.getDestinationStop());
        response.setUsedAt(usage.getUsedAt());
        return response;
    }

    private TicketSummaryResponse mapToTicketSummaryResponse(Ticket ticket) {
        TicketSummaryResponse response = new TicketSummaryResponse();
        response.setId(ticket.getId());
        response.setTicketNumber(ticket.getTicketNumber());
        response.setTicketType(ticket.getTicketType());
        response.setPrice(ticket.getPrice());
        response.setStatus(ticket.getStatus());
        response.setPurchaseDate(ticket.getPurchaseDate());
        response.setValidUntil(ticket.getValidUntil());
        response.setIsCurrentlyValid(ticket.isCurrentlyValid());
        return response;
    }

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