import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { approvalsApi } from "@/app/api/client";
import type { ApprovalDecisionDto } from "@/app/api/types";
import { toast } from "sonner";

export const APPROVALS_QUERY_KEY = ["approvals", "pending"];

export function useApprovals() {
  return useQuery({
    queryKey: APPROVALS_QUERY_KEY,
    queryFn: approvalsApi.getPending,
    refetchInterval: 30000, // Auto-refresh every 30 seconds
  });
}

export function useDecision() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (decision: ApprovalDecisionDto) => approvalsApi.decide(decision),
    onSuccess: () => {
      // Invalidate and refetch
      queryClient.invalidateQueries({ queryKey: APPROVALS_QUERY_KEY });
      toast.success("Decision submitted successfully");
    },
    onError: (error: any) => {
      const message = error.message || "Failed to submit decision";
      toast.error(message);
      
      // Show validation errors if present
      if (error.validationErrors) {
        Object.entries(error.validationErrors).forEach(([field, msg]) => {
          toast.error(`${field}: ${msg}`);
        });
      }
    },
  });
}
