import { useQuery } from "@tanstack/react-query";
import { batchesApi } from "@/app/api/client";

export function useBatchProgress(batchId: string | null) {
  return useQuery({
    queryKey: ["batches", batchId, "progress"],
    queryFn: () => batchesApi.getProgress(batchId!),
    enabled: !!batchId,
    refetchInterval: 5000, // Refresh every 5 seconds for real-time updates
  });
}
