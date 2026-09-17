package com.chn.prestamos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RegistrarPagoRequest(

    @NotNull(message = "El monto del pago es obligatorio")
    @DecimalMin(
        value = "0.01",
        message = "El monto debe ser mayor que cero"
    )
    @Digits(
        integer = 16,
        fraction = 2,
        message = "El monto no es válido (máximo 2 decimales)"
    )
    BigDecimal monto,

    @Size(max = 50)
    @Pattern(
        regexp = "^(?=.*[A-Za-z0-9])[A-Za-z0-9_-]+$",
        message = "El número de recibo solo puede contener letras, números, guiones y guiones bajos"
    )
    String numeroRecibo,

    @Size(max = 250)
    String observaciones
) {
}