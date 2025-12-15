# Voting System (Spring Boot)

A simple voting system backend built with Spring Boot. It allows administrators to manage voters and elections (with options), and allows users to vote in specific election instances (one vote per voter per election). The project demonstrates layered architecture, DTOs, validation, idempotent asynchronous voting with Kafka, database migrations with Flyway, and standardized error responses with RFC 7807 (ProblemDetail). Swagger UI is included for easy testing.

---

## Features
- Voters management (admin):
  - Create voters (with required PESEL; stored as SHA‑256 hash only)
  - Block / unblock voters
  - Pagination & sorting for lists
- Elections management (admin):
  - CRUD for election instances
  - Manage election options (candidates) per election
  - Results endpoint: count votes per option (includes zero‑vote options)
- Voting (user):
  - List active elections and options
  - Submit a vote (asynchronous via Kafka)
  - One vote per voter per election (DB and app enforced)
- Persistence: PostgreSQL via JPA/Hibernate; Flyway migrations
- Robustness:
  - Idempotency for processed vote events
  - Time‑window validation for elections
  - Voter must exist and not be blocked
- API docs: Swagger/OpenAPI via springdoc
- Tests:
  - Unit tests for validators and services
  - Integration boot test with Testcontainers (PostgreSQL + Kafka)

---

## Stack
- Java 17+
- Spring Boot 3.x
- Spring Web, Spring Data JPA, Spring for Apache Kafka
- PostgreSQL, Flyway
- springdoc-openapi (Swagger UI)
- JUnit 5, Testcontainers

---

## Getting started

### Prerequisites
- JDK 17+
- Maven 3.9+
- Docker Desktop (for running tests with Testcontainers; optional for local manual run)
- PostgreSQL 16+ (if running app locally without Testcontainers)
- Apache Kafka broker (if testing async voting locally)

### Configure database & Kafka (local dev)
Default configuration is defined in `src/main/resources/application.properties`:
- PostgreSQL URL: `jdbc:postgresql://localhost:5432/voting`
- Username/password: `voting` / `voting`
- Kafka bootstrap servers: `localhost:9092`
- Flyway migrations enabled

You can run local services using the provided `docker-compose.yml` (Postgres + Kafka):

```
# from project root
# Windows PowerShell:
# docker compose up -d
```

After containers start, the app can connect to both DB and Kafka.

### Swagger / OpenAPI
- Swagger UI: http://localhost:8080/swagger-ui
- OpenAPI JSON: http://localhost:8080/v3/api-docs

---

## API overview

Admin endpoints (`/api/admin/**`):
- Voters
  - `GET /api/admin/voters` — list voters (Page)
  - `POST /api/admin/voters` — create voter
  - `GET /api/admin/voters/{id}` — get by id
  - `PUT /api/admin/voters/{id}` — update voter
  - `PATCH /api/admin/voters/{id}/block` — block
  - `PATCH /api/admin/voters/{id}/unblock` — unblock
- Elections & options
  - `GET /api/admin/elections` — list elections (Page)
  - `POST /api/admin/elections` — create
  - `GET /api/admin/elections/{id}` — get by id
  - `PUT /api/admin/elections/{id}` — update
  - `DELETE /api/admin/elections/{id}` — delete
  - `GET /api/admin/elections/{electionId}/options` — list options
  - `POST /api/admin/elections/{electionId}/options` — add option
  - `PUT /api/admin/elections/{electionId}/options/{optionId}` — update option
  - `DELETE /api/admin/elections/{electionId}/options/{optionId}` — delete option
  - `GET /api/admin/elections/{electionId}/results` — election results (votes per option)

User endpoints (`/api/elections/**`):
- `GET /api/elections/active` — list currently active elections
- `GET /api/elections/{electionId}/options` — list options for an election
- `POST /api/elections/{electionId}/votes` — submit a vote (async)
  - Body: `{ "voterId": number, "optionId": number }`
  - Response: `202 Accepted` with `{ "status": "accepted", "eventId": "uuid" }`


---

## Security
This repository is a demo and intentionally keeps security minimal to simplify testing. In a production system, you should implement the following security controls (not included here due to the demonstrational character of the solution):

- Authentication & authorization
  - OAuth2/OIDC with JWT (Bearer tokens) or an external identity provider (Keycloak).
  - Role-based access control (at least roles: ADMIN and USER) with endpoint rules and method-level guards (`@PreAuthorize`).
  - Protect admin API (`/api/admin/**`) for ADMIN only; restrict vote submission to USER; public read endpoints as needed.
- Passwords & accounts
  - Store user passwords using a strong one-way hash (BCrypt).
  - Account lockout, password rotation policies, optional MFA for admin accounts.
- Transport-level security
  - Enforce HTTPS/TLS (terminate TLS at load balancer or app server), enable HSTS in production.
- API security & clients
  - CORS: restrictive allowlist of origins, methods, and headers for browser clients.
  - Rate limiting and brute-force protection (e.g., at API gateway/reverse proxy).
- Sensitive data protection
  - Data at rest: sensitive identifiers should be hashed or encrypted. PESEL is already stored as SHA‑256 hash; consider additional encryption for other sensitive data if introduced.
- Documentation & tooling
  - Add the OpenAPI security scheme (Bearer) and require authorization in Swagger UI; disable Swagger in production.
- Observability & auditing
  - Structured audit logs (who did what and when) for all admin operations and voting attempts.
  - Centralized logging/metrics/tracing with alerting on anomalies.
- Platform hardening
  - Secure Actuator endpoints (expose only health/info) and protect them with auth/network policies.
  - Set secure HTTP headers (CSP, X-Content-Type-Options, X-Frame-Options, Referrer-Policy, etc.).
---

## Migrations and seed data
Flyway migration files:
- `V1__init.sql` — base schema
- `V2__add_processed_events.sql` — idempotency table for events
- `V3__add_test_voters.sql` — seeds ~15 test voters (PESEL stored as SHA‑256 hash only)
- `V4__add_test_elections.sql` — seeds 3 election instances:
  - Past: 2023‑01‑01 (08:00–20:00 UTC)
  - Current: entire December 2025
  - Future: 2027‑01‑01 (08:00–20:00 UTC)

On first run, Flyway will apply migrations automatically and load seed data.

---

## Asynchronous voting flow
1. Client calls `POST /api/elections/{electionId}/votes` with `voterId` and `optionId`.
2. Controller publishes `VoteRequestedEvent` to Kafka.
3. `VoteConsumer` reads events and invokes `VotingService`.
4. `VotingService` validates:
   - election exists and is in time window
   - option belongs to election
   - voter exists and is not blocked
   - unique vote per voter/election (DB unique index)
5. Event processing is idempotent: duplicate events are ignored using `ProcessedEvent` table.

---

## License
MIT
