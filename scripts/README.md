# LocalStack Setup Scripts

## Quick Reference

```bash
# Complete setup (recommended)
./scripts/setup-localstack.sh

# Verify everything is working
./scripts/verify-localstack.sh

# Create only DynamoDB tables (if needed)
./scripts/create-dynamodb-tables.sh

# Clean up and start fresh
./scripts/cleanup-localstack.sh
```

---

## Scripts Overview

### `setup-localstack.sh` ⭐ Main Setup Script

**What it does:**
- Starts LocalStack container (or starts existing one)
- Creates all 3 DynamoDB tables
- Creates SQS queue
- Shows summary of created resources

**When to use:**
- First time setup
- After cleanup
- Can be run multiple times safely (idempotent)

**Usage:**
```bash
./scripts/setup-localstack.sh
```

---

### `verify-localstack.sh` ✅ Verification Script

**What it does:**
- Checks if LocalStack container is running
- Verifies all 3 DynamoDB tables exist
- Verifies SQS queue exists
- Shows what's missing (if anything)

**When to use:**
- After setup to confirm everything worked
- When you get "table not found" errors
- Before starting Spring Boot

**Usage:**
```bash
./scripts/verify-localstack.sh
```

**Example output:**
```
🔍 Verifying LocalStack Setup...

1. Container Status:
   ✅ LocalStack container is running

2. DynamoDB Tables:
   ✅ defect-tickets-tickets
   ✅ defect-tickets-approvals
   ✅ defect-tickets-workflow-states

3. SQS Queues:
   ✅ defect-ticket-ingestion

✅ All checks passed! LocalStack is ready.
```

---

### `create-dynamodb-tables.sh` 📋 Table Creation Script

**What it does:**
- Creates ONLY the DynamoDB tables
- Skips if tables already exist
- Useful when LocalStack is running but tables are missing

**When to use:**
- When you get "table not found" errors
- After LocalStack restart (if tables disappeared)
- To recreate tables without restarting LocalStack

**Usage:**
```bash
./scripts/create-dynamodb-tables.sh
```

---

### `cleanup-localstack.sh` 🧹 Cleanup Script

**What it does:**
- Stops LocalStack container
- Removes LocalStack container
- Deletes all data

**When to use:**
- When you want to start completely fresh
- When LocalStack is misbehaving
- Before running setup-localstack.sh again

**Usage:**
```bash
./scripts/cleanup-localstack.sh
```

**⚠️ Warning:** This deletes ALL data in LocalStack!

---

## Troubleshooting Workflows

### Problem: "Table not found" error

```bash
# 1. Verify what's wrong
./scripts/verify-localstack.sh

# 2. If tables are missing, create them
./scripts/create-dynamodb-tables.sh

# 3. Verify again
./scripts/verify-localstack.sh
```

---

### Problem: "Container already exists" error

```bash
# Option 1: Just run setup again (script handles it)
./scripts/setup-localstack.sh

# Option 2: Clean up and start fresh
./scripts/cleanup-localstack.sh
./scripts/setup-localstack.sh
```

---

### Problem: LocalStack not responding

```bash
# 1. Check if container is running
podman ps | grep localstack

# 2. Check logs
podman logs localstack-defect-ticket

# 3. Restart LocalStack
./scripts/cleanup-localstack.sh
./scripts/setup-localstack.sh
```

---

## Manual Commands

If scripts don't work, use these manual commands:

### Start LocalStack manually
```bash
podman run -d \
  --name localstack-defect-ticket \
  -p 4566:4566 \
  -e SERVICES=dynamodb,sqs,stepfunctions,lambda,secretsmanager \
  -e DEBUG=1 \
  localstack/localstack:3.0
```

### Check LocalStack health
```bash
curl http://localhost:4566/_localstack/health
```

### List DynamoDB tables
```bash
aws dynamodb list-tables \
  --endpoint-url http://localhost:4566 \
  --region us-east-1
```

### Describe a specific table
```bash
aws dynamodb describe-table \
  --table-name defect-tickets-tickets \
  --endpoint-url http://localhost:4566 \
  --region us-east-1
```

### View table data
```bash
aws dynamodb scan \
  --table-name defect-tickets-tickets \
  --endpoint-url http://localhost:4566 \
  --region us-east-1
```

---

## Common Issues

### Issue: AWS CLI not found
```bash
# Install AWS CLI
brew install awscli
# or
pip install awscli
```

### Issue: Podman not found
```bash
# Install Podman
brew install podman
```

### Issue: LocalStack won't start
```bash
# Check port 4566 is free
lsof -i :4566

# Kill process using port 4566 (if needed)
kill -9 <PID>

# Try again
./scripts/setup-localstack.sh
```

---

## Environment Variables

Scripts use these defaults:
- **LocalStack Endpoint:** `http://localhost:4566`
- **AWS Region:** `us-east-1`
- **Container Name:** `localstack-defect-ticket`

To change, edit the scripts or set environment variables:
```bash
export LOCALSTACK_ENDPOINT=http://localhost:4566
export AWS_REGION=us-east-1
```

---

## Next Steps After Setup

1. **Verify setup:**
   ```bash
   ./scripts/verify-localstack.sh
   ```

2. **Start Spring Boot:**
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

3. **Open browser:**
   ```
   http://localhost:8060/api.html
   ```

4. **Test the API:**
   - Click "Seed Test Data"
   - Click "Classify Ticket"
   - View results

---

## See Also

- `LOCAL_TESTING.md` - Complete testing guide
- `E2E_TESTING_SUMMARY.md` - Testing scenarios
- `README.md` - Project documentation
