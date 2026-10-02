import { FormEvent, useEffect, useState } from "react";
import { api, apiErrorMessage } from "../../api/client";
import { Department } from "../../types";

const ROLES = ["DOCTOR", "NURSE", "RECEPTIONIST", "PHARMACIST", "LAB_TECHNICIAN"];

export default function Staff() {
  const [departments, setDepartments] = useState<Department[]>([]);
  const [form, setForm] = useState({
    fullName: "", email: "", password: "", phone: "", role: "DOCTOR",
    departmentId: "", specialization: "", qualification: "", experienceYears: 0,
  });
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    api.get<Department[]>("/departments").then((r) => setDepartments(r.data));
  }, []);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    try {
      await api.post("/users/staff", form);
      setSuccess(`${form.fullName} onboarded as ${form.role}.`);
      setForm({ ...form, fullName: "", email: "", password: "" });
    } catch (err) {
      setError(apiErrorMessage(err));
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Staff Onboarding</h1>
      <form onSubmit={onSubmit} className="card grid max-w-2xl gap-3">
        <div className="grid grid-cols-2 gap-3">
          <input className="input" placeholder="Full name" required value={form.fullName}
                 onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
          <input className="input" placeholder="Email" type="email" required value={form.email}
                 onChange={(e) => setForm({ ...form, email: e.target.value })} />
        </div>
        <div className="grid grid-cols-2 gap-3">
          <input className="input" placeholder="Temporary password" required value={form.password}
                 onChange={(e) => setForm({ ...form, password: e.target.value })} />
          <input className="input" placeholder="Phone" required value={form.phone}
                 onChange={(e) => setForm({ ...form, phone: e.target.value })} />
        </div>
        <select className="input" value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
          {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
        </select>

        {form.role === "DOCTOR" && (
          <div className="grid grid-cols-2 gap-3 rounded-md bg-slate-50 p-3">
            <select className="input" required value={form.departmentId}
                    onChange={(e) => setForm({ ...form, departmentId: e.target.value })}>
              <option value="">Select department…</option>
              {departments.map((d) => <option key={d.id} value={d.id}>{d.name}</option>)}
            </select>
            <input className="input" placeholder="Specialization" required value={form.specialization}
                   onChange={(e) => setForm({ ...form, specialization: e.target.value })} />
            <input className="input" placeholder="Qualification" value={form.qualification}
                   onChange={(e) => setForm({ ...form, qualification: e.target.value })} />
            <input className="input" type="number" min={0} placeholder="Experience (years)" value={form.experienceYears}
                   onChange={(e) => setForm({ ...form, experienceYears: Number(e.target.value) })} />
          </div>
        )}

        {error && <p className="text-sm text-red-600">{error}</p>}
        {success && <p className="text-sm text-emerald-600">{success}</p>}
        <button className="btn btn-primary w-48" type="submit">Create Account</button>
      </form>
    </div>
  );
}
