# MediSphere AI

**Intelligent Hospital Orchestration & Patient Experience Platform**

> From Patient Arrival to Hospital Decision — One Intelligent Platform.

MediSphere AI coordinates patient flow, clinical workflows, hospital resources and
operational decisions through **explainable, Java-based intelligence engines**.

**Backend and intelligence systems are implemented entirely in Java. No Python, C,
or C++ is used anywhere in this project.** Where the product uses the word "AI" it
means: rule-based scoring, weighted decision engines, statistical projections, and
graph/priority-queue algorithms — all written in plain Java, all fully explainable,
none of it a black-box machine learning model. See
[`docs/intelligence-engine.md`](docs/intelligence-engine.md) for exactly which
technique backs each feature.

---

## 1. What's actually implemented

This is a working, end-to-end vertical slice of the full specification — every
module below is real, wired to a database, exercised by tests, and reachable from
the UI. Nothing is a placeholder or a fake response.

| Area | Status |
|---|---|
| Auth (JWT, BCrypt, refresh tokens, roles) | ✅ |
| Patient management (profile, allergies, conditions, medications) | ✅ |
| Doctor & department management, schedules | ✅ |
| Appointments (booking, conflict detection, reschedule, cancel, check-in) | ✅ |
| **Smart Appointment Slot Optimizer** | ✅ |
| **Smart Queue** (priority scoring, waiting-time aging, emergency queue) | ✅ |
| **Doctor Recommendation Engine** (explainable) | ✅ |
| **Doctor Workload Engine** | ✅ |
| **Smart Bed Allocation** + turnaround tracking | ✅ |
| Pharmacy (catalog, batches, prescriptions, dispensing) + **Medicine Demand Engine** | ✅ |
| Laboratory (orders, sample→result lifecycle, **configurable critical alerts**) | ✅ |
| Consultations (normalized medical record) | ✅ |
| Billing (invoices, payments, categories) | ✅ |
| Discharge Planner (checklist-gated) | ✅ |
| Patient Feedback + **Patient Experience Index** + **Feedback Insight Engine** | ✅ |
| Notifications (in-app, priority-tagged) | ✅ |
| Audit log / Admin Audit Explorer | ✅ |
| **Decision Replay** (every explainable recommendation is stored) | ✅ |
| Hospital Configuration Center (tunable weights/thresholds) | ✅ |
| **Hospital Digital Twin** + **Command Center** + **Intelligence Alerts** | ✅ |
| Operations timeline / patient journey | ✅ |
| Domain events (Spring `ApplicationEventPublisher`) | ✅ |
| React frontend for all 7 roles | ✅ |
| Demo seed data reproducing the spec's sample intelligence scenario | ✅ |
| Backend unit + integration tests (JUnit 5, Mockito, `@DataJpaTest`) | ✅ |

### Deliberately not built in this pass

The original spec is a ~75-section enterprise system; building every corner of it
to the same real, tested standard as the above is multiple engineer-months of
work. Rather than fake these with placeholder endpoints, they are simply **not
present**, so nothing in the app claims a capability it doesn't have:

- **Emergency QR medical card & break-glass access** — the emergency *queue* and
  *mode* (severity-based prioritization, emergency department view) are fully
  implemented; the QR-code identity card and the break-glass audit workflow are not.
- **Insurance claims workflow** (policies/claims/settlement) — patients can store
  an insurance provider/policy number, but there's no claims pipeline.
- **Family health tree**
- **Indoor hospital navigation / route optimization** (the Dijkstra/A* graph feature)
- **What-if hospital simulator**
- **Java-based demand forecasting** (moving averages over historical registration data)
- **Root cause analysis engine** and **security anomaly detection**
- **Revenue leakage / billing discrepancy engine**
- **Surge management mode**

Everything else in the spec — including every intelligence engine's explainability
requirement — is implemented for real.

### One schema simplification, documented

`lab_samples` and `lab_results` are modeled as columns on a single `lab_orders`
table rather than three separate tables. The relationship is strictly 1:1 across
the order's lifecycle (ordered → sample collected → processing → result →
verified), so splitting it into three joined tables would add queries without
adding any real normalization benefit. Every other entity in the spec's schema
(patients, allergies, medications, consultations, prescriptions, prescription
items, beds, wards, invoices, invoice items, payments, decisions, decision
factors, audit logs, hospital events…) is its own properly normalized table.

---

## 2. Architecture

```
Controller → Service → Domain / Intelligence Engine → Repository → PostgreSQL
```

Backend packages (`backend/src/main/java/com/medisphere/`):

```
auth  user  patient  doctor  department  appointment  queue
bed  pharmacy  laboratory  consultation  billing  discharge
feedback  notification  audit  event  configuration  analytics
intelligence   ← the shared explainability framework every engine uses
security  common
```

The **intelligence framework** (`intelligence/DecisionFactor`,
`DecisionExplanation`, `Decision`) is the one abstraction every engine shares:
a recommendation is never just a number, it's a named subject plus a list of
weighted, human-readable factors that sum to the score — and every one of them
is persisted for the admin's Decision Replay page.

Full write-up: [`docs/architecture.md`](docs/architecture.md),
[`docs/database.md`](docs/database.md), [`docs/api.md`](docs/api.md),
[`docs/intelligence-engine.md`](docs/intelligence-engine.md).

---

## 3. Technology stack

- **Backend:** Java 21, Spring Boot 3.3, Spring Security (JWT via `jjwt`), Spring
  Data JPA / Hibernate, Jakarta Validation, Maven
