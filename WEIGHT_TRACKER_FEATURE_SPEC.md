# Weight Tracker Feature — Validated Specification

## Restructured prompt

**Context:** Fitness tracker app (Java Spring Boot, MongoDB).  
**Feature:** Weight-loss journey tracking.

**Requirements:**

1. **Weight tracker** — The app user can add weight entries (date + weight in kg).
2. **Auto-calculated BMI** — For each entry (and for the current state), the app calculates BMI using the user's height. Formula: `BMI = weightKg / (heightM)²` where `heightM = heightCm / 100`.
3. **End goal** — The API returns:
   - **Target (goal) weight** — Either set by the user or suggested from a healthy target BMI (e.g. 22).
   - **Target (goal) BMI** — Either derived from the user's target weight or suggested as a healthy range (e.g. 18.5–24.9).

**Preconditions:**

- User must have **height** stored (in cm) for BMI calculation. Height can be set at registration or via profile update.
- Optionally, user can set a **target weight** (kg). If not set, the app suggests a target weight from a default healthy target BMI (22).

**API behaviour:**

- **Add weight entry:** `POST /api/v1/secured/user/weight` with `weightKg`, optional `date`, optional `note`. Response includes the entry and its BMI (if height is set).
- **Get weight history:** `GET /api/v1/secured/user/weight?from=...&to=...` returns list of entries with date, weight, BMI.
- **Get journey summary:** `GET /api/v1/secured/user/weight/journey` returns:
  - Latest weight and current BMI.
  - Goal weight and goal BMI (user-set or suggested).
  - Optional: short list of recent entries for the journey view.

**Validation rules:**

- `weightKg` > 0 and within a reasonable range (e.g. 20–300 kg).
- `heightCm` > 0 and reasonable (e.g. 100–250 cm).
- `targetWeightKg` (if set) > 0 and reasonable.

**Profile (height & goal):** User sets height and optional target weight via `PUT /api/v1/secured/user/profile` with body `{ "heightCm": 175, "targetWeightKg": 70 }`. Both optional; only provided fields are updated.

**Delete entry:** `DELETE /api/v1/secured/user/weight/{id}` removes a weight entry (owner only).

This document is the single source of truth for the weight tracker feature scope.
