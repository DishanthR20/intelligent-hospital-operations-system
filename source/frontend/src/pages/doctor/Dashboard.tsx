import { useEffect, useState } from "react";
import { api } from "../../api/client";
import MetricCard from "../../components/MetricCard";
import StatusBadge from "../../components/StatusBadge";
import { Appointment, Doctor } from "../../types";

interface Workload { activePatients: number; waitingInQueue: number; level: "LOW" | "MODERATE" | "HIGH" | "CRITICAL"; }

export default function DoctorDashboard() {
  const [doctor, setDoctor] = useState<Doctor | null>(null);
  const [workload, setWorkload] = useState<Workload | null>(null);
  const [appointments, setAppointments] = useState<Appointment[]>([]);

  useEffect(() => {
    api.get<Doctor>("/doctors/me").then((r) => {
      setDoctor(r.data);
      api.get<Workload>(`/intelligence/workload/${r.data.id}`).then((w) => setWorkload(w.data));
      api.get<Appointment[]>(`/appointments/doctor/${r.data.id}`).then((a) => setAppointments(a.data));
    });
  }, []);

  if (!doctor) return <p className="text-slate-500">Loading…</p>;

  const today = appointments.filter((a) => new Date(a.scheduledAt).toDateString() === new Date().toDateString());

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-slate-900">Welcome, Dr. {doctor.fullName}</h1>
        <p className="text-sm text-slate-500">{doctor.specialization} · {doctor.departmentName}</p>
      </div>

      <div className="grid grid-cols-3 gap-4">
        <MetricCard label="Active Patients Today" value={workload?.activePatients ?? "—"} />
        <MetricCard label="Waiting in Queue" value={workload?.waitingInQueue ?? "—"} />
        <div className="card">
          <p className="text-sm text-slate-500">Current Workload</p>
          <div className="mt-2">{workload ? <StatusBadge level={workload.level} /> : "—"}</div>
        </div>
      </div>

      <div className="card">
        <h2 className="mb-3 font-medium text-slate-900">Today's Appointments</h2>
        <table className="data-table">
          <thead><tr><th>Time</th><th>Patient</th><th>Reason</th><th>Status</th></tr></thead>
          <tbody>
            {today.map((a) => (
              <tr key={a.id}>
                <td>{new Date(a.scheduledAt).toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}</td>
                <td>{a.patientName}</td>
                <td>{a.reason ?? "—"}</td>
                <td>{a.status}</td>
              </tr>
            ))}
            {today.length === 0 && <tr><td colSpan={4} className="text-slate-400">No appointments today.</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
}
