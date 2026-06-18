# EmployeeSearch (MT_POC_JAVA) — Web App Run Guide

Modernized web replacement for the WinForms desktop app: **Angular 17/19 + Material**
frontend over a **Java 17+/Spring Boot 3.3** REST backend, reusing the **existing SQLite DB
as-is** (`ddl-auto=validate`, C1).

**Project root:** `D:\career\projects\MT_POC_JAVA` (this folder).
The legacy app, the existing DB, and the design/requirements docs remain under
`D:\career\projects\MT_POC2` (authoritative contracts: `MT_POC2\AgentInstructionSet\DESIGN_MT_POC2.md`,
build plan: `MT_POC2\AgentInstructionSet\PLAN_MT_POC2.md`).

Source: [`backend/`](backend/) · [`frontend/`](frontend/).

## Toolchain on this machine
- JDK 21: `C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot` (set `JAVA_HOME` to it — already set at user scope).
- Maven 3.9.9: `D:\tools\apache-maven-3.9.9\bin\mvn.cmd` (downloaded; not previously installed).
- Node 20 / npm 10 / Angular CLI 19 (design specifies Angular 17; 19 was used as installed — standalone APIs/Material are compatible).

## Backend  (http://localhost:8080)
```powershell
cd D:\career\projects\MT_POC_JAVA
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot"
# Reuse the existing DB (stays in MT_POC2); inject a JWT secret (never commit one).
$env:DB_PATH = "D:\career\projects\MT_POC2\bin\Debug\net8.0-windows\employeesearch.db"
$env:JWT_SECRET = "any-long-random-string-at-least-32-chars-1234567890"
$env:SPRING_PROFILES_ACTIVE = "dev"

# The fat jar is already built — just run it:
& "$env:JAVA_HOME\bin\java" -jar backend\target\employeesearch-api.jar

# To rebuild / run tests instead:
& "D:\tools\apache-maven-3.9.9\bin\mvn.cmd" -f backend\pom.xml test               # 15 tests
& "D:\tools\apache-maven-3.9.9\bin\mvn.cmd" -f backend\pom.xml package -DskipTests
```

## Frontend  (http://localhost:4200)
```powershell
cd D:\career\projects\MT_POC_JAVA
npm --prefix frontend install     # already done
npm --prefix frontend start       # ng serve -> http://localhost:4200
# Production build (optional): npm --prefix frontend run build  -> dist/frontend
```

## Login
Use the existing `admin` account from the DB (desktop default was `admin` / `admin123`).
The desktop "default credentials" UI hint is intentionally removed (SEC-1).

## API contract (all under `/api`, bearer JWT except login)
| Method | Path | Notes |
|---|---|---|
| POST | `/api/auth/login` | `{username,password}` → `{token,username,expiresAt}`; 400 blank, 401 bad creds |
| POST | `/api/auth/logout` | 204 (stateless; client discards token) |
| GET | `/api/employees?name=&department=&role=&status=` | sorted by Name; `All`/blank ignored |
| GET | `/api/employees/export?...` | `text/csv` download; 409 if empty |
| GET | `/api/meta/filters` | fixed dropdown domains (each prefixed `All`) |

## Notes / decisions
- **C1 mapping:** `Users`/`Employees` mapped with exact casing; standard physical naming strategy
  preserves it; `@JdbcTypeCode` pins `Id`→INTEGER and `Salary`→REAL to match SQLite affinity so
  `ddl-auto=validate` passes against the real DB (DESIGN §4.3 R-1). Schema never altered.
- **C2 auth:** `AuthenticationProvider` seam — `DbAuthenticationProvider` (BCrypt, dev/default)
  active now; `AdAuthenticationProvider` is a `TODO (AD)` placeholder under the `prod` profile.
- CSV is UTF-8 **no BOM** (OQ-5), raw numeric salary, CRLF, BR-8 quoting; filename uses server
  local time (OQ-7). Native SQLite `LIKE` semantics preserved (OQ-4), not "fixed".
