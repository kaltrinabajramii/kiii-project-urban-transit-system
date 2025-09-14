package io.github.kaltrinabajramii.urbantransitbackend.controller.rest;

import io.github.kaltrinabajramii.urbantransitbackend.dto.request.CreateRouteRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.request.UpdateRouteRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.request.RouteSearchRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.RouteResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.RouteSummaryResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.RoutePopularityResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.PagedResponse;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TransportType;
import io.github.kaltrinabajramii.urbantransitbackend.service.interfaces.RouteService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public ResponseEntity<List<RouteSummaryResponse>> getAllActiveRoutes() {
        return routeService.getAllActiveRoutes();
    }

    @GetMapping("/paged")
    public ResponseEntity<PagedResponse<RouteSummaryResponse>> getActiveRoutes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return routeService.getActiveRoutes(page, size);
    }

    @GetMapping("/{routeId}")
    public ResponseEntity<RouteResponse> getRouteById(@PathVariable Long routeId) {
        return routeService.getRouteById(routeId);
    }

    @GetMapping("/name/{routeName}")
    public ResponseEntity<RouteResponse> getRouteByName(@PathVariable String routeName) {
        return routeService.getRouteByName(routeName);
    }

    @GetMapping("/search")
    public ResponseEntity<List<RouteSummaryResponse>> searchRoutes(@RequestParam String searchTerm) {
        return routeService.searchRoutes(searchTerm);
    }

    @PostMapping("/search/advanced")
    public ResponseEntity<PagedResponse<RouteSummaryResponse>> searchRoutesAdvanced(
            @Valid @RequestBody RouteSearchRequest searchRequest) {
        return routeService.searchRoutesAdvanced(searchRequest);
    }

    @GetMapping("/transport-type/{transportType}")
    public ResponseEntity<List<RouteSummaryResponse>> getRoutesByTransportType(
            @PathVariable TransportType transportType) {
        return routeService.getRoutesByTransportType(transportType);
    }

    @GetMapping("/stop/{stopName}")
    public ResponseEntity<List<RouteSummaryResponse>> getRoutesByStop(@PathVariable String stopName) {
        return routeService.getRoutesByStop(stopName);
    }

    @GetMapping("/operating-now")
    public ResponseEntity<List<RouteSummaryResponse>> getRoutesOperatingNow(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime currentTime) {
        return routeService.getRoutesOperatingAtTime(currentTime);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteResponse> createRoute(@Valid @RequestBody CreateRouteRequest createRequest) {
        return routeService.createRoute(createRequest);
    }

    @PutMapping("/{routeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RouteResponse> updateRoute(@PathVariable Long routeId,
                                                     @Valid @RequestBody UpdateRouteRequest updateRequest) {
        return routeService.updateRoute(routeId, updateRequest);
    }

    @DeleteMapping("/{routeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteRoute(@PathVariable Long routeId) {
        return routeService.deleteRoute(routeId);
    }

    @PutMapping("/{routeId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateRouteStatus(@PathVariable Long routeId,
                                                    @RequestParam boolean active) {
        return routeService.updateRouteStatus(routeId, active);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<RouteResponse>> getAllRoutesForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return routeService.getAllRoutesForAdmin(page, size);
    }

    @GetMapping("/analytics/popular")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RoutePopularityResponse>> getMostPopularRoutes(
            @RequestParam(defaultValue = "10") int limit) {
        return routeService.getMostPopularRoutes(limit);
    }

    @GetMapping("/analytics/transport-type-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<TransportType, Long>> getRouteCountByTransportType() {
        return routeService.getRouteCountByTransportType();
    }

    @GetMapping("/analytics/underutilized")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RouteSummaryResponse>> getUnderutilizedRoutes(
            @RequestParam(defaultValue = "30") int daysSinceLastUsage) {
        return routeService.getUnderutilizedRoutes(daysSinceLastUsage);
    }
}
