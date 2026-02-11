"use client";

import Link from "next/link";
import { useApprovals } from "./hooks/useApprovals";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { CheckCircle, Clock, TrendingUp, ArrowRight } from "lucide-react";
import { Skeleton } from "@/components/ui/skeleton";
import { TicketStatsChart } from "./components/TicketStatsChart";

export default function DashboardPage() {
  const { data: approvals, isLoading } = useApprovals();

  const pendingCount = approvals?.length || 0;

  return (
    <div className="container mx-auto py-8 px-4 max-w-6xl">
      <div className="mb-8">
        <h1 className="text-4xl font-bold tracking-tight mb-2">
          HITL Dashboard
        </h1>
        <p className="text-muted-foreground text-lg">
          Human-in-the-Loop Defect Ticket Processing System
        </p>
      </div>

      <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3 mb-8">
        {isLoading ? (
          <>
            <Skeleton className="h-32" />
            <Skeleton className="h-32" />
            <Skeleton className="h-32" />
          </>
        ) : (
          <>
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">
                  Pending Approvals
                </CardTitle>
                <Clock className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{pendingCount}</div>
                <p className="text-xs text-muted-foreground mt-1">
                  Awaiting human review
                </p>
                {pendingCount > 0 && (
                  <Badge variant="destructive" className="mt-2">
                    Action Required
                  </Badge>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">
                  System Status
                </CardTitle>
                <CheckCircle className="h-4 w-4 text-green-600" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold text-green-600">Online</div>
                <p className="text-xs text-muted-foreground mt-1">
                  All systems operational
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">
                  Auto-refresh
                </CardTitle>
                <TrendingUp className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">30s</div>
                <p className="text-xs text-muted-foreground mt-1">
                  Real-time updates enabled
                </p>
              </CardContent>
            </Card>
          </>
        )}
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        <Card className="hover:shadow-lg transition-shadow">
          <CardHeader>
            <CardTitle>Approval Requests</CardTitle>
            <CardDescription>
              Review and approve AI-classified defect tickets at HITL gates
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Pending Reviews</p>
                <p className="text-3xl font-bold">{pendingCount}</p>
              </div>
              {pendingCount > 0 && (
                <Badge variant="destructive" className="text-lg px-3 py-1">
                  {pendingCount}
                </Badge>
              )}
            </div>
            <Link href="/approvals">
              <Button className="w-full flex items-center justify-between">
                Go to Approvals
                <ArrowRight className="h-4 w-4" />
              </Button>
            </Link>
          </CardContent>
        </Card>

        <Card className="hover:shadow-lg transition-shadow">
          <CardHeader>
            <CardTitle>Batch Progress</CardTitle>
            <CardDescription>
              Monitor real-time processing status of ticket batches
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <p className="text-sm text-muted-foreground mb-2">
                Track batch processing with detailed status breakdown
              </p>
              <ul className="text-sm space-y-1 text-muted-foreground">
                <li>• Real-time progress tracking</li>
                <li>• Status breakdown by ticket state</li>
                <li>• Auto-refresh every 5 seconds</li>
              </ul>
            </div>
            <Link href="/batches/new">
              <Button variant="outline" className="w-full flex items-center justify-between">
                View Batch Progress
                <ArrowRight className="h-4 w-4" />
              </Button>
            </Link>
          </CardContent>
        </Card>
      </div>

      <div className="mt-8">
        <TicketStatsChart days={7} />
      </div>

      <div className="mt-8 p-6 rounded-lg border bg-muted/50">
        <h3 className="text-lg font-semibold mb-2">About HITL System</h3>
        <p className="text-sm text-muted-foreground">
          The Human-in-the-Loop system enables human reviewers to validate AI-driven ticket 
          classification decisions at critical workflow gates. This ensures high-quality 
          classification while allowing the AI to learn from human decisions and improve over time.
        </p>
      </div>
    </div>
  );
}
