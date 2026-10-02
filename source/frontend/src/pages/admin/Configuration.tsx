import { useEffect, useState } from "react";
import { api } from "../../api/client";

interface ConfigRow { key: string; value: string; description?: string; }

export default function Configuration() {
  const [rows, setRows] = useState<ConfigRow[]>([]);
  const [edited, setEdited] = useState<Record<string, string>>({});
  const [saved, setSaved] = useState(false);

  function load() {
    api.get<ConfigRow[]>("/configuration").then((r) => setRows(r.data));
  }
  useEffect(load, []);

  async function save() {
    if (Object.keys(edited).length === 0) return;
    await api.put("/configuration", edited);
    setEdited({});
    setSaved(true);
    load();
    setTimeout(() => setSaved(false), 2000);
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Hospital Configuration Center</h1>
      <p className="text-sm text-slate-500">
        Every intelligence engine (queue priority, workload, pharmacy reorder, patient experience…) reads its
        thresholds and weights from here — nothing is hardcoded.
      </p>

      <div className="card">
        <table className="data-table">
          <thead><tr><th>Key</th><th>Description</th><th>Value</th></tr></thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.key}>
                <td className="font-mono text-xs">{r.key}</td>
                <td className="text-xs text-slate-500">{r.description}</td>
                <td>
                  <input
                    className="input"
                    defaultValue={r.value}
                    onChange={(e) => setEdited((prev) => ({ ...prev, [r.key]: e.target.value }))}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <button className="btn btn-primary" onClick={save}>Save Changes</button>
      {saved && <span className="ml-3 text-sm text-emerald-600">Saved.</span>}
    </div>
  );
}
