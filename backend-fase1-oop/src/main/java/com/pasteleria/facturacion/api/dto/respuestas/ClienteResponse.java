package com.pasteleria.facturacion.api.dto.respuestas;

public record ClienteResponse(Long idCliente, String documentoIdentidad, String nombreCompleto,
                              String telefono, String correo, String estado) {
}
