package io.github.kaltrinabajramii.urbantransitbackend.dto.request;

import io.github.kaltrinabajramii.urbantransitbackend.model.enums.TransportType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteSearchRequest {
    private String searchTerm;
    private TransportType transportType;
    private String stopName;
    private int page = 0;
    private int size = 20;
    private String sortBy = "routeName";
    private String sortDir = "asc";
}