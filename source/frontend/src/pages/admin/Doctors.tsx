import { useEffect, useState } from "react";
import { api } from "../../api/client";
import StatusBadge from "../../components/StatusBadge";
import { Department, Doctor } from "../../types";

interface Workload {
  activePatients: number;
  waitingInQueue: number;
  level: "LOW" | "MODERATE" | "HIGH" | "CRITICAL";
}

export default function Doctors() {
  const [doctors, setDoctors] = useState<Doctor[]>([]);
  const [departments, setDepartments] = useState<Department[]>([]);
  const [workloads, setWorkloads] = useState<Record<string, Workload>>({});

  useEffect(() => {
    api.get<Department[]>("/departments").then((r) => setDepartments(r.data));
    api.get<Doctor[]>("/doctors").then((r) => {
      setDoctors(r.data);
      r.data.forEach((d) => {
        api.get<Workload>(`/intelligence/workload/${d.id}`).then((w) =>
          setWorkloads((prev) => ({ ...prev, [d.id]: w.data }))
        ).catch(() => {});
      });
    });
  }, []);

  const deptName = (id: string) => departments.find((d) => d.id === id)?.name ?? "—";

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">Doctors</h1>
      <div className="card">
        <table className="data-table">
          <thead>
            <tr>
              <th>Name</th><th>Department</th><th>Specialization</th><th>Experience</th>
              <th>Consult (min)</th><th>Rating</th><th>Workload</th>
            </tr>
          </thead>
          <tbody>
            {doctors.map((d) => (
              <tr key={d.id}>
                <td>{d.fullName}</td>
                <td>{d.departmentName ?? deptName(d.departmentId)}</td>
                <td>{d.specialization}</td>
                <td>{d.experienceYears} yr</td>
                <td>{d.consultationMinutes}</td>
                <td>{d.rating.toFixed(1)}</td>
                <td>{workloads[d.id] ? <StatusBadge level={workloads[d.id].level} /> : "—"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
