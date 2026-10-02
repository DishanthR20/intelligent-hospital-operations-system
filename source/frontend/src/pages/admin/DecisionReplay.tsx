import { useEffect, useState } from "react";
import { api } from "../../api/client";
import ExplanationPanel from "../../components/ExplanationPanel";

interface DecisionFactorRow { name: string; points: number; reason: string; }
interface Decision {
  id: string;
  type: string;
  patientId?: string;
  result: string;
  score: number;
  factors: DecisionFactorRow[];
  createdAt: string;
}

export default function DecisionReplay() {
  const [decisions, setDecisions] = useState<Decision[]>([]);
  const [type, setType] = useState("");

  function load() {
    const params = new URLSearchParams({ size: "25" });
    if (type) params.set("type", type);
    api.get(`/decisions?${params}`).then((r) => setDecisions(r.data.content ?? r.data));
  }
  useEffect(load, [type]);

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Decision Replay</h1>
      <p className="text-sm text-slate-500">Every explainable recommendation the platform has made, with its full reasoning.</p>

      <select className="input max-w-xs" value={type} onChange={(e) => setType(e.target.value)}>
        <option value="">All decision types</option>
        <option value="DOCTOR_RECOMMENDATION">Doctor Recommendation</option>
        <option value="QUEUE_PRIORITY">Queue Priority</option>
        <option value="BED_ALLOCATION">Bed Allocation</option>
      </select>

      <div className="space-y-3">
        {decisions.map((d) => (
          <div key={d.id}>
            <p className="mb-1 text-xs uppercase tracking-wide text-slate-400">
              {d.type} · {new Date(d.createdAt).toLocaleString()}
            </p>
            <ExplanationPanel explanation={{ subject: d.result, factors: d.factors }} score={d.score} />
          </div>
        ))}
        {decisions.length === 0 && <p className="text-sm text-slate-500">No decisions recorded yet.</p>}
      </div>
    </div>
  );
}
