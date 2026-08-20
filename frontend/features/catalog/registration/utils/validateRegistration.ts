import type { RegistrationSubmitRequest } from "../types/clientRegistration";

const BUSINESS_REG_NO_PATTERN = /^\d{3}-\d{2}-\d{5}$/;
const PHONE_PATTERN = /^\d{2,3}-\d{3,4}-\d{4}$/;

export interface RegistrationFormErrors {
  companyName?: string;
  businessRegNo?: string;
  phone?: string;
}

/** 필드별 에러만 채워서 반환 */
export function validateRegistration(values: RegistrationSubmitRequest): RegistrationFormErrors {
  const errors: RegistrationFormErrors = {};

  if (!values.companyName.trim()) {
    errors.companyName = "회사명을 입력해 주세요.";
  }

  if (!values.businessRegNo.trim()) {
    errors.businessRegNo = "사업자등록번호를 입력해 주세요.";
  } else if (!BUSINESS_REG_NO_PATTERN.test(values.businessRegNo)) {
    errors.businessRegNo = "000-00-00000 형식으로 입력해 주세요.";
  }

  if (values.phone && !PHONE_PATTERN.test(values.phone)) {
    errors.phone = "010-0000-0000 또는 02-0000-0000 형식으로 입력해 주세요.";
  }

  return errors;
}

export function hasRegistrationErrors(errors: RegistrationFormErrors): boolean {
  return Object.keys(errors).length > 0;
}