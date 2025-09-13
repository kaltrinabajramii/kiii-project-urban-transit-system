package io.github.kaltrinabajramii.urbantransitbackend.repository;

import io.github.kaltrinabajramii.urbantransitbackend.model.entity.TicketPricing;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketPricingRepository extends JpaRepository<TicketPricing, Long> {

    // ===== PRICING MANAGEMENT (Required for FR-8: Ticket Purchase) =====

    /**
     * Find active pricing by ticket type - used to get current prices for purchase
     */
    Optional<TicketPricing> findActiveByTicketType(TicketType ticketType);

    /**
     * Alternative implementation for findActiveByTicketType
     */
    Optional<TicketPricing> findByTicketTypeAndActiveTrue(TicketType ticketType);

    /**
     * Find all active pricing - used for public price display
     */
    List<TicketPricing> findByActiveTrue();

    /**
     * Alternative implementation for findByActiveTrue
     */
    List<TicketPricing> findByActiveTrueOrderByTicketType();

    /**
     * Find all pricing ordered by ticket type - used for admin pricing management
     */
    List<TicketPricing> findAllOrderByTicketType();

    // ===== ADMIN PRICING MANAGEMENT =====

    /**
     * Check if pricing exists for ticket type - used for validation
     */
    Boolean existsByTicketType(TicketType ticketType);

    /**
     * Find pricing by ticket type (including inactive) - used for admin editing
     */
    Optional<TicketPricing> findByTicketType(TicketType ticketType);

    /**
     * Find all pricing records including inactive - used for admin pricing management
     */
    @Query("SELECT tp FROM TicketPricing tp ORDER BY tp.ticketType")
    List<TicketPricing> findAllPricingRecords();

    // ===== ANALYTICS (Required for pricing analytics) =====

    /**
     * Get current active pricing summary - used for pricing dashboard
     */
    @Query("SELECT tp.ticketType, tp.price, tp.description FROM TicketPricing tp " +
            "WHERE tp.active = true ORDER BY tp.ticketType")
    List<Object[]> getCurrentPricingSummary();

    /**
     * Find pricing history for ticket type - used for pricing analytics
     */
    @Query("SELECT tp FROM TicketPricing tp WHERE tp.ticketType = :ticketType " +
            "ORDER BY tp.updatedAt DESC")
    List<TicketPricing> findPricingHistoryByType(@Param("ticketType") TicketType ticketType);

    /**
     * Get active pricing count - used for admin validation
     */
    @Query("SELECT COUNT(tp) FROM TicketPricing tp WHERE tp.active = true")
    Long countActivePricing();
}
