package com.client.defectticket.domain.repository;

import com.client.defectticket.domain.model.DefectTicket;
import com.client.defectticket.domain.model.enums.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for DefectTicketRepository.
 * Tests DynamoDB operations with Enhanced Client.
 */
@SpringBootTest
@ActiveProfiles("test")
class DefectTicketRepositoryTest {

    @Autowired
    private DefectTicketRepository ticketRepository;

    private DefectTicket testTicket;

    @BeforeEach
    void setUp() {
        testTicket = DefectTicket.builder()
                .ticketId(UUID.randomUUID().toString())
                .batchId("batch-001")
                .sourceSystem("JIRA")
                .sourceReference("PROJ-123")
                .title("Test Ticket")
                .description("Test description")
                .status(TicketStatus.NEW)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .ttl(Instant.now().plusSeconds(7776000).getEpochSecond())
                .build();
    }

    @Test
    void shouldSaveAndFindTicketById() {
        // When
        ticketRepository.save(testTicket);

        // Then
        var found = ticketRepository.findById(testTicket.getTicketId());
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Test Ticket");
        assertThat(found.get().getStatus()).isEqualTo(TicketStatus.NEW);
    }

    @Test
    void shouldFindTicketsByBatchId() {
        // Given
        ticketRepository.save(testTicket);

        // When
        List<DefectTicket> tickets = ticketRepository.findByBatchId("batch-001");

        // Then
        assertThat(tickets).isNotEmpty();
        assertThat(tickets).anyMatch(t -> t.getTicketId().equals(testTicket.getTicketId()));
    }

    @Test
    void shouldFindTicketsByStatus() {
        // Given
        ticketRepository.save(testTicket);

        // When
        List<DefectTicket> tickets = ticketRepository.findByStatus(TicketStatus.NEW);

        // Then
        assertThat(tickets).isNotEmpty();
        assertThat(tickets).anyMatch(t -> t.getTicketId().equals(testTicket.getTicketId()));
    }

    @Test
    void shouldReturnEmptyWhenTicketNotFound() {
        // When
        var found = ticketRepository.findById("non-existent-id");

        // Then
        assertThat(found).isEmpty();
    }
}
