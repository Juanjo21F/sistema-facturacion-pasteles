package com.pasteleria.facturacion.view.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CrearProductoRequest(
        @NotBlank(message = "El código único es obligatorio") String codigoUnico,
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotNull(message = "La categoría es obligatoria") Long idCategoria,
        @NotNull(message = "El precio es obligatorio") @Positive(message = "El precio debe ser mayor que 0") BigDecimal precio,
        @NotNull(message = "El stock es obligatorio") @PositiveOrZero(message = "El stock no puede ser negativo") Integer stock) {
}
