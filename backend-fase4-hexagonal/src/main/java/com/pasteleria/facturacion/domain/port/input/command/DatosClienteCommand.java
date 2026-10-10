package com.pasteleria.facturacion.domain.port.input.command;

public record DatosClienteCommand(String documentoIdentidad, String nombreCompleto,
                                  String telefono, String correo) {
}
