export interface SubscribeResponseDto {
  id: number;
  creatorId: number;
  creatorNickname: string;
  status: 'ACTIVE' | 'CANCELED';
  type: 'FREE' | 'PAID';
  expiredAt: string | null;
}

export interface SubscribedCreatorResponseDto {
  subscriptionId: number;
  creatorId: number;
  creatorNickname: string;
  status: 'ACTIVE' | 'CANCELED';
  type: 'FREE' | 'PAID';
  expiredAt: string | null;
}

export interface SubscribeUpdateRequest {
  status: 'ACTIVE' | 'CANCELED';
}

