# Database

PostgreSQL 16, managed by Hibernate (`spring.jpa.hibernate.ddl-auto: update`)
for this project's stage — a real deployment would switch to a migration tool
(Flyway/Liquibase) before going to production. Every entity extends
`common.entity.BaseEntity`: a UUID primary key plus `created_at`/`updated_at`,
maintained automatically by Spring Data JPA auditing.

## Tables

| Table | Purpose |
|---|---|
| `users` | Every login-capable account (all 7 roles); role-specific data lives in the tables below and links back via `user_id`. |
| `patients`, `patient_allergies`, `patient_chronic_conditions`, `patient_current_medications` | Patient profile + three normalized collection tables — never one free-text field. |
| `doctors`, `doctor_schedules` | Doctor profile + weekly availability windows. |
| `departments` | Department config (capacity, default consultation length). |
| `appointments` | Booking lifecycle (`BOOKED` → … → `COMPLETED`/`CANCELLED`/`NO_SHOW`). |
| `queue_entries` | Live per-department queue; priority score is computed on read, never stored (see intelligence-engine.md). |
| `wards`, `beds` | Ward policy (ICU/isolation/gender) + individual bed lifecycle and turnaround timestamps. |
| `consultations` | One row per doctor–patient encounter — the core of the normalized medical record. |
| `pharmacy_inventory`, `medicine_batches`, `prescriptions`, `prescription_items` | Catalog, FEFO-tracked stock batches, and prescriptions with per-item dispense tracking. |
| `lab_tests`, `lab_orders` | Catalog (with configurable normal/critical ranges) and the order lifecycle (sample → result → verified). See README for why `lab_samples`/`lab_results` are columns here rather than separate tables. |
| `invoices`, `invoice_items`, `payments` | Billing, itemized by category, with partial-payment support. |
| `discharge_records` | Checklist-gated discharge workflow. |
| `patient_feedback` | Raw 1–10 ratings + comments; the Experience Index and theme insights are computed on read. |
| `notifications` | In-app notifications, priority-tagged. |
| `audit_logs` | Every sensitive action (login, view/edit patient, prescriptions, lab reports, bed assignment, billing, permission changes). |
| `hospital_events` | The operations timeline / patient journey — one row per domain event. |
| `decisions`, `decision_factors` | Every explainable recommendation the platform has made, for Decision Replay. |
| `hospital_configurations` | Every tunable weight/threshold used by an intelligence engine. |

## Design notes

- **Normalization over convenience fields.** Allergies, chronic conditions, and
  medications are each their own collection table, not a comma-separated
  string — you can query "which patients are allergic to X" directly.
- **No duplicated derived state.** Department queue length, bed occupancy, and
  doctor workload are never stored columns — they're computed from the live
  rows (queue entries, beds, appointments) every time they're read, so they can
  never drift out of sync with reality.
- **Indexes**: unique index on `users.email`; unique constraints on
  `departments.name`, `pharmacy_inventory.name`, `lab_tests.name`,
  `wards.name`, `beds.bed_number`, `hospital_configurations.config_key`.
- One deliberate naming note: the configuration table's key/value columns are
  named `config_key`/`config_value` rather than the bare words `key`/`value`,
  since several SQL engines (H2 included) reserve those as keywords.
