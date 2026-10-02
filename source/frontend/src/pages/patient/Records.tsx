import { useEffect, useState } from "react";
import { api } from "../../api/client";
import { LabOrder, Prescription } from "../../types";

interface Consultation {
  id: string;
  doctorName: string;
  diagnosis?: string;
  notes: string;
  followUpPlan?: string;
  createdAt: string;
}

export default function PatientRecords() {
  const [consultations, setConsultations] = useState<Consultation[]>([]);
  const [prescriptions, setPrescriptions] = useState<Prescription[]>([]);
  const [labOrders, setLabOrders] = useState<LabOrder[]>([]);

  useEffect(() => {
    api.get<Consultation[]>("/consultations/me").then((r) => setConsultations(r.data));
    api.get<Prescription[]>("/pharmacy/prescriptions/me").then((r) => setPrescriptions(r.data));
    api.get<LabOrder[]>("/laboratory/orders/me").then((r) => setLabOrders(r.data));
  }, []);

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Medical Records</h1>

      <div className="card">
        <h2 className="mb-3 font-medium text-slate-900">Consultation History</h2>
        <div className="space-y-3">
          {consultations.map((c) => (
            <div key={c.id} className="rounded-md border border-slate-100 p-3">
              <p className="text-sm font-medium">Dr. {c.doctorName} — {new Date(c.createdAt).toLocaleDateString()}</p>
              <p className="text-sm text-slate-600">{c.diagnosis ?? "No diagnosis recorded"}</p>
              <p className="text-xs text-slate-500">{c.notes}</p>
              {c.followUpPlan && <p className="text-xs text-brand-600">Follow-up: {c.followUpPlan}</p>}
            </div>
          ))}
          {consultations.length === 0 && <p className="text-sm text-slate-500">No consultations yet.</p>}
        </div>
      </div>

      <div className="card">
        <h2 className="mb-3 font-medium text-slate-900">Prescriptions</h2>
        <div className="space-y-3">
          {prescriptions.map((p) => (
            <div key={p.id} className="rounded-md border border-slate-100 p-3">
              <p className="text-sm font-medium">Prescribed by Dr. {p.doctorName}</p>
              <ul className="mt-1 text-sm text-slate-600">
                {p.items.map((i) => (
                  <li key={i.id}>
                    {i.medicineName} — {i.dosageInstructions} (x{i.quantity}) {i.dispensed ? "✓ Dispensed" : "Pending"}
                  </li>
                ))}
              </ul>
            </div>
          ))}
          {prescriptions.length === 0 && <p className="text-sm text-slate-500">No prescriptions yet.</p>}
        </div>
      </div>

      <div className="card">
        <h2 className="mb-3 font-medium text-slate-900">Lab Reports</h2>
        <table className="data-table">
          <thead><tr><th>Test</th><th>Status</th><th>Result</th></tr></thead>
          <tbody>
            {labOrders.map((o) => (
              <tr key={o.id}>
                <td>{o.test.name}</td>
                <td>{o.status}</td>
                <td className={o.critical ? "font-semibold text-red-600" : ""}>
                  {o.resultValue != null ? `${o.resultValue} ${o.test.unit ?? ""}` : "—"}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
