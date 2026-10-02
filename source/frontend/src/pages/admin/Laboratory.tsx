import { FormEvent, useEffect, useState } from "react";
import { api, apiErrorMessage } from "../../api/client";
import { LabTestCatalog } from "../../types";

export default function Laboratory() {
  const [tests, setTests] = useState<LabTestCatalog[]>([]);
  const [form, setForm] = useState({ name: "", price: 10, unit: "", normalRangeLow: "", normalRangeHigh: "", criticalLow: "", criticalHigh: "" });
  const [error, setError] = useState<string | null>(null);

  function load() {
    api.get<LabTestCatalog[]>("/laboratory/catalog").then((r) => setTests(r.data));
  }
  useEffect(load, []);

  async function onCreate(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await api.post("/laboratory/catalog", {
        name: form.name, price: form.price, unit: form.unit || null,
        normalRangeLow: form.normalRangeLow ? Number(form.normalRangeLow) : null,
        normalRangeHigh: form.normalRangeHigh ? Number(form.normalRangeHigh) : null,
        criticalLow: form.criticalLow ? Number(form.criticalLow) : null,
        criticalHigh: form.criticalHigh ? Number(form.criticalHigh) : null,
      });
      setForm({ name: "", price: 10, unit: "", normalRangeLow: "", normalRangeHigh: "", criticalLow: "", criticalHigh: "" });
      load();
    } catch (err) {
      setError(apiErrorMessage(err));
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Laboratory Test Catalog</h1>
      <p className="text-sm text-slate-500">
        Critical range thresholds configured here drive the Critical Result Alert workflow — a result outside this
        range notifies the ordering doctor automatically.
      </p>

      <form onSubmit={onCreate} className="card grid gap-3 sm:grid-cols-4">
        <input className="input" placeholder="Test name" required value={form.name}
               onChange={(e) => setForm({ ...form, name: e.target.value })} />
        <input className="input" type="number" step="0.01" placeholder="Price" value={form.price}
               onChange={(e) => setForm({ ...form, price: Number(e.target.value) })} />
        <input className="input" placeholder="Unit" value={form.unit} onChange={(e) => setForm({ ...form, unit: e.target.value })} />
        <div />
        <input className="input" placeholder="Normal low" value={form.normalRangeLow}
               onChange={(e) => setForm({ ...form, normalRangeLow: e.target.value })} />
        <input className="input" placeholder="Normal high" value={form.normalRangeHigh}
               onChange={(e) => setForm({ ...form, normalRangeHigh: e.target.value })} />
        <input className="input" placeholder="Critical low" value={form.criticalLow}
               onChange={(e) => setForm({ ...form, criticalLow: e.target.value })} />
        <input className="input" placeholder="Critical high" value={form.criticalHigh}
               onChange={(e) => setForm({ ...form, criticalHigh: e.target.value })} />
        <button className="btn btn-primary sm:col-span-4 sm:w-40" type="submit">Add Test</button>
        {error && <p className="text-sm text-red-600 sm:col-span-4">{error}</p>}
      </form>

      <div className="card">
        <table className="data-table">
          <thead><tr><th>Test</th><th>Price</th><th>Normal Range</th><th>Critical Range</th></tr></thead>
          <tbody>
            {tests.map((t) => (
              <tr key={t.id}>
                <td>{t.name}</td>
                <td>${t.price.toFixed(2)}</td>
                <td>{t.normalRangeLow ?? "—"} – {t.normalRangeHigh ?? "—"} {t.unit}</td>
                <td>{t.criticalLow ?? "—"} – {t.criticalHigh ?? "—"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
