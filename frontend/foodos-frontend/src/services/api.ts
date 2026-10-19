// Provides typed FoodOS api operations for the React application.
// Requests use the shared API client to preserve Java endpoint and response contracts.

import { mockRequest } from "@/mock/mockAdapter";

export const API_BASE_URL: string = import.meta.env.VITE_API_BASE_URL || "/api";

// Mock mode is opt-in for preview/development only.
export const USE_MOCK = import.meta.env.VITE_USE_MOCK === "true";

const TOKEN_KEY = "foodos_token";

export class ApiError extends Error {
  status: number;
  constructor(message: string, status: number) {
    super(message);
    this.status = status;
  }
}

export function setToken(token: string | null) {
  if (typeof window === "undefined") return;
  if (token) localStorage.setItem(TOKEN_KEY, token);
  else localStorage.removeItem(TOKEN_KEY);
}

function getToken(): string | null {
  if (typeof window === "undefined") return null;
  return localStorage.getItem(TOKEN_KEY);
}

type Method = "GET" | "POST" | "PUT" | "DELETE";

async function request<T>(method: Method, path: string, body?: unknown): Promise<T> {
  if (USE_MOCK) return mockRequest<T>(method, path, body);

  const headers: Record<string, string> = { Accept: "application/json" };
  if (body !== undefined) headers["Content-Type"] = "application/json";
  const token = getToken();
  if (token) headers.Authorization = `Bearer ${token}`;

  let res: Response;
  try {
    res = await fetch(`${API_BASE_URL}${path}`, {
      method,
      headers,
      credentials: "include",
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError("Cannot reach the FoodOS server. Is the Java backend running on port 8080?", 0);
  }

  const text = await res.text();
  let data: unknown = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = text;
    }
  }

  if (!res.ok) {
    const envelope = data && typeof data === "object" ? data as Record<string, unknown> : null;
    const payload = envelope?.data && typeof envelope.data === "object"
      ? envelope.data as Record<string, unknown>
      : envelope;
    const msg =
      (payload && ("message" in payload || "error" in payload)
        ? String(payload.message ?? payload.error)
        : typeof data === "string" && data) || defaultMessage(res.status);
    throw new ApiError(msg, res.status);
  }
  if (data && typeof data === "object" && "success" in data && "data" in data) {
    return (data as { data: T }).data;
  }
  return data as T;
}

function defaultMessage(status: number) {
  if (status === 401) return "Please log in to continue.";
  if (status === 403) return "You don't have permission to do that.";
  if (status === 404) return "Not found.";
  if (status === 409) return "This already exists.";
  if (status >= 500) return "Server error. Please try again.";
  return "Request failed.";
}

export const api = {
  get: <T>(path: string) => request<T>("GET", path),
  post: <T>(path: string, body?: unknown) => request<T>("POST", path, body),
  put: <T>(path: string, body?: unknown) => request<T>("PUT", path, body),
  delete: <T>(path: string) => request<T>("DELETE", path),
};

export function errorMessage(e: unknown): string {
  return e instanceof Error ? e.message : "Something went wrong.";
}
