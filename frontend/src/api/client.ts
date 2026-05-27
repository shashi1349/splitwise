import axios, { AxiosError, type AxiosInstance } from 'axios';
import { useAuthStore } from '@/store/authStore';

const baseURL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export const apiClient: AxiosInstance = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
});

apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    // Drop credentials on 401 so the user is redirected to /login by RequireAuth.
    if (error.response?.status === 401) {
      const path = window.location.pathname;
      if (path !== '/login' && path !== '/register') {
        useAuthStore.getState().clear();
      }
    }
    return Promise.reject(error);
  },
);

/** RFC 7807 problem-detail body returned by the backend on errors. */
export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  errors?: Record<string, string>;
}

export function getProblemDetail(err: unknown): ProblemDetail {
  if (axios.isAxiosError(err) && err.response?.data) {
    return err.response.data as ProblemDetail;
  }
  return { detail: err instanceof Error ? err.message : 'Unknown error' };
}
