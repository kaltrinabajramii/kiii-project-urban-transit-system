package io.github.kaltrinabajramii.urbantransitbackend.controller.rest;

import io.github.kaltrinabajramii.urbantransitbackend.dto.request.UpdatePricingRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.TicketPricingResponse;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketType;
import io.github.kaltrinabajramii.urbantransitbackend.service.interfaces.TicketPricingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing")
public class TicketPricingController {

    private final TicketPricingService ticketPricingService;

    public TicketPricingController(TicketPricingService ticketPricingService) {
        this.ticketPricingService = ticketPricingService;
    }

    @GetMapping
    public ResponseEntity<List<TicketPricingResponse>> getAllActivePricing() {
        return ticketPricingService.getAllActivePricing();
    }

    @GetMapping("/{ticketType}")
    public ResponseEntity<TicketPricingResponse> getPricingByType(@PathVariable TicketType ticketType) {
        return ticketPricingService.getPricingByType(ticketType);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TicketPricingResponse>> getAllPricingRecords() {
        return ticketPricingService.getAllPricingRecords();
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketPricingResponse> createPricing(@Valid @RequestBody UpdatePricingRequest updateRequest) {
        return ticketPricingService.createPricing(updateRequest);
    }

    @PutMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TicketPricingResponse> updatePricing(@Valid @RequestBody UpdatePricingRequest updateRequest) {
        return ticketPricingService.updatePricing(updateRequest);
    }

    @PutMapping("/admin/{ticketType}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updatePricingStatus(@PathVariable TicketType ticketType,
                                                      @RequestParam boolean active) {
        return ticketPricingService.updatePricingStatus(ticketType, active);
    }

    @GetMapping("/admin/{ticketType}/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TicketPricingResponse>> getPricingHistory(@PathVariable TicketType ticketType) {
        return ticketPricingService.getPricingHistory(ticketType);
    }

    @GetMapping("/admin/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Object[]>> getCurrentPricingSummary() {
        return ticketPricingService.getCurrentPricingSummary();
    }
}
