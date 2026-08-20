export type RegistrationStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface MyRegistration {
  registered: boolean;
  status: RegistrationStatus | null;
  companyName: string | null;
  ceoName: string | null;
  businessRegNo: string | null;
  phone: string | null;
  address: string | null;
  managerName: string | null;
  rejectReason: string | null;
}

export interface RegistrationSubmitRequest {
  companyName: string;
  ceoName: string;
  businessRegNo: string;
  phone: string;
  address: string;
  managerName: string;
}