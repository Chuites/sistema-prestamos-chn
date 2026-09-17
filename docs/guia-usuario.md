# Guía de usuario

Manual de uso de la aplicación web.

## Acceso

1. Abre el navegador en **http://localhost:4200**.
2. El sistema abre en el **Panel general**.

La barra lateral izquierda tiene el menú: **Dashboard**, **Clientes**, **Solicitudes**,
**Préstamos** y **Pagos**. Los seleccionados con búsqueda permiten escribir para
encontrar una opción rápidamente.

## Dashboard

Muestra un resumen general:

- Totales: clientes, solicitudes, solicitudes pendientes, préstamos y pagos.
- Montos: prestado, pagado y saldo pendiente.
- Tabla **resumen por cliente** (solicitudes, préstamos, pagos y monto pagado).

El botón **Actualizar** recarga los datos.

## Clientes

Registra y administra los datos personales.

**Crear un cliente**

1. Completa el formulario *Nuevo cliente*:
   Nombre, Apellido, Número de identificación, Fecha de nacimiento, Dirección,
   Correo electrónico y Teléfono.
2. Pulsa **Guardar cliente**.

**Editar**: pulsa **Editar** en la fila del cliente. El formulario se llena con sus
datos y el botón cambia a **Guardar cliente**. Usa **Cancelar** para salir.

**Eliminar**: pulsa **Eliminar** en la fila. Aparece un cuadro de confirmación; pulsa
**Sí, eliminar** para confirmar o **Cancelar** para cerrar.

> Reglas: el nombre y el apellido solo admiten letras; la identificación solo números y
> no puede repetirse; la fecha de nacimiento debe ser anterior a hoy; el teléfono admite
> números y los signos `+ ( ) -`.

> Al eliminar un cliente se borran también sus solicitudes, préstamos y pagos. La
> acción no se puede deshacer.

## Solicitudes

Gestiona las peticiones de crédito.

**Registrar una solicitud**

1. En *Nueva solicitud* selecciona el **Cliente** (puedes escribir para buscarlo).
2. Ingresa **Monto solicitado**, **Plazo en meses**, **Destino del préstamo** y, si
   quieres, **Observaciones**.
3. Pulsa **Registrar solicitud**. Se crea en estado **EN_PROCESO**.

**Filtrar**: usa *Filtrar por cliente* y *Filtrar por estado*, o pulsa **Actualizar**.

**Aprobar o rechazar**

1. En la tabla, pulsa **Aprobar** o **Rechazar** en la solicitud.
2. En el cuadro, escribe un **Comentario**. Si apruebas, indica la **Tasa de interés
   anual**.
3. Pulsa **Aprobar**/**Rechazar**.

> Al aprobar se crea automáticamente el préstamo correspondiente. Una solicitud solo
> se puede resolver una vez.

Estados: **EN_PROCESO**, **APROBADA**, **RECHAZADA**.

## Préstamos

Consulta los créditos aprobados y sus saldos.

- Muestra tarjetas con: monto aprobado, saldo pendiente, progreso de pago, monto
  pagado, plazo, tasa anual y fecha de aprobación.
- Filtra con *Filtrar por cliente* (con búsqueda).
- Pulsa **Ver historial de pagos** para ver los pagos del préstamo.

Estados: **PENDIENTE** (sin pagos), **PARCIAL** (abonos parciales) y **PAGADO**.

## Pagos

Registra los abonos a los préstamos.

**Registrar un pago**

1. En *Registrar pago*, selecciona el **Préstamo** (con búsqueda; muestra el cliente y
   el saldo pendiente). Los préstamos ya **PAGADO** aparecen deshabilitados.
2. Ingresa el **Monto recibido** y, opcionalmente, el **Número de recibo** y las
   **Observaciones**.
3. Pulsa **Registrar pago**.

**Historial**: la parte inferior lista los pagos y permite filtrar por cliente.

> El monto no puede superar el saldo pendiente. Al registrar el pago, el sistema
> actualiza el saldo y el estado del préstamo automáticamente.

## Recorrido completo de ejemplo

1. **Clientes** → crea un cliente (por ejemplo, "Ana López").
2. **Solicitudes** → registra una solicitud para ese cliente.
3. En la misma tabla, pulsa **Aprobar**, escribe un comentario y una tasa (por ejemplo
   `14.5`).
4. Ve a **Préstamos**: verás el préstamo nuevo en estado **PENDIENTE** con el saldo
   completo.
5. Ve a **Pagos** → selecciona ese préstamo, registra un monto menor al saldo.
6. Vuelve a **Préstamos**: el estado ahora es **PARCIAL** y el saldo bajó.
7. Registra otro pago por el saldo restante: el préstamo pasa a **PAGADO**.
8. En el **Dashboard** verás reflejados los totales y montos.
