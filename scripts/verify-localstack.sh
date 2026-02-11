#!/bin/bash

echo "🔍 Verifying LocalStack Setup..."
echo ""

# Check if container is running
echo "1. Container Status:"
if podman ps --format '{{.Names}}' | grep -q '^localstack-defect-ticket$'; then
    echo "   ✅ LocalStack container is running"
else
    echo "   ❌ LocalStack container is NOT running"
    echo "   Run: ./scripts/setup-localstack.sh"
    exit 1
fi

echo ""
echo "2. DynamoDB Tables:"
TABLES=$(aws dynamodb list-tables \
  --endpoint-url http://localhost:4566 \
  --region us-east-1 \
  --query 'TableNames' \
  --output text 2>/dev/null)

if [ -z "$TABLES" ]; then
    echo "   ❌ No tables found!"
    echo "   Run the table creation commands from the error message"
    exit 1
fi

EXPECTED_TABLES=("defect-tickets-tickets" "defect-tickets-approvals" "defect-tickets-workflow-states")
MISSING=0

for TABLE in "${EXPECTED_TABLES[@]}"; do
    if echo "$TABLES" | grep -q "$TABLE"; then
        echo "   ✅ $TABLE"
    else
        echo "   ❌ $TABLE (missing)"
        MISSING=1
    fi
done

echo ""
echo "3. SQS Queues:"
QUEUE=$(aws sqs get-queue-url \
  --queue-name defect-ticket-ingestion \
  --endpoint-url http://localhost:4566 \
  --region us-east-1 \
  --output text 2>/dev/null)

if [ -z "$QUEUE" ]; then
    echo "   ❌ Queue not found!"
else
    echo "   ✅ defect-ticket-ingestion"
fi

echo ""
if [ $MISSING -eq 0 ]; then
    echo "✅ All checks passed! LocalStack is ready."
    echo ""
    echo "Next steps:"
    echo "  1. Start Spring Boot: mvn spring-boot:run -Dspring-boot.run.profiles=local"
    echo "  2. Open browser: http://localhost:8060/api.html"
else
    echo "⚠️  Some resources are missing. Please create them manually."
    echo ""
    echo "See LOCAL_TESTING.md for setup instructions."
    exit 1
fi
