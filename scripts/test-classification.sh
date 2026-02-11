#!/bin/bash

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

REGION="${AWS_REGION:-us-east-1}"
FUNCTION_NAME="${FUNCTION_NAME:-defect-tickets-classifyTicket}"
PAYLOAD_FILE="${1:-test-payload.json}"

echo -e "${YELLOW}Testing Classification Lambda Function${NC}"
echo "Region: $REGION"
echo "Function: $FUNCTION_NAME"
echo "Payload: $PAYLOAD_FILE"
echo ""

# Check if payload file exists
if [ ! -f "$PAYLOAD_FILE" ]; then
    echo -e "${RED}Payload file not found: $PAYLOAD_FILE${NC}"
    echo "Creating sample payload..."
    
    cat > "$PAYLOAD_FILE" << 'EOF'
{
  "ticketId": "TEST-001"
}
EOF
    echo -e "${GREEN}Created $PAYLOAD_FILE${NC}"
fi

echo -e "${YELLOW}Invoking Lambda function...${NC}"
aws lambda invoke \
  --function-name "$FUNCTION_NAME" \
  --payload file://"$PAYLOAD_FILE" \
  --region "$REGION" \
  --cli-binary-format raw-in-base64-out \
  response.json

echo ""
echo -e "${GREEN}Response:${NC}"
cat response.json | jq '.'

echo ""
echo -e "${YELLOW}Recent logs:${NC}"
aws logs tail "/aws/lambda/$FUNCTION_NAME" \
  --region "$REGION" \
  --since 5m \
  --format short

echo ""
echo -e "${GREEN}Test complete!${NC}"
