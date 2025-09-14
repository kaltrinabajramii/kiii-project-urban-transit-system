package io.github.kaltrinabajramii.urbantransitbackend.controller.rest;

import io.github.kaltrinabajramii.urbantransitbackend.dto.request.PurchaseTicketRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.request.ValidateTicketRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.request.UseTicketRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.request.TicketFilterRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.TicketResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.TicketSummaryResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.TicketValidationResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.TicketUsageResponse;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.PagedResponse;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketType;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketStatus;
import io.github.kaltrinabajramii.urbantransitbackend.service.interfaces.TicketService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping("/purchase")
    public ResponseEntity<TicketResponse> purchaseTicket(@AuthenticationPrincipal UserDetails userDetails,
                                                         @Valid @RequestBody PurchaseTicketRequest purchaseRequest) {
        return ticketService.purchaseTicket(userDetails, purchaseRequest);
    }

    @GetMapping("/price/{ticketType}")
    public ResponseEntity<BigDecimal> getTicketPrice(@PathVariable TicketType ticketType) {
        return ticketService.getTicketPrice(ticketType);
    }

    @GetMapping("/eligibility/{ticketType}")
    public ResponseEntity<String> checkPurchaseEligibility(@AuthenticationPrincipal UserDetails userDetails,
                                                           @PathVariable TicketType ticketType) {
        return ticketService.checkPurchaseEligibility(userDetails, ticketType);
    }

    @PostMapping("/validate")
    public ResponseEntity<TicketValidationResponse> validateTicket(@Valid @RequestBody ValidateTicketRequest validateRequest) {
        return ticketService.validateTicket(validateRequest);
    }

    @PostMapping("/use")
    public ResponseEntity<TicketUsageResponse> useTicket(@Valid @RequestBody UseTicketRequest useRequest) {
        return ticketService.useTicket(useRequest);
    }

    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<TicketResponse> getTicketByNumber(@PathVariable String ticketNumber) {
        return ticketService.getTicketByNumber(ticketNumber);
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<PagedResponse<TicketSummaryResponse>> getUserTickets(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ticketService.getUserTickets(userDetails, page, size);
    }

    @GetMapping("/my-tickets/valid")
    public ResponseEntity<List<TicketSummaryResponse>> getUserValidTickets(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ticketService.getUserValidTickets(userDetails);
    }

    @GetMapping("/my-tickets/ride")
    public ResponseEntity<List<TicketSummaryResponse>> getUserValidRideTickets(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ticketService.getUserValidRideTickets(userDetails);
    }

    @GetMapping("/my-tickets/unlimited")
    public ResponseEntity<List<TicketSummaryResponse>> getUserValidUnlimitedTickets(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ticketService.getUserValidUnlimitedTickets(userDetails);
    }

    @PostMapping("/my-tickets/filter")
    public ResponseEntity<PagedResponse<TicketSummaryResponse>> getUserTicketsFiltered(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TicketFilterRequest filterRequest) {
        return ticketService.getUserTicketsFiltered(userDetails, filterRequest);
    }

    @GetMapping("/my-tickets/travel-history")
    public ResponseEntity<PagedResponse<TicketUsageResponse>> getUserTravelHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ticketService.getUserTravelHistory(userDetails, page, size);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<TicketResponse>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ticketService.getAllTickets(page, size);
    }

    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<TicketResponse>> getTicketsByStatus(
            @PathVariable TicketStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ticketService.getTicketsByStatus(status, page, size);
    }

    @GetMapping("/admin/type/{ticketType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<TicketResponse>> getTicketsByType(
            @PathVariable TicketType ticketType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ticketService.getTicketsByType(ticketType, page, size);
    }

    @PutMapping("/admin/{ticketId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cancelTicket(@PathVariable Long ticketId,
                                               @RequestParam String reason) {
        return ticketService.cancelTicket(ticketId, reason);
    }

    @GetMapping("/admin/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<TicketResponse>> getTicketsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ticketService.getTicketsByDateRange(startDate, endDate, page, size);
    }
}
