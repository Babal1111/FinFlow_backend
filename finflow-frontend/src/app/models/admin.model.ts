export interface DecisionRequest {
  decision: string;
  remarks?: string;
  approvedAmount?: number;
  tenureMonths?: number;
  interestRate?: number;
}

export interface DecisionResponse {
  id: number;
  applicationId: number;
  adminId: number;
  decision: string;
  remarks?: string;
  approvedAmount?: number;
  tenureMonths?: number;
  interestRate?: number;
  decidedAt: string;
}

export interface ReportsResponse {
  totalDecisions: number;
  approved: number;
  rejected: number;
  approvalRate: string;
}
