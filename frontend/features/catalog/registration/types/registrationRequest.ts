import type { RegistrationStatus } from "./clientRegistration";

export interface RegistrationRequest {
  id: string;
  userEmail: string;
  companyName: string;
  ceoName: string | null;
  businessRegNo: string | null;
  phone: string | null;
  address: string | null;
  managerName: string | null;
  status: RegistrationStatus;
  rejectReason: string | null;
  createdAt: string;
  reviewedAt: string | null;
}

export interface ApproveResult {
  clientId: string;
  clientCode: string;
  companyName: string;
}