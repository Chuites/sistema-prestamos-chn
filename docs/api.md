# API REST

## Información general

- **URL base (directa):** `http://localhost:8080/api`
- **URL base (a través del frontend):** `http://localhost:4200/api`
- **Formato:** JSON (`Content-Type: application/json`).
- **Documentación interactiva (Swagger UI):** `http://localhost:8080/swagger-ui.html`
- **Especificación OpenAPI (JSON):** `http://localhost:8080/v3/api-docs`

Recursos disponibles:

| Recurso | Base |
|---------|------|
| Clientes | `/api/clientes` |
| Solicitudes | `/api/solicitudes` |
| Préstamos | `/api/prestamos` |
| Pagos | `/api/pagos` |

## Formato de errores

**Datos inválidos (`400`)** — incluye el detalle por campo:

```json
{
  "mensaje": "Hay datos inválidos en la solicitud.",
  "errores": {
    "correoElectronico": "El correo electrónico no es válido"
  }
}
```

**Otros errores (`400`, `404`, `409`)** — un solo mensaje:

```json
{ "mensaje": "Cliente no encontrado" }
```

| Código | Cuándo ocurre |
|:------:|---------------|
| `200` | Consulta o actualización correcta. |
| `201` | Recurso creado. |
| `400` | Datos o parámetros inválidos, o el pago supera el saldo. |
| `404` | El recurso no existe. |
| `409` | Conflicto: identificación duplicada, solicitud ya resuelta, préstamo ya pagado. |

---

## Clientes

### `GET /api/clientes`

Lista todos los clientes.

```bash
curl http://localhost:8080/api/clientes
```

**Respuesta `200`:**

```json
[
  {
    "id": 1,
    "nombre": "Ana Lucía",
    "apellido": "López García",
    "numeroIdentificacion": "1001",
    "fechaNacimiento": "1990-03-14",
    "direccion": "5a Avenida 12-34 Zona 1",
    "correoElectronico": "ana.lopez@example.com",
    "telefono": "5555-1001"
  }
]
```

### `GET /api/clientes/{id}`

Devuelve un cliente por su id. `404` si no existe.

```bash
curl http://localhost:8080/api/clientes/1
```

### `POST /api/clientes`

Crea un cliente. Devuelve `201`.

```bash
curl -X POST http://localhost:8080/api/clientes \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Ana Lucía",
    "apellido": "López García",
    "numeroIdentificacion": "1001",
    "fechaNacimiento": "1990-03-14",
    "direccion": "5a Avenida 12-34 Zona 1",
    "correoElectronico": "ana.lopez@example.com",
    "telefono": "5555-1001"
  }'
```

Campos y validaciones:

| Campo | Reglas |
|-------|--------|
| `nombre`, `apellido` | Obligatorios, solo letras y espacios, máximo 100. |
| `numeroIdentificacion` | Obligatorio, solo dígitos, máximo 25, **único**. |
| `fechaNacimiento` | Obligatoria, anterior a hoy (`YYYY-MM-DD`). |
| `direccion` | Obligatoria, máximo 250. |
| `correoElectronico` | Obligatorio, formato de correo, máximo 150. |
| `telefono` | Obligatorio, dígitos y los signos `+ ( ) -`, máximo 20. |

Errores: `400` validación, `409` identificación duplicada.

### `PUT /api/clientes/{id}`

Actualiza un cliente. Mismo cuerpo que `POST`.

```bash
curl -X PUT http://localhost:8080/api/clientes/1 \
  -H "Content-Type: application/json" \
  -d '{ "nombre": "Ana Lucía", "apellido": "López García",
        "numeroIdentificacion": "1001", "fechaNacimiento": "1990-03-14",
        "direccion": "Nueva dirección 1-23", "correoElectronico": "ana@example.com",
        "telefono": "5555-1001" }'
```

### `DELETE /api/clientes/{id}`

Elimina un cliente junto con sus solicitudes, préstamos y pagos. Devuelve `204`.

```bash
curl -X DELETE http://localhost:8080/api/clientes/1
```

---

## Solicitudes

Estados posibles: `EN_PROCESO`, `APROBADA`, `RECHAZADA`.

### `GET /api/solicitudes`

Lista todas las solicitudes (ordenadas por fecha descendente).

```bash
curl http://localhost:8080/api/solicitudes
```

**Respuesta `200`:**

```json
[
  {
    "id": 5,
    "cliente": {
      "id": 1,
      "nombre": "Ana Lucía",
      "apellido": "López García",
      "numeroIdentificacion": "1001",
      "fechaNacimiento": "1990-03-14",
      "direccion": "5a Avenida 12-34 Zona 1",
      "correoElectronico": "ana.lopez@example.com",
      "telefono": "5555-1001"
    },
    "montoSolicitado": 12000.00,
    "plazoMeses": 18,
    "destinoPrestamo": "Capital de trabajo para negocio",
    "observaciones": null,
    "fechaSolicitud": "2026-09-17T16:05:59",
    "estado": "EN_PROCESO",
    "fechaResolucion": null,
    "comentarioResolucion": null
  }
]
```

### `GET /api/solicitudes/{id}`

Devuelve una solicitud por id. `404` si no existe.

