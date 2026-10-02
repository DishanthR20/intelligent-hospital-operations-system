import { FormEvent, useEffect, useState } from "react";
import { api, apiErrorMessage } from "../../api/client";
import { Department } from "../../types";

export default function Departments() {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [name, setName] = useState("");
  const [capacity, setCapacity] = useState(20);
  const [description, setDescription] = useState("");
  const [error, setError] = useState<string | null>(null);

  function load() {
    api.get<Department[]>("/departments").then((r) => setDepartments(r.data));
  }

  useEffect(load, []);

  async function onCreate(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      await api.post("/departments", { name, capacity, description });
      setName("");
      setDescription("");
      load();
    } catch (err) {
      setError(apiErrorMessage(err));
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Departments</h1>

      <form onSubmit={onCreate} className="card grid gap-3 sm:grid-cols-4">
        <input className="input" placeholder="Name" value={name} onChange={(e) => setName(e.target.value)} required />
        <input className="input" type="number" min={1} placeholder="Capacity" value={capacity}
               onChange={(e) => setCapacity(Number(e.target.value))} required />
        <input className="input sm:col-span-2" placeholder="Description" value={description}
               onChange={(e) => setDescription(e.target.value)} />
        <button className="btn btn-primary sm:col-span-4 sm:w-40" type="submit">Add Department</button>
        {error && <p className="text-sm text-red-600 sm:col-span-4">{error}</p>}
      </form>

      <div className="card">
        <table className="data-table">
          <thead>
            <tr><th>Name</th><th>Capacity</th><th>Default Consult (min)</th><th>Status</th></tr>
          </thead>
          <tbody>
            {departments.map((d) => (
              <tr key={d.id}>
                <td>{d.name}</td>
                <td>{d.capacity}</td>
                <td>{d.defaultConsultationMinutes}</td>
                <td>{d.active ? "Active" : "Inactive"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
