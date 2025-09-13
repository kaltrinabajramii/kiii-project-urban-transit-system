package io.github.kaltrinabajramii.urbantransitbackend.dto.request;

import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TransportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UseTicketRequest {

    @NotBlank(message = "Ticket number is required")
    private String ticketNumber;

    @NotNull(message = "Route ID is required")
    private Long routeId;

    @NotNull(message = "Transport type is required")
    private TransportType transportType;

    private String boardingStop;
    private String destinationStop;
}