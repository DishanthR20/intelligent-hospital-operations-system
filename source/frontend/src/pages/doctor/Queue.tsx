import { useEffect, useState } from "react";
import { api, apiErrorMessage } from "../../api/client";
import ExplanationPanel from "../../components/ExplanationPanel";
import { Doctor, LabTestCatalog, Medicine, QueueEntryResponse } from "../../types";

interface PrescriptionLine { medicineId: string; dosageInstructions: string; quantity: number; }

export default function DoctorQueue() {
  const [doctor, setDoctor] = useState<Doctor | null>(null);
  const [entries, setEntries] = useState<QueueEntryResponse[]>([]);
  const [medicines, setMedicines] = useState<Medicine[]>([]);
  const [labTests, setLabTests] = useState<LabTestCatalog[]>([]);
  const [activeEntry, setActiveEntry] = useState<QueueEntryResponse | null>(null);
  const [notes, setNotes] = useState("");
  const [diagnosis, setDiagnosis] = useState("");
  const [followUp, setFollowUp] = useState("");
  const [lines, setLines] = useState<PrescriptionLine[]>([]);
  const [labTestId, setLabTestId] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    api.get<Doctor>("/doctors/me").then((r) => setDoctor(r.data));
    api.get("/pharmacy/medicines?size=200").then((r) => setMedicines(r.data.content ?? r.data));
    api.get<LabTestCatalog[]>("/laboratory/catalog").then((r) => setLabTests(r.data));
  }, []);

  function loadQueue(departmentId: string, doctorId: string) {
    api.get<QueueEntryResponse[]>(`/queues/department/${departmentId}`).then((r) =>
      setEntries(r.data.filter((e) => e.doctorId === doctorId && e.status !== "COMPLETED"))
    );
  }

  useEffect(() => {
    if (doctor) loadQueue(doctor.departmentId, doctor.id);
  }, [doctor]);

  function openConsult(entry: QueueEntryResponse) {
    setActiveEntry(entry);
    setNotes("");
    setDiagnosis("");
    setFollowUp("");
    setLines([]);
    setLabTestId("");
    setError(null);
    api.post(`/queues/${entry.id}/start-consultation`).then(() => doctor && loadQueue(doctor.departmentId, doctor.id));
  }

  function addLine() {
    if (!medicines.length) return;
    setLines([...lines, { medicineId: medicines[0].id, dosageInstructions: "", quantity: 1 }]);
  }

  async function finishConsultation() {
    if (!activeEntry || !doctor) return;
    setBusy(true);
    setError(null);
    try {
      const consultation = await api.post("/consultations", {
        patientId: activeEntry.patientId, notes, diagnosis, followUpPlan: followUp,
      });

      if (lines.length > 0) {
        await api.post("/pharmacy/prescriptions", {
          patientId: activeEntry.patientId,
          consultationId: consultation.data.id,
          items: lines.filter((l) => l.dosageInstructions && l.quantity > 0),
        });
      }
      if (labTestId) {
        await api.post("/laboratory/orders", { patientId: activeEntry.patientId, testId: labTestId });
      }
      await api.post(`/queues/${activeEntry.id}/complete`);
      setActiveEntry(null);
      loadQueue(doctor.departmentId, doctor.id);
    } catch (err) {
      setError(apiErrorMessage(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-900">My Queue</h1>

      <div className="grid gap-6 lg:grid-cols-2">
        <div className="space-y-3">
          {entries.map((e) => (
            <div key={e.id}>
              <ExplanationPanel explanation={e.explanation} score={e.priorityScore} />
              <div className="mt-2 flex items-center justify-between rounded-b-md bg-slate-50 px-4 py-2 text-sm">
                <span>Token {e.tokenNumber} · {e.status}</span>
                <button className="btn btn-primary" onClick={() => openConsult(e)}>
                  {e.status === "IN_CONSULTATION" ? "Continue" : "Start Consultation"}
                </button>
              </div>
            </div>
          ))}
          {entries.length === 0 && <p className="text-sm text-slate-500">No patients currently assigned to you.</p>}
        </div>

        {activeEntry && (
          <div className="card sticky top-4 h-fit space-y-3">
            <h2 className="font-medium text-slate-900">Consultation — {activeEntry.patientName}</h2>
            <div>
              <label className="label">Clinical Notes</label>
              <textarea className="input" rows={3} value={notes} onChange={(e) => setNotes(e.target.value)} required />
            </div>
            <div>
              <label className="label">Diagnosis</label>
              <input className="input" value={diagnosis} onChange={(e) => setDiagnosis(e.target.value)} />
            </div>
            <div>
              <label className="label">Follow-up Plan</label>
              <input className="input" value={followUp} onChange={(e) => setFollowUp(e.target.value)} />
            </div>

            <div>
              <div className="flex items-center justify-between">
                <label className="label mb-0">Prescription</label>
                <button type="button" className="btn btn-secondary" onClick={addLine}>+ Add Medicine</button>
              </div>
              {lines.map((line, i) => (
                <div key={i} className="mt-2 grid grid-cols-6 gap-2">
                  <select className="input col-span-2" value={line.medicineId}
                          onChange={(e) => setLines(lines.map((l, j) => j === i ? { ...l, medicineId: e.target.value } : l))}>
                    {medicines.map((m) => <option key={m.id} value={m.id}>{m.name}</option>)}
                  </select>
                  <input className="input col-span-3" placeholder="Dosage instructions" value={line.dosageInstructions}
                         onChange={(e) => setLines(lines.map((l, j) => j === i ? { ...l, dosageInstructions: e.target.value } : l))} />
                  <input className="input" type="number" min={1} value={line.quantity}
                         onChange={(e) => setLines(lines.map((l, j) => j === i ? { ...l, quantity: Number(e.target.value) } : l))} />
                </div>
              ))}
            </div>

            <div>
              <label className="label">Order Lab Test (optional)</label>
              <select className="input" value={labTestId} onChange={(e) => setLabTestId(e.target.value)}>
                <option value="">None</option>
                {labTests.map((t) => <option key={t.id} value={t.id}>{t.name}</option>)}
              </select>
            </div>

            {error && <p className="text-sm text-red-600">{error}</p>}
            <div className="flex gap-2">
              <button className="btn btn-primary" disabled={busy || !notes} onClick={finishConsultation}>
                {busy ? "Saving…" : "Complete Consultation"}
              </button>
              <button className="btn btn-secondary" onClick={() => setActiveEntry(null)}>Cancel</button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
