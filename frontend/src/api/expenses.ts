import { apiClient } from './client';

export type SplitType = 'EQUAL' | 'EXACT' | 'PERCENT';

export interface ShareInput {
  userId: number;
  amount?: string;
  percent?: string;
}

export interface CreateExpensePayload {
  description: string;
  amount: string; // BigDecimal as string with at most 2 decimal places
  payerId: number;
  splitType: SplitType;
  occurredAt?: string;
  shares: ShareInput[];
}

export interface ExpenseShareDto {
  userId: number;
  displayName: string;
  shareCents: number;
}

export interface ExpenseDto {
  id: number;
  groupId: number;
  payerId: number;
  payerDisplayName: string;
  description: string;
  amountCents: number;
  currencyCode: string;
  splitType: SplitType;
  occurredAt: string;
  createdAt: string;
  shares: ExpenseShareDto[];
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
}

export async function createExpense(
  groupId: number,
  payload: CreateExpensePayload,
): Promise<ExpenseDto> {
  const { data } = await apiClient.post<ExpenseDto>(`/groups/${groupId}/expenses`, payload);
  return data;
}

export async function listExpenses(
  groupId: number,
  page = 0,
  size = 20,
): Promise<PageResponse<ExpenseDto>> {
  const { data } = await apiClient.get<PageResponse<ExpenseDto>>(
    `/groups/${groupId}/expenses`,
    { params: { page, size } },
  );
  return data;
}
