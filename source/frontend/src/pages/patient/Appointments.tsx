import { useEffect, useState } from "react";
import { api, apiErrorMessage } from "../../api/client";
import ExplanationPanel from "../../components/ExplanationPanel";
import { Appointment, Department, DecisionExplanation } from "../../types";

interface SlotSuggestion { slot: string; estimatedWaitMinutes: number; explanation: string; }

export default function PatientAppointments() {
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [departmentId, setDepartmentId] = useState("");
  const [symptomHint, setSymptomHint] = useState("");
  const [recommendations, setRecommendations] = useState<DecisionExplanation[]>([]);
  const [doctorId, setDoctorId] = useState("");
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
  const [slots, setSlots] = useState<SlotSuggestion[]>([]);
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  function load() {
    api.get<Appointment[]>("/appointments/me").then((r) => setAppointments(r.data));
  }
  useEffect(load, []);
  useEffect(() => {
    api.get<Department[]>("/departments").then((r) => setDepartments(r.data));
  }, []);

  async function getRecommendations() {
    if (!departmentId) return;
    const r = await api.get<DecisionExplanation[]>(
      `/intelligence/doctor-recommendations?departmentId=${departmentId}&symptomHint=${encodeURIComponent(symptomHint)}`
    );
    setRecommendations(r.data);
  }

  async function findSlots(docId: string) {
    setDoctorId(docId);
    const r = await api.get<SlotSuggestion[]>(`/appointments/suggested-slots?doctorId=${docId}&date=${date}`);
    setSlots(r.data);
  }

  async function book(slot: string) {
    setError(null);
    setMessage(null);
    try {
      await api.post("/appointments", { doctorId, scheduledAt: slot, reason: symptomHint });
      setMessage("Appointment booked.");
      setSlots([]);
      load();
    } catch (err) {
      setError(apiErrorMessage(err));
    }
  }

  async function cancel(id: string) {
    await api.post(`/appointments/${id}/cancel`);
    load();
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Appointments</h1>

      <div className="card space-y-3">
        <h2 className="font-medium text-slate-900">Book a New Appointment</h2>
        <div className="grid gap-3 sm:grid-cols-3">
          <select className="input" value={departmentId} onChange={(e) => setDepartmentId(e.target.value)}>
            <option value="">Select department…</option>
            {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
          </select>
          <input className="input" placeholder="Describe your symptoms (optional)" value={symptomHint}
                 onChange={(e) => setSymptomHint(e.target.value)} />
          <button className="btn btn-primary" onClick={getRecommendations} disabled={!departmentId}>Find a Doctor</button>
        </div>
        <p className="text-xs text-slate-400">
          This suggests doctors based on specialization match, availability and workload — it does not diagnose your condition.
        </p>
      </div>

      {recommendations.length > 0 && (
        <div className="grid gap-4 lg:grid-cols-2">
          {recommendations.map((rec) => {
            const docId = rec.subject.match(/\(([^)]+)\)$/)?.[1] ?? "";
            return (
              <div key={docId}>
                <ExplanationPanel explanation={rec} />
                <div className="mt-2 flex items-center justify-between rounded-b-md bg-slate-50 px-4 py-2">
                  <input className="input w-40" type="date" value={date} onChange={(e) => setDate(e.target.value)} />
                  <button className="btn btn-primary" onClick={() => findSlots(docId)}>View Slots</button>
                </div>
              </div>
            );
          })}
        </div>
      )}

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
        <h2 className="mb-3 font-medium text-slate-900">My Appointments</h2>
        <table className="data-table">
          <thead><tr><th>When</th><th>Doctor</th><th>Status</th><th></th></tr></thead>
          <tbody>
            {appointments.map((a) => (
              <tr key={a.id}>
                <td>{new Date(a.scheduledAt).toLocaleString()}</td>
                <td>{a.doctorName}</td>
                <td>{a.status}</td>
                <td>
                  {(a.status === "BOOKED" || a.status === "CONFIRMED") && (
                    <button className="btn btn-danger" onClick={() => cancel(a.id)}>Cancel</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
