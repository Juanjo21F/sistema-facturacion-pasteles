package com.pasteleria.facturacion.model.security;

import com.pasteleria.facturacion.model.enums.Rol;

import java.util.Arrays;
import java.util.Objects;

/** Identidad y rol entregados por authcore-service. El sistema nunca los administra. */
public record UsuarioAutenticado(String id, String username, Rol rol) {

    public UsuarioAutenticado {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(rol, "rol");
    }

    public boolean tieneAlgunRol(Rol... roles) {
        return Arrays.asList(roles).contains(rol);
    }
}
