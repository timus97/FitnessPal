# Fitness Tracking Application — Domain Analysis & Feature Development Guide

## 1. Domain Identification

**Domain:** **Personal fitness & nutrition tracking**

The application sits in the **health & wellness** domain, specifically:

- **User identity & access** — registration, login, roles (User, Admin)
- **Workout tracking** — logging exercises with date, type, duration, calories burned, intensity
- **Nutrition / food catalog** — products (food items) with macros and calories; users can "add" products; admins manage the catalog
- **Performance metrics** — embedded in workouts (duration, calories burned, intensity)

**Tech stack:** Spring Boot 3.1, Spring Security (JWT in HttpOnly cookie), Spring Data MongoDB, MongoDB.

---

## 2. Current Features — Summary

| Area | Feature | Status | Notes |
|------|---------|--------|--------|
| **Auth** | Register | Done | `POST /api/v1/auth/register` |
| **Auth** | Login | Done | `POST /api/v1/auth/authenticate` |
| **Auth** | Logout | Done | `POST /api/v1/auth/logout` |
| **Auth** | JWT in HttpOnly cookie | Done | Stateless, role + permission based |
| **User** | Get profile | Done | `GET /api/v1/secured/user` |
| **User** | Get credentials only | Done | `GET /api/v1/secured/user/credentials` |
| **User** | List my products | Done | `GET /api/v1/secured/user/products` |
| **User** | Add product by code | Partial | `POST .../products/{code}` — **replaces** entire list with one product |
| **Workout** | Add workout | Done | `POST /api/v1/secured/user/workout` |
| **Workout** | Delete workout | Done | `DELETE /api/v1/secured/user/workout/{id}` |
| **Workout** | List / get workouts | Missing | No GET endpoints; only via `UserResponse.workouts` |
| **Admin** | List users | Done | `GET /api/v1/secured/admin/users` (USER role only) |
| **Admin** | List products | Done | `GET /api/v1/secured/admin/product` |
| **Admin** | Add product | Done | `POST /api/v1/secured/admin/product` |
| **Admin** | Add product image | Done | `POST /api/v1/secured/admin/product/image/{code}` |
| **Admin** | Delete product | Done | `DELETE /api/v1/secured/admin/product/{code}` |
| **Admin** | Update product | Missing | No PUT/PATCH |
| **Admin** | Manage user (update/delete) | Missing | No user management beyond list |

**Known gaps / bugs:**

1. **User products:** `addProduct` uses `setProducts(List.of(product))`, so it **replaces** the user's product list instead of appending. Need to add to list and support remove.
2. **Product image in response:** `Convertor.productToResponseDto` calls `Base64.getEncoder().encodeToString(product.getImage())` without null check — will NPE when image is not set.
3. **Workouts:** No dedicated "list my workouts" or "get workout by id"; no update workout. Workout uniqueness is by `workoutDate` only (one workout per day).
4. **No daily food log:** README mentions "daily macros and calorie tracking" and "add to daily food catalog," but there is no **daily log** entity (date + products/meals). User only has a flat `List<Product>`.
5. **No product search for users:** Users add by `code` only; no search-by-name or browse catalog API for users.
6. **Admin product update:** No edit product (name, calories, etc.).

---

## 3. Suggested New Features (Within Domain)

Features are grouped by area and ordered by impact and dependency.

### 3.1 Fixes & Completeness (Do First)

| Feature | What | How to develop |
|--------|------|-----------------|
| **Fix add product** | Append product to user's list; support remove | In `UserService.addProduct`: get current list (or empty), add product if not already present, save. Add `DELETE /api/v1/secured/user/products/{code}` and remove from list. |
| **Null-safe product image** | Avoid NPE when product has no image | In `Convertor.productToResponseDto`: if `product.getImage() == null`, use `""` or a placeholder; else Base64 encode. |
| **List/Get workouts** | Dedicated workout endpoints | Add `GET /api/v1/secured/user/workout` (list) and `GET /api/v1/secured/user/workout/{id}`. Use `User.getWorkouts()` or query by `userId` if you add `userId` to `Workout`. Return `WorkoutResponse` list/single. |
| **Update workout** | Edit existing workout | Add `PUT /api/v1/secured/user/workout/{id}`. Load workout, ensure it belongs to current user, update fields (date, exerciseType, performanceMetrics), save. |

### 3.2 Daily Nutrition & Food Log

| Feature | What | How to develop |
|--------|------|-----------------|
| **Daily food log** | Log food per day (daily macros/calories) | New entity e.g. `DailyLog`: `id`, `userId`, `date` (LocalDate), `entries` (list of log entries). Entry: product ref (or code) + optional quantity/servings. Service: create/update log for a date, add/remove entries, compute daily totals from products. |
| **Daily totals** | Calories and macros for a day | From `DailyLog`: sum (product calories/macros × quantity) for that day. Endpoints: e.g. `GET /api/v1/secured/user/daily-log?date=...`, `POST/PUT .../daily-log` with body containing product codes and quantities. |
| **Meals (breakfast/lunch/dinner/snack)** | Group log entries by meal type | Add `mealType` (enum) to log entry. Filter/display by meal. Optional: `Meal` subdocument with `mealType` + list of entries. |

