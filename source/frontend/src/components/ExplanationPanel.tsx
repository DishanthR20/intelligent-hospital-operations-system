interface Factor {
  name: string;
  points: number;
  reason: string;
}

interface Explanation {
  subject: string;
  factors: Factor[];
}

/** Shows an intelligence engine's reasoning: subject, total score, and each contributing factor. */
export default function ExplanationPanel({ explanation, score }: { explanation: Explanation; score?: number }) {
  const total = score ?? explanation.factors.reduce((sum, f) => sum + f.points, 0);
  return (
    <div className="card">
      <div className="flex items-center justify-between">
        <p className="font-medium text-slate-900">{explanation.subject}</p>
        <p className="text-lg font-semibold text-brand-600">{total.toFixed(1)}</p>
      </div>
      <ul className="mt-2 space-y-1 text-sm">
        {explanation.factors.map((f, i) => (
          <li key={i} className="flex justify-between gap-4 text-slate-600">
            <span>{f.name} <span className="text-slate-400">— {f.reason}</span></span>
            <span className="shrink-0 font-medium text-slate-800">{f.points > 0 ? "+" : ""}{f.points}</span>
          </li>
        ))}
      </ul>
    </div>
  );
}
