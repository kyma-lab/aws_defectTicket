#!/bin/bash
set -e

echo "📋 Creating DynamoDB Tables in LocalStack..."
echo ""

ENDPOINT="http://localhost:4566"
REGION="us-east-1"

# Function to create table
create_table() {
    TABLE_NAME=$1
    echo "Creating $TABLE_NAME..."
    
    case $TABLE_NAME in
        "defect-tickets-tickets")
            aws dynamodb create-table \
              --table-name defect-tickets-tickets \
              --attribute-definitions \
                AttributeName=ticketId,AttributeType=S \
                AttributeName=batchId,AttributeType=S \
                AttributeName=status,AttributeType=S \
              --key-schema AttributeName=ticketId,KeyType=HASH \
              --billing-mode PAY_PER_REQUEST \
              --global-secondary-indexes \
                '[{"IndexName":"batch-index","KeySchema":[{"AttributeName":"batchId","KeyType":"HASH"}],"Projection":{"ProjectionType":"ALL"}},{"IndexName":"status-index","KeySchema":[{"AttributeName":"status","KeyType":"HASH"}],"Projection":{"ProjectionType":"ALL"}}]' \
              --endpoint-url $ENDPOINT \
              --region $REGION > /dev/null 2>&1
            ;;
            
        "defect-tickets-approvals")
            aws dynamodb create-table \
              --table-name defect-tickets-approvals \
              --attribute-definitions \
                AttributeName=approvalId,AttributeType=S \
                AttributeName=ticketId,AttributeType=S \
                AttributeName=status,AttributeType=S \
              --key-schema AttributeName=approvalId,KeyType=HASH \
              --billing-mode PAY_PER_REQUEST \
              --global-secondary-indexes \
                '[{"IndexName":"ticket-index","KeySchema":[{"AttributeName":"ticketId","KeyType":"HASH"}],"Projection":{"ProjectionType":"ALL"}},{"IndexName":"status-index","KeySchema":[{"AttributeName":"status","KeyType":"HASH"}],"Projection":{"ProjectionType":"ALL"}}]' \
              --endpoint-url $ENDPOINT \
              --region $REGION > /dev/null 2>&1
            ;;
            
        "defect-tickets-workflow-states")
            aws dynamodb create-table \
              --table-name defect-tickets-workflow-states \
              --attribute-definitions \
                AttributeName=executionId,AttributeType=S \
                AttributeName=batchId,AttributeType=S \
              --key-schema AttributeName=executionId,KeyType=HASH \
              --billing-mode PAY_PER_REQUEST \
              --global-secondary-indexes \
                '[{"IndexName":"batch-index","KeySchema":[{"AttributeName":"batchId","KeyType":"HASH"}],"Projection":{"ProjectionType":"ALL"}}]' \
              --endpoint-url $ENDPOINT \
              --region $REGION > /dev/null 2>&1
            ;;
    esac
    
    if [ $? -eq 0 ]; then
        echo "  ✅ $TABLE_NAME created successfully"
    else
        # Check if table already exists
        if aws dynamodb describe-table --table-name "$TABLE_NAME" --endpoint-url $ENDPOINT --region $REGION > /dev/null 2>&1; then
            echo "  ℹ️  $TABLE_NAME already exists"
        else
            echo "  ❌ Failed to create $TABLE_NAME"
            return 1
        fi
    fi
}

# Create all tables
create_table "defect-tickets-tickets"
create_table "defect-tickets-approvals"
create_table "defect-tickets-workflow-states"

echo ""
echo "📊 Listing tables:"
aws dynamodb list-tables \
  --endpoint-url $ENDPOINT \
  --region $REGION \
  --query 'TableNames' \
  --output table

echo ""
echo "✅ DynamoDB tables setup complete!"
