import { useEffect, useState } from "react";
import { api } from "../../api/client";
import ExplanationPanel from "../../components/ExplanationPanel";
import { Department, QueueEntryResponse } from "../../types";

export default function NurseQueues() {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [departmentId, setDepartmentId] = useState("");
  const [entries, setEntries] = useState<QueueEntryResponse[]>([]);

  useEffect(() => {
    api.get<Department[]>("/departments").then((r) => {
      setDepartments(r.data);
      if (r.data.length) setDepartmentId(r.data[0].id);
    });
  }, []);

  function load(id: string) {
    api.get<QueueEntryResponse[]>(`/queues/department/${id}`).then((r) => setEntries(r.data));
  }

  useEffect(() => {
    if (departmentId) load(departmentId);
  }, [departmentId]);

  async function setRisk(entryId: string, risk: number) {
    await api.post(`/queues/${entryId}/clinical-risk`, { clinicalRisk: risk });
    load(departmentId);
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Department Queues</h1>
      <select className="input max-w-xs" value={departmentId} onChange={(e) => setDepartmentId(e.target.value)}>
        {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
      </select>

      <div className="space-y-3">
        {entries.map((e) => (
          <div key={e.id}>
            <ExplanationPanel explanation={e.explanation} score={e.priorityScore} />
            <div className="mt-2 flex items-center justify-between rounded-b-md bg-slate-50 px-4 py-2 text-sm">
              <span>Token {e.tokenNumber} · Position {e.position} · Est. wait {e.estimatedWaitMinutes} min</span>
              <label className="flex items-center gap-2">
                Triage risk (0-10):
                <input type="number" min={0} max={10} defaultValue={e.clinicalRisk} className="input w-16"
                       onBlur={(ev) => setRisk(e.id, Number(ev.target.value))} />
              </label>
            </div>
          </div>
        ))}
        {entries.length === 0 && <p className="text-sm text-slate-500">No patients currently waiting in this department.</p>}
      </div>
    </div>
  );
}
