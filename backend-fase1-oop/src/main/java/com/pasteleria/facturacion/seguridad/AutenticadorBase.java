package com.pasteleria.facturacion.seguridad;

import com.pasteleria.facturacion.modelo.enumeraciones.Rol;
import com.pasteleria.facturacion.modelo.excepciones.AccesoDenegadoException;
import com.pasteleria.facturacion.modelo.excepciones.AutenticacionException;

/**
 * Clase abstracta (patrón Template Method): fija el algoritmo común de autenticación y deja a las
 * subclases (mock / HTTP) solo el paso que cambia: consultar la identidad del token.
 */
public abstract class AutenticadorBase implements Autenticador {

    @Override
    public final UsuarioAutenticado validarToken(String token) {
        if (token == null || token.isBlank()) {
            throw new AutenticacionException("Se requiere un token de autenticación");
        }
        return consultarIdentidad(token.trim());
    }

    /** Paso variable: cada implementación sabe de dónde sale la identidad. */
    protected abstract UsuarioAutenticado consultarIdentidad(String token);

    protected Rol convertirRol(String rol) {
        try {
            return Rol.valueOf(rol.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AccesoDenegadoException("El rol '" + rol + "' no tiene acceso a este sistema");
        }
    }
}
