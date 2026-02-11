#!/usr/bin/env python3
"""
Send batch data directly to SQS queue for testing.
Bypasses the REST API and sends messages directly to LocalStack SQS.

Usage:
    python3 scripts/send-batch-to-queue.py
    python3 scripts/send-batch-to-queue.py --batch-id custom-batch-001
    python3 scripts/send-batch-to-queue.py --tickets 10
"""

import argparse
import boto3
import json
import sys
from datetime import datetime

# LocalStack configuration
ENDPOINT_URL = "http://localhost:4566"
QUEUE_URL="http://sqs.eu-central-1.localhost.localstack.cloud:4566/000000000000/defect-ticket-ingestion"
REGION = "us-east-1"

# Sample ticket templates
TICKET_TEMPLATES = [
    {
        "title": "Critical security vulnerability in authentication",
        "description": "SQL injection vulnerability found in login endpoint. Allows unauthorized access to user accounts. Immediate fix required. CVE-2024-12345 reported.",
        "severity": "CRITICAL"
    },
    {
        "title": "Application crashes on large file upload",
        "description": "System runs out of memory when uploading files larger than 100MB. OutOfMemoryError in file processor. Affects production users.",
        "severity": "HIGH"
    },
    {
        "title": "Performance degradation during peak hours",
        "description": "API response time increases from 200ms to 5000ms during peak traffic. Database connection pool exhaustion suspected.",
        "severity": "MEDIUM"
    },
    {
        "title": "Minor UI alignment issue on mobile",
        "description": "Submit button slightly misaligned on mobile devices. Cosmetic issue that doesn't affect functionality.",
        "severity": "LOW"
    },
    {
        "title": "Data corruption in export functionality",
        "description": "CSV export produces corrupted files with missing columns. Data loss risk for users. Regression from recent deployment.",
        "severity": "CRITICAL"
    },
    {
        "title": "Typo in help documentation",
        "description": "Found spelling mistake in help section. Says 'Copywrite' instead of 'Copyright'. Low priority fix.",
        "severity": "LOW"
    },
    {
            "title": "Neeed more help",
            "description": "Irgendwie geht hier gar nichts, weiss echt nicht mehr weiter",
            "severity": "HIGH"
        }
]


def send_batch_to_queue(batch_id, source_system, num_tickets):
    """Send batch message to SQS queue."""
    
    # Create SQS client
    sqs = boto3.client(
        'sqs',
        endpoint_url=ENDPOINT_URL,
        region_name=REGION,
        aws_access_key_id='test',
        aws_secret_access_key='test'
    )
    
    # Generate tickets
    tickets = []
    for i in range(num_tickets):
        template = TICKET_TEMPLATES[i % len(TICKET_TEMPLATES)]
        ticket = {
            "sourceReference": f"{source_system}-{1000 + i}",
            "title": template["title"],
            "description": template["description"]
        }
        tickets.append(ticket)
    
    # Create batch message
    batch_message = {
        "batchId": batch_id,
        "sourceSystem": source_system,
        "tickets": tickets
    }
    
    # Send to SQS
    try:
        response = sqs.send_message(
            QueueUrl=QUEUE_URL,
            MessageBody=json.dumps(batch_message)
        )
        
        print(f"✓ Batch sent successfully!")
        print(f"  Batch ID: {batch_id}")
        print(f"  Source System: {source_system}")
        print(f"  Tickets: {num_tickets}")
        print(f"  SQS Message ID: {response['MessageId']}")
        print()
        print(f"Batch message preview:")
        print(json.dumps(batch_message, indent=2))
        
        return True
        
    except Exception as e:
        print(f"✗ Failed to send batch to queue: {e}", file=sys.stderr)
        return False


def main():
    parser = argparse.ArgumentParser(description='Send batch data to SQS queue')
    parser.add_argument('--batch-id', 
                       default=f"batch-{datetime.now().strftime('%Y%m%d-%H%M%S')}",
                       help='Batch ID (default: batch-YYYYMMDD-HHMMSS)')
    parser.add_argument('--source-system', 
                       default='JIRA',
                       choices=['JIRA', 'ServiceNow', 'GitHub'],
                       help='Source system (default: JIRA)')
    parser.add_argument('--tickets', 
                       type=int, 
                       default=3,
                       help='Number of tickets to send (default: 3)')
    
    args = parser.parse_args()
    
    success = send_batch_to_queue(args.batch_id, args.source_system, args.tickets)
    sys.exit(0 if success else 1)


if __name__ == '__main__':
    main()