### 3.3 User-Facing Product Catalog

| Feature | What | How to develop |
|--------|------|-----------------|
| **Search products** | Find foods by name (user) | `GET /api/v1/secured/user/products/search?q=...` — in `ProductRepository` add `findByNameContainingIgnoreCase` (or use MongoDB `TextIndex` + full-text search). Return list of `ProductResponse` (with null-safe image). |
| **Browse catalog** | Paginated list for users | `GET /api/v1/secured/user/products/catalog?page=0&size=20`. Use `ProductRepository.findAll(Pageable)`. Different from "my products" (user's saved/consumed list). |

### 3.4 Goals & Progress

| Feature | What | How to develop |
|--------|------|-----------------|
| **Calorie / macro goals** | Daily targets per user | Add to `User` (or new `UserProfile`): `dailyCalorieGoal`, `dailyProteinGoal`, etc. Endpoint: `GET/PUT /api/v1/secured/user/goals`. Dashboard can show goal vs actual (from daily log). |
| **Workout goals** | e.g. workouts per week | New entity or user fields: `workoutsPerWeekGoal`. Compute progress from workout history (e.g. last 7 days). |
| **Simple analytics** | Calories burned per week, trends | Aggregate from `User.getWorkouts()` or from `WorkoutRepository` by user and date range. Endpoint e.g. `GET /api/v1/secured/user/analytics/calories-burned?from=...&to=...`. |

### 3.5 Admin & Operations

| Feature | What | How to develop |
|--------|------|-----------------|
| **Update product** | Edit name, description, calories, macros, code | `PUT /api/v1/secured/admin/product/{code}` with body (e.g. `ProductRequest`). Load by code, set fields, save. |
| **User management** | Disable user, delete user, change role | Add `enabled` to `User` (and use in `UserDetails.isEnabled()`). `PUT /api/v1/secured/admin/users/{id}` for role/enabled; `DELETE .../users/{id}` to delete (and optionally cascade delete workouts/logs). |
| **Product search (admin)** | Same as user search but admin-only | Reuse repository search method; admin endpoint with same or higher privileges. |

### 3.6 Extras (Optional)

| Feature | What | How to develop |
|--------|------|-----------------|
| **Password change** | User sets new password | `POST /api/v1/secured/user/change-password` with current + new password; verify current, encode new, save. |
| **Profile update** | Edit name, date of birth | `PUT /api/v1/secured/user` with partial payload; update only non-null fields. |
| **Refresh token** | Longer sessions without re-login | Issue refresh token (e.g. in cookie), new endpoint to exchange for new JWT. Store refresh tokens in DB or signed cookie. |
| **Body metrics** | Weight, body fat, etc. | New entity `BodyMetric`: userId, date, weight, optional fields. CRUD by user; use for simple weight-over-time views. |
| **Export data** | Export workouts or nutrition | Endpoint that builds CSV/JSON from workouts and daily logs for the authenticated user. |

---

## 4. Implementation Order (Recommended)

1. **Stabilize:** Fix product image null in `Convertor`; fix user "add product" to append and add "remove product."
2. **Workouts:** Add GET list and GET by id; add PUT for update (and optionally relax "one workout per day" if you add unique constraint by user+date).
3. **Daily log:** Introduce `DailyLog` (and optional meal type), endpoints to get/update log and daily totals.
4. **Goals:** Add goal fields and endpoints; plug into existing daily totals for comparison.
5. **Catalog:** User search and optional browse; then admin product update and user management.

---

## 5. Quick Reference — New/Updated Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/v1/secured/user/workout` | List my workouts |
| GET | `/api/v1/secured/user/workout/{id}` | Get one workout |
| PUT | `/api/v1/secured/user/workout/{id}` | Update workout |
| DELETE | `/api/v1/secured/user/products/{code}` | Remove product from my list |
| GET | `/api/v1/secured/user/products/search?q=` | Search catalog |
| GET | `/api/v1/secured/user/daily-log?date=` | Get daily log + totals |
| POST/PUT | `/api/v1/secured/user/daily-log` | Set daily log entries |
| GET/PUT | `/api/v1/secured/user/goals` | Get/update calorie & macro goals |
| PUT | `/api/v1/secured/admin/product/{code}` | Update product |
| PUT/DELETE | `/api/v1/secured/admin/users/{id}` | Update/delete user |

---

## 6. Data Model Additions (Conceptual)

- **DailyLog:** `id`, `userId`, `date`, `entries[]` (productCode, quantity, optional mealType).
- **User (or UserProfile):** `dailyCalorieGoal`, `dailyProteinGoal`, etc.; optional `enabled`.
- **Workout:** Consider `userId` in document for easier querying (list workouts by user and date range) instead of only via `User.workouts`.

This document gives you the **domain summary**, **current feature list**, **identified gaps**, and a **concrete set of features with how to develop them** so you can extend the project in a structured way.
