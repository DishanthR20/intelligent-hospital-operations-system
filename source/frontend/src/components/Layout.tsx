import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

interface NavItem {
  to: string;
  label: string;
}

const NAV_BY_ROLE: Record<string, NavItem[]> = {
  ADMIN: [
    { to: "/admin", label: "Command Center" },
    { to: "/admin/departments", label: "Departments" },
    { to: "/admin/doctors", label: "Doctors" },
    { to: "/admin/patients", label: "Patients" },
    { to: "/admin/staff", label: "Staff" },
    { to: "/admin/beds", label: "Beds & Wards" },
    { to: "/admin/pharmacy", label: "Pharmacy" },
    { to: "/admin/laboratory", label: "Laboratory" },
    { to: "/admin/decisions", label: "Decision Replay" },
    { to: "/admin/audit", label: "Audit Log" },
    { to: "/admin/configuration", label: "Configuration" },
  ],
  DOCTOR: [
    { to: "/doctor", label: "Dashboard" },
    { to: "/doctor/queue", label: "My Queue" },
    { to: "/doctor/appointments", label: "Appointments" },
  ],
  NURSE: [
    { to: "/nurse", label: "Queues" },
    { to: "/nurse/beds", label: "Beds" },
  ],
  RECEPTIONIST: [
    { to: "/reception", label: "Register Patient" },
    { to: "/reception/appointments", label: "Appointments" },
    { to: "/reception/queues", label: "Queues" },
    { to: "/reception/billing", label: "Billing" },
  ],
  PHARMACIST: [
    { to: "/pharmacy", label: "Prescriptions" },
    { to: "/pharmacy/inventory", label: "Inventory" },
  ],
  LAB_TECHNICIAN: [
    { to: "/lab", label: "Test Orders" },
  ],
  PATIENT: [
    { to: "/patient", label: "Dashboard" },
    { to: "/patient/appointments", label: "Appointments" },
    { to: "/patient/records", label: "Medical Records" },
    { to: "/patient/bills", label: "Bills" },
    { to: "/patient/feedback", label: "Feedback" },
  ],
};

export default function Layout() {
  const { user, logout } = useAuth();
  const items = user ? NAV_BY_ROLE[user.role] ?? [] : [];

  return (
    <div className="flex min-h-screen">
      <aside className="w-64 shrink-0 border-r border-slate-200 bg-white">
        <div className="border-b border-slate-200 px-4 py-4">
          <p className="text-lg font-semibold text-brand-700">MediSphere AI</p>
          <p className="text-xs text-slate-400">Hospital Orchestration Platform</p>
        </div>
        <nav className="p-2">
          {items.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end
              className={({ isActive }) =>
                `block rounded-md px-3 py-2 text-sm font-medium ${
                  isActive ? "bg-brand-50 text-brand-700" : "text-slate-600 hover:bg-slate-50"
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="flex flex-1 flex-col">
        <header className="flex items-center justify-between border-b border-slate-200 bg-white px-6 py-3">
          <p className="text-sm text-slate-500">Signed in as <span className="font-medium text-slate-800">{user?.fullName}</span> ({user?.role})</p>
          <button className="btn btn-secondary" onClick={logout}>Log out</button>
        </header>
        <main className="flex-1 overflow-y-auto p-6">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
