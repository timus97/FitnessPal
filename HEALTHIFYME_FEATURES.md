# HealthifyMe-Style Features for This Project

This document lists features similar to **HealthifyMe** (and similar apps like MyFitnessPal, Lose It!) that you can implement in this Spring Boot Fitness Tracking Application. Each feature is mapped to your existing domain and given concrete implementation steps.

---

## 1. Nutrition & Food Logging (Core)

| HealthifyMe feature | What to build | How to implement in this project |
|---------------------|---------------|-----------------------------------|
| **Log meals by type** | Breakfast, lunch, dinner, snacks | Add `DailyLog` entity: `userId`, `date`, `entries[]`. Each entry: `productCode`, `quantity` (servings), `mealType` enum (BREAKFAST, LUNCH, DINNER, SNACK). Endpoints: `GET/POST/PUT /api/v1/secured/user/daily-log?date=...`, body with list of `{ productCode, quantity, mealType }`. |
| **Daily calorie & macro totals** | Show consumed vs goal for the day | From `DailyLog` entries, join with `Product` to sum calories, protein, carbs, fat (× quantity). Return in daily-log response or separate `GET .../daily-log/summary?date=...` with `totalCalories`, `totalProtein`, etc. |
| **Search food / browse catalog** | Search and add from app food database | `GET /api/v1/secured/user/products/search?q=...&page=0&size=20` using `ProductRepository.findByNameContainingIgnoreCase(q, Pageable)`. Optional: `GET .../products/catalog` paginated for browsing. |
| **Quick add to today** | Add food to today’s log in one tap | `POST .../daily-log/entries` with `{ date, productCode, quantity, mealType }`. Create or update `DailyLog` for that date, append entry. |
| **Edit/remove log entry** | Edit quantity or remove a food entry | Store entries with `id` (or index). `PUT .../daily-log/entries/{entryId}` (quantity, mealType), `DELETE .../daily-log/entries/{entryId}`. |
| **Water intake** | Glasses or ml per day | Add to `DailyLog` or new `WaterLog`: `userId`, `date`, `amountMl` or `glasses`. `GET/PUT .../daily-log/water?date=...` or `.../water-log?date=...`. Include in daily summary. |
| **Custom food** | User-created food (not in catalog) | New entity `CustomFood`: `userId`, name, calories, protein, carbs, fat, servingSize. CRUD under `/api/v1/secured/user/custom-foods`. Daily log entry can reference either `productCode` (catalog) or `customFoodId`. |
| **Favorites / recent foods** | Quickly re-add frequently eaten foods | Store on `User`: `favoriteProductCodes` or `recentProductCodes` (last N). When user adds a product to log, push to recent. Endpoint `GET .../user/favorite-foods` or derive “recent” from last 7 days of logs. |

---

## 2. Goals & Targets

| HealthifyMe feature | What to build | How to implement in this project |
|---------------------|---------------|-----------------------------------|
| **Calorie goal** | Daily calorie target | Add to `User` (or `UserProfile`): `dailyCalorieGoal`. `GET/PUT /api/v1/secured/user/goals` returning/accepting `dailyCalorieGoal`, optional `dailyProteinGoal`, `dailyCarbsGoal`, `dailyFatGoal`. |
| **Macro goals** | Protein / carbs / fat targets | Same as above; store and return in goals API. Dashboard compares daily totals (from daily log) to these goals. |
| **Weight goal** | Target weight (lose/maintain/gain) | Add `targetWeight`, `weightGoalType` (LOSE, MAINTAIN, GAIN) to user profile. Used for display and optional calorie suggestion (can add later). |
| **Water goal** | Glasses or ml per day | Add `dailyWaterGoalMl` or `dailyWaterGoalGlasses` to user. Show in daily summary (consumed vs goal). |

---

## 3. Weight & Body Tracking

| HealthifyMe feature | What to build | How to implement in this project |
|---------------------|---------------|-----------------------------------|
| **Weight log** | Log weight by date | New entity `WeightEntry`: `userId`, `date`, `weightKg`, optional `note`. Repository: `findByUserIdAndDateBetween`. `POST /api/v1/secured/user/weight`, `GET .../weight?from=...&to=...`, `PUT .../weight/{id}`, `DELETE .../weight/{id}`. |
| **Weight trend / chart** | Show weight over time | `GET .../weight?from=...&to=...` returns list; frontend charts it. Optional: endpoint `GET .../user/analytics/weight-trend?days=30` that returns pre-aggregated points. |
| **BMI** | Compute from height + weight | Add `heightCm` to User. Compute BMI = weight / (height/100)². Return in profile or in weight response using latest weight. |
| **Profile: height, gender, DOB** | Richer profile for calculations | Extend `User`: `heightCm`, `gender` (enum). You already have `dateOfBirth`. Use for BMR/calorie estimates later if needed. |

