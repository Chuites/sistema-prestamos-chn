# AGENTS.md

Loan-management system ("sistema de préstamos"). Two apps, no root package manifest:

- `backend/prestamos-api` — Spring Boot 4.1.1, Java 21, Spring Data JPA, SQL Server.
- `frontend` — Angular 22 (standalone components), SCSS, Vitest.

Domain code and endpoints are in Spanish (`clientes`, `solicitudes`, `prestamos`, `pagos`).

## Commands

Backend (run from `backend/prestamos-api`):

- Run API: `.\mvnw.cmd spring-boot:run` (default port 8080).
- Test: `.\mvnw.cmd test` — `PrestamosApiApplicationTests` boots the full Spring context, so SQL Server must be reachable and `DB_PASSWORD` set.
- Service unit tests (Mockito, no DB): `.\mvnw.cmd '-Dtest=ClienteServiceTest,SolicitudPrestamoServiceTest,PagoServiceTest' test`. Quote the `-Dtest=` value in PowerShell or the comma list is parsed as separate args.
- Build: `.\mvnw.cmd clean package`.

Frontend (run from `frontend`):

- `npm install` then `npm start` (ng serve, http://localhost:4200).
- `npm run build` (production by default).
- `npm test` (Vitest via `@angular/build:unit-test`; jsdom).
- No lint target and no `format` script. Prettier config exists (`.prettierrc`); run `npx prettier --write .` manually.

## Local setup gotchas

- Start the database from the repo root: `docker compose up -d` (SQL Server 2022 on `localhost:1433`). Password comes from root `.env` (`DB_PASSWORD`).
- Backend reads `DB_PASSWORD` from the environment; `application.properties` has **no default**. The root `.env` is **not** auto-loaded by Maven/Spring, so export it (or set `DB_PASSWORD` in the shell) before `mvnw` commands.
- Defaults in `application.properties`: user `sa`, database `prestamos_db`, `spring.jpa.hibernate.ddl-auto=update` (schema auto-created — there are no migrations).

## Architecture / API

- Layers: `controller` → `service` → `repository` (Spring Data JPA) → `entity`. DTO records exist only for create/resolve requests (`dto/`).
- REST bases: `/api/clientes`, `/api/solicitudes`, `/api/prestamos`, `/api/pagos` (see `controller/`).
- OpenAPI docs via `springdoc-openapi-starter-webmvc-ui` v3 (Boot 4): UI at `/swagger-ui.html`, JSON at `/v3/api-docs`. Project docs live in `docs/`.
- Approval flow: create a solicitud (state `EN_PROCESO`), then `PUT /api/solicitudes/{id}/resolver`. Approving requires `tasaInteresAnual` and auto-creates a `Prestamo` with `montoPagado = 0` (estado derives to `PENDIENTE`); rejecting sets `RECHAZADA`. A solicitud can only be resolved once.
- Payments: `POST /api/pagos/prestamo/{prestamoId}` increments `Prestamo.montoPagado`; `saldoPendiente` and `estado` are derived (`@Transient` getters, not stored). Payment cannot exceed the balance; a duplicate `numeroRecibo` returns 409.
- Every controller hard-codes `@CrossOrigin(origins = "http://localhost:4200")`; changing the frontend port breaks API calls.
- Frontend calls the API with a **relative** base (`API_BASE_URL = '/api'` in `services/api-base.ts`). In dev, `ng serve` proxies `/api` to `http://localhost:8080` via `proxy.conf.json`; in Docker, nginx proxies `/api` to `backend:8080`. Tests expect request URLs like `/api/clientes`.

## Deployment (Docker Compose)

- `docker compose up --build` from the repo root starts `sqlserver` + `backend` + `frontend` (web on `http://localhost:4200`, API on `:8080`).
- Requires a root `.env` with `DB_PASSWORD` (copy `.env.example`). SQL Server is health-checked before the backend starts.
- The `sqlserver` service creates `prestamos_db` on first boot via the image's native `MSSQL_DB` env var (only when the named volume is empty; idempotent afterwards). Its healthcheck waits for `DB_ID('prestamos_db')` to exist, so `backend` (`depends_on: service_healthy`) never starts before the database exists. Tables are created by Hibernate `ddl-auto=update`; there are no migrations.
- Multi-stage Dockerfiles: `backend/prestamos-api/Dockerfile` (Maven → JRE) and `frontend/Dockerfile` (node → nginx, serves `dist/frontend/browser`).
- `docker compose down -v` deletes the database volume.

## Demo seed data

- `config/DataSeeder` (an `ApplicationRunner`) populates demo data when `app.seed.enabled=true`. It is `false` by default in `application.properties` and enabled via `APP_SEED_ENABLED: "true"` in `docker-compose.yml` (backend), so tests and prod are unaffected.
- Idempotent: skips if `clientes` already has rows (`docker compose restart backend` does not duplicate). Creates 12 clientes, 24 solicitudes (6 `EN_PROCESO`, 12 `APROBADA`, 6 `RECHAZADA`), 12 prestamos (5 `PENDIENTE`, 5 `PARCIAL`, 2 `PAGADO`) and 10 pagos, driving the real services so balances/states stay consistent.
- To reset: `docker compose down -v && docker compose up -d --build`.

## Conventions

- No Lombok: entities use hand-written getters/setters and a `@PrePersist` method sets defaults. All entities extend `BaseEntity` (`creado_en`/`actualizado_en` audit columns).
- `prestamos` does not store `cliente_id`, `saldo_pendiente` or `estado`: the client comes from `solicitud`, and `saldoPendiente`/`estado` are derived `@Transient` getters from `montoAprobado`/`montoPagado`. Controlled vocabularies are enforced with DB `CHECK` constraints (`@Check`, Hibernate-generated enum check) and a filtered unique index on `pagos.numero_recibo`.
- Business errors are thrown as `ResponseStatusException` (404/409/400) and mapped by `GlobalExceptionHandler` to `{ "mensaje": ... }` (no custom exception classes).
- Frontend components are standalone, use Reactive Forms with `inject()`, and manually call `ChangeDetectorRef.detectChanges()` after async loads.
