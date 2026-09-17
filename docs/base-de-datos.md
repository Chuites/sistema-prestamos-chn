# Base de datos

Motor: **SQL Server 2022**. Base de datos: **`prestamos_db`**.

> El esquema lo crea y actualiza **Hibernate** automáticamente
> (`spring.jpa.hibernate.ddl-auto=update`); no hay migraciones. Los scripts
> [`sql/esquema.sql`](sql/esquema.sql) y [`sql/esquema.dbml`](sql/esquema.dbml)
> son el equivalente para referencia o para crear la base manualmente.

## Diagrama entidad-relación

```mermaid
erDiagram
    CLIENTES ||--o{ SOLICITUDES_PRESTAMO : "registra"
    SOLICITUDES_PRESTAMO ||--o| PRESTAMOS : "genera"
    PRESTAMOS ||--o{ PAGOS : "recibe"

    CLIENTES {
        bigint id PK
        varchar nombre
        varchar apellido
        varchar numero_identificacion UK
        date fecha_nacimiento
        varchar direccion
        varchar correo_electronico
        varchar telefono
        datetime2 creado_en
        datetime2 actualizado_en
    }

    SOLICITUDES_PRESTAMO {
        bigint id PK
        bigint cliente_id FK
        numeric monto_solicitado
        int plazo_meses
        varchar destino_prestamo
        varchar observaciones
        datetime2 fecha_solicitud
        varchar estado
        datetime2 fecha_resolucion
        varchar comentario_resolucion
        datetime2 creado_en
        datetime2 actualizado_en
    }

    PRESTAMOS {
        bigint id PK
        bigint solicitud_id FK
        numeric monto_aprobado
        numeric tasa_interes_anual
        int plazo_meses
        numeric monto_pagado
        datetime2 fecha_aprobacion
        datetime2 creado_en
        datetime2 actualizado_en
    }

    PAGOS {
        bigint id PK
        bigint prestamo_id FK
        numeric monto
        datetime2 fecha_pago
        varchar metodo_pago
        varchar numero_recibo
        varchar observaciones
        datetime2 creado_en
        datetime2 actualizado_en
    }
```

Relaciones:

- Un **cliente** tiene muchas **solicitudes**.
- Una **solicitud** genera **como máximo un préstamo** (relación 1 a 1).
- Un **préstamo** recibe muchos **pagos**.

> El cliente de un préstamo se obtiene a través de su solicitud
> (`prestamo → solicitud → cliente`); no se almacena de nuevo.

## Diccionario de datos

### `clientes`

| Columna | Tipo | Nulo | Descripción |
|---------|------|:----:|-------------|
| `id` | `BIGINT IDENTITY` | No | Clave primaria. |
| `nombre` | `VARCHAR(100)` | No | Nombre del cliente. |
| `apellido` | `VARCHAR(100)` | No | Apellido del cliente. |
| `numero_identificacion` | `VARCHAR(25)` | No | Identificación, **única**. |
| `fecha_nacimiento` | `DATE` | No | Fecha de nacimiento. |
| `direccion` | `VARCHAR(250)` | No | Dirección. |
| `correo_electronico` | `VARCHAR(150)` | No | Correo electrónico. |
| `telefono` | `VARCHAR(20)` | No | Teléfono. |
| `creado_en` | `DATETIME2` | No | Fecha de creación (auditoría). |
| `actualizado_en` | `DATETIME2` | No | Última actualización (auditoría). |

### `solicitudes_prestamo`

| Columna | Tipo | Nulo | Descripción |
|---------|------|:----:|-------------|
| `id` | `BIGINT IDENTITY` | No | Clave primaria. |
| `cliente_id` | `BIGINT` | No | FK → `clientes.id`. |
| `monto_solicitado` | `NUMERIC(18,2)` | No | Monto pedido. |
| `plazo_meses` | `INT` | No | Plazo en meses (1–360). |
| `destino_prestamo` | `VARCHAR(200)` | No | Motivo del préstamo. |
| `observaciones` | `VARCHAR(500)` | Sí | Notas adicionales. |
| `fecha_solicitud` | `DATETIME2` | No | Fecha de creación. |
| `estado` | `VARCHAR(20)` | No | `EN_PROCESO`, `APROBADA`, `RECHAZADA` (con `CHECK`). |
| `fecha_resolucion` | `DATETIME2` | Sí | Fecha de aprobación/rechazo. |
| `comentario_resolucion` | `VARCHAR(500)` | Sí | Comentario de la resolución. |
| `creado_en` | `DATETIME2` | No | Auditoría. |
| `actualizado_en` | `DATETIME2` | No | Auditoría. |

### `prestamos`

