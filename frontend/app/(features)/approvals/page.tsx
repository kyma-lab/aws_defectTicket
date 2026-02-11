"use client";

import { useState } from "react";
import { useApprovals } from "@/app/hooks/useApprovals";
import { ApprovalCard } from "./components/ApprovalCard";
import { DecisionDialog } from "./components/DecisionDialog";
import { Skeleton } from "@/components/ui/skeleton";
import { AlertCircle, RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";

export default function ApprovalsPage() {
  const { data: approvals, isLoading, error, refetch } = useApprovals();
  const [dialogState, setDialogState] = useState<{
    open: boolean;
    approvalId: string | null;
    isApprove: boolean;
  }>({
    open: false,
    approvalId: null,
    isApprove: true,
  });

  const handleApprove = (approvalId: string) => {
    setDialogState({
      open: true,
      approvalId,
      isApprove: true,
    });
  };

  const handleReject = (approvalId: string) => {
    setDialogState({
      open: true,
      approvalId,
      isApprove: false,
    });
  };

  const handleDialogClose = (open: boolean) => {
    setDialogState({
      open,
      approvalId: null,
      isApprove: true,
    });
  };

  return (
    <div className="container mx-auto py-8 px-4 max-w-6xl">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Pending Approvals</h1>
          <p className="text-muted-foreground mt-1">
            Review and decide on pending HITL approval requests
          </p>
        </div>
        <Button
          variant="outline"
          size="sm"
          onClick={() => refetch()}
          className="flex items-center gap-2"
        >
          <RefreshCw className="h-4 w-4" />
          Refresh
        </Button>
      </div>

      {isLoading && (
        <div className="space-y-4">
          {[...Array(3)].map((_, i) => (
            <Skeleton key={i} className="h-64 w-full" />
          ))}
        </div>
      )}

      {error && (
        <div className="flex flex-col items-center justify-center py-12 text-center">
          <AlertCircle className="h-12 w-12 text-destructive mb-4" />
          <h3 className="text-lg font-semibold mb-2">Failed to load approvals</h3>
          <p className="text-muted-foreground mb-4">
            {(error as any).message || "An unexpected error occurred"}
          </p>
          <Button onClick={() => refetch()}>Try Again</Button>
        </div>
      )}

      {!isLoading && !error && approvals && approvals.length === 0 && (
        <div className="flex flex-col items-center justify-center py-12 text-center">
          <div className="rounded-full bg-muted p-4 mb-4">
            <AlertCircle className="h-8 w-8 text-muted-foreground" />
          </div>
          <h3 className="text-lg font-semibold mb-2">No pending approvals</h3>
          <p className="text-muted-foreground">
            All approval requests have been processed
          </p>
        </div>
      )}

      {!isLoading && !error && approvals && approvals.length > 0 && (
        <div className="space-y-4">
          {approvals.map((approval) => (
            <ApprovalCard
              key={approval.approvalId}
              approval={approval}
              onApprove={handleApprove}
              onReject={handleReject}
            />
          ))}
        </div>
      )}

      <DecisionDialog
        approvalId={dialogState.approvalId}
        isApprove={dialogState.isApprove}
        open={dialogState.open}
        onOpenChange={handleDialogClose}
      />
    </div>
  );
}
