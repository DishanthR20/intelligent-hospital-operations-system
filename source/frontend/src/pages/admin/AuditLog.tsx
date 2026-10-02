import { useEffect, useState } from "react";
import { api } from "../../api/client";

interface AuditRow {
  id: string;
  actorUserId: string;
  actorRole: string;
  action: string;
  entityType: string;
  entityId: string;
  success: boolean;
  reason?: string;
  createdAt: string;
}

export default function AuditLog() {
  const [rows, setRows] = useState<AuditRow[]>([]);

  useEffect(() => {
    api.get("/audit?size=50").then((r) => setRows(r.data.content ?? r.data));
  }, []);

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Audit Explorer</h1>
      <div className="card">
        <table className="data-table">
          <thead>
            <tr><th>When</th><th>Actor Role</th><th>Action</th><th>Entity</th><th>Result</th></tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.id}>
                <td>{new Date(r.createdAt).toLocaleString()}</td>
                <td>{r.actorRole}</td>
                <td>{r.action}</td>
                <td>{r.entityType} #{r.entityId?.slice(0, 8)}</td>
                <td className={r.success ? "text-emerald-600" : "text-red-600"}>
                  {r.success ? "Success" : `Failed${r.reason ? `: ${r.reason}` : ""}`}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
