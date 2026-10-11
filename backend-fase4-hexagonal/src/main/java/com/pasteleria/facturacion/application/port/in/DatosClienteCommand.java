package com.pasteleria.facturacion.application.port.in;

public record DatosClienteCommand(String documentoIdentidad, String nombreCompleto, String telefono, String correo) {
}
