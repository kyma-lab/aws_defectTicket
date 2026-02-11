// Enums
export enum ApprovalGate {
  CLASSIFICATION_REVIEW = "CLASSIFICATION_REVIEW",
  FINAL_APPROVAL = "FINAL_APPROVAL",
}

export enum ApprovalStatus {
  PENDING = "PENDING",
  APPROVED = "APPROVED",
  REJECTED = "REJECTED",
  TIMED_OUT = "TIMED_OUT",
  ESCALATED = "ESCALATED",
}

export enum TicketStatus {
  NEW = "NEW",
  VALIDATED = "VALIDATED",
  CLASSIFIED = "CLASSIFIED",
  PENDING_CLASSIFICATION_APPROVAL = "PENDING_CLASSIFICATION_APPROVAL",
  CLASSIFICATION_APPROVED = "CLASSIFICATION_APPROVED",
  CLASSIFICATION_REJECTED = "CLASSIFICATION_REJECTED",
  ASSIGNED = "ASSIGNED",
  IN_PROGRESS = "IN_PROGRESS",
  PENDING_FINAL_APPROVAL = "PENDING_FINAL_APPROVAL",
  RESOLVED = "RESOLVED",
  CLOSED = "CLOSED",
  ARCHIVED = "ARCHIVED",
}

export enum Severity {
  CRITICAL = "CRITICAL",
  HIGH = "HIGH",
  MEDIUM = "MEDIUM",
  LOW = "LOW",
  TRIVIAL = "TRIVIAL",
}

// DTOs
export interface ApprovalRequestDto {
  approvalId: string;
  ticketId: string;
  gate: ApprovalGate;
  status: ApprovalStatus;
  context: string; // JSON string
  createdAt: string;
  expiresAt: string;
  aiRecommendation: string;
}

export interface ApprovalDecisionDto {
  approvalId: string;
  approved: boolean;
  reviewerEmail: string;
  comments?: string;
}

export interface BatchProgressDto {
  batchId: string;
  totalTickets: number;
  processedTickets: number;
  pendingTickets: number;
  statusBreakdown: Record<string, number>;
  progressPercentage: number;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  validationErrors?: Record<string, string>;
}

// Ticket Statistics
export interface DailyStats {
  date: string;
  totalTickets: number;
  autoProcessed: number;
  manualReview: number;
}

export interface StatsSummary {
  totalTickets: number;
  autoProcessed: number;
  manualReview: number;
  autoProcessedPercentage: number;
}

export interface TicketStatsDto {
  dailyStats: DailyStats[];
  summary: StatsSummary;
}

// Parsed context types
export interface TicketContext {
  ticketId: string;
  title?: string;
  description?: string;
  reporter?: string;
  createdAt?: string;
  status?: TicketStatus;
  classification?: {
    category?: string;
    subcategory?: string;
    severity?: Severity;
    priority?: number;
    confidenceScore?: number;
    reasoning?: string;
    classificationSource?: string;
  };
  defectTicket?: {
    ticketId: string;
    title: string;
    description: string;
    reporter?: string;
    createdAt?: string;
    status?: TicketStatus;
    sourceReference?: string;
  };
  sourceReference?: string;
}
