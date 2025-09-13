package io.github.kaltrinabajramii.urbantransitbackend.dto.request;

import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TransportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRouteRequest {

    @NotBlank(message = "Route name is required")
    @Size(max = 50, message = "Route name cannot exceed 50 characters")
    private String routeName;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Transport type is required")
    private TransportType transportType;

    @NotNull(message = "Stops list is required")
    @Size(min = 2, message = "Route must have at least 2 stops")
    private List<String> stops;

    private LocalTime operatingStartTime;
    private LocalTime operatingEndTime;
}
