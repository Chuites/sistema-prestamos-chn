# Documentación - Sistema de Préstamos

Documentación del sistema de gestión de préstamos (clientes, solicitudes, préstamos
y pagos).

| Documento | Contenido |
|-----------|-----------|
| [Arquitectura](arquitectura.md) | Cómo está construido el sistema, capas, flujos de negocio y despliegue. |
| [API REST](api.md) | Endpoints, cuerpos de petición/respuesta, ejemplos y errores. |
| [Base de datos](base-de-datos.md) | Diagrama entidad-relación, diccionario de datos y script SQL. |
| [Guía de usuario](guia-usuario.md) | Cómo usar la aplicación pantalla por pantalla. |
| [Script SQL](sql/esquema.sql) | Script para crear el esquema en SQL Server. |

Para instalar y levantar el sistema, consulta el [README principal](../README.md).

## Resumen rápido

- **Backend:** Spring Boot 4 (Java 21) + SQL Server 2022.
- **Frontend:** Angular 22 (aplicación de una sola página).
- **API:** REST en JSON, base `/api`.
- **Base de datos:** `prestamos_db`, 4 tablas.
- **Despliegue:** Docker Compose (base de datos + backend + frontend).
