import { useQuery } from "@tanstack/react-query";
import { batchesApi } from "@/app/api/client";

export function useTicketStats(days: number = 7) {
  return useQuery({
    queryKey: ["ticket-stats", days],
    queryFn: () => batchesApi.getStats(days),
    refetchInterval: 60000, // Refresh every minute
  });
}
