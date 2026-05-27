import { apiClient } from './client';

export interface TransferDto {
  fromUserId: number;
  fromUserDisplayName: string;
  toUserId: number;
  toUserDisplayName: string;
  amountCents: number;
  currencyCode: string;
}

export interface SettlementDto {
  id: number;
  groupId: number;
  fromUserId: number;
  fromUserDisplayName: string;
  toUserId: number;
  toUserDisplayName: string;
  amountCents: number;
  currencyCode: string;
  note: string | null;
  settledAt: string;
  createdAt: string;
}

export interface RecordSettlementPayload {
  fromUserId: number;
  toUserId: number;
  amount: string; // BigDecimal as string
  note?: string;
}

export async function suggestSettlements(groupId: number): Promise<TransferDto[]> {
  const { data } = await apiClient.get<TransferDto[]>(`/groups/${groupId}/settle-up`);
  return data;
}

export async function recordSettlement(
  groupId: number,
  payload: RecordSettlementPayload,
): Promise<SettlementDto> {
  const { data } = await apiClient.post<SettlementDto>(
    `/groups/${groupId}/settlements`,
    payload,
  );
  return data;
}

export async function listSettlements(groupId: number): Promise<SettlementDto[]> {
  const { data } = await apiClient.get<SettlementDto[]>(`/groups/${groupId}/settlements`);
  return data;
}
