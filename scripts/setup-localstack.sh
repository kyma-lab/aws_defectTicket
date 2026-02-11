#!/bin/bash
set -e

echo "🚀 Setting up LocalStack for Defect Ticket Processor..."

# Check if container already exists
if podman ps -a --format '{{.Names}}' | grep -q '^localstack-defect-ticket$'; then
    echo "📦 LocalStack container already exists"
    
    # Check if it's running
    if podman ps --format '{{.Names}}' | grep -q '^localstack-defect-ticket$'; then
        echo "✅ LocalStack is already running"
    else
        echo "▶️  Starting existing LocalStack container..."
        podman start localstack-defect-ticket
        sleep 5
    fi
else
    echo "📥 Creating new LocalStack container..."
    podman run -d \
      --name localstack-defect-ticket \
      -p 4566:4566 \
      -e SERVICES=dynamodb,sqs,stepfunctions,lambda,secretsmanager \
      -e DEBUG=1 \
      -e LAMBDA_RUNTIME_ENVIRONMENT_TIMEOUT=300 \
      localstack/localstack:3.0
    
    # Wait for LocalStack to be ready
    echo "⏳ Waiting for LocalStack to be ready..."
    sleep 15
fi

# Check and create DynamoDB tables
echo "📋 Checking DynamoDB tables..."

# Function to create table if it doesn't exist
create_table_if_not_exists() {
    TABLE_NAME=$1
    if aws --endpoint-url=http://localhost:4566 dynamodb describe-table --table-name "$TABLE_NAME" 2>/dev/null; then
        echo "   ✓ Table $TABLE_NAME already exists"
    else
        echo "   → Creating table $TABLE_NAME..."
        case $TABLE_NAME in
            "defect-tickets-tickets")
                aws --endpoint-url=http://localhost:4566 dynamodb create-table \
                  --table-name defect-tickets-tickets \
                  --attribute-definitions \
                    AttributeName=ticketId,AttributeType=S \
                    AttributeName=batchId,AttributeType=S \
                    AttributeName=status,AttributeType=S \
                  --key-schema AttributeName=ticketId,KeyType=HASH \
                  --billing-mode PAY_PER_REQUEST \
                  --global-secondary-indexes \
                    '[{"IndexName":"batch-index","KeySchema":[{"AttributeName":"batchId","KeyType":"HASH"}],"Projection":{"ProjectionType":"ALL"}},
                      {"IndexName":"status-index","KeySchema":[{"AttributeName":"status","KeyType":"HASH"}],"Projection":{"ProjectionType":"ALL"}}]' > /dev/null
                ;;
            "defect-tickets-approvals")
                aws --endpoint-url=http://localhost:4566 dynamodb create-table \
                  --table-name defect-tickets-approvals \
                  --attribute-definitions \
                    AttributeName=approvalId,AttributeType=S \
                    AttributeName=ticketId,AttributeType=S \
                    AttributeName=status,AttributeType=S \
                  --key-schema AttributeName=approvalId,KeyType=HASH \
                  --billing-mode PAY_PER_REQUEST \
                  --global-secondary-indexes \
                    '[{"IndexName":"ticket-index","KeySchema":[{"AttributeName":"ticketId","KeyType":"HASH"}],"Projection":{"ProjectionType":"ALL"}},
                      {"IndexName":"status-index","KeySchema":[{"AttributeName":"status","KeyType":"HASH"}],"Projection":{"ProjectionType":"ALL"}}]' > /dev/null
                ;;
            "defect-tickets-workflow-states")
                aws --endpoint-url=http://localhost:4566 dynamodb create-table \
                  --table-name defect-tickets-workflow-states \
                  --attribute-definitions \
                    AttributeName=executionId,AttributeType=S \
                    AttributeName=batchId,AttributeType=S \
                  --key-schema AttributeName=executionId,KeyType=HASH \
                  --billing-mode PAY_PER_REQUEST \
                  --global-secondary-indexes \
                    '[{"IndexName":"batch-index","KeySchema":[{"AttributeName":"batchId","KeyType":"HASH"}],"Projection":{"ProjectionType":"ALL"}}]' > /dev/null
                ;;
        esac
        echo "   ✓ Created $TABLE_NAME"
    fi
}

create_table_if_not_exists "defect-tickets-tickets"
create_table_if_not_exists "defect-tickets-approvals"
create_table_if_not_exists "defect-tickets-workflow-states"

# Check and create SQS queue
echo "📨 Checking SQS queues..."
if aws --endpoint-url=http://localhost:4566 sqs get-queue-url --queue-name defect-ticket-ingestion 2>/dev/null; then
    echo "   ✓ Queue defect-ticket-ingestion already exists"
else
    echo "   → Creating queue defect-ticket-ingestion..."
    aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name defect-ticket-ingestion > /dev/null
    echo "   ✓ Created defect-ticket-ingestion"
fi

echo ""
echo "✅ LocalStack setup complete!"
echo ""
echo "📊 Summary:"
aws --endpoint-url=http://localhost:4566 dynamodb list-tables --query 'TableNames' --output table
echo ""
echo "📨 SQS Queue URL:"
aws --endpoint-url=http://localhost:4566 sqs get-queue-url --queue-name defect-ticket-ingestion --query 'QueueUrl' --output text
echo ""
echo "🌐 LocalStack running at: http://localhost:4566"
echo "🔗 DynamoDB: http://localhost:4566"
echo "🔗 SQS: http://localhost:4566"
echo ""
echo "Next steps:"
echo "  1. Start Spring Boot: mvn spring-boot:run -Dspring-boot.run.profiles=local"
echo "  2. Open browser: http://localhost:8060/api.html"
