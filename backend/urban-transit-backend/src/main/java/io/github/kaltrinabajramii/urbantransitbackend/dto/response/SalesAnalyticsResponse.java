package io.github.kaltrinabajramii.urbantransitbackend.dto.response;

import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketType;
import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TransportType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesAnalyticsResponse {
    private TicketType ticketType;
    private Long ticketCount;
    private BigDecimal totalRevenue;
}