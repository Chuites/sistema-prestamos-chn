package com.chn.prestamos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CrearSolicitudRequest(

    @NotNull(message = "El cliente es obligatorio")
    Long clienteId,

    @NotNull(message = "El monto solicitado es obligatorio")
    @DecimalMin(
        value = "1.00",
        message = "El monto debe ser mayor que cero"
    )
    @Digits(
        integer = 16,
        fraction = 2,
        message = "El monto no es válido (máximo 2 decimales)"
    )
    BigDecimal montoSolicitado,

    @NotNull(message = "El plazo es obligatorio")
    @Min(value = 1, message = "El plazo mínimo es de un mes")
    @Max(value = 360, message = "El plazo máximo es de 360 meses")
    Integer plazoMeses,

    @NotBlank(message = "El destino del préstamo es obligatorio")
    @Size(max = 200)
    @Pattern(
        regexp = "^[A-Za-z0-9ÁÉÍÓÚáéíóúÑñÜü .,#\\-/]+$",
        message = "El destino solo puede contener letras, números y signos como # . , - /"
    )
    String destinoPrestamo,

    @Size(max = 500)
    String observaciones
) {
}