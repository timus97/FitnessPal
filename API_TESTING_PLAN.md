# Fitness Tracking Application – API Testing Plan

## 1. Overview

This document describes how to test all REST APIs of the Spring Boot Fitness Tracking Application. The app uses **JWT stored in an HTTP-only cookie** (name: `subproblem`) for authentication. Secured endpoints require a valid cookie set by `/api/v1/auth/authenticate`.

**Base URL (local):** `http://localhost:8080`  
**Base URL (Docker):** Use the host/port where the app is exposed (e.g. `http://localhost:8080` if mapped).

---

## 2. Environment Prerequisites

- **Java 17**, **Maven** (or use `mvnw`), and **MongoDB** running (or use Docker Compose).
- For local run without Docker: override MongoDB in `application.yaml` or use profile so that `host: localhost` and correct `port`/credentials.
- Start app: `./mvnw spring-boot:run` (or run from IDE).
- **Demo data:** To load 5 dummy users and their demo data, start the app with profile `demo`:  
  `./mvnw spring-boot:run -Dspring.profiles.active=demo`  
  See **Section 3** for user credentials and data summary.

---

## 3. Demo Data (5 Dummy Users)

When the app runs with profile **`demo`**, it seeds the database with 5 users and related demo data. Use these accounts in Postman or any API client to test without manual setup. **Password for all demo users:** `password123`.

### 3.1 User 1 – Alice Smith (USER, full data)

| Field | Value |
|-------|--------|
| **Email** | `alice@demo.com` |
| **Password** | `password123` |
| **Role** | USER |
| **Profile** | Height 170 cm, target weight 65 kg, DOB 1992-05-10 |

**Demo data:**

- **Weight entries:** 3 entries (e.g. 68 kg, 67.2 kg, 66.5 kg on 2025-02-01, 2025-02-15, 2025-03-01).
- **Workouts:** 1 workout – Running on 2025-03-01 (duration 35 min, 280 cal, intensity 7).
- **Products:** 2 products linked (Choco Protein Bar code 1001, Oatmeal Pack code 1002).

**Use for:** Get user, update profile, get credentials, weight entries + date range + journey, delete weight entry, get products, add/remove product, workout add/delete.

---

### 3.2 User 2 – Bob Jones (USER, weight + workout)

| Field | Value |
|-------|--------|
| **Email** | `bob@demo.com` |
| **Password** | `password123` |
| **Role** | USER |
| **Profile** | Height 182 cm, target 80 kg, DOB 1988-11-22 |

**Demo data:**

- **Weight entries:** 3 entries (e.g. 85 kg, 83.5 kg, 82 kg on 2025-01-10, 2025-02-10, 2025-03-05).
- **Workouts:** 1 workout – Cycling on 2025-03-02 (60 min, 520 cal, intensity 8).
- **Products:** None.

**Use for:** Weight journey with multiple entries, workouts, adding a product by code (e.g. 1003).

---

### 3.3 User 3 – Carol White (USER, minimal data)

| Field | Value |
|-------|--------|
| **Email** | `carol@demo.com` |
| **Password** | `password123` |
| **Role** | USER |
| **Profile** | Height 165 cm, target 58 kg, DOB 1995-03-08 |

**Demo data:**

- **Weight entries:** 1 entry (59 kg on 2025-03-01).
- **Workouts:** 1 workout – Yoga on 2025-03-03 (45 min, 120 cal, intensity 4).
- **Products:** 1 product (Banana code 1003).

**Use for:** User with one weight and one workout; get products; testing “add product” when user already has a product.

---

### 3.4 User 4 – Dave Brown (USER, profile only)

| Field | Value |
|-------|--------|
| **Email** | `dave@demo.com` |
| **Password** | `password123` |
| **Role** | USER |
| **Profile** | Height 178 cm, target 75 kg, DOB 1990-07-14 |

**Demo data:**

- **Weight entries:** None.
- **Workouts:** None.
- **Products:** None.

**Use for:** Empty weight/workout/product lists; first weight entry, first workout, first product add; journey with no/single entry.

---

### 3.5 User 5 – Eve Admin (ADMIN)

| Field | Value |
|-------|--------|
| **Email** | `admin@demo.com` |
| **Password** | `password123` |
| **Role** | ADMIN |
| **Profile** | Height 168 cm, target 62 kg, DOB 1985-01-01 |

**Demo data:**

