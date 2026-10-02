import { useEffect, useState } from "react";
import { api } from "../../api/client";
import { Patient } from "../../types";

export default function Patients() {
  const [patients, setPatients] = useState<Patient[]>([]);
  const [q, setQ] = useState("");

  function load() {
    api.get(`/patients?q=${encodeURIComponent(q)}&size=50`).then((r) => setPatients(r.data.content ?? r.data));
  }

  useEffect(load, []);

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Patients</h1>
      <div className="card flex gap-3">
        <input className="input" placeholder="Search by name, email or phone" value={q}
               onChange={(e) => setQ(e.target.value)} onKeyDown={(e) => e.key === "Enter" && load()} />
        <button className="btn btn-primary" onClick={load}>Search</button>
      </div>
      <div className="card">
        <table className="data-table">
          <thead>
            <tr><th>Name</th><th>Age</th><th>Gender</th><th>Blood Group</th><th>Phone</th><th>Allergies</th></tr>
          </thead>
          <tbody>
            {patients.map((p) => (
              <tr key={p.id}>
                <td>{p.fullName}</td>
                <td>{p.age}</td>
                <td>{p.gender}</td>
                <td>{p.bloodGroup ?? "—"}</td>
                <td>{p.phone}</td>
                <td>{p.allergies?.length ? p.allergies.join(", ") : "None recorded"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
