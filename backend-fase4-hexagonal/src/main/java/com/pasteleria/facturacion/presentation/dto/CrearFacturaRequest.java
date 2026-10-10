package com.pasteleria.facturacion.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** No incluye total: el backend lo calcula. */
public record CrearFacturaRequest(
        @NotNull(message = "El cliente es obligatorio") Long idCliente,
        @NotEmpty(message = "La factura debe tener al menos un detalle") List<@Valid @NotNull DetalleFacturaRequest> detalles) {
}
