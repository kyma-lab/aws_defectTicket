# AWS Deployment Summary - Defect Ticket Processor with Bedrock

## Overview

Your application is now configured to deploy to AWS with Claude Sonnet 4.5 via AWS Bedrock for AI-powered ticket classification.

## What Was Changed

### 1. Updated to Claude Sonnet 4.5
- **Model**: `anthropic.claude-sonnet-4-5-20251024-v1:0`
- **Configuration**: Updated in `application.yml` and `template.yaml`
- **Max Tokens**: Increased to 2048 for better responses
- **Temperature**: 0.3 (balanced creativity/consistency)

### 2. Enhanced IAM Permissions
- Added `bedrock:InvokeModel` permission
- Added `bedrock:InvokeModelWithResponseStream` permission
- Scoped to Claude Sonnet models
- Location: `template.yaml` - ClassifyTicketFunction

### 3. Created Deployment Scripts

**`scripts/deploy-to-aws.sh`**
- Automated deployment with pre-flight checks
- Validates Java 21, AWS CLI, SAM CLI
- Checks Bedrock availability
- Builds and deploys in one command

**`scripts/check-bedrock-access.sh`**
- Verifies Bedrock service access
- Lists available Anthropic models
- Tests model invocation
- Provides helpful troubleshooting

**`scripts/test-classification.sh`**
- Tests deployed Lambda function
- Shows response and recent logs
- Makes it easy to verify deployment

### 4. Configuration Files

**`src/main/resources/application-aws.yml`**
- AWS-specific Spring configuration
- Production logging levels
- Bedrock settings

**`test-payload.json`**
- Sample payload for testing

### 5. Documentation

**`DEPLOYMENT.md`** (Comprehensive)
- Prerequisites installation
- Bedrock model access setup
- Deployment options (automated & manual)
- Post-deployment verification
- Troubleshooting guide
- Cost estimation
- Security best practices

**`AWS_QUICK_START.md`** (Fast track)
- 5-minute deployment guide
- Essential steps only
- Quick reference

## Deployment Workflow

```
┌─────────────────────────────────────────┐
│ 1. Enable Bedrock Model Access         │
│    (Console or CLI)                     │
└──────────────┬──────────────────────────┘
               │
               v
┌─────────────────────────────────────────┐
│ 2. Run: ./scripts/deploy-to-aws.sh     │
│    - Checks prerequisites               │
│    - Builds Maven project               │
│    - Builds SAM application             │
│    - Deploys to AWS                     │
└──────────────┬──────────────────────────┘
               │
               v
┌─────────────────────────────────────────┐
│ 3. Run: ./scripts/test-classification.sh│
│    - Tests Lambda function              │
│    - Verifies Bedrock integration       │
└─────────────────────────────────────────┘
```

## Quick Deploy Commands

```bash
# Step 1: Check Bedrock access
./scripts/check-bedrock-access.sh

# Step 2: Deploy (all-in-one)
./scripts/deploy-to-aws.sh

# Step 3: Test
./scripts/test-classification.sh
```

## Architecture

```
External Systems → SQS Queue → Lambda (Ingest)
                                   ↓
                    Step Functions Orchestration
                                   ↓
                    Lambda (Load Batch - Claim Check)
                                   ↓
            ┌──────────────────────┴──────────────────────┐
            │  Parallel Processing (MaxConcurrency=10)    │
            └──────────────────────┬──────────────────────┘
                                   ↓
                    Lambda (Classify) ←→ AWS Bedrock
                    - Sonnet 4.5          (Claude)
                    - Confidence: 0.85
                                   ↓
                    Low Confidence? → HITL Approval Gate
                                   ↓
                    DynamoDB (Results Storage)
```

## Deployed AWS Resources

| Resource Type | Name | Purpose |
|--------------|------|---------|
| Lambda | defect-tickets-ingestBatch | Batch ingestion |
| Lambda | defect-tickets-loadBatchTickets | Load ticket IDs |
| Lambda | defect-tickets-classifyTicket | AI classification |
| Lambda | defect-tickets-createApprovalRequest | HITL approval |
| DynamoDB | defect-tickets-tickets | Ticket storage |
| DynamoDB | defect-tickets-approvals | Approval requests |
| DynamoDB | defect-tickets-workflow-states | Workflow state |
| Step Functions | defect-tickets-TicketWorkflow | Orchestration |
| SQS | defect-tickets-ingestion | Message queue |
| SQS | defect-tickets-ingestion-dlq | Dead letter queue |

## Cost Breakdown (Monthly)

Based on **1,000 tickets/day** (~30,000/month):

| Service | Usage | Cost |
|---------|-------|------|
| **AWS Bedrock** | 30K requests × 2K tokens | **$90-120** |
| DynamoDB | PAY_PER_REQUEST, 30K writes | $8-10 |
| Lambda | 120K invocations, 2GB RAM | $1-2 |
| Step Functions | 30K executions | $0.75 |
| SQS | 30K messages | <$1 |
| **Total** | | **~$100-135** |

> **Note**: Bedrock is 80-90% of costs. Adjust `confidence-threshold` to reduce AI calls.

