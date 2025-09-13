package io.github.kaltrinabajramii.urbantransitbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private Long totalUsers;
    private Long totalActiveTickets;
    private Long totalRoutes;
    private Long todayUsageCount;
    private java.math.BigDecimal totalRevenue;
    private Long totalTicketsSold;
}