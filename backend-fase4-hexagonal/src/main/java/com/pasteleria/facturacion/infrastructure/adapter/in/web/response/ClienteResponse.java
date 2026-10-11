package com.pasteleria.facturacion.infrastructure.adapter.in.web.response;

public record ClienteResponse(Long idCliente, String documentoIdentidad, String nombreCompleto,
                              String telefono, String correo, String estado) {
}
