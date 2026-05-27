import { apiClient } from './client';

export interface BalanceDto {
  userId: number;
  displayName: string;
  netCents: number;
  currencyCode: string;
}

export async function listBalances(groupId: number): Promise<BalanceDto[]> {
  const { data } = await apiClient.get<BalanceDto[]>(`/groups/${groupId}/balances`);
  return data;
}
