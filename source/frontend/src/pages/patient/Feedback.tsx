import { FormEvent, useState } from "react";
import { api, apiErrorMessage } from "../../api/client";

const initial = { waitingRating: 5, doctorRating: 5, staffRating: 5, cleanlinessRating: 5, billingRating: 5, communicationRating: 5, comments: "" };

export default function PatientFeedback() {
  const [form, setForm] = useState(initial);
  const [result, setResult] = useState<{ overallScore: number; subScores: Record<string, number> } | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    try {
      const res = await api.post("/feedback", form);
      setResult(res.data.index);
      setForm(initial);
    } catch (err) {
      setError(apiErrorMessage(err));
    }
  }

  function ratingField(key: keyof typeof initial, label: string) {
    return (
      <div>
        <label className="label">{label}: {form[key]}</label>
        <input type="range" min={1} max={10} value={form[key] as number}
               onChange={(e) => setForm({ ...form, [key]: Number(e.target.value) })} className="w-full" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Share Your Feedback</h1>
      <p className="text-sm text-slate-500">
        This produces an operational Patient Experience Index — it is not a medical outcome measure.
      </p>

      <form onSubmit={onSubmit} className="card max-w-lg space-y-4">
        {ratingField("waitingRating", "Waiting Time")}
        {ratingField("doctorRating", "Doctor")}
        {ratingField("staffRating", "Staff")}
        {ratingField("cleanlinessRating", "Cleanliness")}
        {ratingField("billingRating", "Billing")}
        {ratingField("communicationRating", "Communication")}
        <div>
          <label className="label">Comments</label>
          <textarea className="input" rows={3} value={form.comments} onChange={(e) => setForm({ ...form, comments: e.target.value })} />
        </div>
        {error && <p className="text-sm text-red-600">{error}</p>}
        <button className="btn btn-primary" type="submit">Submit Feedback</button>
      </form>

      {result && (
        <div className="card max-w-lg">
          <h2 className="font-medium text-slate-900">Patient Experience Index: {result.overallScore}/100</h2>
          <ul className="mt-2 text-sm text-slate-600">
            {Object.entries(result.subScores).map(([k, v]) => (
              <li key={k} className="flex justify-between"><span>{k}</span><span>{v}</span></li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
}
