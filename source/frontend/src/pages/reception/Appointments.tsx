import { useEffect, useState } from "react";
import { api, apiErrorMessage } from "../../api/client";
import { Appointment, Department, Doctor, Patient } from "../../types";

interface SlotSuggestion { slot: string; estimatedWaitMinutes: number; explanation: string; }

export default function ReceptionAppointments() {
  const [patients, setPatients] = useState<Patient[]>([]);
  const [patientQuery, setPatientQuery] = useState("");
  const [patientId, setPatientId] = useState("");
  const [departments, setDepartments] = useState<Department[]>([]);
  const [departmentId, setDepartmentId] = useState("");
  const [doctors, setDoctors] = useState<Doctor[]>([]);
  const [doctorId, setDoctorId] = useState("");
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
  const [slots, setSlots] = useState<SlotSuggestion[]>([]);
  const [reason, setReason] = useState("");
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    api.get<Department[]>("/departments").then((r) => setDepartments(r.data));
  }, []);

  useEffect(() => {
    if (departmentId) api.get<Doctor[]>(`/doctors?departmentId=${departmentId}`).then((r) => setDoctors(r.data));
  }, [departmentId]);

  function searchPatients() {
    api.get(`/patients?q=${encodeURIComponent(patientQuery)}&size=10`).then((r) => setPatients(r.data.content ?? r.data));
  }

  function loadAppointments(pid: string) {
    api.get<Appointment[]>(`/appointments/patient/${pid}`).then((r) => setAppointments(r.data));
  }

  async function findSlots() {
    if (!doctorId) return;
    const r = await api.get<SlotSuggestion[]>(`/appointments/suggested-slots?doctorId=${doctorId}&date=${date}`);
    setSlots(r.data);
  }

  async function book(slot: string) {
    setError(null);
    setMessage(null);
    try {
      await api.post("/appointments", { patientId, doctorId, scheduledAt: slot, reason });
      setMessage("Appointment booked.");
      loadAppointments(patientId);
      setSlots([]);
    } catch (err) {
      setError(apiErrorMessage(err));
    }
  }

  async function checkIn(id: string) {
    await api.post(`/appointments/${id}/check-in`);
    loadAppointments(patientId);
  }

  async function cancel(id: string) {
    await api.post(`/appointments/${id}/cancel`);
    loadAppointments(patientId);
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Appointments</h1>

      <div className="card space-y-3">
        <div className="flex gap-2">
          <input className="input" placeholder="Search patient by name/email/phone" value={patientQuery}
                 onChange={(e) => setPatientQuery(e.target.value)} onKeyDown={(e) => e.key === "Enter" && searchPatients()} />
          <button className="btn btn-secondary" onClick={searchPatients}>Search</button>
        </div>
        {patients.length > 0 && (
          <select className="input" value={patientId} onChange={(e) => { setPatientId(e.target.value); loadAppointments(e.target.value); }}>
            <option value="">Select patient…</option>
            {patients.map((p) => <option key={p.id} value={p.id}>{p.fullName} — {p.email}</option>)}
          </select>
        )}
      </div>

      {patientId && (
        <>
          <div className="card grid gap-3 sm:grid-cols-4">
            <select className="input" value={departmentId} onChange={(e) => setDepartmentId(e.target.value)}>
              <option value="">Department…</option>
              {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
            <select className="input" value={doctorId} onChange={(e) => setDoctorId(e.target.value)}>
              <option value="">Doctor…</option>
              {doctors.map((d) => <option key={d.id} value={d.id}>{d.fullName}</option>)}
            </select>
            <input className="input" type="date" value={date} onChange={(e) => setDate(e.target.value)} />
            <input className="input" placeholder="Reason for visit" value={reason} onChange={(e) => setReason(e.target.value)} />
            <button className="btn btn-primary sm:col-span-4 sm:w-48" onClick={findSlots} disabled={!doctorId}>
              Find Available Slots
            </button>
          </div>

          {slots.length > 0 && (
            <div className="card">
              <h2 className="mb-3 font-medium text-slate-900">Recommended Slots</h2>
              <div className="grid gap-3 sm:grid-cols-2">
                {slots.map((s) => (
                  <div key={s.slot} className="rounded-md border border-slate-200 p-3">
                    <p className="font-medium">{new Date(s.slot).toLocaleString()}</p>
                    <p className="text-xs text-slate-500">Estimated waiting: {s.estimatedWaitMinutes} min — {s.explanation}</p>
                    <button className="btn btn-primary mt-2" onClick={() => book(s.slot)}>Book</button>
                  </div>
                ))}
              </div>
            </div>
          )}
          {error && <p className="text-sm text-red-600">{error}</p>}
          {message && <p className="text-sm text-emerald-600">{message}</p>}

          <div className="card">
            <h2 className="mb-3 font-medium text-slate-900">Patient's Appointments</h2>
            <table className="data-table">
              <thead><tr><th>When</th><th>Doctor</th><th>Status</th><th></th></tr></thead>
              <tbody>
                {appointments.map((a) => (
                  <tr key={a.id}>
                    <td>{new Date(a.scheduledAt).toLocaleString()}</td>
                    <td>{a.doctorName}</td>
                    <td>{a.status}</td>
                    <td className="space-x-2">
                      {(a.status === "BOOKED" || a.status === "CONFIRMED") && (
                        <>
                          <button className="btn btn-secondary" onClick={() => checkIn(a.id)}>Check In</button>
                          <button className="btn btn-danger" onClick={() => cancel(a.id)}>Cancel</button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}
