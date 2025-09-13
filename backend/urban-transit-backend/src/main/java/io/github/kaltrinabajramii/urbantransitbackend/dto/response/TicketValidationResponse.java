package io.github.kaltrinabajramii.urbantransitbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketValidationResponse {
    private Boolean isValid;
    private String message;
    private TicketResponse ticket;

    public static TicketValidationResponse valid(TicketResponse ticket) {
        return new TicketValidationResponse(true, "Ticket is valid for travel", ticket);
    }

    public static TicketValidationResponse invalid(String message) {
        return new TicketValidationResponse(false, message, null);
    }
}