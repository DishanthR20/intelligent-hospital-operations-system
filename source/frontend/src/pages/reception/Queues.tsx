import { useEffect, useState } from "react";
import { api, apiErrorMessage } from "../../api/client";
import { Department, Patient, QueueEntryResponse } from "../../types";

export default function ReceptionQueues() {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [departmentId, setDepartmentId] = useState("");
  const [entries, setEntries] = useState<QueueEntryResponse[]>([]);
  const [patients, setPatients] = useState<Patient[]>([]);
  const [patientQuery, setPatientQuery] = useState("");
  const [patientId, setPatientId] = useState("");
  const [emergency, setEmergency] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.get<Department[]>("/departments").then((r) => {
      setDepartments(r.data);
      if (r.data.length) setDepartmentId(r.data[0].id);
    });
  }, []);

  function load(id: string) {
    api.get<QueueEntryResponse[]>(`/queues/department/${id}`).then((r) => setEntries(r.data));
  }
  useEffect(() => { if (departmentId) load(departmentId); }, [departmentId]);

  function searchPatients() {
    api.get(`/patients?q=${encodeURIComponent(patientQuery)}&size=10`).then((r) => setPatients(r.data.content ?? r.data));
  }

  async function addWalkIn() {
    setError(null);
    try {
      await api.post("/queues", { patientId, departmentId, emergency, clinicalRisk: emergency ? 5 : 0 });
      setPatientId("");
      load(departmentId);
    } catch (err) {
      setError(apiErrorMessage(err));
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Walk-in Queue</h1>

      <div className="card space-y-3">
        <select className="input max-w-xs" value={departmentId} onChange={(e) => setDepartmentId(e.target.value)}>
          {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
        </select>
        <div className="flex gap-2">
          <input className="input" placeholder="Search patient" value={patientQuery}
                 onChange={(e) => setPatientQuery(e.target.value)} onKeyDown={(e) => e.key === "Enter" && searchPatients()} />
          <button className="btn btn-secondary" onClick={searchPatients}>Search</button>
        </div>
        {patients.length > 0 && (
          <select className="input" value={patientId} onChange={(e) => setPatientId(e.target.value)}>
            <option value="">Select patient…</option>
            {patients.map((p) => <option key={p.id} value={p.id}>{p.fullName}</option>)}
          </select>
        )}
        <label className="flex items-center gap-2 text-sm">
          <input type="checkbox" checked={emergency} onChange={(e) => setEmergency(e.target.checked)} /> Emergency case
        </label>
        <button className="btn btn-primary" disabled={!patientId} onClick={addWalkIn}>Add to Queue</button>
        {error && <p className="text-sm text-red-600">{error}</p>}
      </div>

      <div className="card">
        <table className="data-table">
          <thead><tr><th>Position</th><th>Token</th><th>Patient</th><th>Status</th><th>Est. Wait</th></tr></thead>
          <tbody>
            {entries.map((e) => (
              <tr key={e.id}>
                <td>{e.position}</td><td>{e.tokenNumber}</td><td>{e.patientName}</td><td>{e.status}</td>
                <td>{e.estimatedWaitMinutes} min</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
