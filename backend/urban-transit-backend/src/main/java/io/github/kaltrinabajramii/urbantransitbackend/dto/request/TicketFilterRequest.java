package io.github.kaltrinabajramii.urbantransitbackend.dto.request;

import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketStatus;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketFilterRequest {
    private TicketType ticketType;
    private TicketStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int page = 0;
    private int size = 20;
    private String sortBy = "purchaseDate";
    private String sortDir = "desc";
}