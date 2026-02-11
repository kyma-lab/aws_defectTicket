"use client";

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

interface StatusBreakdownProps {
  statusBreakdown: Record<string, number>;
}

export function StatusBreakdown({ statusBreakdown }: StatusBreakdownProps) {
  const entries = Object.entries(statusBreakdown).sort((a, b) => b[1] - a[1]);

  return (
    <Card>
      <CardHeader>
        <CardTitle>Status Breakdown</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-3">
          {entries.map(([status, count]) => (
            <div
              key={status}
              className="flex items-center justify-between p-3 rounded-md border"
            >
              <div className="flex items-center gap-3">
                <Badge variant={getStatusVariant(status)}>
                  {formatStatus(status)}
                </Badge>
                <span className="text-sm text-muted-foreground">tickets</span>
              </div>
              <span className="text-lg font-semibold">{count}</span>
            </div>
          ))}

          {entries.length === 0 && (
            <div className="text-center py-8 text-muted-foreground">
              No status data available
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}

function formatStatus(status: string): string {
  return status
    .split("_")
    .map((word) => word.charAt(0) + word.slice(1).toLowerCase())
    .join(" ");
}

function getStatusVariant(status: string): "default" | "secondary" | "destructive" | "outline" {
  if (status.includes("REJECTED") || status.includes("ARCHIVED")) {
    return "destructive";
  }
  if (status.includes("APPROVED") || status.includes("CLOSED") || status.includes("RESOLVED")) {
    return "default";
  }
  if (status.includes("PENDING") || status.includes("IN_PROGRESS")) {
    return "secondary";
  }
  return "outline";
}