- **Weight entries:** 1 entry (63 kg on 2025-03-01).
- **Workouts:** 1 workout – Strength on 2025-03-05 (50 min, 300 cal, intensity 6).
- **Products:** None (admin can create products via API).

**Use for:** All admin endpoints: get users, create product, get products, upload product image, delete product. Also tests that admin can call user endpoints (e.g. get user, weight, workout).

---

### 3.6 Seeded products (for “add product by code”)

| Code | Name | Description | Calories | Protein | Carbs | Fat |
|------|------|-------------|----------|---------|-------|-----|
| 1001 | Choco Protein Bar | High protein snack | 200 | 15 | 22 | 8 |
| 1002 | Oatmeal Pack | Quick breakfast | 150 | 5 | 27 | 3 |
| 1003 | Banana | Pre-workout fruit | 105 | 1.3 | 27 | 0.4 |

---

### 3.7 Testing with demo users in the test plan

- **Auth → Authenticate:** Use any of the emails above with `password123`; use **alice@demo.com** or **bob@demo.com** for user flows, **admin@demo.com** for admin flows.
- **Get current user / Get products / Get weight / Get journey:** After authenticating as **alice@demo.com** you should see 2 products, 3 weight entries, 1 workout.
- **Delete weight entry:** Authenticate as Alice, GET weight entries, pick an `id`, then DELETE that id.
- **Add product by code:** Authenticate as **dave@demo.com**, POST `/api/v1/secured/user/products/1001` (product 1001 exists from seed).
- **Admin:** Authenticate as **admin@demo.com**, then GET users (should list Alice, Bob, Carol, Dave – not Eve, if your API returns only USER role), GET products (should list 1001, 1002, 1003), then create/upload/delete products as needed.

---

## 4. API Inventory

| # | Method | Endpoint | Auth | Description |
|---|--------|----------|------|-------------|
| 1 | POST | `/api/v1/auth/register` | No | Register new user |
| 2 | POST | `/api/v1/auth/authenticate` | No | Login; sets JWT cookie |
| 3 | POST | `/api/v1/auth/logout` | No* | Logout; clears cookie |
| 4 | GET | `/api/v1/secured/user` | User | Get current user info |
| 5 | PUT | `/api/v1/secured/user/profile` | User | Update profile (height, target weight) |
| 6 | GET | `/api/v1/secured/user/credentials` | User | Get user credentials only |
| 7 | GET | `/api/v1/secured/user/products` | User | Get user's products |
| 8 | POST | `/api/v1/secured/user/products/{code}` | User | Add product to user by code |
| 9 | POST | `/api/v1/secured/user/weight` | User | Add weight entry |
| 10 | GET | `/api/v1/secured/user/weight` | User | Get weight entries (optional from/to) |
| 11 | GET | `/api/v1/secured/user/weight/journey` | User | Get weight journey summary |
| 12 | DELETE | `/api/v1/secured/user/weight/{id}` | User | Delete weight entry |
| 13 | POST | `/api/v1/secured/user/workout` | User | Add workout |
| 14 | DELETE | `/api/v1/secured/user/workout/{id}` | User | Delete workout |
| 15 | GET | `/api/v1/secured/admin/users` | Admin | Get all users |
| 16 | POST | `/api/v1/secured/admin/product` | Admin | Create product |
| 17 | GET | `/api/v1/secured/admin/product` | Admin | Get all products |
| 18 | POST | `/api/v1/secured/admin/product/image/{code}` | Admin | Upload product image |
| 19 | DELETE | `/api/v1/secured/admin/product/{code}` | Admin | Delete product |

\* Logout may or may not require a valid cookie depending on implementation; test with and without cookie.

---

## 5. Test Execution Order

Authentication is cookie-based. Recommended order:

1. **Auth (no cookie)**  
   - Register → Authenticate (saves cookie in client/Postman).

2. **User APIs (with user cookie)**  
   - Call Authenticate as a **USER** (or use the user created in Register), then run:  
     Get user → Update profile → Get credentials → Weight (add → get → journey → delete) → Workout (add → delete) → Products (get → add by code).  
   - For “add product by code”, a product must exist (created by Admin).

3. **Admin APIs (with admin cookie)**  
   - Register or use an **ADMIN** user, Authenticate, then:  
     Get users → Create product → Get products → Upload product image → Delete product (optional, by code).

4. **Logout**  
   - Call Logout (with cookie); then verify secured endpoints return 401/403 when cookie is cleared.

---

## 6. Test Cases (Detailed)

### 6.1 Auth – Register

