import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const HOME_BY_ROLE: Record<string, string> = {
  ADMIN: "/admin",
  DOCTOR: "/doctor",
  NURSE: "/nurse",
  RECEPTIONIST: "/reception",
  PHARMACIST: "/pharmacy",
  LAB_TECHNICIAN: "/lab",
  PATIENT: "/patient",
};

/** Redirects "/" to the signed-in user's home page for their role. */
export default function RoleHome() {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  return <Navigate to={HOME_BY_ROLE[user.role] ?? "/login"} replace />;
}
