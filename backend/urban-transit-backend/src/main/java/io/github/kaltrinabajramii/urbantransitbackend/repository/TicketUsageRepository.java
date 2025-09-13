package io.github.kaltrinabajramii.urbantransitbackend.repository;

import io.github.kaltrinabajramii.urbantransitbackend.model.entity.Route;
import io.github.kaltrinabajramii.urbantransitbackend.model.entity.Ticket;
import io.github.kaltrinabajramii.urbantransitbackend.model.entity.TicketUsage;
import io.github.kaltrinabajramii.urbantransitbackend.model.entity.User;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TransportType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketUsageRepository extends JpaRepository<TicketUsage, Long> {

    // ===== TICKET USAGE TRACKING (Required for usage analytics) =====

    /**
     * Find usage by ticket ordered by used date - used for user's travel history
     */
    List<TicketUsage> findByTicketOrderByUsedAtDesc(Ticket ticket);

    /**
     * Find usage by route - used for route usage analytics
     */
    List<TicketUsage> findByRouteOrderByUsedAtDesc(Route route);

    /**
     * Find user's ticket usage history - used for user travel history
     */
    @Query("SELECT tu FROM TicketUsage tu WHERE tu.ticket.user = :user ORDER BY tu.usedAt DESC")
    Page<TicketUsage> findUserTicketUsage(@Param("user") User user, Pageable pageable);

    // ===== USER STATISTICS (Required for user analytics) =====

    /**
     * Count rides by user ID - used for user statistics
     */
    @Query("SELECT COUNT(tu) FROM TicketUsage tu WHERE tu.ticket.user.id = :userId")
    Long countRidesByUserId(@Param("userId") Long userId);

    /**
     * Count user rides in date range - used for user activity analytics
     */
    @Query("SELECT COUNT(tu) FROM TicketUsage tu WHERE tu.ticket.user.id = :userId " +
            "AND tu.usedAt BETWEEN :startDate AND :endDate")
    Long countUserRidesByDateRange(@Param("userId") Long userId,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate);

    // ===== BUSINESS ANALYTICS (Required for admin dashboard) =====

    /**
     * Find most popular routes - used for business analytics
     */
    @Query("SELECT r, COUNT(tu.id) as usageCount FROM Route r " +
            "LEFT JOIN TicketUsage tu ON tu.route = r " +
            "WHERE r.active = true " +
            "GROUP BY r.id " +
            "ORDER BY usageCount DESC")
    Page<Object[]> findMostPopularRoutes(Pageable pageable);

    /**
     * Alternative implementation for most popular routes
     */
    @Query("SELECT tu.route, COUNT(tu) as usageCount FROM TicketUsage tu " +
            "GROUP BY tu.route " +
            "ORDER BY usageCount DESC")
    Page<Object[]> findMostUsedRoutes(Pageable pageable);

    // ===== TRANSPORT TYPE ANALYTICS =====

    /**
     * Find usage by transport type - used for transport analytics
     */
    List<TicketUsage> findByTransportTypeOrderByUsedAtDesc(TransportType transportType);

    /**
     * Count usage by transport type - used for dashboard analytics
     */
    @Query("SELECT tu.transportType, COUNT(tu) FROM TicketUsage tu GROUP BY tu.transportType")
    List<Object[]> countUsageByTransportType();

    // ===== TIME-BASED ANALYTICS (Required for usage reporting) =====

    /**
     * Find usage within date range - used for usage reporting
     */
    @Query("SELECT tu FROM TicketUsage tu WHERE tu.usedAt BETWEEN :startDate AND :endDate " +
            "ORDER BY tu.usedAt DESC")
    Page<TicketUsage> findUsageByDateRange(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           Pageable pageable);

    /**
     * Count daily usage - used for daily analytics
     */
    @Query("SELECT DATE(tu.usedAt), COUNT(tu) FROM TicketUsage tu " +
            "WHERE tu.usedAt BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(tu.usedAt) " +
            "ORDER BY DATE(tu.usedAt)")
    List<Object[]> getDailyUsageStats(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    // ===== STOP ANALYTICS (Required for stop usage insights) =====

    /**
     * Find most popular boarding stops - used for stop analytics
     */
    @Query("SELECT tu.boardingStop, COUNT(tu) as usageCount FROM TicketUsage tu " +
            "WHERE tu.boardingStop IS NOT NULL " +
            "GROUP BY tu.boardingStop " +
            "ORDER BY usageCount DESC")
    Page<Object[]> findMostPopularBoardingStops(Pageable pageable);

    /**
     * Find most popular destinations - used for destination analytics
     */
    @Query("SELECT tu.destinationStop, COUNT(tu) as usageCount FROM TicketUsage tu " +
            "WHERE tu.destinationStop IS NOT NULL " +
            "GROUP BY tu.destinationStop " +
            "ORDER BY usageCount DESC")
    Page<Object[]> findMostPopularDestinations(Pageable pageable);

    /**
     * Find today's usage count - used for daily dashboard
     */
    @Query("SELECT COUNT(tu) FROM TicketUsage tu WHERE DATE(tu.usedAt) = CURRENT_DATE")
    Long countTodayUsage();
}
