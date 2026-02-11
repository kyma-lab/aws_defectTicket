"use client";

import { Progress } from "@/components/ui/progress";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

interface ProgressBarProps {
  percentage: number;
  total: number;
  processed: number;
  pending: number;
}

export function ProgressBar({ percentage, total, processed, pending }: ProgressBarProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Batch Progress</CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-2">
          <div className="flex justify-between text-sm">
            <span className="text-muted-foreground">Progress</span>
            <span className="font-medium">{percentage.toFixed(1)}%</span>
          </div>
          <Progress value={percentage} className="h-3" />
        </div>

        <div className="grid grid-cols-3 gap-4 pt-2">
          <div className="text-center">
            <div className="text-2xl font-bold text-primary">{total}</div>
            <div className="text-xs text-muted-foreground">Total Tickets</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-green-600">{processed}</div>
            <div className="text-xs text-muted-foreground">Processed</div>
          </div>
          <div className="text-center">
            <div className="text-2xl font-bold text-orange-600">{pending}</div>
            <div className="text-xs text-muted-foreground">Pending</div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}
