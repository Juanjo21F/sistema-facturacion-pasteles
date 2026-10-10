package com.pasteleria.facturacion.business.dto;

public record ClienteResponse(Long idCliente, String documentoIdentidad, String nombreCompleto,
                              String telefono, String correo, String estado) {
}
