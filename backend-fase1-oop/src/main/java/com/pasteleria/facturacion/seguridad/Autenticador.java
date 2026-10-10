package com.pasteleria.facturacion.seguridad;

public interface Autenticador {

    /**
     * Valida el token contra authcore-service y devuelve la identidad y el rol.
     * @throws AutenticacionException si el token no es válido
     */
    UsuarioAutenticado validarToken(String token);
}
