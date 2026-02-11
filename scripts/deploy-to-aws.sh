#!/bin/bash

set -e

echo "================================"
echo "Defect Ticket Processor - AWS Deployment"
echo "================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default values
REGION="${AWS_REGION:-us-east-1}"
STACK_NAME="${STACK_NAME:-defect-ticket-processor}"
TABLE_PREFIX="${TABLE_PREFIX:-defect-tickets}"
S3_BUCKET="${S3_BUCKET:-}"

# Parse command line arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --region)
      REGION="$2"
      shift 2
      ;;
    --stack-name)
      STACK_NAME="$2"
      shift 2
      ;;
    --table-prefix)
      TABLE_PREFIX="$2"
      shift 2
      ;;
    --s3-bucket)
      S3_BUCKET="$2"
      shift 2
      ;;
    --guided)
      GUIDED=true
      shift
      ;;
    --help)
      echo "Usage: $0 [OPTIONS]"
      echo ""
      echo "Options:"
      echo "  --region REGION          AWS region (default: us-east-1)"
      echo "  --stack-name NAME        CloudFormation stack name (default: defect-ticket-processor)"
      echo "  --table-prefix PREFIX    DynamoDB table prefix (default: defect-tickets)"
      echo "  --s3-bucket BUCKET       S3 bucket for deployment artifacts (optional, SAM will create one)"
      echo "  --guided                 Run SAM deploy in guided mode"
      echo "  --help                   Show this help message"
      exit 0
      ;;
    *)
      echo -e "${RED}Unknown option: $1${NC}"
      exit 1
      ;;
  esac
done

echo -e "${GREEN}Configuration:${NC}"
echo "  Region: $REGION"
echo "  Stack Name: $STACK_NAME"
echo "  Table Prefix: $TABLE_PREFIX"
echo "  S3 Bucket: ${S3_BUCKET:-<SAM will create>}"
echo ""

# Check prerequisites
echo -e "${YELLOW}Checking prerequisites...${NC}"

if ! command -v aws &> /dev/null; then
    echo -e "${RED}AWS CLI not found. Please install it first.${NC}"
    exit 1
fi

if ! command -v sam &> /dev/null; then
    echo -e "${RED}AWS SAM CLI not found. Please install it first.${NC}"
    echo "Install via: brew install aws-sam-cli"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo -e "${RED}Maven not found. Please install it first.${NC}"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" != "21" ]; then
    echo -e "${RED}Java 21 required, found version $JAVA_VERSION${NC}"
    echo "JAVA_HOME is currently set to: $JAVA_HOME"
    exit 1
fi

echo -e "${GREEN}✓ All prerequisites met${NC}"
echo ""

# Check AWS credentials
echo -e "${YELLOW}Verifying AWS credentials...${NC}"
if ! aws sts get-caller-identity --region "$REGION" &> /dev/null; then
    echo -e "${RED}AWS credentials not configured or invalid${NC}"
    echo "Configure with: aws configure"
    exit 1
fi

AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
echo -e "${GREEN}✓ Authenticated as account: $AWS_ACCOUNT_ID${NC}"
echo ""

# Check if Bedrock is available in the region
echo -e "${YELLOW}Checking Bedrock availability in $REGION...${NC}"
if ! aws bedrock list-foundation-models --region "$REGION" --by-provider anthropic &> /dev/null; then
    echo -e "${YELLOW}Warning: Unable to verify Bedrock availability. Make sure Bedrock is enabled in your account.${NC}"
    echo "Visit: https://console.aws.amazon.com/bedrock/home?region=$REGION#/modelaccess"
else
    echo -e "${GREEN}✓ Bedrock is accessible${NC}"
fi
echo ""

# Check if Sonnet 4.5 model is available
echo -e "${YELLOW}Checking if Claude Sonnet 4.5 model is enabled...${NC}"
if aws bedrock list-foundation-models --region "$REGION" --by-provider anthropic 2>/dev/null | grep -q "claude-sonnet-4-5"; then
    echo -e "${GREEN}✓ Claude Sonnet 4.5 model is available${NC}"
else
    echo -e "${YELLOW}Warning: Claude Sonnet 4.5 not found. You may need to request model access.${NC}"
    echo "Visit: https://console.aws.amazon.com/bedrock/home?region=$REGION#/modelaccess"
fi
echo ""

# Build the project
echo -e "${YELLOW}Building project with Maven...${NC}"
mvn clean package -DskipTests

if [ ! -f "target/defect-ticket-processor-1.0.0-SNAPSHOT-aws.jar" ]; then
    echo -e "${RED}Build failed: JAR file not found${NC}"
    exit 1
fi

echo -e "${GREEN}✓ Build successful${NC}"
echo ""

# Build SAM application
echo -e "${YELLOW}Building SAM application...${NC}"
sam build

if [ $? -ne 0 ]; then
    echo -e "${RED}SAM build failed${NC}"
    exit 1
fi

echo -e "${GREEN}✓ SAM build successful${NC}"
echo ""

# Deploy
echo -e "${YELLOW}Deploying to AWS...${NC}"
echo ""

if [ "$GUIDED" = true ]; then
    sam deploy --guided
else
    DEPLOY_CMD="sam deploy \
      --stack-name \"$STACK_NAME\" \
      --region \"$REGION\" \
      --capabilities CAPABILITY_IAM \
      --parameter-overrides TablePrefix=\"$TABLE_PREFIX\" \
      --resolve-s3"
    
    if [ -n "$S3_BUCKET" ]; then
        DEPLOY_CMD="$DEPLOY_CMD --s3-bucket \"$S3_BUCKET\""
    fi
    
    eval $DEPLOY_CMD
fi

if [ $? -ne 0 ]; then
    echo -e "${RED}Deployment failed${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}================================${NC}"
echo -e "${GREEN}Deployment successful!${NC}"
echo -e "${GREEN}================================${NC}"
echo ""

# Get stack outputs
echo -e "${YELLOW}Stack Outputs:${NC}"
aws cloudformation describe-stacks \
    --stack-name "$STACK_NAME" \
    --region "$REGION" \
    --query 'Stacks[0].Outputs[*].[OutputKey,OutputValue]' \
    --output table

echo ""
echo -e "${GREEN}Next Steps:${NC}"
echo "1. Verify Bedrock model access in the AWS Console"
echo "2. Test the classification function:"
echo "   aws lambda invoke --function-name ${TABLE_PREFIX}-classifyTicket --region $REGION --payload file://test-payload.json response.json"
echo "3. Monitor CloudWatch Logs for Lambda execution"
echo "4. Start processing tickets via the API or SQS queue"
echo ""
echo -e "${YELLOW}Important URLs:${NC}"
echo "  CloudFormation: https://console.aws.amazon.com/cloudformation/home?region=$REGION#/stacks"
echo "  Bedrock Model Access: https://console.aws.amazon.com/bedrock/home?region=$REGION#/modelaccess"
echo "  Lambda Functions: https://console.aws.amazon.com/lambda/home?region=$REGION#/functions"
echo "  DynamoDB Tables: https://console.aws.amazon.com/dynamodbv2/home?region=$REGION#tables"
echo ""
