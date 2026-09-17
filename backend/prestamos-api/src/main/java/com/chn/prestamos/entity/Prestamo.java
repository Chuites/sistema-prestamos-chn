package com.chn.prestamos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "prestamos")
public class Prestamo {

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

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

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

    @Digits(
        integer = 16,
        fraction = 2,
        message = "El monto no es válido (máximo 2 decimales)"
    )
    @Column(
        name = "saldo_pendiente",
        nullable = false,
        precision = 18,
        scale = 2
    )
    private BigDecimal saldoPendiente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPrestamo estado;

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

        if (saldoPendiente == null) {
            saldoPendiente = montoAprobado;
        }

        if (estado == null) {
            estado = EstadoPrestamo.PENDIENTE;
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

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
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

    public BigDecimal getSaldoPendiente() {
        return saldoPendiente;
    }

    public void setSaldoPendiente(BigDecimal saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
    }

    public EstadoPrestamo getEstado() {
        return estado;
    }

    public void setEstado(EstadoPrestamo estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaAprobacion() {
        return fechaAprobacion;
    }

    public void setFechaAprobacion(LocalDateTime fechaAprobacion) {
        this.fechaAprobacion = fechaAprobacion;
    }
}