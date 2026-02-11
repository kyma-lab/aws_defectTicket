"use client";

import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { ApprovalRequestDto, ApprovalGate, TicketContext } from "@/app/api/types";
import { Clock, CheckCircle, XCircle, FileText, Brain, AlertCircle } from "lucide-react";
import { formatDistanceToNow } from "@/lib/date-utils";

interface ApprovalCardProps {
  approval: ApprovalRequestDto;
  onApprove: (approvalId: string) => void;
  onReject: (approvalId: string) => void;
}

export function ApprovalCard({ approval, onApprove, onReject }: ApprovalCardProps) {
  const context = parseContext(approval.context);
  const aiClassification = parseAIRecommendation(approval.aiRecommendation);
  
  // Extract ticket data - can be nested or at root level
  const ticket = context.defectTicket || {
    ticketId: context.ticketId || approval.ticketId,
    title: context.title,
    description: context.description,
    reporter: context.reporter,
    createdAt: context.createdAt,
    status: context.status,
    sourceReference: context.sourceReference,
  };
  
  // Use classification from context (priority) or from aiRecommendation
  const classification = context.classification || aiClassification;
  
  const gateLabel = approval.gate === ApprovalGate.CLASSIFICATION_REVIEW 
    ? "Classification Review" 
    : "Final Approval";

  return (
    <Card className="w-full hover:shadow-md transition-shadow">
      <CardHeader>
        <div className="flex items-start justify-between">
          <div className="space-y-1">
            <div className="flex items-center gap-2">
              <h3 className="text-lg font-semibold">Ticket: {approval.ticketId}</h3>
              <Badge variant="outline">{gateLabel}</Badge>
              <Badge variant="secondary" className="flex items-center gap-1">
                <Clock className="h-3 w-3" />
                Expires {formatDistanceToNow(approval.expiresAt)}
              </Badge>
            </div>
          </div>
        </div>
      </CardHeader>

      <CardContent className="space-y-4">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* LEFT SIDE: Original Ticket */}
          <div className="space-y-4 bg-gradient-to-br from-gray-50 to-slate-50 dark:from-gray-950/20 dark:to-slate-950/20 p-4 rounded-lg border">
            <div className="flex items-center gap-2 text-sm font-semibold text-foreground border-b border-gray-200 dark:border-gray-800 pb-2">
              <FileText className="h-4 w-4 text-gray-600 dark:text-gray-400" />
              Original Ticket
            </div>
            
            <div className="space-y-3">
              {/* Ticket ID */}
              <div className="border rounded-md overflow-hidden bg-white dark:bg-gray-900">
                <table className="w-full text-sm">
                  <tbody className="divide-y">
                    <tr className="hover:bg-gray-50 dark:hover:bg-gray-800">
                      <td className="px-3 py-2 font-medium text-muted-foreground w-1/3">Ticket ID</td>
                      <td className="px-3 py-2 font-mono text-xs">{ticket.ticketId}</td>
                    </tr>
                    
                    {ticket.sourceReference && (
                      <tr className="hover:bg-gray-50 dark:hover:bg-gray-800">
                        <td className="px-3 py-2 font-medium text-muted-foreground">Source Reference</td>
                        <td className="px-3 py-2 font-mono text-xs">{ticket.sourceReference}</td>
                      </tr>
                    )}
                    
                    {ticket.title && (
                      <tr className="hover:bg-gray-50 dark:hover:bg-gray-800">
                        <td className="px-3 py-2 font-medium text-muted-foreground align-top">Title</td>
                        <td className="px-3 py-2">{ticket.title}</td>
                      </tr>
                    )}
                    
                    {ticket.description && (
                      <tr className="hover:bg-gray-50 dark:hover:bg-gray-800">
                        <td className="px-3 py-2 font-medium text-muted-foreground align-top">Description</td>
                        <td className="px-3 py-2 whitespace-pre-wrap">{ticket.description}</td>
                      </tr>
                    )}
                    
                    {ticket.reporter && (
                      <tr className="hover:bg-gray-50 dark:hover:bg-gray-800">
                        <td className="px-3 py-2 font-medium text-muted-foreground">Reporter</td>
                        <td className="px-3 py-2">{ticket.reporter}</td>
                      </tr>
                    )}
                    
                    {ticket.status && (
                      <tr className="hover:bg-gray-50 dark:hover:bg-gray-800">
                        <td className="px-3 py-2 font-medium text-muted-foreground">Status</td>
                        <td className="px-3 py-2">
                          <Badge variant="outline">{ticket.status}</Badge>
                        </td>
                      </tr>
                    )}
                    
                    {ticket.createdAt && (
                      <tr className="hover:bg-gray-50 dark:hover:bg-gray-800">
                        <td className="px-3 py-2 font-medium text-muted-foreground">Created</td>
                        <td className="px-3 py-2">{formatDistanceToNow(ticket.createdAt)} ago</td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          {/* RIGHT SIDE: AI Recommendations */}
          <div className="space-y-4 bg-gradient-to-br from-blue-50 to-indigo-50 dark:from-blue-950/20 dark:to-indigo-950/20 p-4 rounded-lg border">
            <div className="flex items-center gap-2 text-sm font-semibold text-foreground border-b border-blue-200 dark:border-blue-800 pb-2">
              <Brain className="h-4 w-4 text-blue-600 dark:text-blue-400" />
              AI Classification
            </div>

            {classification ? (
              <div className="space-y-3">
                {/* Table for AI Classification */}
                <div className="border rounded-md overflow-hidden bg-white dark:bg-gray-900">
                  <table className="w-full text-sm">
                    <tbody className="divide-y">
                      <tr className="hover:bg-gray-50 dark:hover:bg-gray-800">
                        <td className="px-3 py-2 font-medium text-muted-foreground w-1/3">Category</td>
                        <td className="px-3 py-2">{classification.category || "N/A"}</td>
                      </tr>
                      
                      {classification.subcategory && (
                        <tr className="hover:bg-gray-50 dark:hover:bg-gray-800">
                          <td className="px-3 py-2 font-medium text-muted-foreground">Subcategory</td>
                          <td className="px-3 py-2">{classification.subcategory}</td>
                        </tr>
                      )}
                      
                      <tr className="hover:bg-gray-50 dark:hover:bg-gray-800">
                        <td className="px-3 py-2 font-medium text-muted-foreground">Severity</td>
                        <td className="px-3 py-2">
                          <Badge variant={getSeverityVariant(classification.severity)} className="font-semibold">
                            {classification.severity || "N/A"}
                          </Badge>
                        </td>
                      </tr>
                      
                      {classification.priority !== undefined && (
                        <tr className="hover:bg-gray-50 dark:hover:bg-gray-800">
                          <td className="px-3 py-2 font-medium text-muted-foreground">Priority</td>
                          <td className="px-3 py-2 font-medium">P{classification.priority}</td>
                        </tr>
                      )}
                      
                      {classification.confidenceScore !== undefined && (
                        <tr className="hover:bg-gray-50 dark:hover:bg-gray-800">
                          <td className="px-3 py-2 font-medium text-muted-foreground">Confidence</td>
                          <td className="px-3 py-2">
                            <div className="flex items-center gap-2">
                              <div className="flex-1 h-2 bg-gray-200 dark:bg-gray-700 rounded-full overflow-hidden">
                                <div
                                  className="h-full bg-blue-600 dark:bg-blue-400 transition-all"
                                  style={{ width: `${classification.confidenceScore * 100}%` }}
                                />
                              </div>
                              <span className="text-xs font-semibold w-12 text-right">
                                {(classification.confidenceScore * 100).toFixed(1)}%
                              </span>
                            </div>
                          </td>
                        </tr>
                      )}
                      
                      {classification.classificationSource && (
                        <tr className="hover:bg-gray-50 dark:hover:bg-gray-800">
                          <td className="px-3 py-2 font-medium text-muted-foreground">Source</td>
                          <td className="px-3 py-2">
                            <Badge variant="secondary">{classification.classificationSource}</Badge>
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>

                {classification.reasoning && (
                  <div className="pt-2">
                    <label className="text-xs font-medium text-muted-foreground uppercase tracking-wide">AI Reasoning</label>
                    <div className="mt-1 p-3 bg-white dark:bg-gray-900 rounded-md border">
                      <p className="text-sm italic text-muted-foreground whitespace-pre-wrap">
                        "{classification.reasoning}"
                      </p>
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <div className="text-center py-4 text-sm text-muted-foreground">
                No AI classification available
              </div>
            )}
          </div>
        </div>

        <div className="flex items-center gap-4 text-xs text-muted-foreground pt-2 border-t">
          <div className="flex items-center gap-1">
            <Clock className="h-3 w-3" />
            <span>Approval created {formatDistanceToNow(approval.createdAt)}</span>
          </div>
        </div>
      </CardContent>

      <CardFooter className="flex gap-2 justify-end bg-muted/30">
        <Button
          variant="outline"
          onClick={() => onReject(approval.approvalId)}
          className="flex items-center gap-2"
        >
          <XCircle className="h-4 w-4" />
          Reject
        </Button>
        <Button
          onClick={() => onApprove(approval.approvalId)}
          className="flex items-center gap-2"
        >
          <CheckCircle className="h-4 w-4" />
          Approve
        </Button>
      </CardFooter>
    </Card>
  );
}

function parseContext(contextStr: string): TicketContext {
  try {
    return JSON.parse(contextStr);
  } catch {
    return { ticketId: "unknown" };
  }
}

function parseAIRecommendation(recommendation: string): any {
  if (!recommendation) return null;
  try {
    return JSON.parse(recommendation);
  } catch {
    // If not valid JSON, return null
    return null;
  }
}

function getSeverityVariant(severity?: string): "default" | "destructive" | "secondary" {
  switch (severity) {
    case "CRITICAL":
    case "HIGH":
      return "destructive";
    case "MEDIUM":
      return "default";
    case "LOW":
      return "secondary";
    default:
      return "secondary";
  }
}
