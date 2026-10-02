import { useEffect, useState } from "react";
import { api, apiErrorMessage } from "../../api/client";
import { PendingPrescriptionItem } from "../../types";

export default function PharmacyPrescriptions() {
  const [items, setItems] = useState<PendingPrescriptionItem[]>([]);
  const [error, setError] = useState<string | null>(null);

  function load() {
    api.get<PendingPrescriptionItem[]>("/pharmacy/prescriptions/pending-items").then((r) => setItems(r.data));
  }
  useEffect(load, []);

  async function dispense(itemId: string) {
    setError(null);
    try {
      await api.post(`/pharmacy/prescriptions/items/${itemId}/dispense`);
      load();
    } catch (err) {
      setError(apiErrorMessage(err));
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Pending Prescriptions</h1>
      {error && <p className="text-sm text-red-600">{error}</p>}
      <div className="card">
        <table className="data-table">
          <thead><tr><th>Patient</th><th>Medicine</th><th>Dosage</th><th>Qty</th><th></th></tr></thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id}>
                <td>{item.patientName}</td>
                <td>{item.medicineName}</td>
                <td>{item.dosageInstructions}</td>
                <td>{item.quantity}</td>
                <td><button className="btn btn-primary" onClick={() => dispense(item.id)}>Dispense</button></td>
              </tr>
            ))}
            {items.length === 0 && <tr><td colSpan={5} className="text-slate-400">No pending prescriptions.</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
}
