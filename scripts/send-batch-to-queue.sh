#!/bin/bash

# Send batch data directly to SQS queue
# Usage: ./scripts/send-batch-to-queue.sh [batch-id]

QUEUE_URL="http://sqs.eu-central-1.localhost.localstack.cloud:4566/000000000000/defect-ticket-ingestion"
BATCH_ID="${1:-batch-$(date +%Y%m%d-%H%M%S)}"

echo "Sending batch to SQS: $BATCH_ID"

# Create message body
MESSAGE_BODY=$(cat <<EOF
{
  "batchId": "$BATCH_ID",
  "sourceSystem": "JIRA",
  "tickets": [
    {
      "sourceReference": "JIRA-1001",
      "title": "Critical security vulnerability in authentication module",
      "description": "SQL injection vulnerability found in login endpoint. Allows unauthorized access to user accounts. Immediate fix required. CVE-2024-12345 reported."
    },
    {
      "sourceReference": "JIRA-1002",
      "title": "Application crashes when processing large files",
      "description": "System runs out of memory when uploading files larger than 100MB. OutOfMemoryError in file processor. Affects production users."
    },
    {
      "sourceReference": "JIRA-1003",
      "title": "Minor typo in help documentation",
      "description": "Found a spelling mistake in the help section footer. Says 'Copywrite' instead of 'Copyright'. Low priority cosmetic fix."
    }
  ]
}
EOF
)

# Send message to SQS
aws --endpoint-url=http://localhost:4566 sqs send-message \
  --queue-url "$QUEUE_URL" \
  --message-body "$MESSAGE_BODY" \
  --region us-east-1

if [ $? -eq 0 ]; then
  echo "✓ Batch $BATCH_ID sent to queue successfully"
  echo ""
  echo "To verify:"
  echo "  aws --endpoint-url=http://localhost:4566 sqs receive-message --queue-url $QUEUE_URL --region us-east-1"
else
  echo "✗ Failed to send batch to queue"
  exit 1
fi
