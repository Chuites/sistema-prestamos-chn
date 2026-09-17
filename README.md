# Sistema de Préstamos

Aplicación web para administrar clientes, solicitudes, préstamos y pagos.

- **Backend:** Spring Boot 4 (Java 21) + SQL Server 2022.
- **Frontend:** Angular 22.
- **Todo se levanta con Docker Compose.**

## Cómo levantarlo

**1. Crea el archivo de configuración** (solo la primera vez):

```bash
cp .env.example .env
```

En Windows (PowerShell):

```powershell
Copy-Item .env.example .env
```

**2. Arranca el sistema:**

```bash
docker compose up --build
```

La primera vez tarda unos minutos. La base de datos y los datos de prueba se crean
automáticamente.

**3. Abre la aplicación:** http://localhost:4200

Eso es todo.

## Direcciones

| Qué         | Dónde                                        |
|-------------|----------------------------------------------|
| Aplicación  | http://localhost:4200                            |
| API         | http://localhost:4200/api (o http://localhost:8080/api) |
| Swagger UI  | http://localhost:4200/swagger-ui/index.html      |
| SQL Server  | `localhost:1433` (usuario `sa`)                  |

## Datos de prueba

En el primer arranque se cargan solos: **12 clientes**, **24 solicitudes**,
**12 préstamos** y **10 pagos**, para que puedas probar la aplicación de inmediato.

## Documentación

- [Arquitectura](docs/arquitectura.md) — cómo está construido el sistema.
- [API REST](docs/api.md) — endpoints y ejemplos.
- [Base de datos](docs/base-de-datos.md) — diagrama entidad-relación y script SQL.
- [Guía de usuario](docs/guia-usuario.md) — uso de la aplicación.

Swagger UI (con el sistema corriendo): http://localhost:4200/swagger-ui/index.html
(también directo al backend en http://localhost:8080/swagger-ui/index.html).

## Comandos útiles

| Acción                              | Comando                                        |
|-------------------------------------|------------------------------------------------|
| Detener                             | `docker compose down`                          |
| Detener y borrar la base de datos   | `docker compose down -v`                       |
| Empezar de cero                     | `docker compose down -v && docker compose up --build` |

## Desarrollo sin Docker (opcional)

Necesitas Java 21 y Node.js 22.

**1. Base de datos:**

```bash
docker compose up -d sqlserver
```

**2. Backend** (la API queda en http://localhost:8080):

```powershell
# Windows (PowerShell)
cd backend/prestamos-api
$env:DB_PASSWORD = "TuClaveSegura2026*"
.\mvnw.cmd spring-boot:run
```

```bash
# Linux / macOS
cd backend/prestamos-api
export DB_PASSWORD="TuClaveSegura2026*"
./mvnw spring-boot:run
```

**3. Frontend** (queda en http://localhost:4200):

```bash
cd frontend
npm install
npm start
```

## Pruebas

- **Frontend:** `cd frontend && npm test`
- **Backend:** `cd backend/prestamos-api && .\mvnw.cmd '-Dtest=ClienteServiceTest,SolicitudPrestamoServiceTest,PagoServiceTest' test`

## Problemas comunes

- **Contraseña de SQL Server:** debe tener al menos 8 caracteres, con mayúscula,
  minúscula, número y símbolo, o el contenedor no arranca.
- **Puertos ocupados:** si `4200`, `8080` o `1433` están en uso, libera el puerto.
- **`localhost:8080` responde una página vacía:** otro proceso puede estar usando el
  `8080` en IPv4 (por ejemplo, el agente Check Point SandBlast). Usa
  `http://localhost:4200/swagger-ui/index.html` o `http://[::1]:8080/...`.
- **La API tarda la primera vez:** SQL Server debe terminar de iniciar antes de que
  arranque el backend.
- **Tablas:** se crean y actualizan solas, no hay migraciones que ejecutar.
