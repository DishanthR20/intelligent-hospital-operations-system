import { useEffect, useState } from "react";
import { api } from "../../api/client";
import MetricCard from "../../components/MetricCard";
import { Appointment, Notification, Patient } from "../../types";

export default function PatientDashboard() {
  const [patient, setPatient] = useState<Patient | null>(null);
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [notifications, setNotifications] = useState<Notification[]>([]);

  useEffect(() => {
    api.get<Patient>("/patients/me").then((r) => setPatient(r.data));
    api.get<Appointment[]>("/appointments/me").then((r) => setAppointments(r.data));
    api.get<Notification[]>("/notifications").then((r) => setNotifications(r.data.slice(0, 5)));
  }, []);

  if (!patient) return <p className="text-slate-500">Loading…</p>;

  const upcoming = appointments.find((a) => ["BOOKED", "CONFIRMED"].includes(a.status) && new Date(a.scheduledAt) > new Date());

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Welcome, {patient.fullName}</h1>

      <div className="grid grid-cols-3 gap-4">
        <MetricCard label="Blood Group" value={patient.bloodGroup ?? "—"} />
        <MetricCard label="Age" value={patient.age} />
        <MetricCard label="Allergies" value={patient.allergies.length ? patient.allergies.join(", ") : "None recorded"} />
      </div>

      <div className="card">
        <h2 className="mb-2 font-medium text-slate-900">Upcoming Appointment</h2>
        {upcoming ? (
          <p className="text-sm text-slate-700">
            Dr. {upcoming.doctorName} ({upcoming.departmentName}) — {new Date(upcoming.scheduledAt).toLocaleString()}
          </p>
        ) : (
          <p className="text-sm text-slate-500">No upcoming appointments.</p>
        )}
      </div>

      <div className="card">
        <h2 className="mb-2 font-medium text-slate-900">Recent Notifications</h2>
        {notifications.length === 0 ? (
          <p className="text-sm text-slate-500">No notifications.</p>
        ) : (
          <ul className="space-y-1 text-sm text-slate-600">
            {notifications.map((n) => <li key={n.id}>• {n.message}</li>)}
          </ul>
        )}
      </div>
    </div>
  );
}
