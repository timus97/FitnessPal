# Complex Feature Suggestions (4–6 Hours Each)

This document suggests **new features** for the Spring Boot Fitness Tracking Application that are **complex enough to take 4–6 hours** to build, even with agentic AI, and will typically require **multiple prompts** to design, implement, fix edge cases, and test.

Each feature is scoped so that an AI agent (or developer) will need to:
- Introduce new entities, DTOs, repositories, services, and controllers
- Handle non-trivial business logic, validation, or aggregation
- Fix or refactor existing code (e.g. add-product bug, null-safe product image)
- Iterate after initial implementation (edge cases, tests, API consistency)

---

## 1. Daily Food Log + Meals + Daily Totals

**Why 4–6 hours:** New domain entity, multiple endpoints, aggregation logic, and integration with existing Product/User model.

**Scope:**
- **Entity:** `DailyLog` — `id`, `userId`, `date` (LocalDate), `entries` (list of entries). Each entry: `productCode`, `quantity` (servings), `mealType` (enum: BREAKFAST, LUNCH, DINNER, SNACK), optional `entryId` for update/delete.
- **Endpoints:**
  - `GET /api/v1/secured/user/daily-log?date=...` — get log for date + **daily totals** (calories, protein, carbs, fat) computed from Product × quantity.
  - `POST /api/v1/secured/user/daily-log` — create/update log for a date (body: list of entries).
  - `POST /api/v1/secured/user/daily-log/entries` — quick-add one entry to a date.
  - `PUT /api/v1/secured/user/daily-log/entries/{entryId}` — edit quantity/mealType.
  - `DELETE /api/v1/secured/user/daily-log/entries/{entryId}` — remove entry.
- **Logic:** Resolve product by code; sum `calories * quantity`, `protein * quantity`, etc. Handle missing product (404 or skip), duplicate productCode in same meal, and date validation.
- **Dependencies:** Fix **add product** to append (not replace) and **null-safe product image** in `Convertor` so daily-log responses don't NPE when product has no image.

**Suggested prompt sequence:**
1. "Add DailyLog entity and repository for per-day food logging with meal types."
2. "Implement DailyLogService: create/update log for a date, add/remove/edit entries, compute daily calorie and macro totals from Product."
3. "Add DailyLogController with GET daily-log, POST/PUT daily-log, and entry add/edit/delete; fix add product to append and product image NPE in Convertor."
4. "Add validation and edge cases: product not found, duplicate entry, date in future; add integration tests for daily log."

---

## 2. Calorie & Macro Goals + Today's Dashboard

**Why 4–6 hours:** New user fields, new endpoints, aggregation from multiple sources (daily log, workouts, optional water), and comparison logic.

**Scope:**
- **User (or profile):** Add `dailyCalorieGoal`, `dailyProteinGoal`, `dailyCarbsGoal`, `dailyFatGoal`, `dailyWaterGoalMl` (optional).
- **Endpoints:**
  - `GET/PUT /api/v1/secured/user/goals` — get/update goals.
  - `GET /api/v1/secured/user/dashboard/today?date=...` — return: `caloriesConsumed`, `caloriesBurned`, `calorieGoal`, `proteinConsumed`, `proteinGoal`, … `waterConsumed`, `waterGoal`, optional `weightToday` from latest WeightEntry.
