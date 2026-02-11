# DynamoDB LocalStack Connection - IntelliJ Database Tool

## Connection Parameters

### AWS DynamoDB Data Source

**Data Source Type:** Amazon DynamoDB

**Connection Settings:**
- **Endpoint URL:** `http://localhost:4566`
- **Region:** `us-east-1`
- **Access Key ID:** `test`
- **Secret Access Key:** `test`

## Setup Steps in IntelliJ

1. Open Database Tool Window (View → Tool Windows → Database)
2. Click `+` → Data Source → Amazon DynamoDB
3. Configure:
   - **Name:** LocalStack DynamoDB
   - **Endpoint:** `http://localhost:4566`
   - **Region:** `us-east-1`
   - **Authentication:** AWS Credentials
     - **Access Key:** `test`
     - **Secret Key:** `test`
4. Click "Test Connection"
5. Apply & OK

## Available Tables

- `defect-tickets-tickets` - Main ticket data
- `defect-tickets-approvals` - HITL approval requests
- `defect-tickets-workflow-states` - Step Functions state tracking

## CLI Alternative (Verify Connection)

```bash
# List tables
aws --endpoint-url=http://localhost:4566 dynamodb list-tables --region us-east-1

# Scan tickets table
aws --endpoint-url=http://localhost:4566 dynamodb scan \
  --table-name defect-tickets-tickets \
  --region us-east-1

# Scan approvals table
aws --endpoint-url=http://localhost:4566 dynamodb scan \
  --table-name defect-tickets-approvals \
  --region us-east-1
```

## NoSQL Workbench Alternative

If IntelliJ doesn't work well, use AWS NoSQL Workbench:
1. Download: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/workbench.html
2. Add Connection → Amazon DynamoDB Local
3. Hostname: `localhost`, Port: `4566`

## DynamoDB Admin (Web UI Alternative)

```bash
# Install dynamodb-admin globally
npm install -g dynamodb-admin

# Start (connects to LocalStack)
DYNAMO_ENDPOINT=http://localhost:4566 dynamodb-admin

# Open browser: http://localhost:8001
```
