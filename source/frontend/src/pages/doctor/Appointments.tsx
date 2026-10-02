import { useEffect, useState } from "react";
import { api } from "../../api/client";
import { Appointment, Doctor } from "../../types";

export default function DoctorAppointments() {
  const [appointments, setAppointments] = useState<Appointment[]>([]);

  useEffect(() => {
    api.get<Doctor>("/doctors/me").then((r) =>
      api.get<Appointment[]>(`/appointments/doctor/${r.data.id}`).then((a) => setAppointments(a.data))
    );
  }, []);

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">My Appointments</h1>
      <div className="card">
        <table className="data-table">
          <thead><tr><th>Date & Time</th><th>Patient</th><th>Reason</th><th>Status</th></tr></thead>
          <tbody>
            {appointments.map((a) => (
              <tr key={a.id}>
                <td>{new Date(a.scheduledAt).toLocaleString()}</td>
                <td>{a.patientName}</td>
                <td>{a.reason ?? "—"}</td>
                <td>{a.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
