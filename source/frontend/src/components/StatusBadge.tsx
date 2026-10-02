const CLASS_BY_LEVEL: Record<string, string> = {
  LOW: "badge-low",
  MODERATE: "badge-moderate",
  HIGH: "badge-high",
  CRITICAL: "badge-critical",
};

/** Renders a load/status level with both color AND text, per the accessibility requirement (never color alone). */
export default function StatusBadge({ level }: { level: string }) {
  const cls = CLASS_BY_LEVEL[level] ?? "badge-neutral";
  return <span className={`badge ${cls}`}>{level}</span>;
}
