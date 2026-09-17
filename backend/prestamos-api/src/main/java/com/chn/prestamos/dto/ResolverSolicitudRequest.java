package com.chn.prestamos.dto;

import com.chn.prestamos.entity.EstadoSolicitud;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ResolverSolicitudRequest(

    @NotNull(message = "El estado de resolución es obligatorio")
    EstadoSolicitud estado,

    @NotBlank(message = "El comentario de resolución es obligatorio")
    @Size(max = 500)
    String comentario,

    @DecimalMin(
        value = "0.00",
        message = "La tasa de interés no puede ser negativa"
    )
    @DecimalMax(
        value = "999.99",
        message = "La tasa de interés no puede superar 999.99"
    )
    @Digits(
        integer = 3,
        fraction = 2,
        message = "La tasa de interés admite máximo 2 decimales"
    )
    BigDecimal tasaInteresAnual
) {
}