| Columna | Tipo | Nulo | Descripción |
|---------|------|:----:|-------------|
| `id` | `BIGINT IDENTITY` | No | Clave primaria. |
| `solicitud_id` | `BIGINT` | No | FK → `solicitudes_prestamo.id`, **única**. |
| `monto_aprobado` | `NUMERIC(18,2)` | No | Monto aprobado. |
| `tasa_interes_anual` | `NUMERIC(5,2)` | No | Tasa anual (%). |
| `plazo_meses` | `INT` | No | Plazo en meses. |
| `monto_pagado` | `NUMERIC(18,2)` | No | Total abonado (inicia en 0). |
| `fecha_aprobacion` | `DATETIME2` | No | Fecha de aprobación. |
| `creado_en` | `DATETIME2` | No | Auditoría. |
| `actualizado_en` | `DATETIME2` | No | Auditoría. |

Atributos **derivados** (no se almacenan; se calculan en la aplicación):
`saldo_pendiente = monto_aprobado - monto_pagado` y
`estado = PENDIENTE | PARCIAL | PAGADO` según `monto_pagado`.

### `pagos`

| Columna | Tipo | Nulo | Descripción |
|---------|------|:----:|-------------|
| `id` | `BIGINT IDENTITY` | No | Clave primaria. |
| `prestamo_id` | `BIGINT` | No | FK → `prestamos.id`. |
| `monto` | `NUMERIC(18,2)` | No | Monto del pago. |
| `fecha_pago` | `DATETIME2` | No | Fecha del pago. |
| `metodo_pago` | `VARCHAR(20)` | No | `EFECTIVO`, `TRANSFERENCIA`, `TARJETA`, `CHEQUE` (con `CHECK`). |
| `numero_recibo` | `VARCHAR(50)` | Sí | Número de recibo, **único cuando no es nulo**. |
| `observaciones` | `VARCHAR(250)` | Sí | Notas adicionales. |
| `creado_en` | `DATETIME2` | No | Auditoría. |
| `actualizado_en` | `DATETIME2` | No | Auditoría. |

## Restricciones

| Tabla | Restricción | Columnas |
|-------|-------------|----------|
| `clientes` | Clave primaria | `id` |
| `clientes` | Única | `numero_identificacion` |
| `solicitudes_prestamo` | Clave primaria | `id` |
| `solicitudes_prestamo` | Clave foránea | `cliente_id` → `clientes.id` |
| `solicitudes_prestamo` | `CHECK` | `estado` ∈ {EN_PROCESO, APROBADA, RECHAZADA} |
| `prestamos` | Clave primaria | `id` |
| `prestamos` | Única | `solicitud_id` |
| `prestamos` | Clave foránea | `solicitud_id` → `solicitudes_prestamo.id` |
| `pagos` | Clave primaria | `id` |
| `pagos` | Clave foránea | `prestamo_id` → `prestamos.id` |
| `pagos` | `CHECK` | `metodo_pago` ∈ {EFECTIVO, TRANSFERENCIA, TARJETA, CHEQUE} |
| `pagos` | Índice único filtrado | `numero_recibo` (donde no es nulo) |

> El script `sql/esquema.sql` usa nombres legibles (`PK_clientes`, `FK_pago_prestamo`,
> `CK_solicitud_estado`, etc.). Hibernate crea las mismas restricciones pero con
> nombres generados aleatoriamente. Las columnas, tipos y relaciones son idénticos.

## Normalización

El modelo cumple **1FN, 2FN y 3FN/BCNF** en las cuatro tablas:

- **1FN:** todos los atributos son atómicos (sin listas ni grupos repetidos).
- **2FN:** todas las claves primarias son simples (`id`), sin dependencias parciales.
- **3FN/BCNF:** no hay dependencias transitivas entre atributos no clave. Se eliminó
  la duplicación de `cliente_id` en `prestamos` (se obtiene vía `solicitud`) y las
  columnas derivadas `saldo_pendiente` y `estado` (se calculan en la aplicación).

La única denormalización intencional es `prestamos.monto_pagado`, que actúa como
**caché del total pagado** (agregado de `pagos`). Se mantiene de forma transaccional
en `PagoService` para evitar sumar los pagos en cada consulta.

## Cómo se crea y se puebla

1. **Creación de la base:** el contenedor `sqlserver` la crea en el primer arranque
   gracias a la variable `MSSQL_DB=prestamos_db` (solo si el volumen está vacío).
2. **Creación de tablas:** las genera Hibernate al arrancar el backend
   (`ddl-auto=update`).
3. **Datos de prueba:** si `APP_SEED_ENABLED=true` (activado en Docker Compose), el
   `DataSeeder` carga 12 clientes, 24 solicitudes, 12 préstamos y 10 pagos. Es
   idempotente: si ya hay clientes, no hace nada.

Para empezar de cero (borra la base y vuelve a crearla):

```bash
docker compose down -v && docker compose up --build
```
