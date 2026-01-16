export type ApplicationStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface CreatorApplicationResponse {
  id: number;
  nickname: string;
  status: ApplicationStatus;
  appliedAt: string;
}

export interface ApplicationProcessRequest {
  approved: boolean;
}
