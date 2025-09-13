package io.github.kaltrinabajramii.urbantransitbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoutePopularityResponse {
    private RouteSummaryResponse route;
    private Long usageCount;
}
