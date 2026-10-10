package com.pasteleria.facturacion.view.response;

public record ClienteResponse(Long idCliente, String documentoIdentidad, String nombreCompleto,
                              String telefono, String correo, String estado) {
}
