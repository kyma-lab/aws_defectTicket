# AWS Quick Start Guide

Deploy the Defect Ticket Processor to AWS with Bedrock in 5 minutes.

## Prerequisites Check

```bash
# 1. Verify Java 21
java -version
# Should show: openjdk version "21"

# 2. Verify AWS CLI
aws --version

# 3. Verify SAM CLI
sam --version

# 4. Configure AWS credentials (if not done)
aws configure
```

## Step 1: Enable Bedrock Access

**This is critical - do this first!**

1. Go to: https://console.aws.amazon.com/bedrock/home?region=us-east-1#/modelaccess
2. Click **"Manage model access"** or **"Edit"**
3. Find **Anthropic** section and check:
   - ☑️ Claude Sonnet 4.5
   - ☑️ Claude 3.5 Sonnet (backup)
4. Click **"Save changes"**
5. Wait for status: **"Access granted"** ✅

Or run our check script:
```bash
./scripts/check-bedrock-access.sh
```

## Step 2: Deploy to AWS

### Option A: Automated (Recommended)

```bash
./scripts/deploy-to-aws.sh
```

This will:
- ✅ Check prerequisites
- ✅ Build the application
- ✅ Deploy to AWS
- ✅ Show you the results

### Option B: Manual

```bash
# Build
mvn clean package

# Deploy with SAM
sam build
sam deploy --guided
```

Follow the prompts and accept defaults.

## Step 3: Verify Deployment

```bash
# Check stack status
aws cloudformation describe-stacks \
  --stack-name defect-ticket-processor \
  --query 'Stacks[0].StackStatus'

# Should output: "CREATE_COMPLETE"
```

## Step 4: Test Classification

```bash
# Test the classification function
./scripts/test-classification.sh
```

Or manually:
```bash
aws lambda invoke \
  --function-name defect-tickets-classifyTicket \
  --payload '{"ticketId":"TEST-001"}' \
  --region us-east-1 \
  response.json

cat response.json
```

## What Gets Deployed?

- **4 Lambda Functions** (Java 21 with SnapStart)
  - IngestBatch
  - LoadBatchTickets
  - ClassifyTicket (with Bedrock)
  - CreateApprovalRequest

- **3 DynamoDB Tables**
  - defect-tickets-tickets
  - defect-tickets-approvals
  - defect-tickets-workflow-states

- **1 Step Functions Workflow**
  - Orchestrates the entire process

- **2 SQS Queues**
  - Ingestion queue + DLQ

## Deployed Endpoints

Get your API Gateway URL:
```bash
aws cloudformation describe-stacks \
  --stack-name defect-ticket-processor \
  --query 'Stacks[0].Outputs[?OutputKey==`ApiUrl`].OutputValue' \
  --output text
```

## Common Issues

### "Access Denied" when calling Bedrock

**Fix**: Enable model access in Bedrock console (Step 1)

### "Function not found"

**Fix**: Check stack name matches:
```bash
aws cloudformation list-stacks \
  --stack-status-filter CREATE_COMPLETE UPDATE_COMPLETE
```

### "Throttling" errors

**Fix**: Already configured with:
- MaxConcurrency: 10
- Exponential backoff
- Reserved concurrency

If you need more, request quota increase.

## Monitoring

View logs in real-time:
```bash
sam logs -n ClassifyTicketFunction \
  --stack-name defect-ticket-processor \
  --tail
```

Or in AWS Console:
- https://console.aws.amazon.com/cloudwatch/home?region=us-east-1#logsV2:log-groups

## Cost Estimate

For **1,000 tickets/day**:
- Lambda: ~$2/month
- DynamoDB: ~$8/month
- Bedrock: ~$100/month (main cost)
- Step Functions: ~$1/month
- SQS: <$1/month

**Total: ~$110/month**

## Next Steps

1. **Integrate** your ticket system → SQS queue
2. **Build UI** for approvals using the REST API
3. **Monitor** via CloudWatch dashboards
4. **Tune** confidence threshold based on accuracy

## Cleanup

To delete everything:
```bash
sam delete --stack-name defect-ticket-processor
```

## Get Help

- Full documentation: [DEPLOYMENT.md](DEPLOYMENT.md)
- Project README: [README.md](README.md)
- AWS Bedrock Docs: https://docs.aws.amazon.com/bedrock/

---

**Ready to deploy?**
```bash
./scripts/deploy-to-aws.sh
```