- **Database:** PostgreSQL 16 (H2 for tests only)
- **QR generation library present but unused** in this pass (ZXing is on the
  classpath for when the QR card feature is built) — no Python QR libraries
- **API docs:** springdoc-openapi / Swagger UI
- **Frontend:** React 18, TypeScript, Vite, Tailwind CSS, Axios, React Router,
  Recharts
- **Tests:** JUnit 5, Mockito, AssertJ, Spring Boot Test (`@DataJpaTest`)

No Python, C, or C++ appears anywhere in this repository.

---

## 4. Running it locally

### Prerequisites
- JDK 21
- Node.js 18+
- Docker (for PostgreSQL) — or a local PostgreSQL 16 instance

### 4.1 Start PostgreSQL

```bash
docker compose up -d
```

This starts Postgres on `localhost:5432` with database/user/password all
`medisphere` (see `.env.example` — copy it to `.env` and adjust for anything
beyond local development).

### 4.2 Run the backend

```bash
cd backend
./mvnw spring-boot:run
```

(Windows: `mvnw.cmd spring-boot:run`.) On first boot, `DataSeeder` populates
realistic demo data — departments, doctors with schedules, patients, wards/beds,
pharmacy stock, a lab test catalog, and a **live queue scenario matching spec
section 61**: Cardiology queue at CRITICAL load, ICU occupancy at 80%, two
medicines below their reorder threshold, and a doctor workload imbalance in
Cardiology — so the Intelligence Alert Engine has something real to show the
moment an admin logs in.

The API is at `http://localhost:8080/api/v1`, Swagger UI at
`http://localhost:8080/swagger-ui.html`.

### 4.3 Run the frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

### 4.4 Run the backend tests

```bash
cd backend
./mvnw test
```

---

## 5. Demo accounts

All seeded with `SEED_ENABLED=true` (the default). **Change or disable this
before any non-local deployment** — see `.env.example`.

| Role | Email | Password |
|---|---|---|
| Admin | `admin@medisphere.ai` | `Admin123!` |
| Doctor (Cardiology) | `dr.arun@medisphere.ai` | `Doctor123!` |
| Doctor (Cardiology) | `dr.mehta@medisphere.ai` | `Doctor123!` |
| Doctor (General Medicine) | `dr.patel@medisphere.ai` | `Doctor123!` |
| Nurse | `nurse.taylor@medisphere.ai` | `Nurse123!` |
| Receptionist | `reception.jones@medisphere.ai` | `Reception123!` |
| Pharmacist | `pharmacist.lee@medisphere.ai` | `Pharmacy123!` |
| Lab Technician | `labtech.brown@medisphere.ai` | `LabTech123!` |
| Patient | `john.doe@example.com` | `Patient123!` |
| Patient | `mary.jane@example.com` | `Patient123!` |

New patients can also self-register from the login page's flow via
`POST /api/v1/auth/register/patient`.

---

## 6. Demonstration workflow

This end-to-end path is fully wired and works today:

1. Receptionist registers a patient → books an appointment (via the Smart Slot
   Optimizer) → patient checks in → dropped into the department's Smart Queue.
2. Admin/Nurse views the ranked queue with each patient's full priority
   explanation (emergency flag, clinical risk, waiting-time aging, vulnerability).
3. Doctor opens their queue, starts the consultation, records diagnosis/notes,
   writes a prescription and/or orders a lab test — all from one screen.
4. Lab technician collects the sample, processes it, enters the result; if it's
   outside the configured critical range the ordering doctor gets an in-app
   CRITICAL notification automatically.
5. Pharmacist dispenses the prescription (FEFO batch consumption, feeding the
   Medicine Demand Engine's usage rate).
6. Receptionist raises the invoice and collects payment.
7. Doctor/Nurse runs the Discharge Planner checklist to close the visit.
8. Patient submits feedback → Patient Experience Index is computed.
9. Admin's Command Center shows today's metrics, the Digital Twin per
   department, and the Intelligence Alert Engine's current findings; every
   recommendation made along the way is visible in Decision Replay.

---

## 7. Environment configuration

Nothing is hardcoded. Copy `.env.example` and set real values for anything
beyond local dev — in particular `JWT_SECRET` (generate with
`openssl rand -base64 48`) and the database credentials. `SEED_ENABLED=false`
turns off demo data entirely.

Hospital *policy* values (queue weights, workload thresholds, pharmacy reorder
windows, Patient Experience Index weights, alert thresholds…) are **not**
environment variables — they live in the `hospital_configurations` table and are
editable at runtime from Admin → Configuration Center.

---

## 8. Project structure

```
medisphere-ai/
├── backend/                # Spring Boot application (Maven)
│   ├── src/main/java/com/medisphere/...
│   ├── src/main/resources/application.yml
│   └── src/test/java/com/medisphere/...
├── frontend/                # React + TypeScript + Vite
│   └── src/{api,components,context,pages,types.ts}
├── docs/
│   ├── architecture.md
│   ├── api.md
│   ├── database.md
│   └── intelligence-engine.md
├── database/README.md
├── docker-compose.yml       # PostgreSQL only — no Python/C/C++ services
├── .env.example
└── README.md
```

## 9. Future improvements

Beyond the "deliberately not built" list in section 1: audit-log export,
rate limiting on auth endpoints, a proper CI pipeline, and finishing out
Swagger annotations per-endpoint (springdoc auto-generates the schema today
but per-endpoint `@Operation` descriptions are sparse).