- **Endpoint:** `POST /api/v1/auth/register`
- **Body (JSON):**
  ```json
  {
    "firstname": "Test",
    "lastname": "User",
    "email": "testuser@example.com",
    "password": "password123",
    "dateOfBirth": "1990-01-15"
  }
  ```
- **Expect:** 2xx (e.g. 200/201); or 4xx with clear message if email exists / validation fails.
- **Negative:** Duplicate email, invalid email format, missing required fields → 4xx.

---

### 6.2 Auth – Authenticate

- **Endpoint:** `POST /api/v1/auth/authenticate`
- **Body (JSON):**
  ```json
  {
    "email": "testuser@example.com",
    "password": "password123"
  }
  ```
- **Expect:** 2xx; response headers must include `Set-Cookie` with cookie name `subproblem` and JWT value.
- **Negative:** Wrong password or unknown email → 401.

---

### 6.3 Auth – Logout

- **Endpoint:** `POST /api/v1/auth/logout`
- **Body:** None (or empty).
- **Expect:** 2xx; cookie cleared (e.g. `Set-Cookie` with empty or max-age=0).
- **Note:** Call with and without cookie to document behavior.

---

### 6.4 User – Get current user

- **Endpoint:** `GET /api/v1/secured/user`
- **Auth:** Cookie from Authenticate (user role).
- **Expect:** 200; body contains user info (e.g. id, email, firstname, lastname, profile fields).
- **Negative:** No cookie or invalid/expired token → 401/403.

---

### 6.5 User – Update profile

- **Endpoint:** `PUT /api/v1/secured/user/profile`
- **Body (JSON):**
  ```json
  {
    "heightCm": 175.0,
    "targetWeightKg": 70.0
  }
  ```
- **Expect:** 200; response body reflects updated profile.
- **Optional:** Omit one field (e.g. only `heightCm`) and verify partial update.

---

### 6.6 User – Get credentials

- **Endpoint:** `GET /api/v1/secured/user/credentials`
- **Auth:** User cookie.
- **Expect:** 200; body contains credentials (e.g. email; password should not be plain text if exposed).

---

### 6.7 User – Get products

- **Endpoint:** `GET /api/v1/secured/user/products`
- **Auth:** User cookie.
- **Expect:** 200; array of products (may be empty).

---

### 6.8 User – Add product by code

- **Endpoint:** `POST /api/v1/secured/user/products/{code}`
- **Path:** `{code}` = existing product code (integer).
- **Auth:** User cookie.
- **Expect:** 2xx when product exists; 4xx when product not found or already added.
- **Prerequisite:** Create at least one product via Admin “Create product”.

---

### 6.9 Weight – Add entry

- **Endpoint:** `POST /api/v1/secured/user/weight`
- **Body (JSON):**
  ```json
  {
    "weightKg": 72.5,
    "date": "2025-03-10",
    "note": "Morning weigh-in"
  }
  ```
- **Expect:** 201; response body contains created weight entry (id, weightKg, date, note, etc.).
- **Optional:** Omit `date` or `note` and verify defaults/optional handling.

---

### 6.10 Weight – Get entries

- **Endpoint:** `GET /api/v1/secured/user/weight`
- **Query (optional):** `from=2025-03-01&to=2025-03-31` (ISO date).
- **Auth:** User cookie.
- **Expect:** 200; array of weight entries. With from/to, list filtered by date range.

---

### 6.11 Weight – Get journey

- **Endpoint:** `GET /api/v1/secured/user/weight/journey`
- **Auth:** User cookie.
- **Expect:** 200; body contains journey summary (e.g. start/current/target weight, stats).

---

### 6.12 Weight – Delete entry

- **Endpoint:** `DELETE /api/v1/secured/user/weight/{id}`
- **Path:** `{id}` = MongoDB ObjectId of a weight entry from “Get entries” or “Add entry”.
- **Auth:** User cookie.
- **Expect:** 204 No Content. Then GET weight entries should no longer return this id.
- **Negative:** Wrong id or another user’s entry → 403/404.

---

### 6.13 Workout – Add

- **Endpoint:** `POST /api/v1/secured/user/workout`
- **Body (JSON):**
  ```json
  {
    "id": "",
    "workoutDate": "2025-03-10",
    "exerciseType": "Running",
    "performanceMetricsResponse": {
      "duration": 45.0,
      "caloriesBurned": 350,
      "intensity": 7
    }
  }
  ```
