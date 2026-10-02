# Intelligence Engines

Every place MediSphere AI says "AI," "smart," or "intelligent," it means one of
the plain-Java techniques below — never a trained machine-learning model.
Nothing here calls out to Python, an external ML service, or an LLM.

## The shared explainability framework

`intelligence.DecisionFactor` — one named, weighted contributor:
`(name, points, reason)`. `intelligence.DecisionExplanation` — a subject plus a
list of factors; its total score is *always* the sum of the factors, never a
separately-computed number, so the UI's "Total" can never disagree with the
breakdown underneath it. `intelligence.Decision` — the persisted form of an
explanation, saved every time an engine produces a real recommendation, so the
admin Decision Replay page can show exactly why any recommendation happened,
whenever it happened.

## Engine-by-engine

### Smart Appointment Slot Optimizer (`appointment/SlotOptimizer`)
Walks each of a doctor's configured weekly availability windows in
consultation-length steps, skips anything colliding with an existing active
appointment, and estimates the wait for each free slot from the count of
appointments already booked ahead of it that day. Fully deterministic —
every number traces back to a `count()` of real appointments.

### Smart Queue / priority scoring (`queue/QueuePriorityEngine`)
```
score = emergencyBonus (if flagged)
      + clinicalRisk × weight
      + waitingMinutes × weight        ← aging: guarantees no one waits forever
      + vulnerabilityBonus (elderly/child, from configurable age thresholds)
```
Ranked with a `java.util.PriorityQueue` (max-heap). There is **no VIP or status
factor anywhere in the formula** — that's not an oversight, it's the point:
hospital policy requires that status can never outrank clinical emergency, so
the option to add such a factor simply doesn't exist in this engine.

### Doctor Recommendation Engine (`intelligence/DoctorRecommendationEngine`)
A routing aid, not a diagnostic tool. Scores each doctor in a department on
specialization-keyword match, today's availability, current workload (see
below), years of experience, and whether the patient has seen that doctor
before. Never interprets symptoms medically.

### Doctor Workload Engine (`intelligence/DoctorWorkloadEngine`)
`LOW / MODERATE / HIGH / CRITICAL`, from a simple count of a doctor's active
appointments today plus their current queue load, against admin-configurable
thresholds. Feeds both the recommendation engine and the doctor's own dashboard.

### Smart Bed Allocation (`bed/BedAllocationEngine`)
Hard requirements (ICU, isolation, ward gender policy) are filtered first — a
non-ICU bed is never offered for an ICU need. Among the remaining candidates,
prefers the ward that doesn't "waste" a special-purpose bed on a routine
admission, and the ward with the most remaining free capacity, to spread load.

### Medicine Demand Engine (`pharmacy/MedicineDemandEngine`)
A Java statistical projection: average daily usage is the *observed*
consumption rate (real units dispensed since tracking began ÷ days elapsed),
never a forecast. `estimatedRemainingDays = currentStock / averageDailyUsage`.
Reorder status compares that, and raw stock, against admin-configurable
thresholds.

### Configurable Critical Result Alerts (`laboratory/LabTestCatalog`)
Each lab test has admin-configurable normal and critical reference ranges. A
result outside the critical range triggers a `LabResultCompletedEvent(critical=true)`
and a CRITICAL in-app notification to the ordering doctor — an alerting
workflow, explicitly not an autonomous diagnosis.

### Patient Experience Index (`feedback/PatientExperienceEngine`)
Each 1–10 sub-rating (waiting, doctor, staff, cleanliness, billing,
communication) is normalized to 0–100, then combined with admin-configurable
weights into one score. An operational satisfaction metric — never presented
as a clinical outcome measure.

### Feedback Insight Engine (`feedback/FeedbackInsightEngine`)
Rule-based keyword-to-theme matching over free-text feedback comments (waiting,
billing, doctor, staff, cleanliness, pharmacy, laboratory, appointments,
communication), reported as percentages of total mentions. Explicitly not NLP
or sentiment analysis — just configurable keyword buckets.

### Intelligence Alert Engine (`analytics/IntelligenceAlertEngine`)
Reads live operational state — department queue load, ICU/bed occupancy,
pending lab backlog, pharmacy reorder status, and doctor workload spread within
a department — against configurable thresholds, and turns anything crossing
one into a plain-English alert line for the Command Center. Every alert is a
direct, traceable read of current data; there's no model behind any of it.

## Where the thresholds live

Every numeric weight and threshold above is read from the
`hospital_configurations` table via `ConfigService`, not hardcoded — see
Admin → Configuration Center. `DataSeeder` writes sensible defaults on first
boot (`ensureDefault`) so the platform behaves reasonably before an admin ever
visits that screen.
