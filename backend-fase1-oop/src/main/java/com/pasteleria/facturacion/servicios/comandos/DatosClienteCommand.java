package com.pasteleria.facturacion.servicios.comandos;

public record DatosClienteCommand(String documentoIdentidad, String nombreCompleto,
                                  String telefono, String correo) {
}