## Configuration Tuning

### Reduce Costs
```yaml
# application-aws.yml
llm:
  confidence-threshold: 0.90  # Only call AI when really needed
  max-concurrency: 5          # Lower if budget-constrained
```

### Increase Throughput
```yaml
# template.yaml
ClassifyTicketFunction:
  ReservedConcurrentExecutions: 20  # From 10

# application-aws.yml
batch:
  max-concurrency: 20  # Match Lambda concurrency
```

### Adjust AI Behavior
```yaml
spring:
  ai:
    bedrock:
      anthropic:
        chat:
          options:
            temperature: 0.1    # More deterministic
            max-tokens: 4096    # Longer responses
```

## Monitoring

### CloudWatch Dashboards
```bash
# Create custom dashboard
aws cloudwatch put-dashboard \
  --dashboard-name DefectTicketProcessor \
  --dashboard-body file://dashboard.json
```

### Key Metrics to Watch
- Lambda invocation errors
- Bedrock throttling (should be rare with concurrency=10)
- Step Functions execution failures
- DynamoDB throttling (shouldn't happen with PAY_PER_REQUEST)

### Logs
```bash
# Tail classification logs
sam logs -n ClassifyTicketFunction --stack-name defect-ticket-processor --tail

# Query logs
aws logs tail /aws/lambda/defect-tickets-classifyTicket --since 1h
```

## Security Considerations

✅ **Already Implemented**:
- IAM least-privilege roles
- No hardcoded credentials
- DynamoDB encryption at rest
- Bedrock permissions scoped to Claude models only

🔒 **Recommended Additions**:
1. **VPC Integration**: Run Lambdas in VPC with NAT Gateway
2. **Secrets Manager**: Store any API keys or tokens
3. **CloudTrail**: Enable for audit logging
4. **GuardDuty**: Threat detection
5. **AWS WAF**: If exposing via API Gateway

## Troubleshooting

### Deployment Fails: "Model not accessible"
**Solution**: Enable Claude Sonnet 4.5 in Bedrock console
```bash
./scripts/check-bedrock-access.sh
```

### Lambda: AccessDeniedException
**Solution**: Verify IAM role has `bedrock:InvokeModel` permission
```bash
aws iam get-role-policy \
  --role-name defect-ticket-processor-ClassifyTicketFunctionRole-* \
  --policy-name ClassifyTicketFunctionRolePolicy
```

### High Costs
**Solution**: 
1. Increase confidence threshold to reduce AI calls
2. Review CloudWatch Insights for usage patterns
3. Consider using Claude 3.5 Haiku for lower costs

### Throttling
**Solution**: Already handled with:
- ReservedConcurrentExecutions: 10
- Exponential backoff (5 retries)
- Request Bedrock quota increase if needed

## Next Steps

### 1. Integrate Your Systems
Send tickets to SQS:
```bash
aws sqs send-message \
  --queue-url <YOUR_QUEUE_URL> \
  --message-body '{"batchId":"batch-001","tickets":[...]}'
```

### 2. Build Approval UI
Use REST endpoints:
- GET `/api/v1/approvals/pending`
- POST `/api/v1/approvals/decide`

### 3. Monitor & Tune
- Set up CloudWatch alarms
- Review classification accuracy
- Adjust confidence threshold
- Track AI vs Human divergence

### 4. Scale
- Increase concurrency as needed
- Add more Step Functions Map states
- Consider DynamoDB Global Tables for multi-region

## Support Resources

- **Full Deployment Guide**: [DEPLOYMENT.md](DEPLOYMENT.md)
- **Quick Start**: [AWS_QUICK_START.md](AWS_QUICK_START.md)
- **Project README**: [README.md](README.md)
- **AWS Bedrock Docs**: https://docs.aws.amazon.com/bedrock/
- **Spring AI**: https://docs.spring.io/spring-ai/

## Files Created/Modified

### New Files
- ✅ `scripts/deploy-to-aws.sh` - Automated deployment
- ✅ `scripts/check-bedrock-access.sh` - Verify Bedrock access
- ✅ `scripts/test-classification.sh` - Test deployed function
- ✅ `src/main/resources/application-aws.yml` - AWS config
- ✅ `test-payload.json` - Sample test data
- ✅ `DEPLOYMENT.md` - Comprehensive deployment guide
- ✅ `AWS_QUICK_START.md` - Quick reference
- ✅ `.gitignore` - Git ignore rules

### Modified Files
- ✅ `src/main/resources/application.yml` - Updated to Sonnet 4.5
- ✅ `template.yaml` - Enhanced Bedrock permissions
- ✅ `src/main/resources/aws/stepfunctions/ticket-workflow.asl.json` - Fixed function ARNs
- ✅ `README.md` - Added deployment links

## Ready to Deploy?

```bash
# All-in-one command
./scripts/deploy-to-aws.sh

# Or step-by-step
./scripts/check-bedrock-access.sh
mvn clean package
sam build
sam deploy --guided
./scripts/test-classification.sh
```

---

**Your application is ready for production AWS deployment with Claude Sonnet 4.5!** 🚀
