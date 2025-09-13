package io.github.kaltrinabajramii.urbantransitbackend.service.interfaces;

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
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketType;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for ticket management operations
 * Handles ticket purchasing, validation, usage tracking, and ticket analytics
 */
public interface TicketService {

    // ===== TICKET PURCHASING =====

    /**
     * Purchase a new ticket for the authenticated user
     *
     * @param userDetails Current authenticated user
     * @param purchaseRequest Ticket purchase details
     * @return ResponseEntity containing purchased ticket details
     * @throws RuntimeException if user has valid pass and trying to buy duplicate
     */
    ResponseEntity<TicketResponse> purchaseTicket(UserDetails userDetails, PurchaseTicketRequest purchaseRequest);

    /**
     * Calculate ticket price before purchase
     *
     * @param ticketType Type of ticket to price
     * @return ResponseEntity containing ticket price information
     */
    ResponseEntity< BigDecimal > getTicketPrice(TicketType ticketType);

    /**
     * Check if user can purchase a specific ticket type
     *
     * @param userDetails Current authenticated user
     * @param ticketType Ticket type to check
     * @return ResponseEntity containing eligibility information and reason
     */
    ResponseEntity<String> checkPurchaseEligibility(UserDetails userDetails, TicketType ticketType);

    // ===== TICKET VALIDATION AND USAGE =====

    /**
     * Validate ticket by ticket number
     *
     * @param validateRequest Ticket validation details
     * @return ResponseEntity containing TicketValidationResponse with validation status and details
     */
    ResponseEntity<TicketValidationResponse> validateTicket(ValidateTicketRequest validateRequest);

    /**
     * Use a ticket for transit (creates usage record)
     *
     * @param useRequest Ticket usage details including route and stops
     * @return ResponseEntity containing ticket usage confirmation
     * @throws RuntimeException if ticket is invalid or already used
     */
    ResponseEntity<TicketUsageResponse> useTicket(UseTicketRequest useRequest);

    /**
     * Get ticket details by ticket number
     *
     * @param ticketNumber Unique ticket number
     * @return ResponseEntity containing full ticket details
     * @throws RuntimeException if ticket not found
     */
    ResponseEntity<TicketResponse> getTicketByNumber(String ticketNumber);

    // ===== USER TICKET MANAGEMENT =====

    /**
     * Get all tickets for current user
     *
     * @param userDetails Current authenticated user
     * @param page Page number
     * @param size Page size
     * @return ResponseEntity containing PagedResponse with user's ticket history
     */
    ResponseEntity<PagedResponse<TicketSummaryResponse>> getUserTickets(UserDetails userDetails, int page, int size);

    /**
     * Get user's currently valid tickets
     *
     * @param userDetails Current authenticated user
     * @return ResponseEntity containing list of valid active tickets
     */
    ResponseEntity<List<TicketSummaryResponse>> getUserValidTickets(UserDetails userDetails);

    /**
     * Get user's valid ride tickets (unused single-ride tickets)
     *
     * @param userDetails Current authenticated user
     * @return ResponseEntity containing available ride tickets
     */
    ResponseEntity<List<TicketSummaryResponse>> getUserValidRideTickets(UserDetails userDetails);

    /**
     * Get user's valid unlimited tickets (monthly/yearly passes)
     *
     * @param userDetails Current authenticated user
     * @return ResponseEntity containing active unlimited passes
     */
    ResponseEntity<List<TicketSummaryResponse>> getUserValidUnlimitedTickets(UserDetails userDetails);

    /**
     * Get user's ticket history with filters
     *
     * @param userDetails Current authenticated user
     * @param filterRequest Filter criteria for tickets
     * @return ResponseEntity containing PagedResponse with filtered ticket history
     */
    ResponseEntity<PagedResponse<TicketSummaryResponse>> getUserTicketsFiltered(UserDetails userDetails, TicketFilterRequest filterRequest);

