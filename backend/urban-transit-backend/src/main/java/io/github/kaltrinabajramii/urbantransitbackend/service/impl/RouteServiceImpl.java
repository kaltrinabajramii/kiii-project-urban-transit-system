package io.github.kaltrinabajramii.urbantransitbackend.service.impl;

import io.github.kaltrinabajramii.urbantransitbackend.dto.request.CreateRouteRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.request.UpdateRouteRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.request.RouteSearchRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.RouteResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.RouteSummaryResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.RoutePopularityResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.PagedResponse;
import io.github.kaltrinabajramii.urbantransitbackend.model.entity.Route;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TransportType;
import io.github.kaltrinabajramii.urbantransitbackend.repository.RouteRepository;
import io.github.kaltrinabajramii.urbantransitbackend.repository.TicketUsageRepository;
import io.github.kaltrinabajramii.urbantransitbackend.service.interfaces.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final TicketUsageRepository ticketUsageRepository;

    // ===== PUBLIC ROUTE OPERATIONS =====

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<RouteSummaryResponse>> getAllActiveRoutes() {
        try {
            List<Route> routes = routeRepository.findByActiveTrueOrderByRouteNameAsc();

            List<RouteSummaryResponse> responses = routes.stream()
                    .map(this::mapToRouteSummaryResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PagedResponse<RouteSummaryResponse>> getActiveRoutes(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Route> routePage = routeRepository.findByActiveTrueOrderByRouteNameAsc(pageable);

            List<RouteSummaryResponse> routes = routePage.getContent().stream()
                    .map(this::mapToRouteSummaryResponse)
                    .collect(Collectors.toList());

            PagedResponse<RouteSummaryResponse> response = PagedResponse.of(
                    routes, routePage.getNumber(), routePage.getSize(),
                    routePage.getTotalElements(), routePage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<RouteResponse> getRouteById(Long routeId) {
        try {
            Route route = routeRepository.findById(routeId).orElse(null);

            if (route == null || !route.getActive()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(mapToRouteResponse(route));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<RouteResponse> getRouteByName(String routeName) {
        try {
            Route route = routeRepository.findByRouteNameAndActiveTrue(routeName).orElse(null);

            if (route == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(mapToRouteResponse(route));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== ROUTE SEARCH AND FILTERING =====

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<RouteSummaryResponse>> searchRoutes(String searchTerm) {
        try {
            if (!StringUtils.hasText(searchTerm)) {
                return getAllActiveRoutes();
            }

            List<Route> routes = routeRepository.searchActiveRoutesByName(searchTerm);

            List<RouteSummaryResponse> responses = routes.stream()
                    .map(this::mapToRouteSummaryResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PagedResponse<RouteSummaryResponse>> searchRoutesAdvanced(RouteSearchRequest searchRequest) {
        try {
            Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());
            Page<Route> routePage;

            if (StringUtils.hasText(searchRequest.getSearchTerm())) {
                routePage = routeRepository.searchActiveRoutes(searchRequest.getSearchTerm(), pageable);
            } else if (searchRequest.getTransportType() != null) {
                routePage = routeRepository.findByTransportTypeAndActiveTrueOrderByRouteNameAsc(
                        searchRequest.getTransportType(), pageable);
            } else {
                routePage = routeRepository.findByActiveTrueOrderByRouteNameAsc(pageable);
            }

            List<RouteSummaryResponse> routes = routePage.getContent().stream()
                    .map(this::mapToRouteSummaryResponse)
                    .collect(Collectors.toList());

            PagedResponse<RouteSummaryResponse> response = PagedResponse.of(
                    routes, routePage.getNumber(), routePage.getSize(),
                    routePage.getTotalElements(), routePage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<RouteSummaryResponse>> getRoutesByTransportType(TransportType transportType) {
        try {
            List<Route> routes = routeRepository.findByTransportTypeAndActiveTrueOrderByRouteNameAsc(transportType);

            List<RouteSummaryResponse> responses = routes.stream()
                    .map(this::mapToRouteSummaryResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<RouteSummaryResponse>> getRoutesByStop(String stopName) {
        try {
            if (!StringUtils.hasText(stopName)) {
                return ResponseEntity.badRequest().build();
            }

            List<Route> routes = routeRepository.findActiveRoutesByStopContaining(stopName);

            List<RouteSummaryResponse> responses = routes.stream()
                    .map(this::mapToRouteSummaryResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<RouteSummaryResponse>> getRoutesOperatingAtTime(LocalTime currentTime) {
        try {
            List<Route> routes = routeRepository.findRoutesOperatingAtTime(currentTime);

            List<RouteSummaryResponse> responses = routes.stream()
                    .map(this::mapToRouteSummaryResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== ADMIN ROUTE MANAGEMENT =====

    @Override
    public ResponseEntity<RouteResponse> createRoute(CreateRouteRequest createRequest) {
        try {
            // Check if route name exists
            if (routeRepository.findByRouteNameAndActiveTrue(createRequest.getRouteName()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            // Simple validation
            if (!validateRouteStops(createRequest.getStops())) {
                return ResponseEntity.badRequest().build();
            }

            Route newRoute = new Route();
            newRoute.setRouteName(createRequest.getRouteName());
            newRoute.setDescription(createRequest.getDescription());
            newRoute.setTransportType(createRequest.getTransportType());
            newRoute.setStops(createRequest.getStops());
            newRoute.setOperatingStartTime(createRequest.getOperatingStartTime());
            newRoute.setOperatingEndTime(createRequest.getOperatingEndTime());
            newRoute.setActive(true);

            Route savedRoute = routeRepository.save(newRoute);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToRouteResponse(savedRoute));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<RouteResponse> updateRoute(Long routeId, UpdateRouteRequest updateRequest) {
        try {
            Route route = routeRepository.findById(routeId).orElse(null);

            if (route == null) {
                return ResponseEntity.notFound().build();
            }

            // Check name availability (excluding current route)
            if (!isRouteNameAvailable(updateRequest.getRouteName(), routeId)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            if (!validateRouteStops(updateRequest.getStops())) {
                return ResponseEntity.badRequest().build();
            }

            route.setRouteName(updateRequest.getRouteName());
            route.setDescription(updateRequest.getDescription());
            route.setTransportType(updateRequest.getTransportType());
            route.setStops(updateRequest.getStops());
            route.setOperatingStartTime(updateRequest.getOperatingStartTime());
            route.setOperatingEndTime(updateRequest.getOperatingEndTime());
            if (updateRequest.getActive() != null) {
                route.setActive(updateRequest.getActive());
            }

            Route updatedRoute = routeRepository.save(route);
            return ResponseEntity.ok(mapToRouteResponse(updatedRoute));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<String> deleteRoute(Long routeId) {
        try {
            Route route = routeRepository.findById(routeId).orElse(null);

            if (route == null) {
                return ResponseEntity.notFound().build();
            }

            route.setActive(false);
            routeRepository.save(route);

            return ResponseEntity.ok("Route deleted");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Delete failed");
        }
    }

    @Override
    public ResponseEntity<String> updateRouteStatus(Long routeId, boolean active) {
        try {
            Route route = routeRepository.findById(routeId).orElse(null);

            if (route == null) {
                return ResponseEntity.notFound().build();
            }

            route.setActive(active);
            routeRepository.save(route);

            return ResponseEntity.ok(active ? "Route activated" : "Route deactivated");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Update failed");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<PagedResponse<RouteResponse>> getAllRoutesForAdmin(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Route> routePage = routeRepository.findAll(pageable);

            List<RouteResponse> routes = routePage.getContent().stream()
                    .map(this::mapToRouteResponse)
                    .collect(Collectors.toList());

            PagedResponse<RouteResponse> response = PagedResponse.of(
                    routes, routePage.getNumber(), routePage.getSize(),
                    routePage.getTotalElements(), routePage.getTotalPages());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== ROUTE ANALYTICS =====

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<RoutePopularityResponse>> getMostPopularRoutes(int limit) {
        try {
            Pageable pageable = PageRequest.of(0, limit);
            Page<Object[]> results = ticketUsageRepository.findMostPopularRoutes(pageable);

            List<RoutePopularityResponse> responses = results.getContent().stream()
                    .map(result -> {
                        Route route = (Route) result[0];
                        Long usageCount = (Long) result[1];

                        RoutePopularityResponse response = new RoutePopularityResponse();
                        response.setRoute(mapToRouteSummaryResponse(route));
                        response.setUsageCount(usageCount);
                        return response;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Map<TransportType, Long>> getRouteCountByTransportType() {
        try {
            List<Object[]> results = routeRepository.countRoutesByTransportType();

            Map<TransportType, Long> counts = results.stream()
                    .collect(Collectors.toMap(
                            result -> (TransportType) result[0],
                            result -> (Long) result[1]
                    ));

            return ResponseEntity.ok(counts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<RouteSummaryResponse>> getUnderutilizedRoutes(int daysSinceLastUsage) {
        try {
            // Simple implementation - return empty list for now
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== UTILITY METHODS =====

    @Override
    @Transactional(readOnly = true)
    public boolean existsActiveRoute(Long routeId) {
        return routeRepository.findById(routeId)
                .map(Route::getActive)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRouteNameAvailable(String routeName, Long excludeRouteId) {
        return routeRepository.findByRouteNameAndActiveTrue(routeName)
                .map(route -> route.getId().equals(excludeRouteId))
                .orElse(true);
    }

    @Override
    public boolean validateRouteStops(List<String> stops) {
        if (stops == null || stops.size() < 2) {
            return false;
        }

        // Check for duplicates
        Set<String> uniqueStops = new HashSet<>();
        for (String stop : stops) {
            if (!StringUtils.hasText(stop) || !uniqueStops.add(stop.trim().toLowerCase())) {
                return false;
            }
        }

        return true;
    }

    // ===== PRIVATE HELPER METHODS =====

    private RouteResponse mapToRouteResponse(Route route) {
        RouteResponse response = new RouteResponse();
        response.setId(route.getId());
        response.setRouteName(route.getRouteName());
        response.setDescription(route.getDescription());
        response.setTransportType(route.getTransportType());
        response.setStops(route.getStops());
        response.setOperatingStartTime(route.getOperatingStartTime());
        response.setOperatingEndTime(route.getOperatingEndTime());
        response.setActive(route.getActive());
        response.setCreatedAt(route.getCreatedAt());
        response.setUpdatedAt(route.getUpdatedAt());
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