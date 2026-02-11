package com.client.defectticket.api.dto;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for ticket statistics over time.
 */
@Value
@Builder
public class TicketStatsDto {
    List<DailyStats> dailyStats;
    Summary summary;

    @Value
    @Builder
    public static class DailyStats {
        LocalDate date;
        int totalTickets;
        int autoProcessed;
        int manualReview;
    }

    @Value
    @Builder
    public static class Summary {
        int totalTickets;
        int autoProcessed;
        int manualReview;
        double autoProcessedPercentage;
    }
}
