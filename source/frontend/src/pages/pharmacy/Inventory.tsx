import { FormEvent, useEffect, useState } from "react";
import { api, apiErrorMessage } from "../../api/client";
import { Medicine } from "../../types";

export default function PharmacyInventory() {
  const [medicines, setMedicines] = useState<Medicine[]>([]);
  const [selected, setSelected] = useState("");
  const [batch, setBatch] = useState({ batchNumber: "", expiryDate: "", supplier: "", quantity: 100 });
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  function load() {
    api.get("/pharmacy/medicines?size=100").then((r) => setMedicines(r.data.content ?? r.data));
  }
  useEffect(load, []);

  async function addBatch(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setMessage(null);
    try {
      await api.post(`/pharmacy/medicines/${selected}/batches`, batch);
      setMessage("Batch added.");
      setBatch({ batchNumber: "", expiryDate: "", supplier: "", quantity: 100 });
    } catch (err) {
      setError(apiErrorMessage(err));
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Inventory — Add Stock Batch</h1>
      <form onSubmit={addBatch} className="card grid gap-3 sm:grid-cols-5">
        <select className="input sm:col-span-2" value={selected} onChange={(e) => setSelected(e.target.value)} required>
          <option value="">Select medicine…</option>
          {medicines.map((m) => <option key={m.id} value={m.id}>{m.name}</option>)}
        </select>
        <input className="input" placeholder="Batch number" required value={batch.batchNumber}
               onChange={(e) => setBatch({ ...batch, batchNumber: e.target.value })} />
        <input className="input" type="date" required value={batch.expiryDate}
               onChange={(e) => setBatch({ ...batch, expiryDate: e.target.value })} />
        <input className="input" type="number" min={1} placeholder="Quantity" value={batch.quantity}
               onChange={(e) => setBatch({ ...batch, quantity: Number(e.target.value) })} />
        <input className="input sm:col-span-2" placeholder="Supplier" value={batch.supplier}
               onChange={(e) => setBatch({ ...batch, supplier: e.target.value })} />
        <button className="btn btn-primary sm:col-span-3" type="submit">Add Batch</button>
        {error && <p className="text-sm text-red-600 sm:col-span-5">{error}</p>}
        {message && <p className="text-sm text-emerald-600 sm:col-span-5">{message}</p>}
      </form>

      <div className="card">
        <table className="data-table">
          <thead><tr><th>Medicine</th><th>Unit</th><th>Price</th><th>Reorder Threshold</th></tr></thead>
          <tbody>
            {medicines.map((m) => (
              <tr key={m.id}>
                <td>{m.name}</td><td>{m.unit}</td><td>${m.pricePerUnit.toFixed(2)}</td><td>{m.reorderThreshold}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
