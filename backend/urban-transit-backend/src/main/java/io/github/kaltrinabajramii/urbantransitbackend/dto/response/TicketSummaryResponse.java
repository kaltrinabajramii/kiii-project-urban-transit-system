package io.github.kaltrinabajramii.urbantransitbackend.dto.response;

import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketStatus;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketSummaryResponse {
    private Long id;
    private String ticketNumber;
    private TicketType ticketType;
    private BigDecimal price;
    private TicketStatus status;
    private LocalDateTime purchaseDate;
    private LocalDateTime validUntil;
    private Boolean isCurrentlyValid;

    // For user ticket listings
}