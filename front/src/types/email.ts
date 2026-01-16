export interface SendEmailRequest {
  email: string;
}

export interface VerifyEmailRequest {
  email: string;
  authCode: string;
}

export interface VerifyEmailResponse {
  verified: boolean;
}

