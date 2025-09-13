package io.github.kaltrinabajramii.urbantransitbackend.repository;

import io.github.kaltrinabajramii.urbantransitbackend.model.entity.Route;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TransportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    // ===== BASIC ROUTE QUERIES (Required for route browsing) =====

    /**
     * Find all active routes ordered by name - used for public route listing
     */
    List<Route> findAllActiveRoutesOrderByName();

    /**
     * Alternative implementation for findAllActiveRoutesOrderByName
     */
    @Query("SELECT r FROM Route r WHERE r.active = true ORDER BY r.routeName ASC")
    List<Route> findByActiveTrueOrderByRouteNameAsc();

    /**
     * Find route by name - used for route lookup
     */
    Optional<Route> findByRouteNameAndActiveTrue(String routeName);

    /**
     * Find active routes with pagination - used for route browsing
     */
    Page<Route> findByActiveTrueOrderByRouteNameAsc(Pageable pageable);

    // ===== ROUTE SEARCH (Required for FR-6: Route Search) =====

    /**
     * Search active routes by name - used for route search functionality
     */
    @Query("SELECT r FROM Route r WHERE r.active = true AND " +
            "LOWER(r.routeName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "ORDER BY r.routeName ASC")
    List<Route> searchActiveRoutesByName(@Param("searchTerm") String searchTerm);

    /**
     * Search routes by name or description - enhanced search
     */
    @Query("SELECT r FROM Route r WHERE r.active = true AND " +
            "(LOWER(r.routeName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY r.routeName ASC")
    Page<Route> searchActiveRoutes(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find active routes by stop containing - used for "Find routes to X stop"
     */
    @Query("SELECT r FROM Route r JOIN r.stops s WHERE r.active = true AND " +
            "LOWER(s) LIKE LOWER(CONCAT('%', :stopName, '%')) " +
            "ORDER BY r.routeName ASC")
    List<Route> findActiveRoutesByStopContaining(@Param("stopName") String stopName);

    // ===== TRANSPORT TYPE FILTERING (Required for route filtering) =====

    /**
     * Find active routes by transport type - used for filter by bus/metro/tram
     */
    List<Route> findActiveRoutesByTransportType(TransportType transportType);

    /**
     * Alternative implementation for findActiveRoutesByTransportType
     */
    List<Route> findByTransportTypeAndActiveTrueOrderByRouteNameAsc(TransportType transportType);

    /**
     * Find routes by transport type with pagination
     */
    Page<Route> findByTransportTypeAndActiveTrueOrderByRouteNameAsc(TransportType transportType, Pageable pageable);

    // ===== OPERATING TIME QUERIES (Required for schedule functionality) =====

    /**
     * Find routes operating at specific time - used for real-time availability
     */
    @Query("SELECT r FROM Route r WHERE r.active = true AND " +
            "r.operatingStartTime <= :currentTime AND r.operatingEndTime >= :currentTime " +
            "ORDER BY r.routeName ASC")
    List<Route> findRoutesOperatingAtTime(@Param("currentTime") LocalTime currentTime);

    // ===== ANALYTICS (Required for admin dashboard) =====

    /**
     * Count routes by transport type - used for analytics
     */
    @Query("SELECT r.transportType, COUNT(r) FROM Route r WHERE r.active = true GROUP BY r.transportType")
    List<Object[]> countRoutesByTransportType();
}
