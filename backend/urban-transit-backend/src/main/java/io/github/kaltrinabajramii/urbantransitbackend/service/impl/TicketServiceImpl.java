package io.github.kaltrinabajramii.urbantransitbackend.service.impl;

import io.github.kaltrinabajramii.urbantransitbackend.dto.request.PurchaseTicketRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.request.ValidateTicketRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.request.UseTicketRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.request.TicketFilterRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.TicketResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.TicketSummaryResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.TicketValidationResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.TicketUsageResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.SalesAnalyticsResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.PagedResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.RouteSummaryResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.UserSummaryResponse;
import io.github.kaltrinabajramii.urbantransitbackend.model.entity.Ticket;
import io.github.kaltrinabajramii.urbantransitbackend.model.entity.TicketUsage;
import io.github.kaltrinabajramii.urbantransitbackend.model.entity.User;
import io.github.kaltrinabajramii.urbantransitbackend.model.entity.Route;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketType;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketStatus;
import io.github.kaltrinabajramii.urbantransitbackend.repository.TicketRepository;
import io.github.kaltrinabajramii.urbantransitbackend.repository.TicketUsageRepository;
import io.github.kaltrinabajramii.urbantransitbackend.repository.RouteRepository;
import io.github.kaltrinabajramii.urbantransitbackend.service.interfaces.TicketService;
import io.github.kaltrinabajramii.urbantransitbackend.service.interfaces.TicketPricingService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketUsageRepository ticketUsageRepository;
    private final RouteRepository routeRepository;
    private final TicketPricingService ticketPricingService;
    private final UserService userService;

    // ===== TICKET PURCHASING =====

    @Override
    public ResponseEntity<TicketResponse> purchaseTicket(UserDetails userDetails, PurchaseTicketRequest purchaseRequest) {
        try {
            User user = userService.getCurrentUser(userDetails);

            // Check if route exists
            Route route = routeRepository.findById(purchaseRequest.getRouteId()).orElse(null);
            if (route == null || !route.getActive()) {
                return ResponseEntity.badRequest().build();
            }

            // Prevent buying duplicate unlimited passes
            if (purchaseRequest.getTicketType().isUnlimitedRides()) {
                boolean hasValidPass = ticketRepository.userHasValidPass(user, LocalDateTime.now());
                if (hasValidPass) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).build();
                }
            }

            // Get current price and create ticket
            BigDecimal price = ticketPricingService.getCurrentPrice(purchaseRequest.getTicketType());
            Ticket ticket = createTicket(user, purchaseRequest.getTicketType(), price);

            Ticket savedTicket = ticketRepository.save(ticket);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToTicketResponse(savedTicket));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<BigDecimal> getTicketPrice(TicketType ticketType) {
        try {
            BigDecimal price = ticketPricingService.getCurrentPrice(ticketType);
            return ResponseEntity.ok(price);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<String> checkPurchaseEligibility(UserDetails userDetails, TicketType ticketType) {
        try {
            User user = userService.getCurrentUser(userDetails);

            // Check if user already has a valid unlimited pass
            if (ticketType.isUnlimitedRides()) {
                boolean hasValidPass = ticketRepository.userHasValidPass(user, LocalDateTime.now());
                if (hasValidPass) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Already has valid pass");
                }
            }

            return ResponseEntity.ok("Eligible");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Check failed");
        }
    }

    // ===== TICKET VALIDATION AND USAGE =====

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<TicketValidationResponse> validateTicket(ValidateTicketRequest validateRequest) {
        try {
            Ticket ticket = ticketRepository.findByTicketNumber(validateRequest.getTicketNumber()).orElse(null);

            if (ticket == null) {
                return ResponseEntity.ok(TicketValidationResponse.invalid("Ticket not found"));
            }

            if (!ticket.isCurrentlyValid()) {
                return ResponseEntity.ok(TicketValidationResponse.invalid("Ticket not valid"));
            }

            return ResponseEntity.ok(TicketValidationResponse.valid(mapToTicketResponse(ticket)));
        } catch (Exception e) {
            return ResponseEntity.ok(TicketValidationResponse.invalid("Validation error"));
        }
    }

    @Override
    public ResponseEntity<TicketUsageResponse> useTicket(UseTicketRequest useRequest) {
        try {
            Ticket ticket = ticketRepository.findByTicketNumber(useRequest.getTicketNumber()).orElse(null);
            if (ticket == null) {
                return ResponseEntity.notFound().build();
            }

            if (!ticket.canBeUsedForTransit()) {
                return ResponseEntity.badRequest().build();
            }

            Route route = routeRepository.findById(useRequest.getRouteId()).orElse(null);
            if (route == null || !route.getActive()) {
                return ResponseEntity.badRequest().build();
            }

            // Use the ticket (for single rides)
            if (ticket.getTicketType() == TicketType.RIDE) {
                ticket.useTicket();
                ticketRepository.save(ticket);
            }

            // Create usage record
            TicketUsage usage = createTicketUsage(ticket, route, useRequest);
            TicketUsage savedUsage = ticketUsageRepository.save(usage);

            return ResponseEntity.ok(mapToTicketUsageResponse(savedUsage));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<TicketResponse> getTicketByNumber(String ticketNumber) {
        try {
            Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber).orElse(null);
            if (ticket == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(mapToTicketResponse(ticket));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== USER TICKET MANAGEMENT =====

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PagedResponse<TicketSummaryResponse>> getUserTickets(UserDetails userDetails, int page, int size) {
        try {
            User user = userService.getCurrentUser(userDetails);
            Pageable pageable = PageRequest.of(page, size);
            Page<Ticket> ticketPage = ticketRepository.findByUserOrderByPurchaseDateDesc(user, pageable);

            List<TicketSummaryResponse> tickets = ticketPage.getContent().stream()
                    .map(this::mapToTicketSummaryResponse)
                    .collect(Collectors.toList());

            PagedResponse<TicketSummaryResponse> response = PagedResponse.of(
                    tickets, ticketPage.getNumber(), ticketPage.getSize(),
                    ticketPage.getTotalElements(), ticketPage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<TicketSummaryResponse>> getUserValidTickets(UserDetails userDetails) {
        try {
            User user = userService.getCurrentUser(userDetails);
            List<Ticket> validTickets = ticketRepository.findUserValidActiveTickets(user, LocalDateTime.now());

            List<TicketSummaryResponse> tickets = validTickets.stream()
                    .map(this::mapToTicketSummaryResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<TicketSummaryResponse>> getUserValidRideTickets(UserDetails userDetails) {
        try {
            User user = userService.getCurrentUser(userDetails);
            List<Ticket> rideTickets = ticketRepository.findUserValidRideTickets(user, LocalDateTime.now());

            List<TicketSummaryResponse> tickets = rideTickets.stream()
                    .map(this::mapToTicketSummaryResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<TicketSummaryResponse>> getUserValidUnlimitedTickets(UserDetails userDetails) {
        try {
            User user = userService.getCurrentUser(userDetails);
            List<Ticket> unlimitedTickets = ticketRepository.findUserValidUnlimitedTickets(user, LocalDateTime.now());

            List<TicketSummaryResponse> tickets = unlimitedTickets.stream()
                    .map(this::mapToTicketSummaryResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(tickets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PagedResponse<TicketSummaryResponse>> getUserTicketsFiltered(UserDetails userDetails, TicketFilterRequest filterRequest) {
        try {
            User user = userService.getCurrentUser(userDetails);
            Pageable pageable = PageRequest.of(filterRequest.getPage(), filterRequest.getSize());

            Page<Ticket> ticketPage;
            if (filterRequest.getTicketType() != null) {
                List<Ticket> tickets = ticketRepository.findByUserAndTicketTypeOrderByPurchaseDateDesc(user, filterRequest.getTicketType());
                ticketPage = createPageFromList(tickets, pageable);
            } else if (filterRequest.getStatus() != null) {
                List<Ticket> tickets = ticketRepository.findByUserAndStatusOrderByPurchaseDateDesc(user, filterRequest.getStatus());
                ticketPage = createPageFromList(tickets, pageable);
            } else {
                ticketPage = ticketRepository.findByUserOrderByPurchaseDateDesc(user, pageable);
            }

            List<TicketSummaryResponse> tickets = ticketPage.getContent().stream()
                    .map(this::mapToTicketSummaryResponse)
                    .collect(Collectors.toList());

            PagedResponse<TicketSummaryResponse> response = PagedResponse.of(
                    tickets, ticketPage.getNumber(), ticketPage.getSize(),
                    ticketPage.getTotalElements(), ticketPage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

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

    // ===== ADMIN OPERATIONS =====

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PagedResponse<TicketResponse>> getAllTickets(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Ticket> ticketPage = ticketRepository.findAll(pageable);

            List<TicketResponse> tickets = ticketPage.getContent().stream()
                    .map(this::mapToTicketResponse)
                    .collect(Collectors.toList());

            PagedResponse<TicketResponse> response = PagedResponse.of(
                    tickets, ticketPage.getNumber(), ticketPage.getSize(),
                    ticketPage.getTotalElements(), ticketPage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PagedResponse<TicketResponse>> getTicketsByStatus(TicketStatus status, int page, int size) {
        try {
            List<Ticket> tickets = ticketRepository.findByStatusOrderByPurchaseDateDesc(status);
            Pageable pageable = PageRequest.of(page, size);
            Page<Ticket> ticketPage = createPageFromList(tickets, pageable);

            List<TicketResponse> ticketResponses = ticketPage.getContent().stream()
                    .map(this::mapToTicketResponse)
                    .collect(Collectors.toList());

            PagedResponse<TicketResponse> response = PagedResponse.of(
                    ticketResponses, ticketPage.getNumber(), ticketPage.getSize(),
                    ticketPage.getTotalElements(), ticketPage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PagedResponse<TicketResponse>> getTicketsByType(TicketType ticketType, int page, int size) {
        try {
            List<Ticket> tickets = ticketRepository.findByTicketTypeOrderByPurchaseDateDesc(ticketType);
            Pageable pageable = PageRequest.of(page, size);
            Page<Ticket> ticketPage = createPageFromList(tickets, pageable);

            List<TicketResponse> ticketResponses = ticketPage.getContent().stream()
                    .map(this::mapToTicketResponse)
                    .collect(Collectors.toList());

            PagedResponse<TicketResponse> response = PagedResponse.of(
                    ticketResponses, ticketPage.getNumber(), ticketPage.getSize(),
                    ticketPage.getTotalElements(), ticketPage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<String> cancelTicket(Long ticketId, String reason) {
        try {
            Ticket ticket = ticketRepository.findById(ticketId).orElse(null);
            if (ticket == null) {
                return ResponseEntity.notFound().build();
            }

            if (ticket.getStatus() == TicketStatus.USED || ticket.getStatus() == TicketStatus.EXPIRED) {
                return ResponseEntity.badRequest().body("Cannot cancel used or expired ticket");
            }

            ticket.setStatus(TicketStatus.EXPIRED);
            ticketRepository.save(ticket);

            return ResponseEntity.ok("Ticket cancelled");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Cancel failed");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PagedResponse<TicketResponse>> getTicketsByDateRange(LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Ticket> ticketPage = ticketRepository.findTicketsPurchasedBetween(startDate, endDate, pageable);

            List<TicketResponse> tickets = ticketPage.getContent().stream()
                    .map(this::mapToTicketResponse)
                    .collect(Collectors.toList());

            PagedResponse<TicketResponse> response = PagedResponse.of(
                    tickets, ticketPage.getNumber(), ticketPage.getSize(),
                    ticketPage.getTotalElements(), ticketPage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== ANALYTICS =====

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<SalesAnalyticsResponse>> getSalesAnalyticsByType() {
        try {
            List<Object[]> results = ticketRepository.countTicketsByType();

            List<SalesAnalyticsResponse> analytics = results.stream()
                    .map(result -> {
                        TicketType type = (TicketType) result[0];
                        Long count = (Long) result[1];

                        // Calculate revenue for this type
                        List<Ticket> typeTickets = ticketRepository.findByTicketTypeOrderByPurchaseDateDesc(type);
                        BigDecimal revenue = typeTickets.stream()
                                .map(Ticket::getPrice)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

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
    @Transactional(readOnly = true)
    public ResponseEntity<BigDecimal> getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            BigDecimal revenue = ticketRepository.calculateRevenueByDateRange(startDate, endDate)
                    .orElse(BigDecimal.ZERO);
            return ResponseEntity.ok(revenue);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<Object[]>> getTopPurchasingUsers(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            Page<Object[]> results = ticketRepository.findTopPurchasingUsers(pageable);
            return ResponseEntity.ok(results.getContent());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<Object[]>> getDailySalesStats(int days) {
        try {
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(days);
            List<Object[]> stats = ticketUsageRepository.getDailyUsageStats(startDate, endDate);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== SYSTEM MAINTENANCE =====

    @Override
    public int processExpiredTickets() {
        try {
            List<Ticket> expiredTickets = ticketRepository.findExpiredActiveTickets(LocalDateTime.now());

            for (Ticket ticket : expiredTickets) {
                ticket.setStatus(TicketStatus.EXPIRED);
                ticketRepository.save(ticket);
            }

            return expiredTickets.size();
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public ResponseEntity<String> generateDailyReport(LocalDateTime date) {
        try {
            LocalDateTime startOfDay = date.withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfDay = date.withHour(23).withMinute(59).withSecond(59);

            long ticketsSold = ticketRepository.findTicketsPurchasedBetween(startOfDay, endOfDay,
                    PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();

            BigDecimal revenue = ticketRepository.calculateRevenueByDateRange(startOfDay, endOfDay)
                    .orElse(BigDecimal.ZERO);

            String report = String.format("Date: %s, Tickets Sold: %d, Revenue: $%.2f",
                    date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), ticketsSold, revenue);

            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Report failed");
        }
    }

    // ===== UTILITY METHODS =====

    @Override
    @Transactional(readOnly = true)
    public boolean userHasValidPass(UserDetails userDetails) {
        try {
            User user = userService.getCurrentUser(userDetails);
            return ticketRepository.userHasValidPass(user, LocalDateTime.now());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String generateTicketNumber(TicketType ticketType) {
        String prefix = getTicketTypePrefix(ticketType);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return prefix + "-" + timestamp + "-" + random;
    }

    // ===== PRIVATE HELPER METHODS =====

    private Ticket createTicket(User user, TicketType ticketType, BigDecimal price) {
        Ticket ticket = new Ticket();
        ticket.setUser(user);
        ticket.setTicketNumber(generateTicketNumber(ticketType));
        ticket.setTicketType(ticketType);
        ticket.setPrice(price);
        ticket.setStatus(TicketStatus.ACTIVE);
        ticket.setPurchaseDate(LocalDateTime.now());

        LocalDateTime now = LocalDateTime.now();
        ticket.setValidFrom(now);
        ticket.setValidUntil(now.plusDays(ticketType.getValidityDays()));

        return ticket;
    }

    private TicketUsage createTicketUsage(Ticket ticket, Route route, UseTicketRequest useRequest) {
        TicketUsage usage = new TicketUsage();
        usage.setTicket(ticket);
        usage.setRoute(route);
        usage.setTransportType(useRequest.getTransportType());
        usage.setBoardingStop(useRequest.getBoardingStop());
        usage.setDestinationStop(useRequest.getDestinationStop());
        return usage;
    }

    private String getTicketTypePrefix(TicketType ticketType) {
        switch (ticketType) {
            case RIDE: return "RD";
            case MONTHLY: return "MO";
            case YEARLY: return "YR";
            default: return "TK";
        }
    }

    private TicketResponse mapToTicketResponse(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setTicketNumber(ticket.getTicketNumber());
        response.setTicketType(ticket.getTicketType());
        response.setPrice(ticket.getPrice());
        response.setStatus(ticket.getStatus());
        response.setPurchaseDate(ticket.getPurchaseDate());
        response.setValidFrom(ticket.getValidFrom());
        response.setValidUntil(ticket.getValidUntil());
        response.setUsedDate(ticket.getUsedDate());
        response.setIsCurrentlyValid(ticket.isCurrentlyValid());
        response.setCanBeUsedForTransit(ticket.canBeUsedForTransit());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());

        if (ticket.getUser() != null) {
            UserSummaryResponse userSummary = new UserSummaryResponse();
            userSummary.setId(ticket.getUser().getId());
            userSummary.setFullName(ticket.getUser().getFullName());
            userSummary.setEmail(ticket.getUser().getEmail());
            userSummary.setRole(ticket.getUser().getRole());
            userSummary.setActive(ticket.getUser().getActive());
            response.setUser(userSummary);
        }

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

    private Page<Ticket> createPageFromList(List<Ticket> tickets, Pageable pageable) {
        int start = Math.min((int) pageable.getOffset(), tickets.size());
        int end = Math.min(start + pageable.getPageSize(), tickets.size());
        List<Ticket> pageContent = tickets.subList(start, end);

        return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, tickets.size());
    }
}