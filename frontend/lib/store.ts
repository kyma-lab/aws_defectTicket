import { create } from "zustand";

interface UIState {
  isDecisionDialogOpen: boolean;
  selectedApprovalId: string | null;
  openDecisionDialog: (approvalId: string) => void;
  closeDecisionDialog: () => void;
}

export const useUIStore = create<UIState>((set) => ({
  isDecisionDialogOpen: false,
  selectedApprovalId: null,
  openDecisionDialog: (approvalId) =>
    set({ isDecisionDialogOpen: true, selectedApprovalId: approvalId }),
  closeDecisionDialog: () =>
    set({ isDecisionDialogOpen: false, selectedApprovalId: null }),
}));
