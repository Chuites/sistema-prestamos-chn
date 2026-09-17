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
- Approval flow: create a solicitud (state `EN_PROCESO`), then `PUT /api/solicitudes/{id}/resolver`. Approving requires `tasaInteresAnual` and auto-creates a `Prestamo` (`PENDIENTE`); rejecting sets `RECHAZADA`. A solicitud can only be resolved once.
- Payments: `POST /api/pagos/prestamo/{prestamoId}` updates `montoPagado`/`saldoPendiente` and sets prestamo state to `PARCIAL` or `PAGADO`. Payment cannot exceed the balance.
- Every controller hard-codes `@CrossOrigin(origins = "http://localhost:4200")`; changing the frontend port breaks API calls.
- Frontend calls the API with a **relative** base (`API_BASE_URL = '/api'` in `services/api-base.ts`). In dev, `ng serve` proxies `/api` to `http://localhost:8080` via `proxy.conf.json`; in Docker, nginx proxies `/api` to `backend:8080`. Tests expect request URLs like `/api/clientes`.

## Deployment (Docker Compose)

- `docker compose up --build` from the repo root starts `sqlserver` + `backend` + `frontend` (web on `http://localhost:4200`, API on `:8080`).
- Requires a root `.env` with `DB_PASSWORD` (copy `.env.example`). SQL Server is health-checked before the backend starts.
- Multi-stage Dockerfiles: `backend/prestamos-api/Dockerfile` (Maven → JRE) and `frontend/Dockerfile` (node → nginx, serves `dist/frontend/browser`).
- `docker compose down -v` deletes the database volume.

## Conventions

- No Lombok: entities use hand-written getters/setters, and a `@PrePersist` method sets timestamps/defaults.
- Business errors are thrown as `ResponseStatusException` (404/409/400), not custom exception classes.
- Frontend components are standalone, use Reactive Forms with `inject()`, and manually call `ChangeDetectorRef.detectChanges()` after async loads.