- **Logic:** Consumed from DailyLog (if feature #1 exists) else 0; burned from Workout for that date (sum `performanceMetrics.caloriesBurned`); goals from user; water from DailyLog or dedicated `waterConsumedMl` field.
- **Optional:** Add `waterConsumedMl` to DailyLog and `GET/PUT .../daily-log/water?date=...` so dashboard can show water.

**Suggested prompt sequence:**
1. "Add goal fields to User (dailyCalorieGoal, dailyProteinGoal, etc.) and GET/PUT /user/goals."
2. "Implement dashboard/today: aggregate calories consumed from DailyLog, calories burned from Workout for date, compare to user goals; include weight if available."
3. "Add water goal and water log (field on DailyLog or endpoint) and include in dashboard/today."
4. "Handle missing data: no log for day, no goals set; add tests for dashboard."

---

## 3. User Product Catalog: Search, Browse, Remove + Fix Add Product

**Why 3–4 hours:** Repository search/pagination, new endpoints, and fixing two existing bugs.

**Scope:**
- **Fixes:**
  - In `UserService.addProduct`, append product to user's list instead of replacing.
  - In `Convertor.productToResponseDto`, null-safe product image (empty string or placeholder if `image == null`).
- **Endpoints:**
  - `DELETE /api/v1/secured/user/products/{code}` — remove product from current user's list.
  - `GET /api/v1/secured/user/products/search?q=...&page=0&size=20` — search catalog by name (e.g. `findByNameContainingIgnoreCase` or MongoDB text index).
  - `GET /api/v1/secured/user/products/catalog?page=0&size=20` — paginated browse (e.g. `ProductRepository.findAll(Pageable)`).
- **Logic:** Search returns ProductResponse list (with null-safe image). "My products" stays as is; catalog is global.

**Suggested prompt sequence:**
1. "Fix add product to append to user's list and add DELETE /user/products/{code}; fix product image NPE in Convertor."
2. "Add ProductRepository search by name (ignore case) and pagination; add GET /user/products/search and GET /user/products/catalog."
3. "Add validation and tests: search empty query, pagination bounds, remove product not in list."

---

## 4. Workout List, Get, Update + Optional userId on Workout

**Why 3–4 hours:** New endpoints, ownership checks, and optional schema change with backfill.

**Scope:**
- **Endpoints:**
  - `GET /api/v1/secured/user/workout?from=...&to=...` — list current user's workouts (optional date range).
  - `GET /api/v1/secured/user/workout/{id}` — get one workout; ensure it belongs to current user.
  - `PUT /api/v1/secured/user/workout/{id}` — update workout (exerciseType, workoutDate, performanceMetrics); ensure ownership.
- **Logic:** Workouts are stored in both `Workout` collection and `User.workouts`. List/get can use `user.getWorkouts()` filtered by date, or add `userId` to `Workout` and query by `userId` + date range (requires backfill for existing documents).
- **Validation:** Uniqueness (e.g. one workout per user per day) on update; prevent reassigning to another user.

**Suggested prompt sequence:**
1. "Add GET /user/workout (list) and GET /user/workout/{id} for current user; optional from/to date filter."
2. "Add PUT /user/workout/{id} to update exerciseType, date, performanceMetrics; ensure workout belongs to user."
3. "Optionally add userId to Workout and backfill; add repository findByUserIdAndWorkoutDateBetween for list."

---

## 5. Streaks (Logging + Workout)

**Why 4–5 hours:** Algorithm over time-series data, persistence of "longest streak," and edge cases (timezone, today, gaps).

**Scope:**
- **Logging streak:** Consecutive days (going back from today) where user has at least one DailyLog entry. Return `currentStreakDays`, `longestStreakDays`.
- **Workout streak:** Same idea over workout dates (consecutive days with at least one workout).
- **Endpoints:** `GET /api/v1/secured/user/streak`, `GET /api/v1/secured/user/workout-streak`.
- **Persistence:** Optionally store `longestStreakDays` (and `longestWorkoutStreakDays`) on User and update when a new streak exceeds it.
- **Edge cases:** "Today" not yet logged; timezone (use server date or user profile timezone); gaps in data.

**Suggested prompt sequence:**
1. "Implement logging streak: from DailyLog, compute consecutive days with at least one entry; GET /user/streak returning currentStreakDays and longestStreakDays."
2. "Implement workout streak from workout dates; GET /user/workout-streak; optionally persist longest streaks on User."
3. "Handle edge cases: today not logged, no data; add tests for streak logic."

---

## 6. Custom Foods (User-Created) + Daily Log Support

**Why 4–5 hours:** New entity, CRUD, and extending DailyLog to support both catalog products and custom foods in totals.

**Scope:**
- **Entity:** `CustomFood` — `id`, `userId`, `name`, `calories`, `protein`, `carbs`, `fat`, `servingSize` (e.g. string or grams).
- **Endpoints:** `GET/POST/PUT/DELETE /api/v1/secured/user/custom-foods`.
- **DailyLog extension:** Log entry can be either `productCode` (catalog) or `customFoodId`. When computing daily totals, resolve product by code or custom food by id and sum (calories × quantity, etc.).
- **Logic:** Only the owner can CRUD their custom foods; daily log must validate that customFoodId belongs to current user.

**Suggested prompt sequence:**
1. "Add CustomFood entity and repository; implement CRUD under /user/custom-foods."
2. "Extend DailyLog entries to support either productCode or customFoodId; update daily totals to resolve both products and custom foods."
3. "Add validation and tests: custom food ownership, missing product/custom food, quantity."

---

## 7. Reminders (Backend: Settings + Scheduler)

**Why 4–5 hours:** New entity, CRUD, and scheduled job that "fires" reminders (e.g. log or send stub notification).

**Scope:**
- **Entity:** `UserReminder` — `userId`, `type` (MEAL, WATER, WORKOUT), `enabled`, `timeOfDay` (e.g. "08:00", "13:00"). One document per user per type or a list embedded in User.
- **Endpoints:** `GET /api/v1/secured/user/reminders`, `PUT /api/v1/secured/user/reminders` (replace or update list).
- **Scheduler:** `@Scheduled` (e.g. every minute or every 5 minutes) to find users with `enabled` reminder at current time (match timeOfDay); call a notification service (e.g. log "Reminder sent for user X" or send email if configured).
- **Logic:** Time comparison (server time vs user timezone if stored); avoid sending same reminder twice in the same window.

**Suggested prompt sequence:**
1. "Add UserReminder entity and GET/PUT /user/reminders for reminder settings (type, enabled, timeOfDay)."
2. "Add a scheduled job that finds users with reminders due at current time and calls a notification service (stub that logs)."
3. "Wire notification stub; add idempotency so we don't send duplicate reminders in same minute; add tests."

---

## 8. Export My Data

**Why 4–5 hours:** Aggregation from multiple collections, large data, and format (ZIP vs JSON).

**Scope:**
- **Endpoint:** `GET /api/v1/secured/user/export` — returns all data for current user: profile, workouts, weight entries, daily logs (if any), custom foods (if any), products (user's list).
- **Format:** Single JSON object or ZIP containing JSON files per entity type.
- **Logic:** Stream or paginate when building payload to avoid OOM; respect authentication (only current user's data).

**Suggested prompt sequence:**
1. "Implement export service that aggregates current user's profile, workouts, weight entries into a single JSON structure."
2. "Add GET /user/export that returns this JSON; then extend to include daily logs and custom foods if present."
3. "Use streaming or chunking for large data; add test for export content and size."

---

## 9. Admin: Product Update + User Management

**Why 3–4 hours:** New admin endpoints, User schema change (`enabled`), and cascade/soft-delete decisions.

**Scope:**
- **Product:** `PUT /api/v1/secured/admin/product/{code}` — update name, description, calories, protein, carbs, fat (and code if allowed).
- **User:** Add `enabled` (boolean) to User; use in `UserDetails.isEnabled()`.
- **Endpoints:** `PUT /api/v1/secured/admin/users/{id}` — update role, enabled. `DELETE /api/v1/secured/admin/users/{id}` — delete user (and optionally cascade delete workouts, weight entries, daily logs or mark user as deleted).
- **Logic:** Prevent deleting last admin; validate role enum; consider soft delete vs hard delete.

**Suggested prompt sequence:**
1. "Add PUT /admin/product/{code} to update product fields; add enabled to User and use in isEnabled()."
2. "Add PUT /admin/users/{id} (role, enabled) and DELETE /admin/users/{id}; decide cascade behavior and implement."
3. "Add validation: cannot delete self, cannot remove last admin; add tests."

---

## 10. Exercise Library + Workout from Library

**Why 4–5 hours:** New entity, seed or admin CRUD, and linking workouts to exercises for default calories.

**Scope:**
- **Entity:** `Exercise` — `id`, `name`, `defaultCaloriesPerMin`, `category` (e.g. CARDIO, STRENGTH).
- **Data:** Seed collection with common exercises or admin CRUD.
- **Endpoints:** `GET /api/v1/secured/user/exercises` or public `GET /api/v1/exercises` — list for dropdown.
- **Workout:** When adding a workout, optional `exerciseId`; if provided, prefill `exerciseType` from exercise name and suggest calories from `defaultCaloriesPerMin * duration`.
- **Logic:** Admin: POST/PUT/DELETE exercises if you want editable library.

**Suggested prompt sequence:**
1. "Add Exercise entity and repository; seed with a few default exercises; add GET /exercises."
2. "Allow workout POST to accept optional exerciseId; prefill exerciseType and suggested calories from Exercise."
3. "Add admin CRUD for exercises and tests for list and workout creation with exerciseId."

---

## Recommended Order and Dependencies

| Order | Feature | Depends on / note |
|-------|---------|-------------------|
| 1 | **Catalog + fix add product** (#3) | None; unblocks daily log and dashboard |
| 2 | **Daily food log + meals + totals** (#1) | Fix add product + null-safe image (#3) |
| 3 | **Goals + dashboard** (#2) | Daily log (#1) for full dashboard; can start with goals only |
| 4 | **Workout list/get/update** (#4) | None |
| 5 | **Streaks** (#5) | Daily log (#1) for logging streak |
| 6 | **Custom foods + daily log** (#6) | Daily log (#1) for entry extension |
| 7 | **Reminders** (#7) | None |
| 8 | **Export** (#8) | More valuable after #1, #6 |
| 9 | **Admin product update + user management** (#9) | None |
| 10 | **Exercise library** (#10) | None |

Use **FEATURE_DEVELOPMENT_GUIDE.md** for implementation details and **HEALTHIFYME_FEATURES.md** for full backlog and endpoint naming. Each feature above is designed so that building and fixing it will take roughly **4–6 hours** and **multiple prompts** when using an AI agent.
