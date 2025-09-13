package io.github.kaltrinabajramii.urbantransitbackend.dto.response;

import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TransportType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteSummaryResponse {
    private Long id;
    private String routeName;
    private String description;
    private TransportType transportType;
    private Integer stopCount;
    private Boolean active;

    // For route listings without full stop details
}