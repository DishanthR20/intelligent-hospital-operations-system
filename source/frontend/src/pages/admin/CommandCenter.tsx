import { useEffect, useState } from "react";
import { api } from "../../api/client";
import MetricCard from "../../components/MetricCard";
import StatusBadge from "../../components/StatusBadge";
import { DepartmentStatus, HospitalMetrics } from "../../types";

export default function CommandCenter() {
  const [metrics, setMetrics] = useState<HospitalMetrics | null>(null);
  const [departments, setDepartments] = useState<DepartmentStatus[]>([]);
  const [alerts, setAlerts] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      api.get<HospitalMetrics>("/analytics/command-center"),
      api.get<DepartmentStatus[]>("/analytics/digital-twin"),
      api.get<string[]>("/analytics/alerts"),
    ])
      .then(([m, d, a]) => {
        setMetrics(m.data);
        setDepartments(d.data);
        setAlerts(a.data);
      })
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <p className="text-slate-500">Loading command center…</p>;
  if (!metrics) return <p className="text-red-600">Unable to load hospital metrics.</p>;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-slate-900">Hospital Command Center</h1>
        <p className="text-sm text-slate-500">Live operational overview, generated from current data — not a forecast.</p>
      </div>

      <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
        <MetricCard label="Patients Today" value={metrics.patientsToday} />
        <MetricCard label="Appointments Today" value={metrics.appointmentsToday} />
        <MetricCard label="Emergency Cases" value={metrics.emergencyCasesToday} />
        <MetricCard label="Avg Waiting Time" value={`${metrics.avgWaitingMinutes.toFixed(0)} min`} />
        <MetricCard label="Bed Occupancy" value={`${metrics.bedOccupancyPercent}%`} />
        <MetricCard label="ICU Occupancy" value={`${metrics.icuOccupancyPercent}%`} />
        <MetricCard label="Patient Experience Index" value={metrics.patientExperienceIndex.toFixed(0)} sub="out of 100" />
        <MetricCard label="Revenue Today" value={`$${metrics.revenueToday.toFixed(2)}`} />
      </div>

      <div className="card">
        <h2 className="mb-3 font-medium text-slate-900">Intelligence Alerts</h2>
        {alerts.length === 0 ? (
          <p className="text-sm text-slate-500">No active alerts. Hospital operations look normal.</p>
        ) : (
          <ul className="space-y-2">
            {alerts.map((a, i) => (
              <li key={i} className="rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-900">
                {a}
              </li>
            ))}
          </ul>
        )}
      </div>

      <div className="card">
        <h2 className="mb-3 font-medium text-slate-900">Hospital Digital Twin</h2>
        <table className="data-table">
          <thead>
            <tr>
              <th>Department</th>
              <th>Queue Length</th>
              <th>Available Doctors</th>
              <th>Avg Wait (min)</th>
              <th>Load</th>
            </tr>
          </thead>
          <tbody>
            {departments.map((d) => (
              <tr key={d.departmentId}>
                <td>{d.departmentName}</td>
                <td>{d.queueLength}</td>
                <td>{d.availableDoctors}</td>
                <td>{d.avgWaitingMinutes.toFixed(0)}</td>
                <td><StatusBadge level={d.loadStatus} /></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