---

## 4. Workouts & Activity

| HealthifyMe feature | What to build | How to implement in this project |
|---------------------|---------------|-----------------------------------|
| **List my workouts** | See past workouts | `GET /api/v1/secured/user/workout?from=...&to=...` (optional). Return list of `WorkoutResponse` from user’s workouts or from `Workout` collection if you add `userId` to Workout. |
| **Workout detail** | View single workout | `GET .../workout/{id}`. Ensure workout belongs to current user. |
| **Edit workout** | Change exercise type, duration, calories | `PUT .../workout/{id}` with updated fields (exerciseType, performanceMetrics). |
| **Exercise library** | Predefined list of exercises | New collection `Exercise`: `id`, `name`, `defaultCaloriesPerMin`, `category` (e.g. CARDIO, STRENGTH). Admin or seed data. `GET .../exercises` for dropdown in app. When logging workout, user picks exercise or types custom. |
| **Calories burned today / week** | Activity summary | From workouts in date range: sum `performanceMetrics.caloriesBurned`. `GET .../user/analytics/calories-burned?date=...` or `?from=...&to=...`. |
| **Steps** (optional) | Step count per day | New `StepLog`: `userId`, `date`, `steps`. Log via API or future device sync. Include in daily “activity” summary. |

---

## 5. Dashboard & Summary

| HealthifyMe feature | What to build | How to implement in this project |
|---------------------|---------------|-----------------------------------|
| **Today’s summary** | Calories in, calories out, macros, water | Single endpoint `GET /api/v1/secured/user/dashboard/today?date=...` returning: `caloriesConsumed`, `caloriesBurned`, `calorieGoal`, `proteinConsumed`, `proteinGoal`, … `waterConsumed`, `waterGoal`, optional `weightToday` from latest weight entry. |
| **Weekly summary** | 7-day calorie/workout overview | `GET .../dashboard/week?startDate=...` returning array of daily summaries or aggregates (total consumed, total burned, workouts count). |
| **Calorie balance** | In vs out (deficit/surplus) | In today/week summary: `caloriesIn`, `caloriesOut`, `balance` = in − out. |

---

## 6. Streaks & Engagement

| HealthifyMe feature | What to build | How to implement in this project |
|---------------------|---------------|-----------------------------------|
| **Logging streak** | Consecutive days user logged food | Compute from `DailyLog`: for each day going back from today, check if log exists; count consecutive days. `GET .../user/streak` → `{ currentStreakDays, longestStreakDays }`. Store `longestStreak` on User and update when streak breaks or grows. |
| **Workout streak** | Consecutive days with workout | Same idea over workout dates. Optional: `GET .../user/workout-streak`. |

---

## 7. User Profile & Onboarding

| HealthifyMe feature | What to build | How to implement in this project |
|---------------------|---------------|-----------------------------------|
| **Extended profile** | Height, gender, activity level, goal | Add to User: `heightCm`, `gender`, `activityLevel` (SEDENTARY, LIGHT, MODERATE, ACTIVE), `weightGoalType`, `targetWeight`. `GET/PUT /api/v1/secured/user/profile`. |
| **Update profile** | Edit name, DOB, etc. | `PUT .../user` or `.../user/profile` with partial update (name, dateOfBirth, height, goals). |
| **Diet preference** | Vegetarian / vegan / etc. | Add `dietPreference` enum to User. Use for filtering recipe/food suggestions later or just display. |
| **Photo** | Profile picture | Add `profileImageUrl` or store byte[] in User; endpoint `POST .../user/photo`, `GET` in profile. |

---

## 8. Reminders (Backend Support)

| HealthifyMe feature | What to build | How to implement in this project |
|---------------------|---------------|-----------------------------------|
| **Reminder settings** | Meal, water, workout reminder times | New entity `UserReminder`: `userId`, `type` (MEAL, WATER, WORKOUT), `enabled`, `timeOfDay` (e.g. "08:00", "13:00"). CRUD `GET/PUT .../user/reminders`. Actual push/email sent by a separate job or external service that reads these settings. |
| **Cron / scheduler** | Trigger reminder logic | Spring `@Scheduled` job that finds users with reminders at current time and calls a notification service (email, push, or just store “reminder sent” for demo). |

