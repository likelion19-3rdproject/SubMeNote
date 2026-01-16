export interface AccountRequest {
  bankName: string;
  accountNumber: string;
  holderName: string;
}

export interface UserResponseDto {
  id: number;
  email: string;
  nickname: string;
  roles: ('ROLE_USER' | 'ROLE_CREATOR' | 'ROLE_ADMIN')[];
}

