import axios, { AxiosError } from "axios";
import type {
  ApprovalRequestDto,
  ApprovalDecisionDto,
  BatchProgressDto,
  TicketStatsDto,
  ErrorResponse,
} from "./types";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8060/api/v1";

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 30000,
});

// Error interceptor
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ErrorResponse>) => {
    if (error.response?.data) {
      throw error.response.data;
    }
    throw {
      timestamp: new Date().toISOString(),
      status: error.response?.status || 500,
      error: "Network Error",
      message: error.message || "An unexpected error occurred",
    } as ErrorResponse;
  }
);

// API functions
export const approvalsApi = {
  getPending: async (): Promise<ApprovalRequestDto[]> => {
    const response = await apiClient.get<ApprovalRequestDto[]>("/approvals/pending");
    return response.data;
  },

  decide: async (decision: ApprovalDecisionDto): Promise<void> => {
    await apiClient.post("/approvals/decide", decision);
  },
};

export const batchesApi = {
  getProgress: async (batchId: string): Promise<BatchProgressDto> => {
    const response = await apiClient.get<BatchProgressDto>(`/batches/${batchId}/progress`);
    return response.data;
  },

  getStats: async (days: number = 7): Promise<TicketStatsDto> => {
    const response = await apiClient.get<TicketStatsDto>(`/batches/stats`, {
      params: { days }
    });
    return response.data;
  },
};

export default apiClient;
