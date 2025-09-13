package io.github.kaltrinabajramii.urbantransitbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateTicketRequest {

    @NotBlank(message = "Ticket number is required")
    private String ticketNumber;

    private Long routeId; // Optional - for route-specific validation
}
