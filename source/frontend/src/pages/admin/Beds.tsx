import { FormEvent, useEffect, useState } from "react";
import { api, apiErrorMessage } from "../../api/client";
import { Bed, Ward } from "../../types";

export default function Beds() {
  const [wards, setWards] = useState<Ward[]>([]);
  const [selectedWard, setSelectedWard] = useState<string>("");
  const [beds, setBeds] = useState<Bed[]>([]);
  const [wardForm, setWardForm] = useState({ name: "", icu: false, isolation: false, genderPolicy: "ANY" });
  const [bedNumber, setBedNumber] = useState("");
  const [error, setError] = useState<string | null>(null);

  function loadWards() {
    api.get<Ward[]>("/beds/wards").then((r) => {
      setWards(r.data);
      if (!selectedWard && r.data.length) setSelectedWard(r.data[0].id);
    });
  }
  useEffect(loadWards, []);

  useEffect(() => {
    if (selectedWard) api.get<Bed[]>(`/beds/wards/${selectedWard}`).then((r) => setBeds(r.data));
  }, [selectedWard]);

  async function createWard(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await api.post("/beds/wards", wardForm);
      setWardForm({ name: "", icu: false, isolation: false, genderPolicy: "ANY" });
      loadWards();
    } catch (err) {
      setError(apiErrorMessage(err));
    }
  }

  async function addBed(e: FormEvent) {
    e.preventDefault();
    if (!selectedWard) return;
    await api.post(`/beds/wards/${selectedWard}/beds`, { bedNumber });
    setBedNumber("");
    const r = await api.get<Bed[]>(`/beds/wards/${selectedWard}`);
    setBeds(r.data);
  }

  async function release(bedId: string) {
    await api.post(`/beds/${bedId}/release`);
    const r = await api.get<Bed[]>(`/beds/wards/${selectedWard}`);
    setBeds(r.data);
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Wards & Beds</h1>

      <form onSubmit={createWard} className="card grid gap-3 sm:grid-cols-5">
        <input className="input" placeholder="Ward name" required value={wardForm.name}
               onChange={(e) => setWardForm({ ...wardForm, name: e.target.value })} />
        <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={wardForm.icu}
               onChange={(e) => setWardForm({ ...wardForm, icu: e.target.checked })} /> ICU</label>
        <label className="flex items-center gap-2 text-sm"><input type="checkbox" checked={wardForm.isolation}
               onChange={(e) => setWardForm({ ...wardForm, isolation: e.target.checked })} /> Isolation</label>
        <select className="input" value={wardForm.genderPolicy}
                onChange={(e) => setWardForm({ ...wardForm, genderPolicy: e.target.value })}>
          <option value="ANY">Any gender</option>
          <option value="MALE">Male only</option>
          <option value="FEMALE">Female only</option>
        </select>
        <button className="btn btn-primary" type="submit">Add Ward</button>
        {error && <p className="text-sm text-red-600 sm:col-span-5">{error}</p>}
      </form>

      <div className="card">
        <div className="mb-3 flex items-center gap-3">
          <select className="input max-w-xs" value={selectedWard} onChange={(e) => setSelectedWard(e.target.value)}>
            {wards.map((w) => <option key={w.id} value={w.id}>{w.name} {w.icu ? "(ICU)" : ""}</option>)}
          </select>
          <form onSubmit={addBed} className="flex gap-2">
            <input className="input" placeholder="Bed number" required value={bedNumber}
                   onChange={(e) => setBedNumber(e.target.value)} />
            <button className="btn btn-secondary" type="submit">Add Bed</button>
          </form>
        </div>
        <table className="data-table">
          <thead><tr><th>Bed</th><th>Status</th><th></th></tr></thead>
          <tbody>
            {beds.map((b) => (
              <tr key={b.id}>
                <td>{b.bedNumber}</td>
                <td>{b.status}</td>
                <td>{b.status === "OCCUPIED" && <button className="btn btn-secondary" onClick={() => release(b.id)}>Release</button>}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
