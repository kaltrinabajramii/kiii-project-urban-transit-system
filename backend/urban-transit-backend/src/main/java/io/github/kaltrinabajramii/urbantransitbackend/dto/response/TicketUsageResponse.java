package io.github.kaltrinabajramii.urbantransitbackend.dto.response;

import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TransportType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketUsageResponse {
    private Long id;
    private TicketSummaryResponse ticket;
    private RouteSummaryResponse route;
    private TransportType transportType;
    private String boardingStop;
    private String destinationStop;
    private LocalDateTime usedAt;
}