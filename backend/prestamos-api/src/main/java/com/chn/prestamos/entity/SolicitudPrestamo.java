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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_prestamo")
public class SolicitudPrestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El cliente es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @NotNull(message = "El monto solicitado es obligatorio")
    @DecimalMin(value = "1.00", message = "El monto debe ser mayor que cero")
    @Digits(
        integer = 16,
        fraction = 2,
        message = "El monto no es válido (máximo 2 decimales)"
    )
    @Column(
        name = "monto_solicitado",
        nullable = false,
        precision = 18,
        scale = 2
    )
    private BigDecimal montoSolicitado;

    @NotNull(message = "El plazo es obligatorio")
    @Min(value = 1, message = "El plazo mínimo es de un mes")
    @Max(value = 360, message = "El plazo máximo es de 360 meses")
    @Column(name = "plazo_meses", nullable = false)
    private Integer plazoMeses;

    @NotBlank(message = "El destino del préstamo es obligatorio")
    @Size(max = 200)
    @Pattern(
        regexp = "^[A-Za-z0-9ÁÉÍÓÚáéíóúÑñÜü .,#\\-/]+$",
        message = "El destino solo puede contener letras, números y signos como # . , - /"
    )
    @Column(name = "destino_prestamo", nullable = false, length = 200)
    private String destinoPrestamo;

    @Size(max = 500)
    @Column(length = 500)
    private String observaciones;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Size(max = 500)
    @Column(name = "comentario_resolucion", length = 500)
    private String comentarioResolucion;

    public SolicitudPrestamo() {
    }

    @PrePersist
    public void antesDeGuardar() {
        if (fechaSolicitud == null) {
            fechaSolicitud = LocalDateTime.now();
        }

        if (estado == null) {
            estado = EstadoSolicitud.EN_PROCESO;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public BigDecimal getMontoSolicitado() {
        return montoSolicitado;
    }

    public void setMontoSolicitado(BigDecimal montoSolicitado) {
        this.montoSolicitado = montoSolicitado;
    }

    public Integer getPlazoMeses() {
        return plazoMeses;
    }

    public void setPlazoMeses(Integer plazoMeses) {
        this.plazoMeses = plazoMeses;
    }

    public String getDestinoPrestamo() {
        return destinoPrestamo;
    }

    public void setDestinoPrestamo(String destinoPrestamo) {
        this.destinoPrestamo = destinoPrestamo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(LocalDateTime fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }

    public String getComentarioResolucion() {
        return comentarioResolucion;
    }

    public void setComentarioResolucion(String comentarioResolucion) {
        this.comentarioResolucion = comentarioResolucion;
    }
}