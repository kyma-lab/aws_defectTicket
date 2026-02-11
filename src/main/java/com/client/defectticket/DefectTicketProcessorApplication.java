package com.client.defectticket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Defect Ticket Processing System.
 * 
 * This serverless application combines AI-based classification with deterministic rules
 * in a Human-in-the-Loop (HITL) architecture for processing defect tickets.
 */
@SpringBootApplication
@EnableScheduling
public class DefectTicketProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DefectTicketProcessorApplication.class, args);
    }
}
