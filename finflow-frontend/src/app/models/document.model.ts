export type DocumentStatus = 'PENDING' | 'VERIFIED' | 'REJECTED';
export type DocumentType =
  | 'PAN_CARD'
  | 'AADHAAR'
  | 'SALARY_SLIP'
  | 'BANK_STATEMENT'
  // | 'ITR'
  // | 'PHOTOGRAPH'
 ;

export interface DocumentRequest {
  applicationId: number;
  documentType: string;
}

export interface DocumentResponse {
  id: number;
  applicationId: number;
  userId: number;
  documentType: DocumentType;
  fileName: string;
  fileType: string;
  fileSize: number;
  status: DocumentStatus;
  uploadedAt?: string;
  verifiedAt?: string;
  allDocsVerified?: boolean;
}
