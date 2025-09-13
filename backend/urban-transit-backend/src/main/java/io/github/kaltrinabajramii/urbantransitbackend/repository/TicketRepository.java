package io.github.kaltrinabajramii.urbantransitbackend.repository;

import io.github.kaltrinabajramii.urbantransitbackend.model.entity.Ticket;
import io.github.kaltrinabajramii.urbantransitbackend.model.entity.User;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketStatus;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // ===== TICKET VALIDATION (Required for FR-11, FR-12) =====

    /**
     * Find ticket by ticket number - used for ticket validation/usage
     */
    Optional<Ticket> findByTicketNumber(String ticketNumber);

    /**
     * Find user's currently valid active tickets - used for user's current valid tickets
     */
    @Query("SELECT t FROM Ticket t WHERE t.user = :user AND t.status = 'ACTIVE' AND " +
            "t.validFrom <= :now AND t.validUntil > :now " +
            "ORDER BY t.purchaseDate DESC")
    List<Ticket> findUserValidActiveTickets(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Check if user has valid pass - used to prevent buying duplicate monthly/yearly passes
     */
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Ticket t " +
            "WHERE t.user = :user AND t.ticketType IN ('MONTHLY', 'YEARLY') AND " +
            "t.status = 'ACTIVE' AND t.validFrom <= :now AND t.validUntil > :now")
    Boolean userHasValidPass(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Find expired active tickets - used for cleanup job for expired tickets
     */
    @Query("SELECT t FROM Ticket t WHERE t.status = 'ACTIVE' AND t.validUntil < :now")
    List<Ticket> findExpiredActiveTickets(@Param("now") LocalDateTime now);

    // ===== USER TICKET MANAGEMENT (Required for FR-10) =====

    /**
     * Find all tickets by user - used for ticket history
     */
    List<Ticket> findByUserOrderByPurchaseDateDesc(User user);

    /**
     * Find tickets by user with pagination - used for ticket history with pagination
     */
    Page<Ticket> findByUserOrderByPurchaseDateDesc(User user, Pageable pageable);

    /**
     * Find user's valid RIDE tickets (unused) - used for ride ticket validation
     */
    @Query("SELECT t FROM Ticket t WHERE t.user = :user AND t.ticketType = 'RIDE' AND " +
            "t.status = 'ACTIVE' AND t.usedDate IS NULL AND " +
            "t.validFrom <= :now AND t.validUntil > :now " +
            "ORDER BY t.purchaseDate ASC")
    List<Ticket> findUserValidRideTickets(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Find user's valid unlimited tickets (monthly/yearly)
     */
    @Query("SELECT t FROM Ticket t WHERE t.user = :user AND t.ticketType IN ('MONTHLY', 'YEARLY') AND " +
            "t.status = 'ACTIVE' AND " +
            "t.validFrom <= :now AND t.validUntil > :now " +
            "ORDER BY t.validUntil DESC")
    List<Ticket> findUserValidUnlimitedTickets(@Param("user") User user, @Param("now") LocalDateTime now);

    // ===== TICKET STATUS AND TYPE QUERIES =====

    /**
     * Find tickets by status - used for admin management
     */
    List<Ticket> findByStatusOrderByPurchaseDateDesc(TicketStatus status);

    /**
     * Find tickets by type - used for analytics
     */
    List<Ticket> findByTicketTypeOrderByPurchaseDateDesc(TicketType ticketType);

    /**
     * Find user tickets by type - used for user's ticket filtering
     */
    List<Ticket> findByUserAndTicketTypeOrderByPurchaseDateDesc(User user, TicketType ticketType);

    /**
     * Find user tickets by status - used for user's ticket filtering
     */
    List<Ticket> findByUserAndStatusOrderByPurchaseDateDesc(User user, TicketStatus status);

    // ===== ANALYTICS (Required for dashboard and reporting) =====

    /**
     * Count tickets by type - used for sales analytics
     */
    @Query("SELECT t.ticketType, COUNT(t) FROM Ticket t GROUP BY t.ticketType")
    List<Object[]> countTicketsByType();

    /**
     * Find tickets purchased in date range - used for sales reporting
     */
    @Query("SELECT t FROM Ticket t WHERE t.purchaseDate BETWEEN :startDate AND :endDate " +
            "ORDER BY t.purchaseDate DESC")
    Page<Ticket> findTicketsPurchasedBetween(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate,
                                             Pageable pageable);

    /**
     * Calculate total revenue by date range - used for revenue analytics
     */
    @Query("SELECT SUM(t.price) FROM Ticket t WHERE t.purchaseDate BETWEEN :startDate AND :endDate")
    Optional< BigDecimal > calculateRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Find top purchasing users - used for customer analytics
     */
    @Query("SELECT t.user, COUNT(t), SUM(t.price) FROM Ticket t " +
            "GROUP BY t.user " +
            "ORDER BY COUNT(t) DESC")
    Page<Object[]> findTopPurchasingUsers(Pageable pageable);

    Page< Ticket> findByUserAndTicketTypeAndStatusOrderByPurchaseDateDesc(User user, TicketType ticketType, TicketStatus status, Pageable pageable);

    Long countByStatus(TicketStatus ticketStatus);
}
