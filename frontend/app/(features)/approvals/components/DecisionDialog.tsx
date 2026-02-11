"use client";

import { useState } from "react";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useDecision } from "@/app/hooks/useApprovals";
import { Loader2 } from "lucide-react";

interface DecisionDialogProps {
  approvalId: string | null;
  isApprove: boolean;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function DecisionDialog({
  approvalId,
  isApprove,
  open,
  onOpenChange,
}: DecisionDialogProps) {
  const [email, setEmail] = useState("");
  const [comments, setComments] = useState("");
  const [emailError, setEmailError] = useState("");
  
  const decision = useDecision();

  const validateEmail = (email: string): boolean => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!email) {
      setEmailError("Email is required");
      return false;
    }
    if (!emailRegex.test(email)) {
      setEmailError("Please enter a valid email address");
      return false;
    }
    setEmailError("");
    return true;
  };

  const handleSubmit = () => {
    if (!approvalId || !validateEmail(email)) {
      return;
    }

    decision.mutate(
      {
        approvalId,
        approved: isApprove,
        reviewerEmail: email,
        comments: comments || undefined,
      },
      {
        onSuccess: () => {
          setEmail("");
          setComments("");
          setEmailError("");
          onOpenChange(false);
        },
      }
    );
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>
            {isApprove ? "Approve Request" : "Reject Request"}
          </DialogTitle>
          <DialogDescription>
            {isApprove
              ? "Confirm your approval decision. This will resume the workflow."
              : "Provide a reason for rejecting this request."}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-4 py-4">
          <div className="space-y-2">
            <Label htmlFor="email">Reviewer Email *</Label>
            <Input
              id="email"
              type="email"
              placeholder="reviewer@example.com"
              value={email}
              onChange={(e) => {
                setEmail(e.target.value);
                setEmailError("");
              }}
              onBlur={() => validateEmail(email)}
              className={emailError ? "border-destructive" : ""}
            />
            {emailError && (
              <p className="text-sm text-destructive">{emailError}</p>
            )}
          </div>

          <div className="space-y-2">
            <Label htmlFor="comments">
              Comments {!isApprove && "*"}
            </Label>
            <Textarea
              id="comments"
              placeholder={
                isApprove
                  ? "Optional: Add any comments..."
                  : "Explain why you're rejecting this request..."
              }
              value={comments}
              onChange={(e) => setComments(e.target.value)}
              rows={4}
            />
          </div>
        </div>

        <DialogFooter>
          <Button
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={decision.isPending}
          >
            Cancel
          </Button>
          <Button
            onClick={handleSubmit}
            disabled={decision.isPending || !email}
            variant={isApprove ? "default" : "destructive"}
          >
            {decision.isPending && (
              <Loader2 className="mr-2 h-4 w-4 animate-spin" />
            )}
            {isApprove ? "Confirm Approval" : "Confirm Rejection"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
