# API

Base path: `/api/v1`. Full interactive reference: `/swagger-ui.html` once the
backend is running (springdoc-openapi auto-generates the schema from the
controllers and DTOs below).

Auth: `Authorization: Bearer <accessToken>` on every endpoint except
`/auth/**`, `/swagger-ui/**`, `/api-docs/**`, and `/actuator/health`.

Errors always come back as:
```json
{ "timestamp": "...", "status": 404, "error": "PATIENT_NOT_FOUND", "message": "...", "path": "/api/v1/patients/...", "details": [] }
```

## Endpoint groups

| Prefix | Covers |
|---|---|
| `/auth` | login, refresh, patient self-registration, change password |
| `/users` | admin staff onboarding |
| `/patients` | search, get, update, `/me` |
| `/doctors` | list, get, update, `/me`, weekly schedule |
| `/departments` | CRUD (admin write) |
| `/appointments` | book, suggested slots, reschedule, cancel, no-show, check-in, per-patient/doctor lists, `/me` |
| `/queues` | add (walk-in), ranked department queue, emergency queue, clinical-risk triage, assign doctor, start/complete consultation |
| `/intelligence` | doctor recommendations, doctor workload |
| `/beds` | wards, beds, allocate, release, cleaning/inspection workflow |
| `/pharmacy/medicines` | catalog, batches, demand report, expiring stock |
| `/pharmacy/prescriptions` | create, per-patient/`/me`, pending items, dispense |
| `/laboratory` | test catalog, orders (collect → process → result → verify), per-patient/`/me`, pending |
| `/consultations` | create, per-patient/doctor/`/me` |
| `/billing` | invoices, payments, per-patient/`/me` |
| `/discharge` | start, checklist updates, complete, per-patient |
| `/feedback` | submit, `/me`, admin theme report |
| `/notifications` | list, unread count, mark read |
| `/audit` | admin explorer, filterable by action |
| `/decisions` | admin Decision Replay, filterable by type/patient |
| `/configuration` | admin: read/update every tunable threshold |
| `/analytics` | digital twin, command-center metrics, intelligence alerts |
| `/events` | operations timeline, per-patient journey |

## Role access at a glance

- **PATIENT** — only their own data (`/me` endpoints), booking/cancelling their
  own appointments, submitting feedback.
- **RECEPTIONIST** — patient registration, appointment booking/check-in,
  walk-in queue, billing.
- **NURSE** — queue triage (clinical risk), bed allocation, discharge checklist.
- **DOCTOR** — their queue, consultations, prescriptions, lab orders/verify,
  their own schedule/workload.
- **PHARMACIST** — medicine catalog/batches, dispensing.
- **LAB_TECHNICIAN** — sample collection through result entry.
- **ADMIN** — everything, plus staff onboarding, configuration, audit, decision
  replay, and the command center.

Enforced via `@PreAuthorize` on every controller method — see
`architecture.md` for the security model.
