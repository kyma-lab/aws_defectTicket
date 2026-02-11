"use client";

import { useState } from "react";
import { useBatchProgress } from "@/app/hooks/useBatchProgress";
import { ProgressBar } from "../components/ProgressBar";
import { StatusBreakdown } from "../components/StatusBreakdown";
import { Skeleton } from "@/components/ui/skeleton";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { AlertCircle, RefreshCw, Search } from "lucide-react";

export default function BatchProgressPage({ params }: { params: { batchId: string } }) {
  const [inputBatchId, setInputBatchId] = useState(params.batchId);
  const [currentBatchId, setCurrentBatchId] = useState(params.batchId);
  
  const { data: progress, isLoading, error, refetch } = useBatchProgress(
    currentBatchId !== "new" ? currentBatchId : null
  );

  const handleSearch = () => {
    if (inputBatchId && inputBatchId !== currentBatchId) {
      setCurrentBatchId(inputBatchId);
      // Update URL without reload
      window.history.pushState({}, "", `/batches/${inputBatchId}`);
    }
  };

  const showSearchPrompt = currentBatchId === "new" || !progress;

  return (
    <div className="container mx-auto py-8 px-4 max-w-6xl">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Batch Progress</h1>
          <p className="text-muted-foreground mt-1">
            Monitor real-time ticket batch processing status
          </p>
        </div>
        {progress && (
          <Button
            variant="outline"
            size="sm"
            onClick={() => refetch()}
            className="flex items-center gap-2"
          >
            <RefreshCw className="h-4 w-4" />
            Refresh
          </Button>
        )}
      </div>

      <Card className="mb-6">
        <CardHeader>
          <CardTitle className="text-base">Batch ID Lookup</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex gap-2">
            <div className="flex-1">
              <Label htmlFor="batchId" className="sr-only">
                Batch ID
              </Label>
              <Input
                id="batchId"
                placeholder="Enter batch ID (e.g., batch-001)"
                value={inputBatchId}
                onChange={(e) => setInputBatchId(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleSearch()}
              />
            </div>
            <Button onClick={handleSearch} className="flex items-center gap-2">
              <Search className="h-4 w-4" />
              Search
            </Button>
          </div>
        </CardContent>
      </Card>

      {isLoading && (
        <div className="space-y-4">
          <Skeleton className="h-48 w-full" />
          <Skeleton className="h-64 w-full" />
        </div>
      )}

      {error && (
        <div className="flex flex-col items-center justify-center py-12 text-center">
          <AlertCircle className="h-12 w-12 text-destructive mb-4" />
          <h3 className="text-lg font-semibold mb-2">Failed to load batch progress</h3>
          <p className="text-muted-foreground mb-4">
            {(error as any).message || "An unexpected error occurred"}
          </p>
          <Button onClick={() => refetch()}>Try Again</Button>
        </div>
      )}

      {!isLoading && !error && showSearchPrompt && (
        <div className="flex flex-col items-center justify-center py-12 text-center">
          <div className="rounded-full bg-muted p-4 mb-4">
            <Search className="h-8 w-8 text-muted-foreground" />
          </div>
          <h3 className="text-lg font-semibold mb-2">Enter a Batch ID</h3>
          <p className="text-muted-foreground">
            Search for a batch ID to view its processing progress
          </p>
        </div>
      )}

      {!isLoading && !error && progress && (
        <div className="space-y-6">
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <span className="font-medium">Batch ID:</span>
            <code className="px-2 py-1 bg-muted rounded">{progress.batchId}</code>
            <span className="ml-auto">Auto-refreshing every 5 seconds</span>
          </div>

          <ProgressBar
            percentage={progress.progressPercentage}
            total={progress.totalTickets}
            processed={progress.processedTickets}
            pending={progress.pendingTickets}
          />

          <StatusBreakdown statusBreakdown={progress.statusBreakdown} />
        </div>
      )}
    </div>
  );
}
