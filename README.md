# Defect Ticket Processing System

Serverless HITL (Human-in-the-Loop) system for processing defect tickets using AI classification combined with deterministic rules.

## 🏗️ Architecture

- **Java 21** with Virtual Threads and Lombok
- **Spring Boot 3.3** with Spring Cloud Function for AWS Lambda
- **Spring AI** for Bedrock integration (no manual JSON parsing)
- **AWS Services**: Lambda (SnapStart), Step Functions, DynamoDB, SQS, Bedrock
- **Event-Driven**: SQS buffering → Step Functions orchestration → HITL gates

### Key Features

✅ **SnapStart Enabled** - <1s Lambda cold starts  
✅ **Claim Check Pattern** - Avoids 256KB Step Functions payload limit  
✅ **MaxConcurrency=10** - Prevents Bedrock throttling  
✅ **AI vs Human Divergence Tracking** - Compliance & ML training  
✅ **Granular Error Handling** - Exponential backoff for throttling  
✅ **Virtual Threads** - Better I/O performance  
✅ **TTL Auto-Archival** - 90-day DynamoDB cleanup  

## 🚀 Quick Start

### Prerequisites

- Java 21 (JAVA_HOME already set)
- Maven 3.9+
- Podman (for LocalStack)
- AWS CLI configured

### Local Development

1. **Start LocalStack**
   ```bash
   ./scripts/setup-localstack.sh
   ```

2. **Build the project**
   ```bash
   mvn clean package
   ```

3. **Run tests**
   ```bash
   mvn test
   ```

4. **Start Spring Boot locally**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```
   Server runs on port 8060.

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/approvals/pending` | List pending HITL approvals |
| POST | `/api/v1/approvals/decide` | Submit approval decision |
| GET | `/api/v1/batches/{batchId}/progress` | Get batch progress |

## 🏗️ Project Structure

```
src/main/java/com/client/defectticket/
├── domain/
│   ├── model/          # Entities (DefectTicket, ApprovalRequest, etc.)
│   ├── repository/     # DynamoDB repositories
│   └── service/        # Business logic (ApprovalService, BatchProgressService)
├── lambda/
│   ├── handler/        # Lambda function handlers
│   ├── classifier/     # Spring AI LLM integration
│   └── rules/          # Deterministic rule engine
├── messaging/
│   └── sqs/            # SQS message handlers
├── config/
│   └── aws/            # AWS SDK configuration
└── api/
    ├── controller/     # REST controllers
    └── dto/            # API DTOs
```

## 🔧 Configuration

See `application.yml` for configuration options:

- `server.port`: 8060 (local dev)
- `llm.confidence-threshold`: 0.85
- `llm.max-concurrency`: 10
- `hitl.approval.timeout-hours`: 24
- `hitl.approval.track-divergence`: true
- `batch.ttl-days`: 90

## 🧪 Testing

Test quality > coverage. Focus on:

- Unit tests for business logic
- Integration tests with Testcontainers/LocalStack
- Lambda handler tests with Spring Cloud Function

```bash
mvn test
```

## 🚢 Deployment

### Quick Start: Deploy to AWS with Bedrock

```bash
# 1. Enable Bedrock model access (required!)
./scripts/check-bedrock-access.sh

# 2. Deploy to AWS
./scripts/deploy-to-aws.sh

# 3. Test classification
./scripts/test-classification.sh
```

**See [AWS_QUICK_START.md](AWS_QUICK_START.md) for 5-minute setup or [DEPLOYMENT.md](DEPLOYMENT.md) for detailed guide.**

### Manual Deployment

```bash
# Build
mvn clean package

# Deploy with SAM
sam build
sam deploy --guided
```

## 📊 Workflow

```
REST API → SQS Queue → Batch Ingestion → Load Ticket IDs (Claim Check)
  → Parallel Classification (MaxConcurrency=10) → Rule Evaluation
  → HITL Gate 1 (if needed) → Step Functions Resume → Complete
```

## 🔐 Security

- AWS Secrets Manager for credentials
- IAM least-privilege roles
- DynamoDB encryption at rest
- TLS in transit
- No credentials in code

## 📝 Conventions

- **Java 21**: Use modern features (records, pattern matching, virtual threads)
- **Lombok**: Reduce boilerplate (`@Data`, `@Builder`, `@Slf4j`)
- **get/set**: Reserved ONLY for property accessors
- **Naming**: Descriptive method names (`classifyTicket()`, not `getClassification()`)
- **Ports**: 8060-8061 range

## 🐛 Troubleshooting

### LocalStack not starting
```bash
podman stop localstack-defect-ticket
podman rm localstack-defect-ticket
./scripts/setup-localstack.sh
```

### Port already in use
```bash
lsof -ti:8060 | xargs kill -9
```

### SnapStart issues
Ensure `AutoPublishAlias: live` is set in SAM template for all functions.

## 📚 Resources

- [Spring Boot 3.3 Docs](https://spring.io/projects/spring-boot)
- [Spring AI Bedrock](https://docs.spring.io/spring-ai/reference/api/bedrock.html)
- [AWS SAM Documentation](https://docs.aws.amazon.com/serverless-application-model/)
- [Lambda SnapStart](https://docs.aws.amazon.com/lambda/latest/dg/snapstart.html)

## 📄 License

Internal project - all rights reserved.
