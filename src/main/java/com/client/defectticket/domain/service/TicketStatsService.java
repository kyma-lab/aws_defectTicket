package com.client.defectticket.domain.service;

import com.client.defectticket.api.dto.TicketStatsDto;
import com.client.defectticket.domain.model.ApprovalRequest;
import com.client.defectticket.domain.model.DefectTicket;
import com.client.defectticket.domain.model.enums.ApprovalStatus;
import com.client.defectticket.domain.model.enums.TicketStatus;
import com.client.defectticket.domain.repository.ApprovalRequestRepository;
import com.client.defectticket.domain.repository.DefectTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for calculating ticket statistics over time.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketStatsService {

    private final DefectTicketRepository ticketRepository;
    private final ApprovalRequestRepository approvalRepository;

    /**
     * Calculate ticket statistics for the last N days.
     */
    public TicketStatsDto calculateStats(int days) {
        log.info("Calculating ticket statistics for last {} days", days);

        Instant cutoffDate = Instant.now().minus(days, ChronoUnit.DAYS);
        
        // Fetch all tickets from the last N days (using scan)
        List<DefectTicket> tickets = ticketRepository.scanAll().stream()
                .filter(ticket -> ticket.getCreatedAt() != null)
                .filter(ticket -> ticket.getCreatedAt().isAfter(cutoffDate))
                .collect(Collectors.toList());

        log.debug("Found {} tickets in the last {} days", tickets.size(), days);

        // Get all approval requests for these tickets
        Set<String> ticketIds = tickets.stream()
                .map(DefectTicket::getTicketId)
                .collect(Collectors.toSet());
        
        List<ApprovalRequest> allApprovals = ticketIds.stream()
                .flatMap(ticketId -> approvalRepository.findByTicketId(ticketId).stream())
                .collect(Collectors.toList());

        // Group tickets by date
        Map<LocalDate, List<DefectTicket>> ticketsByDate = tickets.stream()
                .collect(Collectors.groupingBy(ticket -> 
                    LocalDate.ofInstant(ticket.getCreatedAt(), ZoneId.systemDefault())
                ));

        // Calculate daily stats
        List<TicketStatsDto.DailyStats> dailyStats = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            List<DefectTicket> dayTickets = ticketsByDate.getOrDefault(date, List.of());
            
            int totalTickets = dayTickets.size();
            int autoProcessed = (int) dayTickets.stream()
                    .filter(ticket -> isAutoProcessed(ticket, allApprovals))
                    .count();
            int manualReview = (int) dayTickets.stream()
                    .filter(ticket -> requiresManualReview(ticket, allApprovals))
                    .count();

            dailyStats.add(TicketStatsDto.DailyStats.builder()
                    .date(date)
                    .totalTickets(totalTickets)
                    .autoProcessed(autoProcessed)
                    .manualReview(manualReview)
                    .build());
        }

        // Calculate summary
        int totalTickets = tickets.size();
        int totalAutoProcessed = (int) tickets.stream()
                .filter(ticket -> isAutoProcessed(ticket, allApprovals))
                .count();
        int totalManualReview = (int) tickets.stream()
                .filter(ticket -> requiresManualReview(ticket, allApprovals))
                .count();
        double autoProcessedPercentage = totalTickets > 0 
                ? (double) totalAutoProcessed / totalTickets * 100 
                : 0.0;

        TicketStatsDto.Summary summary = TicketStatsDto.Summary.builder()
                .totalTickets(totalTickets)
                .autoProcessed(totalAutoProcessed)
                .manualReview(totalManualReview)
                .autoProcessedPercentage(autoProcessedPercentage)
                .build();

        log.info("Stats summary: {} total, {} auto, {} manual ({:.1f}% auto)",
                totalTickets, totalAutoProcessed, totalManualReview, autoProcessedPercentage);

        return TicketStatsDto.builder()
                .dailyStats(dailyStats)
                .summary(summary)
                .build();
    }

    /**
     * Check if ticket was auto-processed (no manual intervention).
     * A ticket is auto-processed if it has NO approval requests OR all approvals were never pending.
     */
    private boolean isAutoProcessed(DefectTicket ticket, List<ApprovalRequest> allApprovals) {
        List<ApprovalRequest> ticketApprovals = allApprovals.stream()
                .filter(a -> a.getTicketId().equals(ticket.getTicketId()))
                .collect(Collectors.toList());
        
        // No approvals = auto-processed
        if (ticketApprovals.isEmpty()) {
            return true;
        }
        
        // If any approval was pending or required human decision, it's NOT auto-processed
        return ticketApprovals.stream()
                .noneMatch(a -> a.getStatus() == ApprovalStatus.PENDING ||
                               a.getStatus() == ApprovalStatus.APPROVED ||
                               a.getStatus() == ApprovalStatus.REJECTED);
    }

    /**
     * Check if ticket required or currently requires manual review.
     * A ticket requires manual review if it has approval requests that were/are pending.
     */
    private boolean requiresManualReview(DefectTicket ticket, List<ApprovalRequest> allApprovals) {
        List<ApprovalRequest> ticketApprovals = allApprovals.stream()
                .filter(a -> a.getTicketId().equals(ticket.getTicketId()))
                .collect(Collectors.toList());
        
        // Has any approval that was pending or processed by human
        return ticketApprovals.stream()
                .anyMatch(a -> a.getStatus() == ApprovalStatus.PENDING ||
                              a.getStatus() == ApprovalStatus.APPROVED ||
                              a.getStatus() == ApprovalStatus.REJECTED);
    }
}
