package io.github.kaltrinabajramii.urbantransitbackend.service.interfaces;

import io.github.kaltrinabajramii.urbantransitbackend.dto.request.UpdatePricingRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.TicketPricingResponse;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketType;
import org.springframework.http.ResponseEntity;
import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for ticket pricing management
 * Handles ticket price CRUD operations and pricing analytics
 */
public interface TicketPricingService {

    // ===== PUBLIC PRICING OPERATIONS =====

    /**
     * Get all active ticket prices (public access)
     *
     * @return ResponseEntity containing list of current active pricing
     */
    ResponseEntity<List<TicketPricingResponse>> getAllActivePricing();

    /**
     * Get price for specific ticket type (public access)
     *
     * @param ticketType Ticket type to get price for
     * @return ResponseEntity containing pricing information
     * @throws RuntimeException if pricing not found or inactive
     */
    ResponseEntity<TicketPricingResponse> getPricingByType(TicketType ticketType);

    /**
     * Get current price value for ticket type (for calculations)
     *
     * @param ticketType Ticket type to get price for
     * @return Price as BigDecimal
     * @throws RuntimeException if pricing not found
     */
    BigDecimal getCurrentPrice(TicketType ticketType);

    // ===== ADMIN PRICING MANAGEMENT =====

    /**
     * Get all pricing records including inactive ones (admin only)
     *
     * @return ResponseEntity containing all pricing records
     */
    ResponseEntity<List<TicketPricingResponse>> getAllPricingRecords();

    /**
     * Update ticket pricing (admin only)
     *
     * @param updateRequest Pricing update details
     * @return ResponseEntity containing updated pricing information
     */
    ResponseEntity<TicketPricingResponse> updatePricing(UpdatePricingRequest updateRequest);

    /**
     * Activate/Deactivate pricing (admin only)
     *
     * @param ticketType Ticket type to update
     * @param active New active status
     * @return ResponseEntity with confirmation message
     */
    ResponseEntity<String> updatePricingStatus(TicketType ticketType, boolean active);

    /**
     * Create new pricing record (admin only)
     *
     * @param updateRequest New pricing details
     * @return ResponseEntity containing created pricing information
     */
    ResponseEntity<TicketPricingResponse> createPricing(UpdatePricingRequest updateRequest);

    // ===== PRICING ANALYTICS =====

    /**
     * Get pricing history for ticket type
     *
     * @param ticketType Ticket type to get history for
     * @return ResponseEntity containing pricing change history
     */
    ResponseEntity<List<TicketPricingResponse>> getPricingHistory(TicketType ticketType);

    /**
     * Get pricing summary for dashboard
     *
     * @return ResponseEntity containing current pricing overview
     */
    ResponseEntity<List<Object[]>> getCurrentPricingSummary();

    // ===== UTILITY METHODS =====

    /**
     * Validate pricing data
     *
     * @param price Price to validate
     * @param ticketType Ticket type for context
     * @return true if pricing is valid
     */
    boolean validatePricing(BigDecimal price, TicketType ticketType);

    /**
     * Check if pricing exists for ticket type
     *
     * @param ticketType Ticket type to check
     * @return true if pricing exists
     */
    boolean pricingExists(TicketType ticketType);
}
