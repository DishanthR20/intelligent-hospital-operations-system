import { useEffect, useState } from "react";
import { api, apiErrorMessage } from "../../api/client";
import { Ward } from "../../types";

export default function NurseBeds() {
  const [wards, setWards] = useState<Ward[]>([]);
  const [form, setForm] = useState({ patientId: "", needsIcu: false, needsIsolation: false, genderPolicy: "" });
  const [result, setResult] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    api.get<Ward[]>("/beds/wards").then((r) => setWards(r.data));
  }, []);

  async function allocate() {
    setError(null);
    setResult(null);
    try {
      const res = await api.post("/beds/allocate", { ...form, genderPolicy: form.genderPolicy || null });
      setResult(`Bed ${res.data.bedNumber} allocated.`);
    } catch (err) {
      setError(apiErrorMessage(err));
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Smart Bed Allocation</h1>
      <p className="text-sm text-slate-500">Wards available: {wards.map((w) => w.name).join(", ") || "none configured"}</p>

      <div className="card max-w-md space-y-3">
        <div>
          <label className="label">Patient ID</label>
          <input className="input" value={form.patientId} onChange={(e) => setForm({ ...form, patientId: e.target.value })} />
        </div>
        <label className="flex items-center gap-2 text-sm">
          <input type="checkbox" checked={form.needsIcu} onChange={(e) => setForm({ ...form, needsIcu: e.target.checked })} />
          Needs ICU
        </label>
        <label className="flex items-center gap-2 text-sm">
          <input type="checkbox" checked={form.needsIsolation} onChange={(e) => setForm({ ...form, needsIsolation: e.target.checked })} />
          Needs isolation
        </label>
        <div>
          <label className="label">Gender (for ward policy)</label>
          <select className="input" value={form.genderPolicy} onChange={(e) => setForm({ ...form, genderPolicy: e.target.value })}>
            <option value="">Not specified</option>
            <option value="MALE">Male</option>
            <option value="FEMALE">Female</option>
          </select>
        </div>
        <button className="btn btn-primary" onClick={allocate} disabled={!form.patientId}>Allocate Bed</button>
        {result && <p className="text-sm text-emerald-600">{result}</p>}
        {error && <p className="text-sm text-red-600">{error}</p>}
      </div>
    </div>
  );
}
