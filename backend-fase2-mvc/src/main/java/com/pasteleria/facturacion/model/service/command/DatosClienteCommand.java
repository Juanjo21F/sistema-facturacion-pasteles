package com.pasteleria.facturacion.model.service.command;

public record DatosClienteCommand(String documentoIdentidad, String nombreCompleto,
                                  String telefono, String correo) {
}
