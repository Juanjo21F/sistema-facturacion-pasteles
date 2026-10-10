package com.pasteleria.facturacion.api.dto.peticiones;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DetalleFacturaRequest(
        @NotNull(message = "El producto es obligatorio") Long idProducto,
        @NotNull(message = "La cantidad es obligatoria") @Positive(message = "La cantidad debe ser mayor que 0") Integer cantidad) {
}
