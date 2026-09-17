-- ============================================================================
-- Sistema de Préstamos — Esquema de base de datos (SQL Server 2022)
--
-- Script de referencia. En el sistema real, las tablas las crea y actualiza
-- Hibernate automáticamente (spring.jpa.hibernate.ddl-auto=update) y la base
-- la crea el contenedor de SQL Server con la variable MSSQL_DB.
--
-- Es idempotente: se puede ejecutar varias veces sin errores.
-- Los nombres de las restricciones son legibles; Hibernate genera nombres
-- aleatorios (ver docs/base-de-datos.md).
-- ============================================================================

IF DB_ID(N'prestamos_db') IS NULL
    CREATE DATABASE prestamos_db;
GO

USE prestamos_db;
GO

-- ----------------------------------------------------------------------------
-- clientes
-- ----------------------------------------------------------------------------
IF OBJECT_ID(N'dbo.clientes', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.clientes (
        id                    BIGINT IDENTITY(1,1) NOT NULL,
        nombre                VARCHAR(100)  NOT NULL,
        apellido              VARCHAR(100)  NOT NULL,
        numero_identificacion VARCHAR(25)   NOT NULL,
        fecha_nacimiento      DATE          NOT NULL,
        direccion             VARCHAR(250)  NOT NULL,
        correo_electronico    VARCHAR(150)  NOT NULL,
        telefono              VARCHAR(20)   NOT NULL,

        CONSTRAINT PK_clientes PRIMARY KEY (id),
        CONSTRAINT uk_cliente_identificacion UNIQUE (numero_identificacion)
    );
END
GO

-- ----------------------------------------------------------------------------
-- solicitudes_prestamo
-- ----------------------------------------------------------------------------
IF OBJECT_ID(N'dbo.solicitudes_prestamo', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.solicitudes_prestamo (
        id                    BIGINT IDENTITY(1,1) NOT NULL,
        cliente_id            BIGINT        NOT NULL,
        monto_solicitado      NUMERIC(18,2) NOT NULL,
        plazo_meses           INT           NOT NULL,
        destino_prestamo      VARCHAR(200)  NOT NULL,
        observaciones         VARCHAR(500)  NULL,
        fecha_solicitud       DATETIME2     NOT NULL,
        estado                VARCHAR(20)   NOT NULL, -- EN_PROCESO | APROBADA | RECHAZADA
        fecha_resolucion      DATETIME2     NULL,
        comentario_resolucion VARCHAR(500)  NULL,

        CONSTRAINT PK_solicitudes_prestamo PRIMARY KEY (id),
        CONSTRAINT FK_solicitud_cliente
            FOREIGN KEY (cliente_id) REFERENCES dbo.clientes (id)
    );
END
GO

-- ----------------------------------------------------------------------------
-- prestamos
-- ----------------------------------------------------------------------------
IF OBJECT_ID(N'dbo.prestamos', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.prestamos (
        id                 BIGINT IDENTITY(1,1) NOT NULL,
        solicitud_id       BIGINT        NOT NULL,
        cliente_id         BIGINT        NOT NULL,
        monto_aprobado     NUMERIC(18,2) NOT NULL,
        tasa_interes_anual NUMERIC(5,2)  NOT NULL,
        plazo_meses        INT           NOT NULL,
        monto_pagado       NUMERIC(18,2) NOT NULL,
        saldo_pendiente    NUMERIC(18,2) NOT NULL,
        estado             VARCHAR(20)   NOT NULL, -- PENDIENTE | PARCIAL | PAGADO
        fecha_aprobacion   DATETIME2     NOT NULL,

        CONSTRAINT PK_prestamos PRIMARY KEY (id),
        CONSTRAINT UQ_prestamo_solicitud UNIQUE (solicitud_id),
        CONSTRAINT FK_prestamo_solicitud
            FOREIGN KEY (solicitud_id) REFERENCES dbo.solicitudes_prestamo (id),
        CONSTRAINT FK_prestamo_cliente
            FOREIGN KEY (cliente_id) REFERENCES dbo.clientes (id)
    );
END
GO

-- ----------------------------------------------------------------------------
-- pagos
-- ----------------------------------------------------------------------------
IF OBJECT_ID(N'dbo.pagos', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.pagos (
        id            BIGINT IDENTITY(1,1) NOT NULL,
        prestamo_id   BIGINT        NOT NULL,
        monto         NUMERIC(18,2) NOT NULL,
        fecha_pago    DATETIME2     NOT NULL,
        metodo_pago   VARCHAR(20)   NOT NULL,
        numero_recibo VARCHAR(50)   NULL,
        observaciones VARCHAR(250)  NULL,

        CONSTRAINT PK_pagos PRIMARY KEY (id),
        CONSTRAINT FK_pago_prestamo
            FOREIGN KEY (prestamo_id) REFERENCES dbo.prestamos (id)
    );
END
GO
