import { apiClient } from './client';

export type MemberRole = 'OWNER' | 'MEMBER';

export interface GroupSummary {
  id: number;
  name: string;
  currencyCode: string;
  myRole: MemberRole;
  createdAt: string;
  memberCount: number;
}

export interface MemberDto {
  userId: number;
  email: string;
  displayName: string;
  role: MemberRole;
  joinedAt: string;
}

export interface GroupDetail {
  id: number;
  name: string;
  currencyCode: string;
  createdById: number;
  createdAt: string;
  members: MemberDto[];
}

export interface CreateGroupPayload {
  name: string;
  currencyCode?: string;
}

export async function listGroups(): Promise<GroupSummary[]> {
  const { data } = await apiClient.get<GroupSummary[]>('/groups');
  return data;
}

export async function createGroup(payload: CreateGroupPayload): Promise<GroupDetail> {
  const { data } = await apiClient.post<GroupDetail>('/groups', payload);
  return data;
}

export async function getGroup(groupId: number): Promise<GroupDetail> {
  const { data } = await apiClient.get<GroupDetail>(`/groups/${groupId}`);
  return data;
}

export async function listMembers(groupId: number): Promise<MemberDto[]> {
  const { data } = await apiClient.get<MemberDto[]>(`/groups/${groupId}/members`);
  return data;
}

export async function inviteMember(groupId: number, email: string): Promise<MemberDto> {
  const { data } = await apiClient.post<MemberDto>(`/groups/${groupId}/members`, { email });
  return data;
}
