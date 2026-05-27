import { apiClient } from './client';
import type { AuthUser } from '@/store/authStore';

export interface AuthResponse {
  token: string;
  expiresInSeconds: number;
  user: AuthUser;
}

export interface RegisterPayload {
  email: string;
  displayName: string;
  password: string;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export async function registerUser(payload: RegisterPayload): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/auth/register', payload);
  return data;
}

export async function loginUser(payload: LoginPayload): Promise<AuthResponse> {
  const { data } = await apiClient.post<AuthResponse>('/auth/login', payload);
  return data;
}
