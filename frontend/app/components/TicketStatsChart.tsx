"use client";

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useTicketStats } from "@/app/hooks/useTicketStats";
import { Skeleton } from "@/components/ui/skeleton";
import { AlertCircle, TrendingUp, CheckCircle2, AlertTriangle } from "lucide-react";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";

interface TicketStatsChartProps {
  days?: number;
}

export function TicketStatsChart({ days = 7 }: TicketStatsChartProps) {
  const { data: stats, isLoading, error } = useTicketStats(days);

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <Skeleton className="h-6 w-48" />
          <Skeleton className="h-4 w-72 mt-2" />
        </CardHeader>
        <CardContent>
          <Skeleton className="h-80 w-full" />
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Ticket Statistics</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col items-center justify-center py-12 text-center">
            <AlertCircle className="h-12 w-12 text-destructive mb-4" />
            <p className="text-muted-foreground">Failed to load statistics</p>
          </div>
        </CardContent>
      </Card>
    );
  }

  if (!stats) return null;

  // Format data for chart
  const chartData = stats.dailyStats.map((day) => ({
    date: new Date(day.date).toLocaleDateString("de-DE", {
      month: "short",
      day: "numeric",
    }),
    "Total Tickets": day.totalTickets,
    "Auto-Processed": day.autoProcessed,
    "Manual Review": day.manualReview,
  }));

  return (
    <Card>
      <CardHeader>
        <CardTitle>Ticket Processing Statistics</CardTitle>
        <CardDescription>
          Overview of ticket processing over the last {days} days
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Summary Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="flex items-center gap-3 p-4 rounded-lg border bg-card">
            <div className="p-2 rounded-full bg-blue-100 dark:bg-blue-900/20">
              <TrendingUp className="h-5 w-5 text-blue-600 dark:text-blue-400" />
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Total Tickets</p>
              <p className="text-2xl font-bold">{stats.summary.totalTickets}</p>
            </div>
          </div>

          <div className="flex items-center gap-3 p-4 rounded-lg border bg-card">
            <div className="p-2 rounded-full bg-green-100 dark:bg-green-900/20">
              <CheckCircle2 className="h-5 w-5 text-green-600 dark:text-green-400" />
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Auto-Processed</p>
              <p className="text-2xl font-bold">{stats.summary.autoProcessed}</p>
              <p className="text-xs text-green-600 dark:text-green-400 font-medium">
                {stats.summary.autoProcessedPercentage.toFixed(1)}% automated
              </p>
            </div>
          </div>

          <div className="flex items-center gap-3 p-4 rounded-lg border bg-card">
            <div className="p-2 rounded-full bg-orange-100 dark:bg-orange-900/20">
              <AlertTriangle className="h-5 w-5 text-orange-600 dark:text-orange-400" />
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Manual Review</p>
              <p className="text-2xl font-bold">{stats.summary.manualReview}</p>
            </div>
          </div>
        </div>

        {/* Chart */}
        <div className="h-80">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" className="stroke-muted" />
              <XAxis
                dataKey="date"
                className="text-xs"
                tick={{ fill: "currentColor" }}
              />
              <YAxis className="text-xs" tick={{ fill: "currentColor" }} />
              <Tooltip
                contentStyle={{
                  backgroundColor: "hsl(var(--card))",
                  border: "1px solid hsl(var(--border))",
                  borderRadius: "0.5rem",
                }}
              />
              <Legend />
              <Line
                type="monotone"
                dataKey="Total Tickets"
                stroke="hsl(217 91% 60%)"
                strokeWidth={2}
                dot={{ r: 4 }}
              />
              <Line
                type="monotone"
                dataKey="Auto-Processed"
                stroke="hsl(142 76% 36%)"
                strokeWidth={2}
                dot={{ r: 4 }}
              />
              <Line
                type="monotone"
                dataKey="Manual Review"
                stroke="hsl(25 95% 53%)"
                strokeWidth={2}
                dot={{ r: 4 }}
              />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </CardContent>
    </Card>
  );
}
