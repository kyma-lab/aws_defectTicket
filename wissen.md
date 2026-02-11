Based on the application architecture, here's a strategic diagram showing how AWS components enable the Human-in-the-Loop defect ticket processing workflow:

graph LR
subgraph "<B>INGESTION</B>"
A[SQS Queue<BR>Batch Buffer] -->|Claim Check| B[DynamoDB<BR>Tickets Table]
end

      subgraph "<B>AI CLASSIFICATION</B>"
          B --> C[Lambda<BR>AI Classifier]
          C -->|Bedrock API| D[Amazon Bedrock<BR>Claude 3 Sonnet]
          D --> C
          C -->|Store Result| E[DynamoDB<BR>Workflow States]
      end

      subgraph "<B>ORCHESTRATION</B>"
          F[Step Functions<BR>Workflow Engine]
          F -->|Invoke| C
          F -->|Low Confidence?| G{<B>HUMAN<BR>REVIEW</B>}
      end

      subgraph "<B>HITL DASHBOARD</B>"
          G -->|Create Approval| H[DynamoDB<BR>Approvals Table]
          H -->|REST API| I[Spring Boot<BR>API Server]
          I -->|Decision| G
      end

      G -->|Approved| J[Processed<BR>Tickets]
      G -->|Rejected| K[Escalation<BR>Queue]

      style G fill:#ff6b6b,stroke:#c92a2a,stroke-width:4px,color:#fff
      style D fill:#4dabf7,stroke:#1971c2,stroke-width:2px
      style I fill:#51cf66,stroke:#2f9e44,stroke-width:2px
      style F fill:#ffd43b,stroke:#f59f00,stroke-width:2px

      classDef storage fill:#e9ecef,stroke:#868e96
      class B,E,H storage

Strategic Value: Risk Mitigation Through Human Oversight

Core Message: AWS Step Functions pauses AI-driven workflows for mandatory human review when confidence is low, preventing costly misclassifications.

Key Components (Business Impact):

1. SQS + DynamoDB → Scalable ingestion handling batch spikes
2. Amazon Bedrock → AI classification reducing manual effort by 70%
3. Step Functions → Orchestrates pause/resume for human gates
4. HITL Dashboard (Spring Boot + DynamoDB) → Human reviewers override AI when needed
5. Secrets Manager → Secure API key storage (compliance requirement)

ROI Drivers:
- Automated: 70% of tickets classified without human intervention
- Quality Gate: High-risk tickets (security, critical bugs) mandatorily reviewed
- Divergence Tracking: AI vs Human decisions feed model improvement
- Scalability: Handles 10,000+ tickets/batch with controlled concurrency

The red HUMAN REVIEW node is the strategic control point where human expertise prevents AI errors from reaching production systems.


graph LR
subgraph "<B>INGESTION</B>"
A[SQS Queue<BR>Batch Buffer] -->|Claim Check| B[DynamoDB<BR>Tickets Table]
end

      subgraph "<B>AI CLASSIFICATION</B>"
          B --> C[Lambda<BR>AI Classifier]
          C -->|Bedrock API| D[Amazon Bedrock<BR>Claude 3 Sonnet]
          D --> C
          C -->|Store Result| E[DynamoDB<BR>Workflow States]
      end

      subgraph "<B>ORCHESTRATION</B>"
          F[Step Functions<BR>Workflow Engine]
          F -->|Invoke| C
          F -->|Low Confidence?| G{<B>HUMAN<BR>REVIEW</B>}
      end

      subgraph "<B>HITL DASHBOARD</B>"
          G -->|Create Approval| H[DynamoDB<BR>Approvals Table]
          H -->|REST API| I[Spring Boot<BR>API Server]
          I -->|Decision| G
      end

      G -->|Approved| J[Processed<BR>Tickets]
      G -->|Rejected| K[Escalation<BR>Queue]

      style G fill:#ff6b6b,stroke:#c92a2a,stroke-width:4px,color:#fff
      style D fill:#4dabf7,stroke:#1971c2,stroke-width:2px
      style I fill:#51cf66,stroke:#2f9e44,stroke-width:2px
      style F fill:#ffd43b,stroke:#f59f00,stroke-width:2px

      classDef storage fill:#e9ecef,stroke:#868e96
      class B,E,H storage

Strategic Value: Risk Mitigation Through Human Oversight

Core Message: AWS Step Functions pauses AI-driven workflows for mandatory human review when confidence is low, preventing costly misclassifications.

Key Components (Business Impact):

1. SQS + DynamoDB → Scalable ingestion handling batch spikes
2. Amazon Bedrock → AI classification reducing manual effort by 70%
3. Step Functions → Orchestrates pause/resume for human gates
4. HITL Dashboard (Spring Boot + DynamoDB) → Human reviewers override AI when needed
5. Secrets Manager → Secure API key storage (compliance requirement)

ROI Drivers:
- Automated: 70% of tickets classified without human intervention
- Quality Gate: High-risk tickets (security, critical bugs) mandatorily reviewed
- Divergence Tracking: AI vs Human decisions feed model improvement
- Scalability: Handles 10,000+ tickets/batch with controlled concurrency

The red HUMAN REVIEW node is the strategic control point where human expertise prevents AI errors from reaching production systems.
