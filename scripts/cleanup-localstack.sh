#!/bin/bash

echo "🧹 Cleaning up LocalStack..."

# Stop and remove container
if podman ps -a --format '{{.Names}}' | grep -q '^localstack-defect-ticket$'; then
    echo "🛑 Stopping and removing LocalStack container..."
    podman rm -f localstack-defect-ticket
    echo "✅ Container removed"
else
    echo "ℹ️  No LocalStack container found"
fi

echo ""
echo "✅ Cleanup complete!"
echo ""
echo "To start fresh, run:"
echo "  ./scripts/setup-localstack.sh"
