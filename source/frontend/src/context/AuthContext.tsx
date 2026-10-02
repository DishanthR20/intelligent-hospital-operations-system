import { createContext, useContext, useState, ReactNode } from "react";
import { api } from "../api/client";

export type Role = "ADMIN" | "DOCTOR" | "NURSE" | "RECEPTIONIST" | "PHARMACIST" | "LAB_TECHNICIAN" | "PATIENT";

export interface CurrentUser {
  userId: string;
  fullName: string;
  email: string;
  role: Role;
}

interface AuthContextValue {
  user: CurrentUser | null;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(() => {
    const raw = localStorage.getItem("user");
    return raw ? (JSON.parse(raw) as CurrentUser) : null;
  });

  async function login(email: string, password: string) {
    const res = await api.post("/auth/login", { email, password });
    const { accessToken, refreshToken, userId, fullName, email: userEmail, role } = res.data;
    localStorage.setItem("accessToken", accessToken);
    localStorage.setItem("refreshToken", refreshToken);
    const currentUser: CurrentUser = { userId, fullName, email: userEmail, role };
    localStorage.setItem("user", JSON.stringify(currentUser));
    setUser(currentUser);
  }

  function logout() {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
    setUser(null);
  }

  return <AuthContext.Provider value={{ user, login, logout }}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
