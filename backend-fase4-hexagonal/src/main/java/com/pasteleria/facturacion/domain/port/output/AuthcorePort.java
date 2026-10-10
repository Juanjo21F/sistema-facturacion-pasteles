package com.pasteleria.facturacion.domain.port.output;

import com.pasteleria.facturacion.domain.valueobject.UsuarioAutenticado;

public interface AuthcorePort {

    /**
     * Valida el token contra authcore-service y devuelve la identidad y el rol.
     * @throws com.pasteleria.facturacion.domain.exception.AutenticacionException si el token no es válido
     */
    UsuarioAutenticado validarToken(String token);
}
