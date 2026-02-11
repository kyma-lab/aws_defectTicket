# HITL Frontend

Human-in-the-Loop (HITL) Frontend Dashboard for the Defect Ticket Processing System.

## Tech Stack

- **Framework**: Next.js 14 with App Router
- **Language**: TypeScript (strict mode)
- **Styling**: Tailwind CSS
- **UI Components**: shadcn/ui
- **Data Fetching**: TanStack Query (React Query)
- **State Management**: Zustand
- **Animations**: Framer Motion

## Features

### 1. Approvals Dashboard (`/approvals`)
- View all pending HITL approval requests
- Review AI classification recommendations
- Approve or reject tickets with comments
- Real-time updates (auto-refresh every 30 seconds)

### 2. Batch Progress View (`/batches/[batchId]`)
- Monitor batch processing in real-time
- Visual progress bar with percentage
- Status breakdown by ticket state
- Auto-refresh every 5 seconds

### 3. Main Dashboard (`/`)
- Overview of system status
- Quick stats: pending approvals count
- Navigation to detailed views

## Getting Started

### Prerequisites

- Node.js 18+ or 20+
- npm or yarn
- Backend API running on `http://localhost:8060`

### Installation

```bash
# Install dependencies
npm install

# Copy environment variables
cp .env.example .env.local

# Start development server (runs on port 8061)
npm run dev
```

### Build for Production

```bash
npm run build
npm start
```

## Project Structure

```
frontend/
├── app/
│   ├── (features)/
│   │   ├── approvals/          # Approvals feature
│   │   │   ├── components/
│   │   │   │   ├── ApprovalCard.tsx
│   │   │   │   └── DecisionDialog.tsx
│   │   │   └── page.tsx
│   │   └── batches/            # Batch progress feature
│   │       ├── [batchId]/
│   │       │   └── page.tsx
│   │       └── components/
│   │           ├── ProgressBar.tsx
│   │           └── StatusBreakdown.tsx
│   ├── api/                    # API client and types
│   │   ├── client.ts
│   │   └── types.ts
│   ├── hooks/                  # Custom React hooks
│   │   ├── useApprovals.ts
│   │   └── useBatchProgress.ts
│   ├── layout.tsx              # Root layout
│   ├── page.tsx                # Main dashboard
│   ├── providers.tsx           # React Query provider
│   └── globals.css
├── components/
│   ├── ui/                     # shadcn/ui components
│   ├── ErrorBoundary.tsx
│   └── Navigation.tsx
└── lib/
    ├── queryClient.ts          # TanStack Query config
    ├── store.ts                # Zustand store
    ├── utils.ts                # Utility functions
    └── date-utils.ts

```

## API Integration

### Backend Endpoints

- `GET /api/v1/approvals/pending` - List pending approvals
- `POST /api/v1/approvals/decide` - Submit approval decision
- `GET /api/v1/batches/{batchId}/progress` - Get batch progress

### Type Safety

All API responses are fully typed using TypeScript interfaces that match the backend DTOs:
- `ApprovalRequestDto`
- `ApprovalDecisionDto`
- `BatchProgressDto`
- `ErrorResponse`

## Configuration

### Environment Variables

Create a `.env.local` file:

```env
NEXT_PUBLIC_API_URL=http://localhost:8060/api/v1
```

### Port Configuration

The frontend runs on port **8061** by default (configured in `package.json`).

## Development

### Code Style

- TypeScript strict mode enabled
- ESLint with Next.js config
- Tailwind CSS for styling
- Mobile-first responsive design

### Key Libraries

- `@tanstack/react-query` - Server state management
- `zustand` - Global UI state
- `axios` - HTTP client
- `lucide-react` - Icons
- `sonner` - Toast notifications
- `framer-motion` - Animations

## Testing

(Tests to be added)

## Deployment

The application can be deployed to:
- Vercel (recommended for Next.js)
- AWS Amplify
- Docker container
- Any Node.js hosting platform

## License

Internal project
