import { useEffect, useState } from "react";
import { api, apiErrorMessage } from "../../api/client";
import { LabOrder } from "../../types";

export default function LabOrders() {
  const [orders, setOrders] = useState<LabOrder[]>([]);
  const [resultForm, setResultForm] = useState<Record<string, { value: string; notes: string }>>({});
  const [error, setError] = useState<string | null>(null);

  function load() {
    api.get<LabOrder[]>("/laboratory/orders/pending").then((r) => setOrders(r.data));
  }
  useEffect(load, []);

  async function collectSample(id: string) {
    await api.post(`/laboratory/orders/${id}/collect-sample`);
    load();
  }
  async function startProcessing(id: string) {
    await api.post(`/laboratory/orders/${id}/start-processing`);
    load();
  }
  async function submitResult(id: string) {
    setError(null);
    const form = resultForm[id];
    if (!form?.value) return;
    try {
      await api.post(`/laboratory/orders/${id}/result`, { value: Number(form.value), notes: form.notes });
      load();
    } catch (err) {
      setError(apiErrorMessage(err));
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Lab Test Orders</h1>
      {error && <p className="text-sm text-red-600">{error}</p>}
      <div className="space-y-3">
        {orders.map((o) => (
          <div key={o.id} className="card">
            <div className="flex items-center justify-between">
              <div>
                <p className="font-medium">{o.test.name} — {o.patient.fullName}</p>
                <p className="text-xs text-slate-500">Ordered by Dr. {o.orderingDoctor.fullName} · Status: {o.status}</p>
                {o.sampleId && <p className="text-xs text-slate-400">Sample ID: {o.sampleId}</p>}
              </div>
              <div className="flex gap-2">
                {o.status === "ORDERED" && <button className="btn btn-secondary" onClick={() => collectSample(o.id)}>Collect Sample</button>}
                {o.status === "SAMPLE_COLLECTED" && <button className="btn btn-secondary" onClick={() => startProcessing(o.id)}>Start Processing</button>}
              </div>
            </div>
            {o.status === "PROCESSING" && (
              <div className="mt-3 flex gap-2">
                <input className="input" placeholder={`Result value (${o.test.unit ?? ""})`}
                       onChange={(e) => setResultForm({ ...resultForm, [o.id]: { ...resultForm[o.id], value: e.target.value } })} />
                <input className="input" placeholder="Notes"
                       onChange={(e) => setResultForm({ ...resultForm, [o.id]: { ...resultForm[o.id], notes: e.target.value } })} />
                <button className="btn btn-primary" onClick={() => submitResult(o.id)}>Submit Result</button>
              </div>
            )}
            {o.status === "COMPLETED" && (
              <p className={`mt-2 text-sm ${o.critical ? "font-semibold text-red-600" : "text-slate-600"}`}>
                Result: {o.resultValue} {o.test.unit} {o.critical ? "— CRITICAL, doctor notified" : ""}
              </p>
            )}
          </div>
        ))}
        {orders.length === 0 && <p className="text-sm text-slate-500">No pending lab orders.</p>}
      </div>
    </div>
  );
}
