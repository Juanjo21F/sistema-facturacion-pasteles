package com.pasteleria.facturacion.business.dto;

public record DatosClienteCommand(String documentoIdentidad, String nombreCompleto,
                                  String telefono, String correo) {
}
