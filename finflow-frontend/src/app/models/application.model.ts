export type ApplicationStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'DOCS_PENDING'
  | 'DOCS_VERIFIED'
  | 'UNDER_REVIEW'
  | 'APPROVED'
  | 'REJECTED'
  | 'CLOSED';

export interface ApplicationRequest {
  loanAmount: number;
  purpose: string;
  tenureMonths: number;
  firstName?: string;
  lastName?: string;
  phone?: string;
  address?: string;
  employerName?: string;
  employmentType?: string;
  monthlyIncome?: number;
}

export interface ApplicationResponse extends ApplicationRequest {
  id: number;
  userId: number;
  status: ApplicationStatus;
  createdAt?: string;
  updatedAt?: string;
  submittedAt?: string;
}

export interface StatusResponse {
  applicationId: number;
  currentStatus: ApplicationStatus;
  lastUpdated: string;
}

export interface SubmitResponse {
  message: string;
  applicationId: number;
  status: ApplicationStatus;
  submittedAt: string;
}