- **Expect:** 201.
- **Negative:** Duplicate workout for same date → 4xx (e.g. 409 or 400 with message).

---

### 6.14 Workout – Delete

- **Endpoint:** `DELETE /api/v1/secured/user/workout/{id}`
- **Path:** `{id}` = workout id (from add response or from user’s workout list if such endpoint exists).
- **Auth:** User cookie.
- **Expect:** 204. Negative: invalid id or not owner → 403/404.

---

### 6.15 Admin – Get all users

- **Endpoint:** `GET /api/v1/secured/admin/users`
- **Auth:** Admin cookie.
- **Expect:** 200; array of user objects.
- **Negative:** User role cookie → 403.

---

### 6.16 Admin – Create product

- **Endpoint:** `POST /api/v1/secured/admin/product`
- **Body (JSON):**
  ```json
  {
    "description": "Protein bar",
    "name": "Choco Bar",
    "calories": 200,
    "protein": 15.0,
    "carbs": 20.0,
    "fat": 8.0,
    "code": 1001
  }
  ```
- **Expect:** 2xx; product created. Use same `code` later for “Upload product image” and “Add product to user”.

---

### 6.17 Admin – Get all products

- **Endpoint:** `GET /api/v1/secured/admin/product`
- **Auth:** Admin cookie.
- **Expect:** 200; array of products.

---

### 6.18 Admin – Upload product image

- **Endpoint:** `POST /api/v1/secured/admin/product/image/{code}`
- **Path:** `{code}` = product code (e.g. 1001).
- **Body:** `multipart/form-data`; key `image`, type file (e.g. JPEG/PNG).
- **Expect:** 2xx when product exists; 4xx when code not found or invalid file.

---

### 6.19 Admin – Delete product

- **Endpoint:** `DELETE /api/v1/secured/admin/product/{code}`
- **Path:** `{code}` = product code.
- **Auth:** Admin cookie.
- **Expect:** 2xx (e.g. 204 or 200). Then GET products should not list this code.

---

## 7. Security & Error Checks

- **401 Unauthorized:** No cookie or invalid/expired JWT on secured endpoints.
- **403 Forbidden:** Valid cookie but wrong role (e.g. USER calling admin endpoints).
- **404 Not Found:** Invalid path or resource id.
- **400 Bad Request:** Validation errors (e.g. invalid date, missing required fields).
- **409 Conflict / 4xx:** Business rules (e.g. duplicate email, duplicate workout date).

---

## 8. Postman Usage

- Import the provided **Fitness-Tracking-API.postman_collection.json**.
- Set collection variable **`baseUrl`** to `http://localhost:8080` (or your app URL).
- **Cookies:** Use Postman’s default “Send cookies with requests”. Run **Auth → Authenticate** first; the `subproblem` cookie will be stored and sent on subsequent requests.
- **User flow:** Register → Authenticate (as user) → run User / Weight / Workout / User Products requests. With demo data, use **alice@demo.com** / **password123** for a user who already has weight entries, workouts, and products.
- **Admin flow:** Authenticate as **admin@demo.com** / **password123** → run Admin requests (get users, products; create/upload/delete product).
- For **Upload product image**, use the “Body” tab → “form-data” → key `image`, type “File”, choose a small image.
- **Workout delete:** Add workout returns 201 with no body. Run **Workout → Get current user (to get workout id)** after adding a workout; the test script will set `workoutId` from the first workout. Then run **Delete workout**.

---

## 9. Automated Testing (Optional)

- Use **Postman Collection Runner** or **Newman** (CLI) to run the collection and assert status codes and response bodies.
- Alternatively, add **Spring Boot integration tests** (MockMvc or TestRestTemplate) that:
  - Register and authenticate (or mock JWT).
  - Call each endpoint and assert status and JSON paths.

---

## 10. Summary Checklist

- [ ] Register new user (success + duplicate email)
- [ ] Authenticate (success + wrong password)
- [ ] Logout and verify cookie cleared
- [ ] Get/update user profile and credentials (with user cookie)
- [ ] Weight: add, get (with/without from/to), journey, delete
- [ ] Workout: add, delete
- [ ] User products: get, add by code (after admin creates product)
- [ ] Admin: get users, create product, get products, upload image, delete product
- [ ] All secured endpoints return 401/403 without valid cookie or with wrong role
- [ ] Test with demo users (profile `demo`): authenticate as alice@demo.com, bob@demo.com, admin@demo.com and verify expected data per Section 3
