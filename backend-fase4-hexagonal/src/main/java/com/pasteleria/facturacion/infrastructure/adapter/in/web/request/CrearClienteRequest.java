package com.pasteleria.facturacion.infrastructure.adapter.in.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CrearClienteRequest(
        @NotBlank(message = "El documento de identidad es obligatorio") String documentoIdentidad,
        @NotBlank(message = "El nombre completo es obligatorio") String nombreCompleto,
        @NotBlank(message = "El teléfono es obligatorio") String telefono,
        @NotBlank(message = "El correo es obligatorio") @Email(message = "El correo no es válido") String correo) {
}
