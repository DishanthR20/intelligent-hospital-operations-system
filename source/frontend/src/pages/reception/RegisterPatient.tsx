import { FormEvent, useState } from "react";
import { api, apiErrorMessage } from "../../api/client";

const initial = {
  fullName: "", email: "", password: "", phone: "", dateOfBirth: "", gender: "MALE",
  bloodGroup: "", address: "", emergencyContactName: "", emergencyContactPhone: "",
};

export default function RegisterPatient() {
  const [form, setForm] = useState(initial);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    try {
      await api.post("/auth/register/patient", form);
      setSuccess(`${form.fullName} registered successfully. They can now log in with the password you set.`);
      setForm(initial);
    } catch (err) {
      setError(apiErrorMessage(err));
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Register New Patient</h1>
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
        <div className="grid grid-cols-3 gap-3">
          <input className="input" type="date" required value={form.dateOfBirth}
                 onChange={(e) => setForm({ ...form, dateOfBirth: e.target.value })} />
          <select className="input" value={form.gender} onChange={(e) => setForm({ ...form, gender: e.target.value })}>
            <option value="MALE">Male</option>
            <option value="FEMALE">Female</option>
            <option value="OTHER">Other</option>
          </select>
          <input className="input" placeholder="Blood group" value={form.bloodGroup}
                 onChange={(e) => setForm({ ...form, bloodGroup: e.target.value })} />
        </div>
        <input className="input" placeholder="Address" value={form.address}
               onChange={(e) => setForm({ ...form, address: e.target.value })} />
        <div className="grid grid-cols-2 gap-3">
          <input className="input" placeholder="Emergency contact name" value={form.emergencyContactName}
                 onChange={(e) => setForm({ ...form, emergencyContactName: e.target.value })} />
          <input className="input" placeholder="Emergency contact phone" value={form.emergencyContactPhone}
                 onChange={(e) => setForm({ ...form, emergencyContactPhone: e.target.value })} />
        </div>
        {error && <p className="text-sm text-red-600">{error}</p>}
        {success && <p className="text-sm text-emerald-600">{success}</p>}
        <button className="btn btn-primary w-40" type="submit">Register Patient</button>
      </form>
    </div>
  );
}