    /**
     * Get user's travel history (ticket usage records)
     *
     * @param userDetails Current authenticated user
     * @param page Page number
     * @param size Page size
     * @return ResponseEntity containing PagedResponse with user's travel history
     */
    ResponseEntity<PagedResponse<TicketUsageResponse>> getUserTravelHistory(UserDetails userDetails, int page, int size);

    // ===== ADMIN TICKET MANAGEMENT =====

    /**
     * Get all tickets with pagination (admin only)
     *
     * @param page Page number
     * @param size Page size
     * @return ResponseEntity containing PagedResponse with all tickets
     */
    ResponseEntity<PagedResponse<TicketResponse>> getAllTickets(int page, int size);

    /**
     * Get tickets by status (admin only)
     *
     * @param status Ticket status to filter by
     * @param page Page number
     * @param size Page size
     * @return ResponseEntity containing PagedResponse with tickets of specified status
     */
    ResponseEntity<PagedResponse<TicketResponse>> getTicketsByStatus(TicketStatus status, int page, int size);

    /**
     * Get tickets by type (admin only)
     *
     * @param ticketType Ticket type to filter by
     * @param page Page number
     * @param size Page size
     * @return ResponseEntity containing PagedResponse with tickets of specified type
     */
    ResponseEntity<PagedResponse<TicketResponse>> getTicketsByType(TicketType ticketType, int page, int size);

    /**
     * Cancel/Refund ticket (admin only)
     *
     * @param ticketId Ticket ID to cancel
     * @param reason Cancellation reason
     * @return ResponseEntity with confirmation message
     */
    ResponseEntity<String> cancelTicket(Long ticketId, String reason);

    /**
     * Get tickets purchased in date range (admin only)
     *
     * @param startDate Start date for search
     * @param endDate End date for search
     * @param page Page number
     * @param size Page size
     * @return ResponseEntity containing PagedResponse with tickets purchased in date range
     */
    ResponseEntity<PagedResponse<TicketResponse>> getTicketsByDateRange(LocalDateTime startDate, LocalDateTime endDate, int page, int size);

    // ===== TICKET ANALYTICS =====

    /**
     * Get sales analytics by ticket type
     *
     * @return ResponseEntity containing sales statistics by ticket type
     */
    ResponseEntity<List<SalesAnalyticsResponse>> getSalesAnalyticsByType();

    /**
     * Get revenue analytics for date range
     *
     * @param startDate Start date for analytics
     * @param endDate End date for analytics
     * @return ResponseEntity containing revenue information
     */
    ResponseEntity<java.math.BigDecimal> getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get top purchasing users
     *
     * @param limit Number of top users to return
     * @return ResponseEntity containing users ranked by ticket purchases
     */
    ResponseEntity<List<Object[]>> getTopPurchasingUsers(int limit);

    /**
     * Get daily ticket sales statistics
     *
     * @param days Number of days to include in statistics
     * @return ResponseEntity containing daily sales data
     */
    ResponseEntity<List<Object[]>> getDailySalesStats(int days);

    // ===== SYSTEM MAINTENANCE =====

    /**
     * Process expired tickets (system job)
     * Updates expired active tickets to EXPIRED status
     *
     * @return Number of tickets processed
     */
    int processExpiredTickets();

    /**
     * Generate daily ticket reports (system job)
     *
     * @param date Date for report generation
     * @return ResponseEntity containing report summary
     */
    ResponseEntity<String> generateDailyReport(LocalDateTime date);

    // ===== UTILITY METHODS =====

    /**
     * Check if user has valid pass (monthly/yearly)
     *
     * @param userDetails Current authenticated user
     * @return true if user has valid unlimited pass
     */
    boolean userHasValidPass(UserDetails userDetails);

    /**
     * Generate unique ticket number
     *
     * @param ticketType Type of ticket for numbering scheme
     * @return Unique ticket number string
     */
    String generateTicketNumber(TicketType ticketType);
}