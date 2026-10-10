package com.pasteleria.facturacion.presentation.dto;

public record ClienteResponse(Long idCliente, String documentoIdentidad, String nombreCompleto,
                              String telefono, String correo, String estado) {
}