### `GET /api/solicitudes/cliente/{clienteId}`

Solicitudes de un cliente. `404` si el cliente no existe.

### `GET /api/solicitudes/estado/{estado}`

Filtra por estado (`EN_PROCESO`, `APROBADA` o `RECHAZADA`).

```bash
curl http://localhost:8080/api/solicitudes/estado/EN_PROCESO
```

### `GET /api/solicitudes/cliente/{clienteId}/estado/{estado}`

Combina ambos filtros.

### `POST /api/solicitudes`

Crea una solicitud en estado `EN_PROCESO`. Devuelve `201`.

```bash
curl -X POST http://localhost:8080/api/solicitudes \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 1,
    "montoSolicitado": 12000.00,
    "plazoMeses": 18,
    "destinoPrestamo": "Capital de trabajo para negocio",
    "observaciones": "Cliente recurrente"
  }'
```

| Campo | Reglas |
|-------|--------|
| `clienteId` | Obligatorio; el cliente debe existir. |
| `montoSolicitado` | Obligatorio, mayor que 0, máximo 2 decimales. |
| `plazoMeses` | Obligatorio, entre 1 y 360. |
| `destinoPrestamo` | Obligatorio, máximo 200. |
| `observaciones` | Opcional, máximo 500. |

### `PUT /api/solicitudes/{id}/resolver`

Aprueba o rechaza una solicitud. **Solo se puede resolver una vez.**

```bash
# Aprobar (requiere tasa de interés anual)
curl -X PUT http://localhost:8080/api/solicitudes/5/resolver \
  -H "Content-Type: application/json" \
  -d '{ "estado": "APROBADA", "comentario": "Documentación verificada",
        "tasaInteresAnual": 14.50 }'

# Rechazar (la tasa no aplica)
curl -X PUT http://localhost:8080/api/solicitudes/5/resolver \
  -H "Content-Type: application/json" \
  -d '{ "estado": "RECHAZADA", "comentario": "No cumple capacidad de pago" }'
```

- Al **aprobar** se crea un `Prestamo` (`PENDIENTE`) con el mismo monto y plazo.
- Errores: `400` si el estado es `EN_PROCESO` o falta la tasa al aprobar;
  `409` si la solicitud ya fue resuelta.

---

## Préstamos

Estados posibles: `PENDIENTE`, `PARCIAL`, `PAGADO`.

### `GET /api/prestamos`

Lista todos los préstamos (ordenados por fecha de aprobación descendente).

```bash
curl http://localhost:8080/api/prestamos
```

**Respuesta `200`:**

```json
[
  {
    "id": 1,
    "solicitud": { "id": 1, "cliente": { "id": 1 }, "estado": "APROBADA" },
    "cliente": { "id": 1, "nombre": "Ana Lucía", "apellido": "López García" },
    "montoAprobado": 15000.00,
    "tasaInteresAnual": 14.50,
    "plazoMeses": 12,
    "montoPagado": 2000.00,
    "saldoPendiente": 13000.00,
    "estado": "PARCIAL",
    "fechaAprobacion": "2026-09-17T16:05:59"
  }
]
```

### `GET /api/prestamos/{id}`

Devuelve un préstamo por id. `404` si no existe.

### `GET /api/prestamos/cliente/{clienteId}`

Préstamos de un cliente.

### `GET /api/prestamos/solicitud/{solicitudId}`

Devuelve el préstamo generado por una solicitud. `404` si no existe.

---

## Pagos

### `GET /api/pagos`

Lista todos los pagos (ordenados por fecha descendente).

```bash
curl http://localhost:8080/api/pagos
```

### `GET /api/pagos/prestamo/{prestamoId}`

Pagos de un préstamo. `404` si el préstamo no existe.

### `POST /api/pagos/prestamo/{prestamoId}`

Registra un pago sobre un préstamo. Devuelve `201`.

```bash
curl -X POST http://localhost:8080/api/pagos/prestamo/1 \
  -H "Content-Type: application/json" \
  -d '{ "monto": 2000.00, "numeroRecibo": "REC-0001",
        "observaciones": "Abono mensual" }'
```

**Respuesta `201`:**

```json
{
  "id": 1,
  "prestamo": { "id": 1, "estado": "PARCIAL", "saldoPendiente": 13000.00 },
  "monto": 2000.00,
  "fechaPago": "2026-09-17T16:05:59",
  "metodoPago": "EFECTIVO",
  "numeroRecibo": "REC-0001",
  "observaciones": "Abono mensual"
}
```

| Campo | Reglas |
|-------|--------|
| `monto` | Obligatorio, mayor que 0, máximo 2 decimales. |
| `numeroRecibo` | Opcional, letras/números/guiones, máximo 50. |
| `observaciones` | Opcional, máximo 250. |

Efectos del pago:

- Se actualiza `montoPagado` del préstamo. `saldoPendiente`
  (`montoAprobado - montoPagado`) y `estado` son **derivados**: no se almacenan.
- El estado resulta `PARCIAL` (si queda saldo) o `PAGADO` (si el saldo llega a 0).
- Errores: `400` si el monto supera el saldo; `409` si el préstamo ya está pagado o
  si el `numeroRecibo` ya existe.
