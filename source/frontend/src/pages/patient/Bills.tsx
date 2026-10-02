import { useEffect, useState } from "react";
import { api } from "../../api/client";
import { Invoice } from "../../types";

export default function PatientBills() {
  const [invoices, setInvoices] = useState<Invoice[]>([]);

  useEffect(() => {
    api.get<Invoice[]>("/billing/invoices/me").then((r) => setInvoices(r.data));
  }, []);

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">My Bills</h1>
      <div className="space-y-3">
        {invoices.map((inv) => {
          const total = inv.items.reduce((s, i) => s + i.unitPrice * i.quantity, 0);
          const paid = inv.payments.reduce((s, p) => s + p.amount, 0);
          return (
            <div key={inv.id} className="card">
              <div className="flex justify-between">
                <p className="font-medium">Invoice — {inv.status}</p>
                <p>${total.toFixed(2)} (paid ${paid.toFixed(2)})</p>
              </div>
              <table className="data-table mt-2">
                <thead><tr><th>Description</th><th>Category</th><th>Qty</th><th>Amount</th></tr></thead>
                <tbody>
                  {inv.items.map((item, i) => (
                    <tr key={i}>
                      <td>{item.description}</td><td>{item.category}</td><td>{item.quantity}</td>
                      <td>${(item.unitPrice * item.quantity).toFixed(2)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          );
        })}
        {invoices.length === 0 && <p className="text-sm text-slate-500">No bills yet.</p>}
      </div>
    </div>
  );
}
