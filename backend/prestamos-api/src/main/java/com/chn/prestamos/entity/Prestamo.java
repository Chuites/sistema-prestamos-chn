package com.chn.prestamos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "prestamos")
public class Prestamo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(
        name = "solicitud_id",
        nullable = false,
        unique = true
    )
    private SolicitudPrestamo solicitud;

    @Digits(
        integer = 16,
        fraction = 2,
        message = "El monto no es válido (máximo 2 decimales)"
    )
    @Column(
        name = "monto_aprobado",
        nullable = false,
        precision = 18,
        scale = 2
    )
    private BigDecimal montoAprobado;

    @Digits(
        integer = 3,
        fraction = 2,
        message = "La tasa de interés admite máximo 2 decimales"
    )
    @Column(
        name = "tasa_interes_anual",
        nullable = false,
        precision = 5,
        scale = 2
    )
    private BigDecimal tasaInteresAnual;

    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;

    @Digits(
        integer = 16,
        fraction = 2,
        message = "El monto no es válido (máximo 2 decimales)"
    )
    @Column(
        name = "monto_pagado",
        nullable = false,
        precision = 18,
        scale = 2
    )
    private BigDecimal montoPagado;

    @Column(name = "fecha_aprobacion", nullable = false)
    private LocalDateTime fechaAprobacion;

    public Prestamo() {
    }

    @PrePersist
    public void antesDeGuardar() {
        if (fechaAprobacion == null) {
            fechaAprobacion = LocalDateTime.now();
        }

        if (montoPagado == null) {
            montoPagado = BigDecimal.ZERO;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SolicitudPrestamo getSolicitud() {
        return solicitud;
    }

    public void setSolicitud(SolicitudPrestamo solicitud) {
        this.solicitud = solicitud;
    }

    public BigDecimal getMontoAprobado() {
        return montoAprobado;
    }

    public void setMontoAprobado(BigDecimal montoAprobado) {
        this.montoAprobado = montoAprobado;
    }

    public BigDecimal getTasaInteresAnual() {
        return tasaInteresAnual;
    }

    public void setTasaInteresAnual(BigDecimal tasaInteresAnual) {
        this.tasaInteresAnual = tasaInteresAnual;
    }

    public Integer getPlazoMeses() {
        return plazoMeses;
    }

    public void setPlazoMeses(Integer plazoMeses) {
        this.plazoMeses = plazoMeses;
    }

    public BigDecimal getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(BigDecimal montoPagado) {
        this.montoPagado = montoPagado;
    }

    public LocalDateTime getFechaAprobacion() {
        return fechaAprobacion;
    }

    public void setFechaAprobacion(LocalDateTime fechaAprobacion) {
        this.fechaAprobacion = fechaAprobacion;
    }

    @Transient
    public Cliente getCliente() {
        return solicitud == null ? null : solicitud.getCliente();
    }

    @Transient
    public BigDecimal getSaldoPendiente() {
        if (montoAprobado == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal pagado = montoPagado == null
            ? BigDecimal.ZERO
            : montoPagado;

        return montoAprobado.subtract(pagado);
    }

    @Transient
    public EstadoPrestamo getEstado() {
        if (
            montoAprobado == null
                || montoAprobado.compareTo(BigDecimal.ZERO) <= 0
        ) {
            return EstadoPrestamo.PENDIENTE;
        }

        BigDecimal pagado = montoPagado == null
            ? BigDecimal.ZERO
            : montoPagado;

        if (pagado.compareTo(BigDecimal.ZERO) <= 0) {
            return EstadoPrestamo.PENDIENTE;
        }

        if (pagado.compareTo(montoAprobado) >= 0) {
            return EstadoPrestamo.PAGADO;
        }

        return EstadoPrestamo.PARCIAL;
    }
}
