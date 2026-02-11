#!/bin/bash

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

REGION="${AWS_REGION:-us-east-1}"

echo -e "${YELLOW}Checking AWS Bedrock Access${NC}"
echo "Region: $REGION"
echo ""

# Check AWS credentials
echo -e "${YELLOW}1. Checking AWS credentials...${NC}"
if aws sts get-caller-identity --region "$REGION" &> /dev/null; then
    ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
    echo -e "${GREEN}✓ Authenticated as account: $ACCOUNT_ID${NC}"
else
    echo -e "${RED}✗ AWS credentials not configured${NC}"
    exit 1
fi
echo ""

# Check Bedrock availability
echo -e "${YELLOW}2. Checking Bedrock service availability...${NC}"
if aws bedrock list-foundation-models --region "$REGION" &> /dev/null; then
    echo -e "${GREEN}✓ Bedrock service is available${NC}"
else
    echo -e "${RED}✗ Bedrock not available in region $REGION${NC}"
    echo "Available regions: us-east-1, us-west-2, eu-west-1, ap-southeast-1, ap-northeast-1"
    exit 1
fi
echo ""

# List Anthropic models
echo -e "${YELLOW}3. Checking Anthropic models access...${NC}"
MODELS=$(aws bedrock list-foundation-models \
    --region "$REGION" \
    --by-provider anthropic \
    --query 'modelSummaries[*].[modelId,modelName]' \
    --output text 2>/dev/null)

if [ -z "$MODELS" ]; then
    echo -e "${RED}✗ No Anthropic models available${NC}"
    echo "You need to enable model access in the Bedrock console"
    echo "Visit: https://console.aws.amazon.com/bedrock/home?region=$REGION#/modelaccess"
    exit 1
fi

echo -e "${GREEN}Available Anthropic models:${NC}"
echo "$MODELS"
echo ""

# Check for specific Sonnet 4.5 model
echo -e "${YELLOW}4. Checking for Claude Sonnet 4.5...${NC}"
if echo "$MODELS" | grep -q "claude-sonnet-4-5\|anthropic.claude-sonnet-4-5"; then
    echo -e "${GREEN}✓ Claude Sonnet 4.5 is available!${NC}"
    SONNET_MODEL=$(echo "$MODELS" | grep "claude-sonnet-4-5\|anthropic.claude-sonnet-4-5" | head -1)
    echo "Model: $SONNET_MODEL"
else
    echo -e "${YELLOW}⚠ Claude Sonnet 4.5 not found${NC}"
    echo "Available models above. You may need to:"
    echo "1. Request access to Claude Sonnet 4.5"
    echo "2. Check if it's available in your region"
    echo "3. Use an alternative model (Claude 3.5 Sonnet, etc.)"
fi
echo ""

# Test Bedrock invocation (if jq is available)
if command -v jq &> /dev/null; then
    echo -e "${YELLOW}5. Testing Bedrock model invocation...${NC}"
    
    # Find the first available Claude model
    FIRST_MODEL=$(echo "$MODELS" | head -1 | awk '{print $1}')
    
    if [ -n "$FIRST_MODEL" ]; then
        echo "Testing with model: $FIRST_MODEL"
        
        # Create test payload
        cat > /tmp/bedrock-test.json << 'EOF'
{
    "anthropic_version": "bedrock-2023-05-31",
    "max_tokens": 100,
    "messages": [
        {
            "role": "user",
            "content": "Say 'Hello, I am working!' in exactly 5 words."
        }
    ]
}
EOF
        
        # Invoke model
        if aws bedrock-runtime invoke-model \
            --model-id "$FIRST_MODEL" \
            --body file:///tmp/bedrock-test.json \
            --region "$REGION" \
            /tmp/bedrock-response.json &> /dev/null; then
            
            echo -e "${GREEN}✓ Successfully invoked Bedrock model!${NC}"
            echo "Response:"
            cat /tmp/bedrock-response.json | jq '.content[0].text' -r
            
            rm -f /tmp/bedrock-test.json /tmp/bedrock-response.json
        else
            echo -e "${RED}✗ Failed to invoke Bedrock model${NC}"
            echo "This might be a permissions issue or the model requires additional setup"
        fi
    fi
else
    echo -e "${YELLOW}⚠ Skipping invocation test (jq not installed)${NC}"
fi
echo ""

echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}Bedrock Access Check Complete${NC}"
echo -e "${GREEN}================================${NC}"
echo ""
echo "Next steps:"
echo "1. If models are missing, enable them at:"
echo "   https://console.aws.amazon.com/bedrock/home?region=$REGION#/modelaccess"
echo "2. Deploy the application with: ./scripts/deploy-to-aws.sh"
echo "3. Test classification with: ./scripts/test-classification.sh"
