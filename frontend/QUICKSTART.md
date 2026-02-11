# Quick Start Guide

## Prerequisites

1. Backend API must be running on `http://localhost:8060`
2. Node.js 18+ or 20+ installed
3. npm or yarn installed

## Installation & Setup

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Create environment file
cp .env.example .env.local

# Start development server
npm run dev
```

The frontend will be available at: **http://localhost:8061**

## First Steps

1. **Open the Dashboard** - Navigate to http://localhost:8061
   - See overview of pending approvals
   - View system status

2. **Review Approvals** - Click "Go to Approvals" or navigate to `/approvals`
   - View all pending HITL approval requests
   - Click "Approve" or "Reject" on any card
   - Fill in your email and optional comments
   - Submit your decision

3. **Monitor Batches** - Navigate to `/batches/new`
   - Enter a batch ID (e.g., `batch-001`)
   - View real-time progress with auto-refresh
   - See status breakdown by ticket state

## Troubleshooting

### Backend Connection Issues

If you see "Failed to load approvals":
1. Verify backend is running: `curl http://localhost:8060/api/v1/approvals/pending`
2. Check CORS is enabled on backend
3. Verify `NEXT_PUBLIC_API_URL` in `.env.local`

### Port Already in Use

If port 8061 is taken:
1. Stop the process using the port
2. Or change port in `package.json` scripts:
   ```json
   "dev": "next dev -p 8062"
   ```

### Dependencies Installation Fails

Try clearing cache:
```bash
rm -rf node_modules package-lock.json
npm install
```

## Default Test Data

To test the application:

1. **Generate test approvals** (using backend):
   ```bash
   # From project root
   cd scripts
   ./send-batch-to-queue.sh test-batch 5
   ```

2. **Wait for approvals** to appear in the dashboard
3. **Make decisions** through the UI

## Key URLs

- Dashboard: http://localhost:8061
- Approvals: http://localhost:8061/approvals
- Batch Progress: http://localhost:8061/batches/[batchId]

## Auto-Refresh Intervals

- Approvals: **30 seconds**
- Batch Progress: **5 seconds**

These can be adjusted in the respective hooks:
- `frontend/app/hooks/useApprovals.ts`
- `frontend/app/hooks/useBatchProgress.ts`
