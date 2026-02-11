# AWS Deployment Guide

This guide walks you through deploying the Defect Ticket Processing System to AWS with Bedrock Claude Sonnet 4.5.

## Prerequisites

### 1. Install Required Tools

```bash
# AWS CLI
brew install awscli  # macOS
# or visit: https://aws.amazon.com/cli/

# AWS SAM CLI
brew install aws-sam-cli  # macOS
# or visit: https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html

# Maven (if not already installed)
brew install maven

# Verify installations
aws --version
sam --version
mvn --version
java -version  # Should be Java 21
```

### 2. Configure AWS Credentials

```bash
aws configure
```

Enter your:
- AWS Access Key ID
- AWS Secret Access Key
- Default region (e.g., `us-east-1`)
- Default output format: `json`

Verify:
```bash
aws sts get-caller-identity
```

### 3. Enable Bedrock Model Access

**IMPORTANT**: You must enable Claude Sonnet 4.5 in your AWS account before deployment.

1. Navigate to [Bedrock Model Access](https://console.aws.amazon.com/bedrock/home?region=us-east-1#/modelaccess)
2. Click **"Manage model access"** or **"Edit"**
3. Find **Anthropic** section
4. Check the box for **Claude Sonnet 4.5**
5. Click **"Save changes"**
6. Wait for the status to change to **"Access granted"** (usually takes a few minutes)

> Note: Model availability varies by region. Ensure your deployment region supports Claude Sonnet 4.5.

## Deployment Options

### Option 1: Automated Deployment (Recommended)

Use the provided deployment script:

```bash
# Make the script executable
chmod +x scripts/deploy-to-aws.sh

# Deploy with defaults (us-east-1)
./scripts/deploy-to-aws.sh

# Deploy to specific region
./scripts/deploy-to-aws.sh --region us-west-2

# Deploy with custom configuration
./scripts/deploy-to-aws.sh \
  --region us-east-1 \
  --stack-name my-ticket-processor \
  --table-prefix my-tickets

# Interactive guided deployment
./scripts/deploy-to-aws.sh --guided
```

The script will:
- ✅ Check prerequisites (Java 21, Maven, AWS CLI, SAM CLI)
- ✅ Verify AWS credentials
- ✅ Check Bedrock availability
- ✅ Build the Maven project
- ✅ Build the SAM application
- ✅ Deploy to AWS
- ✅ Display stack outputs

### Option 2: Manual Deployment

#### Step 1: Build the Application

```bash
mvn clean package
```

This creates `target/defect-ticket-processor-1.0.0-SNAPSHOT-aws.jar`.

#### Step 2: Build SAM Application

```bash
sam build
```

#### Step 3: Deploy

**First-time deployment (guided):**
```bash
sam deploy --guided
```

Follow the prompts:
- Stack Name: `defect-ticket-processor`
- AWS Region: `us-east-1` (or your preferred region)
- Parameter TablePrefix: `defect-tickets`
- Confirm changes: `Y`
- Allow SAM CLI IAM role creation: `Y`
- Save arguments to configuration file: `Y`

**Subsequent deployments:**
```bash
sam deploy
```

## Post-Deployment

### 1. Verify Deployment

Check CloudFormation stack status:
```bash
aws cloudformation describe-stacks \
  --stack-name defect-ticket-processor \
  --query 'Stacks[0].StackStatus'
```

Should return: `"CREATE_COMPLETE"` or `"UPDATE_COMPLETE"`

### 2. Get Stack Outputs

```bash
aws cloudformation describe-stacks \
  --stack-name defect-ticket-processor \
  --query 'Stacks[0].Outputs[*].[OutputKey,OutputValue]' \
  --output table
```

Key outputs:
- `StateMachineArn` - Step Functions workflow ARN
- `IngestionQueueUrl` - SQS queue for batch ingestion
- `DefectTicketsTableName` - DynamoDB tickets table
- `ApprovalRequestsTableName` - DynamoDB approvals table

### 3. Test the Classification Function

Create a test payload:

```bash
cat > test-payload.json << 'EOF'
{
  "ticketId": "TEST-001"
}
EOF
```

Invoke the Lambda function:
```bash
aws lambda invoke \
  --function-name defect-tickets-classifyTicket \
  --payload file://test-payload.json \
  --region us-east-1 \
  response.json

cat response.json
```

### 4. Monitor Logs

View Lambda logs:
```bash
# List log groups
aws logs describe-log-groups --log-group-name-prefix /aws/lambda/defect-tickets

# Tail logs
sam logs -n ClassifyTicketFunction --stack-name defect-ticket-processor --tail
```

## Architecture Overview

### Deployed Resources

| Resource | Type | Purpose |
|----------|------|---------|
| `IngestBatchFunction` | Lambda | Batch ticket ingestion |
| `LoadBatchTicketsFunction` | Lambda | Load ticket IDs (claim check) |
| `ClassifyTicketFunction` | Lambda | AI classification with Bedrock |
| `CreateApprovalRequestFunction` | Lambda | HITL approval creation |
| `DefectTicketsTable` | DynamoDB | Ticket storage |
| `ApprovalRequestsTable` | DynamoDB | Approval requests |
| `WorkflowStatesTable` | DynamoDB | Workflow state |
| `TicketWorkflowStateMachine` | Step Functions | Orchestration |
| `IngestionQueue` | SQS | Batch ingestion buffer |
| `IngestionDLQ` | SQS | Dead letter queue |

### Lambda Configuration

- **Runtime**: Java 21
- **Memory**: 2048 MB
- **Timeout**: 60 seconds
- **SnapStart**: Enabled (cold start <1s)
- **Concurrency**: 10 for classification (Bedrock throttling protection)

### Bedrock Integration

- **Model**: Claude Sonnet 4.5 (`anthropic.claude-sonnet-4-5-20251024-v1:0`)
- **Temperature**: 0.3 (balanced creativity/consistency)
- **Max Tokens**: 2048
- **Confidence Threshold**: 0.85 (triggers HITL if below)

## Configuration

### Environment Variables

Set via CloudFormation parameters or Lambda environment:

| Variable | Default | Description |
|----------|---------|-------------|
| `AWS_REGION` | us-east-1 | AWS region |
| `TABLE_PREFIX` | defect-tickets | DynamoDB table prefix |
| `SPRING_PROFILES_ACTIVE` | aws | Spring profile |

### Update Configuration

Modify `src/main/resources/application-aws.yml` and redeploy:

```yaml
llm:
  confidence-threshold: 0.90  # Increase threshold
  max-concurrency: 20         # Increase concurrency
```

Then:
```bash
mvn clean package
sam deploy
```

## Troubleshooting

### Issue: Bedrock Access Denied

**Symptom**: Lambda logs show `AccessDeniedException` for Bedrock.

**Solution**:
1. Verify model access is granted in Bedrock console
2. Check Lambda IAM role has `bedrock:InvokeModel` permission
3. Verify model ID is correct for your region

### Issue: Lambda Timeout

**Symptom**: Function times out after 60 seconds.

**Solution**:
1. Increase timeout in `template.yaml`:
   ```yaml
   Timeout: 120
   ```
2. Check Bedrock latency in CloudWatch metrics
3. Consider increasing SnapStart memory

### Issue: Throttling Errors

**Symptom**: `LlmThrottlingException` in logs.

**Solution**:
1. Verify `ReservedConcurrentExecutions: 10` is set
2. Increase retry backoff in Step Functions
3. Request increased Bedrock quotas

### Issue: Cold Starts

**Symptom**: First invocation takes >3 seconds.

**Solution**:
1. SnapStart is enabled by default
2. Verify `AutoPublishAlias: live` in template
3. Check Lambda provisioned concurrency if needed

## Cost Estimation

### Monthly Cost Breakdown (1000 tickets/day)

| Service | Usage | Estimated Cost |
|---------|-------|---------------|
| Lambda | ~120K invocations | $1-2 |
| DynamoDB | PAY_PER_REQUEST | $5-10 |
| Bedrock | 30K requests (2K tokens/req) | $90-120 |
| Step Functions | 30K executions | $0.75 |
| SQS | 30K messages | <$1 |
| **Total** | | **~$100-135/month** |

> Bedrock is the primary cost driver. Adjust `confidence-threshold` to reduce AI calls.

## Security Best Practices

✅ **Implemented**:
- IAM least-privilege roles
- DynamoDB encryption at rest
- VPC endpoints (optional, not in template)
- Secrets Manager for sensitive data
- CloudWatch Logs retention

🔒 **Additional Recommendations**:
1. Enable VPC for Lambda functions
2. Use AWS WAF if exposing API Gateway
3. Enable AWS CloudTrail for audit logs
4. Rotate IAM credentials regularly
5. Enable GuardDuty for threat detection

## Cleanup

To delete all resources:

```bash
sam delete --stack-name defect-ticket-processor
```

Or via AWS CLI:
```bash
aws cloudformation delete-stack \
  --stack-name defect-ticket-processor \
  --region us-east-1
```

> Note: DynamoDB tables are deleted. Backup data first if needed.

## Next Steps

1. **Integrate with your systems**: Send tickets to the SQS queue
2. **Build the UI**: Use the approval API endpoints
3. **Monitor performance**: Set up CloudWatch dashboards
4. **Tune the model**: Adjust confidence threshold based on accuracy
5. **Scale up**: Increase concurrency as needed

## Support

- [AWS SAM Documentation](https://docs.aws.amazon.com/serverless-application-model/)
- [Spring AI Bedrock](https://docs.spring.io/spring-ai/reference/api/bedrock.html)
- [AWS Bedrock Documentation](https://docs.aws.amazon.com/bedrock/)

---

For issues or questions, check the project README.md or raise an issue in the repository.
