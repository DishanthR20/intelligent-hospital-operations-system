# Architecture

## Layering

```
Controller  (REST, @PreAuthorize role checks, DTOs only — never raw entities
             across the API boundary, so lazy-loaded associations never leak
             past the transaction that loaded them)
    ↓
Service     (@Transactional business logic, orchestrates repositories +
             intelligence engines + event publishing)
    ↓
Domain / Intelligence Engine  (pure Java scoring/decision logic — see
             intelligence-engine.md)
    ↓
Repository  (Spring Data JPA)
    ↓
PostgreSQL
```

## Why DTOs everywhere

`spring.jpa.open-in-view` is deliberately set to `false`: a request's database
session closes the moment its `@Transactional` service method returns, before
the controller serializes the response. Combined with most entity associations
being `FetchType.LAZY` (to avoid N+1 queries), this means **every response
object is built inside the transactional service method**, never handed back
as a raw JPA entity for Jackson to serialize later. Every module has a
`dto/` package for exactly this reason.

## Package layout

Each business capability is its own top-level package under
`com.medisphere`, containing its own entities, repository, service,
controller, and `dto/` subpackage:

```
auth  user  patient  doctor  department  appointment  queue  bed
pharmacy  laboratory  consultation  billing  discharge  feedback
notification  audit  event  configuration  analytics
```

Two packages are cross-cutting infrastructure used by everything above:

- **`security`** — JWT issuance/validation, `SecurityConfig`, `CurrentUser` helper.
- **`intelligence`** — the shared explainability framework (`DecisionFactor`,
  `DecisionExplanation`, `Decision` for replay) plus the engines that don't
  belong to one specific domain module (`DoctorRecommendationEngine`,
  `DoctorWorkloadEngine`).
- **`common`** — `BaseEntity` (UUID id + audit timestamps), the exception
  hierarchy, and `GlobalExceptionHandler`.

## Domain events

Ten Spring `ApplicationEventPublisher` events (`event/*Event.java`, a sealed
interface `DomainEvent`) decouple side effects from the services that trigger
them:

- `HospitalEventListener` persists every event onto the operations timeline
  (`hospital_events` table) — after the triggering transaction commits, so a
  rolled-back action never shows up on the timeline.
- `NotificationEventListener` turns patient-facing events into in-app
  notifications.

This is genuinely event-driven, not just services calling each other directly.

## Security model

- Passwords: BCrypt (Spring Security `PasswordEncoder`).
- Auth: JWT access token (default 60 min) + refresh token (default 7 days),
  HMAC-signed, secret from environment (`JWT_SECRET`).
- Authorization: `@PreAuthorize("hasRole('...')")` / `hasAnyRole(...)` on every
  controller method, plus an explicit ownership check in `PatientController`
  so a patient can never fetch or edit another patient's record by guessing an id.
- Centralized error handling: `GlobalExceptionHandler` translates every
  exception into a stable `{timestamp, status, error, message, path}` shape —
  no stack trace ever reaches a client.
