# Sistema de Préstamos — Crédito Nacional

Sistema web para la gestión de préstamos bancarios: clientes, solicitudes, préstamos
aprobados y pagos en efectivo.

- **Backend**: Spring Boot 4 · Java 21 · Spring Data JPA · SQL Server.
- **Frontend**: Angular 22 (standalone) · SCSS.
- **Base de datos**: SQL Server 2022.
- **Despliegue**: Docker Compose (un solo comando).

## Estructura

```
sistema-prestamos-chn/
├─ backend/prestamos-api/   # API REST (Spring Boot)
├─ frontend/                # Aplicación Angular
├─ docker-compose.yml       # Orquestación (base de datos + API + web)
├─ .env.example             # Plantilla de variables de entorno
└─ README.md
```

## Requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) con Docker Compose.
- Opcional (desarrollo sin Docker): Java 21, Node.js 22 y npm.

## Inicio rápido con Docker Compose

1. Crea el archivo de entorno a partir de la plantilla y define la contraseña de SQL Server:

   ```bash
   cp .env.example .env
   ```

   En Windows (PowerShell):

   ```powershell
   Copy-Item .env.example .env
   ```

2. Levanta todo el sistema:

   ```bash
   docker compose up --build
   ```

   La primera vez tarda unos minutos (descarga de imágenes y compilación). Espera a que
   SQL Server termine de arrancar; el backend espera a que la base de datos esté sana.

3. Abre la aplicación:

   | Servicio            | URL                              |
   |---------------------|----------------------------------|
   | Aplicación web      | http://localhost:4200            |
   | API (a través del proxy) | http://localhost:4200/api     |
   | API (directo)       | http://localhost:8080/api        |
   | SQL Server          | `localhost:1433` (`sa`)          |

Para detener los contenedores:

```bash
docker compose down
```

Para detenerlos y **borrar los datos** de la base de datos:

```bash
docker compose down -v
```

## Desarrollo local (sin Docker)

### 1. Base de datos

Desde la raíz del repositorio:

```bash
docker compose up -d sqlserver
```

### 2. Backend

El backend lee `DB_PASSWORD` del entorno y **no** carga el `.env` automáticamente.

Windows (PowerShell):

```powershell
$env:DB_PASSWORD = "TuClaveSegura2026*"
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
export DB_PASSWORD="TuClaveSegura2026*"
./mvnw spring-boot:run
```

La API queda en `http://localhost:8080`.

### 3. Frontend

```bash
cd frontend
npm install
npm start
```

`ng serve` usa `proxy.conf.json` y reenvía `/api` a `http://localhost:8080`, por lo que
la aplicación queda disponible en `http://localhost:4200`.

## Pruebas

Frontend (Vitest):

```bash
cd frontend
npm test
```

Backend: pruebas unitarias de servicios (Mockito, no requieren base de datos):

```powershell
cd backend/prestamos-api
.\mvnw.cmd '-Dtest=ClienteServiceTest,SolicitudPrestamoServiceTest,PagoServiceTest' test
```

El comando `.\mvnw.cmd test` completo levanta el contexto de Spring, por lo que requiere
SQL Server accesible y `DB_PASSWORD` definida.

## Notas y solución de problemas

- **Contraseña de SQL Server**: debe cumplir la política de complejidad (mínimo 8
  caracteres, con mayúsculas, minúsculas, dígitos y un símbolo) o el contenedor de SQL
  Server no arrancará.
- **Puertos ocupados**: si `4200`, `8080` o `1433` están en uso, detén el proceso que los
  ocupa o ajusta los mapeos en `docker-compose.yml`.
- **La API tarda en responder la primera vez**: SQL Server tarda en inicializar; el
  backend no arranca hasta que la base de datos está sana.
- **Esquema de la base de datos**: se crea/actualiza automáticamente
  (`spring.jpa.hibernate.ddl-auto=update`); no hay migraciones.