---

## 9. Reports & Export

| HealthifyMe feature | What to build | How to implement in this project |
|---------------------|---------------|-----------------------------------|
| **Weekly report** | PDF or in-app summary | `GET .../user/reports/week?startDate=...` returning JSON summary (or generate PDF with a library). Data: daily calories, macros, workouts, weight trend. |
| **Export my data** | Download all logs | `GET .../user/export` returning ZIP or JSON with user profile, workouts, daily logs, weight entries, custom foods for the user. |

---

## 10. Admin & Catalog (HealthifyMe-style)

| HealthifyMe feature | What to build | How to implement in this project |
|---------------------|---------------|-----------------------------------|
| **Food catalog management** | Add, edit, delete foods | You have add/delete. Add `PUT /api/v1/secured/admin/product/{code}` to update name, description, calories, macros. |
| **Categories / tags** | Group foods (e.g. Fruits, Dairy) | Add `category` or `tags[]` to `Product`. Filter in search: `GET .../products/search?q=...&category=...`. |
| **Serving size** | Per serving vs per 100g | Add `servingSizeG` or `servingDescription` to Product. Log entry `quantity` = number of servings; totals = product.calories × quantity. |
| **Barcode** (optional) | Lookup food by barcode | Add `barcode` to Product, unique index. `GET .../products/by-barcode/{barcode}`. |

---

## 11. Implementation Priority (HealthifyMe-like experience)

1. **Daily log + meals + daily totals** — core “log food” experience.
2. **Goals (calorie + macro + water)** and **today’s dashboard** (consumed vs goal).
3. **Fix add product** (append, remove) and **search/browse catalog**.
4. **Weight log + trend** and **extended profile** (height, gender, targets).
5. **Workout list/get/update** and **exercise library** (optional).
6. **Water log**, **custom food**, **favorites/recent**.
7. **Streaks**, **weekly summary**, **reminders** (settings + optional cron).
8. **Reports/export**, **admin product update**, **categories/barcode**.

---

## 12. New Entities Summary

| Entity | Purpose |
|--------|---------|
| `DailyLog` | Per-day food log: userId, date, entries (productCode/customFoodId, quantity, mealType), waterMl. |
| `WeightEntry` | userId, date, weightKg, note. |
| `CustomFood` | User-created food: userId, name, calories, protein, carbs, fat, servingSize. |
| `Exercise` | Catalog of exercises for workout logging (optional). |
| `UserReminder` | Reminder type, enabled, timeOfDay per user (optional). |
| `WaterLog` | Or just a field on DailyLog: waterConsumedMl. |

---

## 13. New / Updated Endpoints Quick List

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/v1/secured/user/daily-log?date=` | Get day’s log + totals |
| POST/PUT | `/api/v1/secured/user/daily-log` | Set log entries for a date |
| POST/DELETE | `.../daily-log/entries` | Add / remove single entry |
| GET | `.../dashboard/today?date=` | Today’s summary (in, out, goals) |
| GET | `.../dashboard/week?startDate=` | Week summary |
| GET/PUT | `.../user/goals` | Calorie & macro goals |
| GET/PUT | `.../user/profile` | Extended profile (height, gender, targets) |
| GET | `.../user/weight?from=&to=` | Weight entries |
| POST/PUT/DELETE | `.../user/weight` | Log/update/delete weight |
| GET | `.../user/streak` | Logging streak |
| GET | `.../user/products/search?q=` | Search food catalog |
| GET | `.../user/workout` | List workouts |
| GET | `.../user/workout/{id}` | Get workout |
| PUT | `.../user/workout/{id}` | Update workout |
| GET | `.../user/custom-foods` | List custom foods |
| POST/PUT/DELETE | `.../user/custom-foods` | CRUD custom food |
| GET/PUT | `.../user/reminders` | Reminder settings |
| GET | `.../user/export` | Export my data |
| GET | `.../exercises` | Exercise library (optional) |
| PUT | `/api/v1/secured/admin/product/{code}` | Update product |

Use this as a product backlog to copy a HealthifyMe-like experience into your Spring Boot fitness project.
