import { FormEvent, useEffect, useState } from "react";
import { api, apiErrorMessage } from "../../api/client";
import { Medicine, MedicineDemandReport } from "../../types";

const STATUS_CLASS: Record<string, string> = {
  OK: "badge-low",
  REORDER_RECOMMENDED: "badge-moderate",
  URGENT_REORDER: "badge-critical",
};

export default function Pharmacy() {
  const [report, setReport] = useState<MedicineDemandReport[]>([]);
  const [form, setForm] = useState({ name: "", genericName: "", unit: "tablet", pricePerUnit: 1, reorderThreshold: 20 });
  const [error, setError] = useState<string | null>(null);

  function load() {
    api.get<MedicineDemandReport[]>("/pharmacy/medicines/demand-report").then((r) => setReport(r.data));
  }
  useEffect(load, []);

  async function onCreate(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await api.post<Medicine>("/pharmacy/medicines", form);
      setForm({ name: "", genericName: "", unit: "tablet", pricePerUnit: 1, reorderThreshold: 20 });
      load();
    } catch (err) {
      setError(apiErrorMessage(err));
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Pharmacy Inventory</h1>

      <form onSubmit={onCreate} className="card grid gap-3 sm:grid-cols-5">
        <input className="input" placeholder="Medicine name" required value={form.name}
               onChange={(e) => setForm({ ...form, name: e.target.value })} />
        <input className="input" placeholder="Generic name" value={form.genericName}
               onChange={(e) => setForm({ ...form, genericName: e.target.value })} />
        <input className="input" placeholder="Unit (tablet, vial…)" value={form.unit}
               onChange={(e) => setForm({ ...form, unit: e.target.value })} />
        <input className="input" type="number" step="0.01" placeholder="Price/unit" value={form.pricePerUnit}
               onChange={(e) => setForm({ ...form, pricePerUnit: Number(e.target.value) })} />
        <input className="input" type="number" placeholder="Reorder threshold" value={form.reorderThreshold}
               onChange={(e) => setForm({ ...form, reorderThreshold: Number(e.target.value) })} />
        <button className="btn btn-primary sm:col-span-5 sm:w-40" type="submit">Add Medicine</button>
        {error && <p className="text-sm text-red-600 sm:col-span-5">{error}</p>}
      </form>

      <div className="card">
        <h2 className="mb-3 font-medium text-slate-900">Medicine Demand Report</h2>
        <table className="data-table">
          <thead>
            <tr><th>Medicine</th><th>Stock</th><th>Avg Daily Usage</th><th>Est. Remaining Days</th><th>Status</th><th>Explanation</th></tr>
          </thead>
          <tbody>
            {report.map((r) => (
              <tr key={r.medicineId}>
                <td>{r.medicineName}</td>
                <td>{r.currentStock}</td>
                <td>{r.averageDailyUsage.toFixed(1)}</td>
                <td>{r.estimatedRemainingDays != null ? r.estimatedRemainingDays.toFixed(1) : "—"}</td>
                <td><span className={`badge ${STATUS_CLASS[r.status]}`}>{r.status.replace("_", " ")}</span></td>
                <td className="text-xs text-slate-500">{r.explanation}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
