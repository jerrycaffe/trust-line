# Trustline Backend — Deployment Runbook

This document is the **complete, step-by-step guide** for deploying the Trustline
Spring Boot (Kotlin) backend for a new institution. Follow the sections in order:
provision infrastructure → configure secrets → run the app → onboard the
institution → verify.

---

## 1. Prerequisites (what you need before touching code)

### 1.1 Accounts / external services to create

| Service | Purpose | What to obtain |
|---|---|---|
| **PostgreSQL 13+** (Supabase, RDS, Cloud SQL, self-hosted) | Primary datastore. App runs Flyway migrations on boot. | JDBC URL, username, password. SSL recommended. |
| **Resend** (https://resend.com) | Transactional email (OTPs, invites, case notifications). Sender is hard-coded to `admin@trustline.com.ng` — you must verify that domain in Resend, or change the sender in `EmailServiceImpl.kt`. | `RESEND_API_KEY` |
| **Cloudinary** (https://cloudinary.com) | All file uploads (profile photos, case attachments, resource documents). | Cloud name, API key, API secret. Cloud name is currently hard-coded as `ddj3hjr57` in the YAML files — change it per environment if you want a dedicated Cloudinary account. |
| **Firebase** (optional, but expected by config) | Service-account JSON is loaded at startup. Currently only used for the bundled WebSocket signaling; FCM push is not wired in yet. You can ship any valid service-account JSON. | Service-account JSON file. |
| **Docker Hub / container registry** (optional, for CI) | Image distribution. | Repo + push credentials. |
| **Jenkins + Render** (optional, used by the existing `Jenkinsfile`) | CI/CD pipeline. | Jenkins creds `dockerhub-creds`, `render-service-id`, `render-api-key`. |
| **HashiCorp Vault** (optional) | `bootstrap.yml` is wired for Vault; if `VAULT_ADDR` / `VAULT_TOKEN` are set, secrets are pulled from `secret/trustline/...`. If you don't use Vault, just provide env vars directly. | `VAULT_ADDR`, `VAULT_TOKEN` |

### 1.2 Local tooling (for building / running)

- **JDK 21** (Kotlin toolchain is pinned to 21 in [build.gradle.kts](build.gradle.kts))
- **Gradle 8.10+** — the included `./gradlew` wrapper handles this
- **Docker** (if you want to build/run via container)
- **psql** or any Postgres client (for verification)

---

## 2. Provision the database

1. Create an empty Postgres database (e.g. `trustline_prod`).
2. Ensure the connecting role can `CREATE EXTENSION` — the base migration installs `uuid-ossp`.
   - If your managed Postgres restricts extensions (e.g. Supabase pooler), enable `uuid-ossp` via the platform UI first.
3. Capture the JDBC URL in the form:
   ```
   jdbc:postgresql://<host>:<port>/<db>?sslmode=require&prepareThreshold=0
   ```
   `prepareThreshold=0` is recommended when sitting behind a connection pooler (PgBouncer, Supabase pooler).

**Flyway runs automatically on startup.** All migrations under
[src/main/resources/db/migration](src/main/resources/db/migration) are applied in
order. You do **not** need to run any SQL by hand.

What the migrations build for you:

| Area | Created by |
|---|---|
| Core users / roles / permissions / join tables | [V2024.08.31_03.04__base.sql](src/main/resources/db/migration/V2024.08.31_03.04__base.sql) |
| OTP verification | [V2025.07.21_19.51__verification.sql](src/main/resources/db/migration/V2025.07.21_19.51__verification.sql) |
| `institutions` table | [V2026.03.14_04.28__institution_config.sql](src/main/resources/db/migration/V2026.03.14_04.28__institution_config.sql) |
| Units per institution | [V2026.03.14_06.00__create_units.sql](src/main/resources/db/migration/V2026.03.14_06.00__create_units.sql) |
| `users.institution_id` FK | [V2026.03.14_08.30__add_user_institution.sql](src/main/resources/db/migration/V2026.03.14_08.30__add_user_institution.sql) |
| Cases, incident types, comments, file uploads, notifications | [V2026.03.15_00.01__cases.sql](src/main/resources/db/migration/V2026.03.15_00.01__cases.sql) |
| Resources | [V2026.04.04_04.18__resources.sql](src/main/resources/db/migration/V2026.04.04_04.18__resources.sql) |
| **Default global roles + permissions seeded** | [V2026.04.13_06.30__add_default_roles_permissions.sql](src/main/resources/db/migration/V2026.04.13_06.30__add_default_roles_permissions.sql) |
| Institution-scoping for roles / permissions | [V2026.04.25_00.03__add_institution_to_roles_permissions.sql](src/main/resources/db/migration/V2026.04.25_00.03__add_institution_to_roles_permissions.sql) |
| Institution-scoped unique constraints + dynamic permissions | V2026.05.13_00.01__institution_scoped_uniques_and_dynamic_permissions.sql |
| Activities + entries | later `V2026.*` migrations under the same folder |

**Seed data created automatically (global, `institution_id = NULL`):**

- Roles: `User`, `Administrator`
- Permissions: `VIEW_ALL_USERS`, `CREATE_ADMIN_USER`, `MANAGE_USERS`, `MANAGE_ROLES`,
  `MANAGE_PERMISSIONS`, `MANAGE_CASES`, `MANAGE_INCIDENT_TYPES`,
  `MANAGE_RESOURCES`, `MANAGE_NOTIFICATIONS`
- The `Administrator` role is wired to all of the above

> These are **global** rows. They are NOT created per institution — every
> institution's admins inherit them through the `Administrator` role.

---

## 3. Configure secrets / environment variables

The app reads configuration from `application-<profile>.yml`. Pick a profile
(`dev`, `prod`, or `local`) via `SPRING_PROFILES_ACTIVE`. The following
environment variables **must** be set:

```bash
# --- Spring profile ---
SPRING_PROFILES_ACTIVE=prod

# --- Database ---
TRUSTLINE_SERVER_DATABASE_URL=jdbc:postgresql://HOST:PORT/DB?sslmode=require&prepareThreshold=0
TRUSTLINE_SERVER_DATABASE_USERNAME=...
TRUSTLINE_SERVER_DATABASE_PASS=...

# --- JWT ---
JWT_KEY=trustline                       # arbitrary key id
JWT_SECRET=<min 64 random chars>        # HS512 secret; KEEP SECRET

# --- Email (Resend) ---
RESEND_API_KEY=re_xxx

# --- Cloudinary ---
CLOUDINARY_API_KEY=...
CLOUDINARY_SECRET=...
# (Cloud name `ddj3hjr57` is hard-coded in the YAMLs — edit if needed.)

# --- Firebase (file must exist at this path inside the container) ---
FIREBASE_CONFIG_LOCATION=/app/firebase/service-account.json

# --- Server ---
PORT=8080

# --- Vault (optional; only if you want Spring Cloud Vault to resolve secrets) ---
VAULT_ADDR=https://vault.example.com
VAULT_TOKEN=...
```

Configured but **not currently wired in code** (safe to leave unset):
`SENDGRID_API_KEY`, `MAIL_SENDER_API_KEY`.

### Generating a JWT secret

```bash
openssl rand -base64 64
```

### Where these are read

- DB / JPA / Flyway: [application-prod.yml](src/main/resources/application-prod.yml)
- JWT signing & verification: [JWTConfigServiceImple.kt](src/main/kotlin/trustline/config/security/JWTConfigServiceImple.kt)
- Email (Resend): [EmailServiceImpl.kt](src/main/kotlin/trustline/appuser/service/EmailServiceImpl.kt)
- Cloudinary: [CloudinaryConfig.kt](src/main/kotlin/trustline/config/CloudinaryConfig.kt)
- Vault bootstrap: [bootstrap.yml](src/main/resources/bootstrap.yml)

---

## 4. Firebase configuration file

The application expects a Firebase service-account JSON at the path given by
`FIREBASE_CONFIG_LOCATION`.

- Generate one in Firebase Console → Project Settings → Service Accounts → "Generate new private key".
- For Docker, mount it as a volume or copy it into the image at a known path and
  point `FIREBASE_CONFIG_LOCATION` to it.
- The example file `src/main/resources/firebase/trustline-b92df-7c1a0fdeb463.json`
  is git-ignored — **do not commit your real key**.

---

## 5. Build & run

### 5.1 Local run (development)

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 5.2 Build a fat JAR

```bash
./gradlew clean bootJar
java -jar build/libs/trustline-*.jar --spring.profiles.active=prod
```

### 5.3 Docker

Build:
```bash
docker build -t trustline:latest .
```

Run:
```bash
docker run -d --name trustline -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e TRUSTLINE_SERVER_DATABASE_URL=... \
  -e TRUSTLINE_SERVER_DATABASE_USERNAME=... \
  -e TRUSTLINE_SERVER_DATABASE_PASS=... \
  -e JWT_KEY=trustline -e JWT_SECRET=... \
  -e RESEND_API_KEY=... \
  -e CLOUDINARY_API_KEY=... -e CLOUDINARY_SECRET=... \
  -e FIREBASE_CONFIG_LOCATION=/app/firebase/service-account.json \
  -v /secrets/firebase.json:/app/firebase/service-account.json:ro \
  trustline:latest
```

The Dockerfile is a multi-stage build (Gradle → Temurin 21). See
[Dockerfile](Dockerfile).

### 5.4 CI/CD via Jenkins → Render

The included [Jenkinsfile](Jenkinsfile) builds, tests, pushes to Docker Hub, and
triggers a Render deploy. Required Jenkins credentials:

- `dockerhub-creds` — Docker Hub username + password
- `render-service-id` — Render service id
- `render-api-key` — Render API key

On startup, watch the logs for:
```
o.f.core.internal.command.DbMigrate : Successfully applied N migrations
Tomcat started on port(s): 8080
```

---

## 6. First-time institution onboarding

This is the **exact API call sequence** to create a brand-new institution and
its first administrator. Replace `BASE_URL` with your deployed host.

### Step 1 — Create the institution
Public endpoint, no auth.
```http
POST {BASE_URL}/api/v1/institution
Content-Type: application/json

{
  "name": "Acme Corp",
  "contactNumber": "08000000000",
  "address": "1 Main St, Lagos"
}
```
Save the returned `id` — this is your `institutionId`.

### Step 2 — Register the first user (will be promoted to admin)
Public endpoint.
```http
POST {BASE_URL}/api/v1/auth/register
Content-Type: application/json

{
  "email": "admin@acme.com",
  "password": "StrongPassw0rd!",
  "phoneNumber": "08011111111",
  "firstName": "Acme",
  "lastName": "Admin",
  "institutionId": "<institutionId from step 1>"
}
```
The user is created with status `OTP_VALIDATION` and emailed a 6-digit code by Resend.

### Step 3 — Verify the OTP
```http
POST {BASE_URL}/api/v1/auth/verify-otp
Content-Type: application/json

{
  "verificationId": "<from email/response>",
  "userId": "<userId from step 2>",
  "otp": "123456"
}
```
The user status becomes `ACTIVE`.

### Step 4 — Promote the user to `Administrator`
By default, self-registration assigns the global `User` role. To give the first
user admin access you have two options:

**Option A (recommended, one-time SQL):** While bootstrapping, attach the
global `Administrator` role to the new user directly:
```sql
INSERT INTO users_roles (user_id, role_id)
SELECT
  (SELECT id FROM users WHERE email = 'admin@acme.com' AND institution_id = '<institutionId>'),
  (SELECT id FROM roles WHERE name = 'Administrator' AND institution_id IS NULL);
```

**Option B:** Use an existing super-admin account (from another institution or a
seeded one) to call `POST /api/v1/admin/users/{userId}/roles` and attach the
Administrator role.

> After this step, **all subsequent admin actions** can be done via the API by
> logging in as this user.

### Step 5 — Log in and grab a JWT
```http
POST {BASE_URL}/api/v1/auth/login
Content-Type: application/json

{
  "email": "admin@acme.com",
  "password": "StrongPassw0rd!",
  "institutionId": "<institutionId>"
}
```
Use the returned token as `Authorization: Bearer <token>` for everything below.

### Step 6 — Create units (teams/departments within the institution)
```http
POST {BASE_URL}/api/v1/institution/units
{
  "name": "Patrol",
  "institutionId": "<institutionId>"
}
```

### Step 7 — Create incident types
```http
POST {BASE_URL}/api/v1/incident-types
{
  "name": "Theft",
  "description": "..."
}
```

### Step 8 — Invite more staff (admins or regular users)
```http
POST {BASE_URL}/api/v1/admin/invite
{
  "email": "officer@acme.com",
  "firstName": "Jane",
  "lastName": "Doe",
  "roleIds": ["<roleId>"]
}
```
A temp password is emailed to the invitee.

### Step 9 — (Optional) Create institution-scoped roles & permissions
The seeded global roles (`User`, `Administrator`) and permissions
(`MANAGE_*`, `VIEW_ALL_USERS`, etc.) are shared across all institutions. You can
also create roles/permissions **scoped to your institution only**:

```http
POST {BASE_URL}/api/v1/admin/roles
{ "name": "Supervisor", "description": "Reviews cases" }

POST {BASE_URL}/api/v1/admin/permissions
{ "name": "EXPORT_REPORTS", "description": "Can export reports" }

POST {BASE_URL}/api/v1/admin/roles/{roleId}/permissions
{ "permissionIds": ["..."] }
```
Authorization uses Spring Security's `@PreAuthorize("hasAuthority('...')")` —
either a role name or a permission name works.

---

## 7. Multi-tenancy notes (important)

The codebase enforces per-institution isolation:
- Every domain table has an `institution_id` FK.
- Login is `(email, password, institutionId)` — the same email can exist in
  multiple institutions.
- Repositories take `institutionId` (sourced from the JWT) on every query, so
  Acme admins cannot see Beta's data.

**However**, the following are currently shared across all tenants:
- **Cloudinary cloud** (`ddj3hjr57`) — all institutions upload to the same bucket.
- **Resend account** — single sender domain (`admin@trustline.com.ng`).
- **Firebase project** — one service-account JSON.
- **Global default roles/permissions** — shared rows, not duplicated per institution.

If you need full per-institution isolation of these, you'll need code changes
(introduce a per-institution config table holding API keys, and resolve credentials
by `institutionId` at runtime).

---

## 8. Post-deploy smoke test

Run through this checklist after every deploy:

- [ ] `GET {BASE_URL}/actuator/health` returns 200 (if actuator enabled)
- [ ] Logs show `Successfully applied N migrations` and no Flyway errors
- [ ] `POST /api/v1/institution` creates an institution
- [ ] `POST /api/v1/auth/register` triggers a real OTP email arriving in inbox
- [ ] `POST /api/v1/auth/verify-otp` activates the account
- [ ] `POST /api/v1/auth/login` returns a JWT
- [ ] A profile-image upload succeeds (Cloudinary credentials valid)
- [ ] A protected endpoint with `Authorization: Bearer <token>` returns 200
- [ ] Cross-institution isolation: a token from institution A returns 404/empty
  when querying institution B's data

---

## 9. Quick reference — secret checklist

Use this as the final pre-deploy gate:

| Required | Variable | Source |
|:---:|---|---|
| ✅ | `SPRING_PROFILES_ACTIVE` | you choose (`prod` / `dev` / `local`) |
| ✅ | `TRUSTLINE_SERVER_DATABASE_URL` | DB provider |
| ✅ | `TRUSTLINE_SERVER_DATABASE_USERNAME` | DB provider |
| ✅ | `TRUSTLINE_SERVER_DATABASE_PASS` | DB provider |
| ✅ | `JWT_KEY` | any string |
| ✅ | `JWT_SECRET` | `openssl rand -base64 64` |
| ✅ | `RESEND_API_KEY` | Resend dashboard |
| ✅ | `CLOUDINARY_API_KEY` | Cloudinary dashboard |
| ✅ | `CLOUDINARY_SECRET` | Cloudinary dashboard |
| ✅ | `FIREBASE_CONFIG_LOCATION` + the JSON file | Firebase console |
| ⚪ | `VAULT_ADDR`, `VAULT_TOKEN` | only if using Vault |
| ⚪ | `PORT` | defaults to 8080 |
