package com.client.defectticket.api.controller;

import com.client.defectticket.api.dto.BatchProgressDto;
import com.client.defectticket.api.dto.TicketStatsDto;
import com.client.defectticket.domain.service.BatchProgressService;
import com.client.defectticket.domain.service.TicketStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API controller for batch operations.
 * Provides progress tracking for HITL dashboard.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/batches")
@RequiredArgsConstructor
public class BatchController {

    private final BatchProgressService progressService;
    private final TicketStatsService statsService;

    /**
     * Get batch processing progress.
     * Shows real-time progress for dashboard.
     */
    @GetMapping("/{batchId}/progress")
    public ResponseEntity<BatchProgressDto> getBatchProgress(@PathVariable String batchId) {
        log.info("API: Fetching progress for batch: {}", batchId);
        BatchProgressDto progress = progressService.calculateProgress(batchId);
        return ResponseEntity.ok(progress);
    }

    /**
     * Get ticket statistics over time.
     * Shows how many tickets were auto-processed vs manual review.
     */
    @GetMapping("/stats")
    public ResponseEntity<TicketStatsDto> getTicketStats(
            @RequestParam(defaultValue = "7") int days) {
        log.info("API: Fetching ticket statistics for last {} days", days);
        TicketStatsDto stats = statsService.calculateStats(days);
        return ResponseEntity.ok(stats);
    }
}
