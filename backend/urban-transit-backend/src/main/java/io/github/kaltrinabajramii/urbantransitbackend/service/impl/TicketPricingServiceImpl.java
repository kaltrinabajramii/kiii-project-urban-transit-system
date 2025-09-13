package io.github.kaltrinabajramii.urbantransitbackend.service.impl;

import io.github.kaltrinabajramii.urbantransitbackend.dto.request.UpdatePricingRequest;
import io.github.kaltrinabajramii.urbantransitbackend.dto.response.TicketPricingResponse;
import io.github.kaltrinabajramii.urbantransitbackend.model.entity.TicketPricing;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketType;
import io.github.kaltrinabajramii.urbantransitbackend.repository.TicketPricingRepository;
import io.github.kaltrinabajramii.urbantransitbackend.service.interfaces.TicketPricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketPricingServiceImpl implements TicketPricingService {

    private final TicketPricingRepository ticketPricingRepository;

    // ===== PUBLIC PRICING OPERATIONS =====

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<TicketPricingResponse>> getAllActivePricing() {
        try {
            List<TicketPricing> activePricing = ticketPricingRepository.findByActiveTrueOrderByTicketType();

            List<TicketPricingResponse> responses = activePricing.stream()
                    .map(this::mapToPricingResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<TicketPricingResponse> getPricingByType(TicketType ticketType) {
        try {
            TicketPricing pricing = ticketPricingRepository.findByTicketTypeAndActiveTrue(ticketType)
                    .orElse(null);

            if (pricing == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(mapToPricingResponse(pricing));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCurrentPrice(TicketType ticketType) {
        TicketPricing pricing = ticketPricingRepository.findByTicketTypeAndActiveTrue(ticketType)
                .orElseThrow(() -> new RuntimeException("No pricing found for ticket type: " + ticketType));
        return pricing.getPrice();
    }

    // ===== ADMIN PRICING MANAGEMENT =====

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<TicketPricingResponse>> getAllPricingRecords() {
        try {
            List<TicketPricing> allPricing = ticketPricingRepository.findAllOrderByTicketType();

            List<TicketPricingResponse> responses = allPricing.stream()
                    .map(this::mapToPricingResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<TicketPricingResponse> updatePricing(UpdatePricingRequest updateRequest) {
        try {
            TicketPricing pricing = ticketPricingRepository.findByTicketType(updateRequest.getTicketType())
                    .orElse(null);

            if (pricing == null) {
                return ResponseEntity.notFound().build();
            }

            // Simple validation - price must be positive
            if (updateRequest.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().build();
            }

            pricing.setPrice(updateRequest.getPrice());
            if (updateRequest.getDescription() != null) {
                pricing.setDescription(updateRequest.getDescription());
            }
            if (updateRequest.getActive() != null) {
                pricing.setActive(updateRequest.getActive());
            }

            TicketPricing updatedPricing = ticketPricingRepository.save(pricing);
            return ResponseEntity.ok(mapToPricingResponse(updatedPricing));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<String> updatePricingStatus(TicketType ticketType, boolean active) {
        try {
            TicketPricing pricing = ticketPricingRepository.findByTicketType(ticketType)
                    .orElse(null);

            if (pricing == null) {
                return ResponseEntity.notFound().build();
            }

            pricing.setActive(active);
            ticketPricingRepository.save(pricing);

            return ResponseEntity.ok(active ? "Pricing activated" : "Pricing deactivated");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Update failed");
        }
    }

    @Override
    public ResponseEntity<TicketPricingResponse> createPricing(UpdatePricingRequest updateRequest) {
        try {
            // Check if pricing already exists
            if (ticketPricingRepository.existsByTicketType(updateRequest.getTicketType())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            // Simple validation
            if (updateRequest.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().build();
            }

            TicketPricing newPricing = new TicketPricing();
            newPricing.setTicketType(updateRequest.getTicketType());
            newPricing.setPrice(updateRequest.getPrice());
            newPricing.setDescription(updateRequest.getDescription());
            newPricing.setActive(updateRequest.getActive() != null ? updateRequest.getActive() : true);

            TicketPricing savedPricing = ticketPricingRepository.save(newPricing);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToPricingResponse(savedPricing));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== PRICING ANALYTICS =====

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<TicketPricingResponse>> getPricingHistory(TicketType ticketType) {
        try {
            List<TicketPricing> history = ticketPricingRepository.findPricingHistoryByType(ticketType);

            List<TicketPricingResponse> responses = history.stream()
                    .map(this::mapToPricingResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<List<Object[]>> getCurrentPricingSummary() {
        try {
            List<Object[]> summary = ticketPricingRepository.getCurrentPricingSummary();
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ===== UTILITY METHODS =====

    @Override
    public boolean validatePricing(BigDecimal price, TicketType ticketType) {
        return price != null && price.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean pricingExists(TicketType ticketType) {
        return ticketPricingRepository.existsByTicketType(ticketType);
    }

    // ===== PRIVATE HELPER METHODS =====

    private TicketPricingResponse mapToPricingResponse(TicketPricing pricing) {
        TicketPricingResponse response = new TicketPricingResponse();
        response.setId(pricing.getId());
        response.setTicketType(pricing.getTicketType());
        response.setPrice(pricing.getPrice());
        response.setDescription(pricing.getDescription());
        response.setActive(pricing.getActive());
        response.setCreatedAt(pricing.getCreatedAt());
        response.setUpdatedAt(pricing.getUpdatedAt());
        return response;
    }
}