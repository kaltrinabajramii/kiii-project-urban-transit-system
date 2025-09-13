package io.github.kaltrinabajramii.urbantransitbackend.dto.response;

import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TransportType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteResponse {
    private Long id;
    private String routeName;
    private String description;
    private TransportType transportType;
    private List<String> stops;
    private LocalTime operatingStartTime;
    private LocalTime operatingEndTime;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}