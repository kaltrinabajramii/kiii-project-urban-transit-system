package io.github.kaltrinabajramii.urbantransitbackend.dto.request;

import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TicketType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePricingRequest {

    @NotNull(message = "Ticket type is required")
    private TicketType ticketType;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @Size(max = 200, message = "Description cannot exceed 200 characters")
    private String description;

    private Boolean active = true;
}