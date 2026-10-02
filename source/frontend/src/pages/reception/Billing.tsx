import { useState } from "react";
import { api, apiErrorMessage } from "../../api/client";
import { Invoice, Patient } from "../../types";

const CATEGORIES = ["CONSULTATION", "LABORATORY", "PHARMACY", "PROCEDURE", "BED", "OTHER"];

interface Line { description: string; category: string; unitPrice: number; quantity: number; }

export default function ReceptionBilling() {
  const [patientQuery, setPatientQuery] = useState("");
  const [patients, setPatients] = useState<Patient[]>([]);
  const [patientId, setPatientId] = useState("");
  const [lines, setLines] = useState<Line[]>([{ description: "", category: "CONSULTATION", unitPrice: 0, quantity: 1 }]);
  const [invoices, setInvoices] = useState<Invoice[]>([]);
  const [error, setError] = useState<string | null>(null);

  function searchPatients() {
    api.get(`/patients?q=${encodeURIComponent(patientQuery)}&size=10`).then((r) => setPatients(r.data.content ?? r.data));
  }

  function loadInvoices(pid: string) {
    api.get<Invoice[]>(`/billing/invoices/patient/${pid}`).then((r) => setInvoices(r.data));
  }

  async function createInvoice() {
    setError(null);
    try {
      await api.post("/billing/invoices", { patientId, items: lines.filter((l) => l.description) });
      setLines([{ description: "", category: "CONSULTATION", unitPrice: 0, quantity: 1 }]);
      loadInvoices(patientId);
    } catch (err) {
      setError(apiErrorMessage(err));
    }
  }

  async function pay(invoiceId: string, amount: number) {
    await api.post(`/billing/invoices/${invoiceId}/payments`, { amount, method: "CASH" });
    loadInvoices(patientId);
  }

  function total() {
    return lines.reduce((sum, l) => sum + l.unitPrice * l.quantity, 0);
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Billing</h1>

      <div className="card space-y-3">
        <div className="flex gap-2">
          <input className="input" placeholder="Search patient" value={patientQuery}
                 onChange={(e) => setPatientQuery(e.target.value)} onKeyDown={(e) => e.key === "Enter" && searchPatients()} />
          <button className="btn btn-secondary" onClick={searchPatients}>Search</button>
        </div>
        {patients.length > 0 && (
          <select className="input" value={patientId} onChange={(e) => { setPatientId(e.target.value); loadInvoices(e.target.value); }}>
            <option value="">Select patient…</option>
            {patients.map((p) => <option key={p.id} value={p.id}>{p.fullName}</option>)}
          </select>
        )}
      </div>

      {patientId && (
        <>
          <div className="card space-y-3">
            <h2 className="font-medium text-slate-900">New Invoice</h2>
            {lines.map((line, i) => (
              <div key={i} className="grid grid-cols-6 gap-2">
                <input className="input col-span-2" placeholder="Description" value={line.description}
                       onChange={(e) => setLines(lines.map((l, j) => j === i ? { ...l, description: e.target.value } : l))} />
                <select className="input" value={line.category}
                        onChange={(e) => setLines(lines.map((l, j) => j === i ? { ...l, category: e.target.value } : l))}>
                  {CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
                </select>
                <input className="input" type="number" step="0.01" placeholder="Unit price" value={line.unitPrice}
                       onChange={(e) => setLines(lines.map((l, j) => j === i ? { ...l, unitPrice: Number(e.target.value) } : l))} />
                <input className="input" type="number" min={1} placeholder="Qty" value={line.quantity}
                       onChange={(e) => setLines(lines.map((l, j) => j === i ? { ...l, quantity: Number(e.target.value) } : l))} />
              </div>
            ))}
            <div className="flex items-center justify-between">
              <button className="btn btn-secondary" onClick={() => setLines([...lines, { description: "", category: "OTHER", unitPrice: 0, quantity: 1 }])}>
                + Add Line
              </button>
              <p className="font-medium">Total: ${total().toFixed(2)}</p>
            </div>
            {error && <p className="text-sm text-red-600">{error}</p>}
            <button className="btn btn-primary" onClick={createInvoice}>Create Invoice</button>
          </div>

          <div className="card">
            <h2 className="mb-3 font-medium text-slate-900">Invoices</h2>
            <table className="data-table">
              <thead><tr><th>Total</th><th>Paid</th><th>Status</th><th></th></tr></thead>
              <tbody>
                {invoices.map((inv) => {
                  const totalAmt = inv.items.reduce((s, i) => s + i.unitPrice * i.quantity, 0);
                  const paid = inv.payments.reduce((s, p) => s + p.amount, 0);
                  return (
                    <tr key={inv.id}>
                      <td>${totalAmt.toFixed(2)}</td>
                      <td>${paid.toFixed(2)}</td>
                      <td>{inv.status}</td>
                      <td>
                        {paid < totalAmt && (
                          <button className="btn btn-secondary" onClick={() => pay(inv.id, totalAmt - paid)}>
                            Collect Balance
                          </button>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}
