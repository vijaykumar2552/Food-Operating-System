// Provides typed FoodOS auth service operations for the React application.
// Requests use the shared API client to preserve Java endpoint and response contracts.

import { api, setToken } from "./api";
import type { LoginRequest, RegisterRequest, User } from "@/types";

type LoginResponse = User & { token: string };
type RegisterResponse = { message: string };

export const authService = {
  async login(body: LoginRequest) {
    const response = await api.post<LoginResponse>("/auth/login", body);
    setToken(response.token);
    const { token: _token, ...user } = response;
    return user;
  },
  async register(body: RegisterRequest) {
    return api.post<RegisterResponse>("/auth/register", body);
  },
  async logout() {
    try {
      await api.post("/auth/logout");
    } finally {
      setToken(null);
    }
  },
};
