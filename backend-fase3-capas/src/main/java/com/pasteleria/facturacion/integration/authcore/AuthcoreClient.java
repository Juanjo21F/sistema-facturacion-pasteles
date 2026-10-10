package com.pasteleria.facturacion.integration.authcore;

import com.pasteleria.facturacion.business.security.UsuarioAutenticado;

public interface AuthcoreClient {

    /**
     * Valida el token contra authcore-service y devuelve la identidad y el rol.
     * @throws AutenticacionException si el token no es válido
     */
    UsuarioAutenticado validarToken(String token);
}
